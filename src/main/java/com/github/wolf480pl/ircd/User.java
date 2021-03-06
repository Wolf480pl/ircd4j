/*
 * This file is part of IRCd4j.
 *
 * Copyright (c) 2014 Wolf480pl <wolf480@interia.pl>
 * IRCd4j is licensed under the GNU Lesser General Public License.
 *
 * IRCd4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * IRCd4j is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.wolf480pl.ircd;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import com.github.wolf480pl.ircd.util.AttributeKey;
import com.github.wolf480pl.ircd.util.FunctionalMutableString;


public class User {
    private final Session session;
    private final ConcurrentMap<Class<? extends IRCNumerics>, IRCNumerics> numericsCache = new ConcurrentHashMap<>();
    private final String server;
    private final FunctionalMutableString nickRef;
    private final AtomicBoolean pingSent = new AtomicBoolean(false);
    private final AtomicBoolean isRegistered = new AtomicBoolean(false);
    private final AtomicBoolean quitted = new AtomicBoolean(false);
    private String nick;
    private String username;
    private String hostname;
    private String realName;

    private final ConcurrentMap<AttributeKey<?>, Object> attrs = new ConcurrentHashMap<>();

    public User(Session session, String server) {
        this.session = session;
        this.server = server;
        this.nickRef = new FunctionalMutableString(this::getNick);
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getHostname() {
        return hostname;
    }

    public Session getSession() {
        return session;
    }

    public void send(Message msg) {
        session.send(msg);
    }

    public void maybeSend(Message msgOrNull) {
        if (msgOrNull != null) {
            send(msgOrNull);
        }
    }

    public String getHostmask() {
        //TODO: Ident
        return nick + "!~" + username + "@" + hostname;
    }

    public IRCNumerics numerics() {
        return numerics(IRCNumerics.class);
    }

    public <T extends IRCNumerics> T numerics(Class<T> clazz) {
        @SuppressWarnings("unchecked")
        T numerics = (T) numericsCache.get(clazz);
        if (numerics == null) {
            try {
                numerics = getNumerics(clazz, this);
                numericsCache.putIfAbsent(clazz, numerics);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new IllegalArgumentException("Bad numerics class " + clazz.getSimpleName() + " - doesn't let us instantiate it", e);
            }
        }
        return numerics;
    }

    public String getServer() {
        //TODO: Is this right?
        return server;
    }

    public boolean isPingSent() {
        return pingSent.get();
    }

    public boolean setPingSent() {
        return pingSent.compareAndSet(false, true);
    }

    public void clearPingSent() {
        pingSent.set(false);
    }

    public boolean isRegistered() {
        return isRegistered.get();
    }

    public boolean setRegisterd() {
        return isRegistered.compareAndSet(false, true);
    }

    public boolean isQuitted() {
        return quitted.get();
    }

    public boolean setQuitted() {
        return quitted.compareAndSet(false, true);
    }

    @SuppressWarnings("unchecked")
    public <T> T attr(AttributeKey<T> key) {
        return (T) attrs.get(key);
    }

    @SuppressWarnings("unchecked")
    public <T> T attr(AttributeKey<T> key, T putIfAbsent) {
        return (T) attrs.putIfAbsent(key, putIfAbsent);
    }

    @SuppressWarnings("unchecked")
    public <T> T attr(AttributeKey<T> key, Supplier<T> factory) {
        return (T) attrs.computeIfAbsent(key, (x) -> factory.get());
    }

    // non-API
    public void resolveHostName() {
        if (hostname != null) {
            return;
        }
        //TODO: do asynchronous DNS lookup
        this.hostname = ((InetSocketAddress) session.getRemoteAddress()).getAddress().getCanonicalHostName();
    }

    private static final ConcurrentMap<Class<? extends IRCNumerics>, Constructor<? extends IRCNumerics>> constructorCache = new ConcurrentHashMap<>();

    protected static <T extends IRCNumerics> T getNumerics(Class<T> clazz, User user) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        @SuppressWarnings("unchecked")
        Constructor<T> constructor = (Constructor<T>) constructorCache.get(clazz);
        if (constructor == null) {
            constructor = clazz.getDeclaredConstructor(String.class, CharSequence.class);
            constructorCache.putIfAbsent(clazz, constructor);
        }
        return constructor.newInstance(user.server, user.nickRef);
    }
}
