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

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import com.github.wolf480pl.ircd.util.AttributeKey;
import com.github.wolf480pl.ircd.util.Util;

public class IRCCommands {
    private final UserRegistry registry;

    public IRCCommands() {
        this(null);
    }

    public IRCCommands(UserRegistry registry) {
        this.registry = registry;
    }

    public void register(CommandRegistry handler) {
        handler.putCommand("NICK", this::nick);
        handler.putCommand("USER", this::user);
        handler.putCommand("QUIT", this::quit);
        handler.putCommand("PONG", (user, args)->{});
        handler.putCommand("PING", this::ping);
    }

    private static final AttributeKey<AtomicReference<RegistrationData>> ATTR_REGDATA = AttributeKey.valueOf(RegistrationData.class.getCanonicalName());

    public void nick(IRCUser user, List<String> args) {
        if (args.size() < 1) {
            user.send(user.numerics().errNoNickNameGiven());
            return;
        }

        String nick = args.get(0);
        if (!verifyNick(nick)) {
            user.send(user.numerics().errErrorneusNickname(nick));
            return;
        }

        if (registry != null && registry.getUser(nick) != null) {
            /* RFC 1459 says if it's during registration, this should be ERR_NICKCOLLISION,
             * but RFC 2812 says NICKCOLLISION is only if both are already registered.
             * As for ERR_NICKNAMEINUSE, both RFCs say it's for nick change only.
             * However, HexChat, ZNC, and Charybdis expect/send NICKNAMEINUSE in both cases, so that's what we do.
             */
            user.send(user.numerics().errNicknameInUse(nick));
            return;
        }

        if (user.isRegistered()) {
            CompletableFuture<String> futureNick;

            if (registry == null) {
                futureNick = CompletableFuture.completedFuture(nick);
            } else {
                futureNick = registry.changeNick(user, nick);
            }

            futureNick.thenAccept((String newNick) -> {
                if (newNick == null) {
                    user.send(user.numerics().errNicknameInUse(nick));
                } else {
                    user.send(Message.withPrefix(user.getHostmask(), "NICK", newNick));
                    user.setNick(newNick);
                }

            }).exceptionally((Throwable t) -> {
                if (!(t instanceof DropMessageException)) {
                    throw Util.ensureUnchecked(t);
                }

                user.maybeSend(user.numerics().rplTryAgain("NICK", (DropMessageException) t));
                return null;
            });
        } else {
            RegistrationData regdata = user.attr(ATTR_REGDATA, makeRegdata).get();

            user.setNick(nick);
            regdata.gotNick = true;

            if (regdata.gotUser) {
                registerUser(user);
            }
        }
    }

    public void user(IRCUser user, List<String> args) {
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

    protected void registerUser(IRCUser user) {
        RegistrationData regdata = user.attr(ATTR_REGDATA).getAndSet(null);
        if (regdata == null) {
            // Already registered
            return;
        }
        CompletableFuture<Void> future;
        if (registry == null) {
            future = CompletableFuture.completedFuture(null);
        } else {
            future = registry.register(user);
        }

        future.thenRun(() -> {
            user.send(user.numerics().rplWelcome("TODO"));
            user.setRegisterd();
            luser(user);
            motd(user);

        }).exceptionally((Throwable t) -> {
            if (!(t instanceof DropMessageException)) {
                throw Util.ensureUnchecked(t);
            }

            // TODO: Should we differentiate between NICK and USER ?
            user.maybeSend(user.numerics().rplTryAgain("NICK", (DropMessageException) t));
            return null;
        });
    }

    public void quit(IRCUser user, List<String> args) {
        String reason;
        if (args.size() < 1) {
            reason = user.getNick();
        } else {
            reason = args.get(0);
        }
        quit(user, reason);
    }

    public void ping(IRCUser user, List<String> args) {
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

    public void quit(IRCUser user, String reason) {
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

    public void luser(IRCUser user) {
        //TODO get some sensible numbers into the vars below
        int users = 1;
        int invisible = 0;
        int servers = 1;
        int myClients = 1;
        int myServers = 0;

        user.send(user.numerics().rplLuserClient(users, invisible, servers));
        user.send(user.numerics().rplLuserMe(myClients, myServers));
    }

    public void motd(IRCUser user) {
        user.send(user.numerics().rplMotdStart());
        user.send(user.numerics().rplMotd(" === TODO === ")); //TODO
        user.send(user.numerics().rplEndOfMotd());
    }

    public void ping(IRCUser user) {
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
