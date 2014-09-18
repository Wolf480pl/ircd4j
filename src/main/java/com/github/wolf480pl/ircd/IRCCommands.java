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

import java.util.List;

public class IRCCommands {

    public IRCCommands() {
    }

    public void register(IRCSessionHandler handler) {
        handler.putCommand("NICK", this::nick);
        handler.putCommand("USER", this::user);
        handler.putCommand("QUIT", this::quit);
    }

    public void nick(User user, List<String> args) {
        if (args.size() < 1) {
            user.send(user.numerics().errNoNickNameGiven());
            return;
        }

        String nick = args.get(0);
        if (!verifyNick(nick)) {
            user.send(user.numerics().errErrorneusNickname(nick));
        }

        //TODO: check for collisions, maintain a nick->user map

        if (user.getNick() != null) {
            user.send(Message.withPrefix(user.getHostmask(), "NICK", nick));
        }
        user.setNick(nick);
    }

    public void user(User user, List<String> args) {
        if (args.size() < 4) {
            user.send(user.numerics().errNeedMoreParams("USER"));
            return;
        }

        if (false /*TODO*/) {
            user.send(user.numerics().errAlreadyRegistered());
        }
        luser(user);
    }

    public void quit(User user, List<String> args) {
        String reason;
        if (args.size() < 1) {
            reason = user.getNick();
        } else {
            reason = args.get(0);
        }
        quit(user, reason);
    }

    public void quit(User user, String reason) {
        //TODO: broadcast this
        user.send(Message.withPrefix(user.getServer(), "QUIT", reason));
        user.getSession().disconnect();
    }

    public void luser(User user) {
        //TODO get some sensible numbers into the vars below
        int users = 1;
        int invisible = 0;
        int servers = 1;
        int myClients = 1;
        int myServers = 0;

        user.send(user.numerics().rplLuserClient(users, invisible, servers));
        user.send(user.numerics().rplLuserMe(myClients, myServers));
    }

    protected boolean verifyNick(String nick) {
        //TODO
        return true;
    }

}
