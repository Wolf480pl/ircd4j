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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IRCNumerics {
    private final String prefix;
    private final CharSequence target;

    public IRCNumerics(CharSequence target) {
        this(null, target);
    }

    public IRCNumerics(String prefix, CharSequence target) {
        this.prefix = prefix;
        this.target = target;
    }

    public IRCNumerics withPrefix(String prefix) {
        return new IRCNumerics(prefix, this.target);
    }

    public IRCNumerics withTarget(String target) {
        return new IRCNumerics(this.prefix, target);
    }

    public Message numeric(int cmd, String... args) {
        List<String> params = new ArrayList<>(args.length + 1);
        String targetName = target.toString();
        params.add(targetName == null ? "*" : targetName);
        params.addAll(Arrays.asList(args));
        return new Message(prefix, String.valueOf(cmd), params);
    }

    public Message rplLuserClient(int users, int invisible, int servers) {
        return numeric(251, "There are " + users + " users and " + invisible + " invisible on " + servers + " servers");
    }

    public Message rplLuserOp(int ops) {
        return numeric(252, String.valueOf(ops), "operator(s) online");
    }

    public Message rplLuserUnknown(int unknown) {
        return numeric(253, String.valueOf(unknown), "unknown connection(s)");
    }

    public Message rplLuserChannels(int channels) {
        return numeric(254, String.valueOf(channels), "channels formed");
    }

    public Message rplLuserMe(int clients, int servers) {
        return numeric(251, "I have " + clients + " clients and " + servers + " servers");
    }

    public Message errNoSuchNick(String nick) {
        return numeric(401, nick, "No such nick/channel");
    }

    public Message errNoSuchServer(String server) {
        return numeric(402, server, "No such server");
    }

    public Message errNoSuchChannel(String channel) {
        return numeric(403, channel, "No such channel");
    }

    public Message errCannotSendToChan(String channel) {
        return numeric(404, channel, "Cannot send to channel");
    }

    public Message errTooManyChannels(String channel) {
        return numeric(405, channel, "You have joined too many channels");
    }

    public Message errWasNuSuchNick(String nick) {
        return numeric(406, nick, "There was no such nickname");
    }

    public Message errTooManyTargets(String target) {
        return numeric(407, target, "Duplicate recipients. No message delivered");
    }

    public Message errNoOrigin() {
        return numeric(409, "No origin specified");
    }

    public Message errNoRecipient(String command) {
        return numeric(411, "No recipient given (" + command + ")");
    }

    public Message errNoTextToSend() {
        return numeric(412, "No text to send");
    }

    public Message errNoTopLevel(String mask) {
        return numeric(413, mask, "No toplevel domain specified");
    }

    public Message errWildTopLevel(String mask) {
        return numeric(414, mask, "Wildcard in toplevel domain");
    }

    public Message errUnknownCommand(String command) {
        return numeric(421, command, "Unknown command");
    }

    public Message errNoMotd() {
        return numeric(422, "MOTD File is missing");
    }

    public Message errNoAdminInfo(String server) {
        return numeric(423, server, "No administrative info available");
    }

    public Message errFileError(String fileOp, String file) {
        return numeric(424, "File error doing " + fileOp + " on " + file);
    }

    public Message errNoNickNameGiven() {
        return numeric(431, "No nickname given");
    }

    public Message errErrorneusNickname(String nick) {
        return numeric(432, nick, "Erreneus nickname");
    }

    public Message errNicknameInUse(String nick) {
        return numeric(433, nick, "Nickname is already in use");
    }

    public Message errNickCollision(String nick) {
        return numeric(436, nick, "Nickname collision KILL");
    }

    public Message errNeedMoreParams(String command) {
        return numeric(461, command, "Not enough parameters");
    }

    public Message errAlreadyRegistered() {
        return numeric(461, "You may not reregister");
    }
}
