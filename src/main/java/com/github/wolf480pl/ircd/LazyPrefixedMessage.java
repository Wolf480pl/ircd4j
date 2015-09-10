package com.github.wolf480pl.ircd;

import com.github.wolf480pl.ircd.Message.Prefix;

public class LazyPrefixedMessage implements LazyNickedMessage {
    private final Prefix prefix;
    private final String command;
    private final String[] params;

    public LazyPrefixedMessage(Prefix prefix, String command, String... params) {
        this.prefix = prefix;
        this.command = command;
        this.params = params;
    }

    @Override
    public Message fill(String nick) {
        Prefix newPrefix = new Prefix(nick, prefix.username(), prefix.host());
        return Message.withPrefix(newPrefix.toString(), command, params);
    }

}
