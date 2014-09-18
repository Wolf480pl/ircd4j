/*
 * This file is part of java-lib-ircd.
 *
 * Copyright (c) ${inceptionYear} Wolf480pl <wolf480@interia.pl>
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

import com.github.wolf480pl.ircd.util.FunctionalMutableString;


public class User {
    private final Session session;
    private final IRCNumerics numerics;
    private final String server;
    private String nick;

    public User(Session session, String server) {
        this.session = session;
        this.server = server;
        this.numerics = new IRCNumerics(server, new FunctionalMutableString(this::getNick));
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public Session getSession() {
        return session;
    }

    public void send(Message msg) {
        session.send(msg);
    }

    public String getHostmask() {
        //TODO
        return nick;
    }

    public IRCNumerics numerics() {
        return numerics;
    }

    public String getServer() {
        //TODO: Is this right?
        return server;
    }
}
