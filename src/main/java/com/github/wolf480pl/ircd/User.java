/*
 * This file is part of java-lib-ircd.
 *
 * Copyright (c) 2014 Wolf480pl <wolf480@interia.pl>
 * java-lib-ircd is licensed under the GNU Lesser General Public License.
 *
 * java-lib-ircd is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * java-lib-ircd is distributed in the hope that it will be useful,
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

import com.github.wolf480pl.ircd.util.FunctionalMutableString;


public class User {
    private final Session session;
    private final ConcurrentMap<Class<? extends IRCNumerics>, IRCNumerics> numericsCache = new ConcurrentHashMap<>();
    private final String server;
    private final FunctionalMutableString nickRef;
    private final AtomicBoolean pingSent = new AtomicBoolean(false);
    private final AtomicBoolean gotNick = new AtomicBoolean(false);
    private final AtomicBoolean gotUser = new AtomicBoolean(false);
    private final AtomicBoolean isRegistered = new AtomicBoolean(false);
    private String nick;
    private String username;
    private String hostname;
    private String realName;

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

    public boolean isUserReceived() {
        return gotUser.get();
    }

    public boolean setUserReceived() {
        return gotUser.compareAndSet(false, true);
    }

    public boolean isNickReceived() {
        return gotNick.get();
    }

    public boolean setNickReceived() {
        return gotNick.compareAndSet(false, true);
    }

    public boolean isRegistered() {
        return isRegistered.get();
    }

    public boolean setRegisterd() {
        return isRegistered.compareAndSet(false, true);
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
