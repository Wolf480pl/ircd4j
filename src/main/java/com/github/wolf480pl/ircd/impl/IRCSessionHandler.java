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
package com.github.wolf480pl.ircd.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.wolf480pl.ircd.Command;
import com.github.wolf480pl.ircd.CommandRegistry;
import com.github.wolf480pl.ircd.IRCCommands;
import com.github.wolf480pl.ircd.Message;
import com.github.wolf480pl.ircd.Session;
import com.github.wolf480pl.ircd.SessionHandler;
import com.github.wolf480pl.ircd.User;

public class IRCSessionHandler implements SessionHandler, CommandRegistry {
    private static final Logger logger = LogManager.getLogger(IRCSessionHandler.class);

    private final Map<String, Command> commandMap = new HashMap<>();
    private final ConcurrentMap<Session, User> userMap = new ConcurrentHashMap<>();
    private final String serverName = "localhost";
    private final IRCCommands ircCmds;

    public IRCSessionHandler() {
        this.ircCmds = new IRCCommands();
        ircCmds.register(this);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void messageReceived(Session session, Message msg) {
        logger.debug(msg);

        User user = getUser(session);
        user.resolveHostName();

        user.clearPingSent();
        final String prefix = msg.getPrefix();
        if (prefix != null && !prefix.equalsIgnoreCase(user.getNick())) {
            logger.debug("Ignoring message with wrong prefix: " + prefix);
            return;
        }

        String command = msg.getCommand().toUpperCase();
        Command cmd = commandMap.get(command);
        if (cmd == null) {
            logger.debug("Unknown command: " + command);
            session.send(user.numerics().errUnknownCommand(command));
            return;
        }

        cmd.execute(user, msg.getParams());
        //session.send(new Message("PING", Collections.singletonList("12761418")));
    }

    @Override
    public void onInboundThrowable(Session session, Throwable t) {
        logger.warn("Exception in inbound pipeline", t);
    }

    @Override
    public void onOutboundThrowable(Session session, Throwable t) {
        logger.warn("Exception in outbound pipeline", t);
    }

    @Override
    public Logger getLogger(Session session) {
        return logger;
    }

    protected User getUser(Session session) {
        User user = userMap.get(session);
        if (user != null) {
            return user;
        }
        User newUser = new User(session, serverName);
        user = userMap.putIfAbsent(session, newUser);
        return user == null ? newUser : user;
    }

    @Override
    public void putCommand(String name, Command cmd) {
        commandMap.put(name, cmd);
    }

    @Override
    public void onInboundIdle(Session session) {
        User user = getUser(session);
        logger.debug("User idle: " + user.getNick());
        if (user.setPingSent()) {
            ircCmds.ping(user);
        } else {
            ircCmds.quit(user, "Ping timeout");
        }

    }
}

