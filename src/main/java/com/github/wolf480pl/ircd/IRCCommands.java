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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.github.wolf480pl.ircd.util.AttributeKey;
import com.github.wolf480pl.ircd.util.ExHandler;

public class IRCCommands {
    private final UserRegistry registry;
    private final ChannelRegistry chanReg;

    public IRCCommands() {
        this(null, null);
    }

    public IRCCommands(UserRegistry registry, ChannelRegistry chanReg) {
        this.registry = registry;
        this.chanReg = chanReg;
    }

    public void register(CommandRegistry handler) {
        handler.putCommand("NICK", this::nick);
        handler.putCommand("USER", this::user);
        handler.putCommand("QUIT", this::quit);
        handler.putCommand("PONG", (user, args)->{});
        handler.putCommand("PING", this::ping);
        handler.putCommand("JOIN", this::join);
        handler.putCommand("PART", this::part);
        handler.putCommand("NAMES", this::names);
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
            changeNick(user, nick).thenAccept((String newNick) -> {
                if (newNick == null) {
                    user.send(user.numerics().errNicknameInUse(nick));
                }

            }).exceptionally(dropHandler(user, "NICK"));

        } else {
            RegistrationData regdata = user.attr(ATTR_REGDATA, makeRegdata).get();

            user.setNick(nick);
            regdata.gotNick = true;

            if (regdata.gotUser) {
                registerUser(user);
            }
        }
    }

    public CompletableFuture<String> changeNick(IRCUser user, String nick) {
        CompletableFuture<String> futureNick;

        if (registry == null) {
            futureNick = CompletableFuture.completedFuture(nick);
        } else {
            futureNick = registry.changeNick(user, nick);
        }

        return user.maybeEnqueue(futureNick).thenApply((String newNick) -> {
            if (newNick != null) {
                user.send(Message.withPrefix(user.getHostmask(), "NICK", newNick));
                user.setNick(newNick);
            }
            return newNick;
        });
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

        user.maybeEnqueue(future).thenRun(() -> {
            user.send(user.numerics().rplWelcome("TODO"));
            user.setRegisterd();
            luser(user);
            motd(user);

        }).exceptionally(dropHandler(user, "NICK"));
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

    public void join(IRCUser user, List<String> args) {
        if (args.size() < 1) {
            user.send(user.numerics().errNeedMoreParams("JOIN"));
            return;
        }
        String[] chans = args.get(0).split(",");
        String[] keys = new String[chans.length];
        if (args.size() >= 2) {
            String[] k = args.get(1).split(",");
            System.arraycopy(k, 0, keys, 0, Math.max(chans.length, k.length));
        }

        for (int i = 0; i < chans.length; ++i) {
            join(user, chans[i], keys[i]);
        }
    }

    public void part(IRCUser user, List<String> args) {
        if (args.size() < 1) {
            user.send(user.numerics().errNeedMoreParams("PART"));
            return;
        }

        String[] chans = args.get(0).split(",");
        for (String chan : chans) {
            part(user, chan);
        }
    }

    public void names(IRCUser user, List<String> args) {
        if (args.isEmpty()) {
            // TODO: can we get the list of all users and all channels somehow?
            names(user, "*", false).accept(Collections.emptyList());
            user.send(user.numerics().rplEndOfNames("*"));
            return;
        }

        String[] chans = args.get(0).split(",");
        for (String chan : chans) {
            Channel channel = (chanReg != null) ? chanReg.getChannel(chan) : null;
            if (channel != null) {
                user.maybeEnqueue(channel.getMembers()).thenAccept(names(user, chan, true));
            } else {
                // This is the way to signalize non-existence of a channel per both RFCs
                names(user, chan, true).accept(Collections.emptyList());
            }
        }

    }

    protected void join(IRCUser user, String channel, String key) {
        if (chanReg != null) {
            Channel chan = chanReg.getChannel(channel);
            if (chan != null) {
                join(user, chan, key).exceptionally(dropHandler(user, "JOIN")).exceptionally((ExHandler<Void>) ex -> {
                    if (ex instanceof JoinRefusedException) {
                        user.send(user.numerics().joinRefused(channel, (JoinRefusedException) ex));
                        return null;
                    }
                    throw ex;
                });
                return;
            }
        }
        user.send(user.numerics().errNoSuchChannel(channel));
    }

    public CompletableFuture<Void> join(IRCUser user, Channel channel, String key) {
        return user.maybeEnqueue(channel.join(user, key)).thenRun(() -> {
            String chanName = channel.getName();
            user.send(Message.withPrefix(user.getHostmask(), "JOIN", chanName));

            String topic = channel.getTopic();
            if (topic != null) {
                user.send(user.numerics().rplTopic(chanName, topic));
            } else {
                user.send(user.numerics().rplNoTopic(chanName));
            }

            // names() sends numerics. Numerics contain the user's nick, so they need to be sent from his event loop,
            // and channel.getMembers() may finish in any thread, so we need to use user.maybeEnqueue().
            user.maybeEnqueue(channel.getMembers()).thenAccept(names(user, chanName, true));
        });
    }

    protected void part(IRCUser user, String chan) {
        if (chanReg != null) {
            Channel channel = chanReg.getChannel(chan);
            if (channel != null) {
                part(user, channel).exceptionally(dropHandler(user, "PART")).exceptionally((ExHandler<Void>) ex -> {
                    if (!(ex instanceof NotOnChannelException)) {
                        throw ex;
                    }
                    user.send(user.numerics().errNotOnChannel(channel.getName()));
                    return null;
                });
                return;
            }
        }
        user.send(user.numerics().errNoSuchChannel(chan));
    }

    public CompletableFuture<Void> part(IRCUser user, Channel channel) {
        return user.maybeEnqueue(channel.part(user)).thenRun(() -> {
            user.send(Message.withPrefix(user.getHostmask(), "PART", channel.getName()));
        });
    }

    public Consumer<Collection<User>> names(IRCUser user, String chanName, boolean sendEndOfNames) {
        return members -> {
            if (!members.isEmpty()) {
                user.send(
                        // TODO: Split it so that it fits in a message buffer
                        //       The splitting might have to be done on the numerics side, because they are close to the message text
                        user.numerics().rplNamReply(chanName, members.stream().map(member -> member.getNick()).toArray(String[]::new)));
            }
            if (sendEndOfNames) {
                user.send(user.numerics().rplEndOfNames(chanName));
            }
        };
    }

    public void quit(IRCUser user, String reason) {
        if (user.setQuitted() && user.isRegistered()) {
            // Quit not broadcasted yet, and user already registered.
            user.send(Message.withPrefix(user.getHostmask(), "QUIT", reason));
            onQuit(user, reason);
        }
        user.getSession().disconnect();
    }

    public void onQuit(User user, String reason) {
        if (registry != null) {
            registry.unregister(user);
        }
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

    private static ExHandler<Void> dropHandler(IRCUser user, String command) {
        return t -> {
            if (!(t instanceof DropMessageException)) {
                throw t;
            }

            user.maybeSend(user.numerics().rplTryAgain(command, (DropMessageException) t));
            return null;
        };
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
