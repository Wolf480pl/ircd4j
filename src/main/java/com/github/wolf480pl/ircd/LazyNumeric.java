package com.github.wolf480pl.ircd;

public class LazyNumeric implements LazyNickedMessage {
    private final String prefix;
    private final String command;
    private final String[] params;

    public LazyNumeric(String prefix, String cmd, String... params) {
        this.prefix = prefix;
        this.command = cmd;
        this.params = new String[params.length + 1];
        System.arraycopy(params, 0, this.params, 1, params.length);
    }

    @Override
    public Message fill(String nick) {
        params[0] = (nick == null) ? "*" : nick;
        return Message.withPrefix(prefix, command, params);
    }

}
