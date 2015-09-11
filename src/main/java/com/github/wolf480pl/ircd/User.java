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

import java.util.function.Supplier;

import com.github.wolf480pl.ircd.util.AttributeKey;
import com.github.wolf480pl.ircd.util.EventExecutor;

public interface User {

    String getNick();

    String getUsername();

    String getRealName();

    String getHostname();

    Session getSession();

    void send(Message msg);

    void maybeSend(Message msgOrNull);

    String getHostmask();

    String getServer();

    boolean isRegistered();

    boolean isQuitted();

    EventExecutor executor();

    <T> T attr(AttributeKey<T> key);

    <T> T attr(AttributeKey<T> key, T putIfAbsent);

    <T> T attr(AttributeKey<T> key, Supplier<T> factory);

}
