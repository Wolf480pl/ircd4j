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

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import com.github.wolf480pl.ircd.util.AttributeKey;

public class IRCCommands {

    public IRCCommands() {
    }

    public void register(CommandRegistry handler) {
        handler.putCommand("NICK", this::nick);
        handler.putCommand("USER", this::user);
        handler.putCommand("QUIT", this::quit);
        handler.putCommand("PONG", (user, args)->{});
        handler.putCommand("PING", this::ping);
    }

    private static final AttributeKey<AtomicReference<RegistrationData>> ATTR_REGDATA = AttributeKey.valueOf(RegistrationData.class.getCanonicalName());

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

        if (user.isRegistered()) {
            //TODO: broadcast this
            user.send(Message.withPrefix(user.getHostmask(), "NICK", nick));
            user.setNick(nick);
        } else {
            RegistrationData regdata = user.attr(ATTR_REGDATA, makeRegdata).get();

            user.setNick(nick);
            regdata.gotNick = true;

            if (regdata.gotUser) {
                registerUser(user);
            }
        }
    }

    public void user(User user, List<String> args) {
        if (args.size() < 4) {
            user.send(user.numerics().errNeedMoreParams("USER"));
            return;
        }
        String username = args.get(0);
        String realname = args.get(3);

        if (user.isRegistered()) {
            user.send(user.numerics().errAlreadyRegistered());
        } else {
            RegistrationData regdata = user.attr(ATTR_REGDATA, makeRegdata).get();

            user.setUsername(username);
            user.setRealName(realname);

            regdata.gotUser = true;

            if (regdata.gotNick) {
                registerUser(user);
            }
        }
    }

    protected void registerUser(User user) {
        RegistrationData regdata = user.attr(ATTR_REGDATA).getAndSet(null);
        if (regdata == null) {
            // Already registered
            return;
        }
        user.send(user.numerics().rplWelcome("TODO"));
        user.setRegisterd();
        luser(user);
        motd(user);
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

    public void ping(User user, List<String> args) {
        if (args.size() < 1) {
            user.send(user.numerics().errNeedMoreParams("PING"));
            return;
        }
        final String origin = args.get(0);
        final String we = user.getServer();
        String target;
        if (args.size() >= 2) {
            target = args.get(1);
            if (!target.equalsIgnoreCase(we)) {
                //TODO: Support other servers
                user.send(user.numerics().errNoSuchServer(target));
                return;
            }
        } else {
            target = we;
        }
        user.send(Message.withPrefix(we, "PONG", target, origin));

    }

    public void quit(User user, String reason) {
        if (!user.setQuitted()) {
            // Quit already broadcasted
            return;
        }
        user.send(Message.withPrefix(user.getHostmask(), "QUIT", reason));
        user.getSession().disconnect();
        onQuit(user, reason);
    }

    public void onQuit(User user, String reason) {
        // TODO: broadcast this
        user.getSession().getLogger().debug("B " + Message.withPrefix(user.getHostmask(), "QUIT", reason));
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

    public void motd(User user) {
        user.send(user.numerics().rplMotdStart());
        user.send(user.numerics().rplMotd(" === TODO === ")); //TODO
        user.send(user.numerics().rplEndOfMotd());
    }

    public void ping(User user) {
        final String server = user.getServer();
        user.send(Message.withoutPrefix("PING", server));
    }

    protected boolean verifyNick(String nick) {
        //TODO
        return true;
    }

    private static class RegistrationData {
        public volatile boolean gotNick = false;
        public volatile boolean gotUser = false;
    }

    private static AtomicReference<RegistrationData> makeRegdata() {
        return new AtomicReference<>(new RegistrationData());
    }

    private static final Supplier<AtomicReference<RegistrationData>> makeRegdata = IRCCommands::makeRegdata;

}
