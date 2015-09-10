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

    public LazyNickedMessage numeric(int cmd, String... args) {
        return new LazyNumeric(prefix, String.format("%03d", cmd), args);
    }

    public static final int RPL_WELCOME = 001;

    public LazyNickedMessage rplWelcome(String networkName) {
        return numeric(RPL_WELCOME, "Welcome to the " + networkName + " Internet Relay Chat Network " + target);
    }

    public static final int RPL_LUSERCLIENT = 251;

    public LazyNickedMessage rplLuserClient(int users, int invisible, int servers) {
        return numeric(RPL_LUSERCLIENT, "There are " + users + " users and " + invisible + " invisible on " + servers + " servers");
    }

    public static final int RPL_LUSEROP = 252;

    public LazyNickedMessage rplLuserOp(int ops) {
        return numeric(RPL_LUSEROP, String.valueOf(ops), "operator(s) online");
    }

    public static final int RPL_LUSERUNKNOWN = 253;

    public LazyNickedMessage rplLuserUnknown(int unknown) {
        return numeric(RPL_LUSERUNKNOWN, String.valueOf(unknown), "unknown connection(s)");
    }

    public static final int RPL_LUSERCHANNELS = 254;

    public LazyNickedMessage rplLuserChannels(int channels) {
        return numeric(RPL_LUSERCHANNELS, String.valueOf(channels), "channels formed");
    }

    public static final int RPL_LUSERME = 255;

    public LazyNickedMessage rplLuserMe(int clients, int servers) {
        return numeric(RPL_LUSERME, "I have " + clients + " clients and " + servers + " servers");
    }

    public static final int RPL_TRYAGAIN = 263;

    public LazyNickedMessage rplTryAgain(String command) {
        return numeric(RPL_TRYAGAIN, command, "Command dropped. Please try again later.");
    }

    public static final int RPL_MOTDSTART = 375;

    public LazyNickedMessage rplMotdStart() {
        return numeric(RPL_MOTDSTART, "- " + prefix + " Message of the day - ");
    }

    public static final int RPL_MOTD = 372;

    public LazyNickedMessage rplMotd(String text) {
        return numeric(RPL_MOTD, "- " + text);
    }

    public static final int RPL_ENDOFMOTD = 376;

    public LazyNickedMessage rplEndOfMotd() {
        return numeric(RPL_ENDOFMOTD, "End of /MOTD command");
    }

    public static final int ERR_NOSUCHNICK = 401;

    public LazyNickedMessage errNoSuchNick(String nick) {
        return numeric(ERR_NOSUCHNICK, nick, "No such nick/channel");
    }

    public static final int ERR_NOSUCHSERVER = 402;

    public LazyNickedMessage errNoSuchServer(String server) {
        return numeric(ERR_NOSUCHSERVER, server, "No such server");
    }

    public static final int ERR_NOSUCHCHANNEL = 403;

    public LazyNickedMessage errNoSuchChannel(String channel) {
        return numeric(ERR_NOSUCHCHANNEL, channel, "No such channel");
    }

    public static final int ERR_CANNOTSENDTOCHAN = 404;

    public LazyNickedMessage errCannotSendToChan(String channel) {
        return numeric(ERR_CANNOTSENDTOCHAN, channel, "Cannot send to channel");
    }

    public static final int ERR_TOOMANYCHANNELS = 405;

    public LazyNickedMessage errTooManyChannels(String channel) {
        return numeric(ERR_TOOMANYCHANNELS, channel, "You have joined too many channels");
    }

    public static final int ERR_WASNUSUCHNICK = 406;

    public LazyNickedMessage errWasNuSuchNick(String nick) {
        return numeric(ERR_WASNUSUCHNICK, nick, "There was no such nickname");
    }

    public static final int ERR_TOOMANYTARGETS = 407;

    public LazyNickedMessage errTooManyTargets(String target) {
        return numeric(ERR_TOOMANYTARGETS, target, "Duplicate recipients. No message delivered");
    }

    public static final int ERR_NOORIGIN = 409;

    public LazyNickedMessage errNoOrigin() {
        return numeric(ERR_NOORIGIN, "No origin specified");
    }

    public static final int ERR_NORECIPIENT = 411;

    public LazyNickedMessage errNoRecipient(String command) {
        return numeric(ERR_NORECIPIENT, "No recipient given (" + command + ")");
    }

    public static final int ERR_NOTEXTTOSEND = 412;

    public LazyNickedMessage errNoTextToSend() {
        return numeric(ERR_NOTEXTTOSEND, "No text to send");
    }

    public static final int ERR_NOTOPLEVEL = 413;

    public LazyNickedMessage errNoTopLevel(String mask) {
        return numeric(ERR_NOTOPLEVEL, mask, "No toplevel domain specified");
    }

    public static final int ERR_WILDTOPLEVEL = 414;

    public LazyNickedMessage errWildTopLevel(String mask) {
        return numeric(ERR_WILDTOPLEVEL, mask, "Wildcard in toplevel domain");
    }

    public static final int ERR_UNKNOWNCOMMAND = 421;

    public LazyNickedMessage errUnknownCommand(String command) {
        return numeric(ERR_UNKNOWNCOMMAND, command, "Unknown command");
    }

    public static final int ERR_NOMOTD = 422;

    public LazyNickedMessage errNoMotd() {
        return numeric(ERR_NOMOTD, "MOTD File is missing");
    }

    public static final int ERR_NOADMININFO = 423;

    public LazyNickedMessage errNoAdminInfo(String server) {
        return numeric(ERR_NOADMININFO, server, "No administrative info available");
    }

    public static final int ERR_FILEERROR = 424;

    public LazyNickedMessage errFileError(String fileOp, String file) {
        return numeric(ERR_FILEERROR, "File error doing " + fileOp + " on " + file);
    }

    public static final int ERR_NONICKNAMEGIVEN = 431;

    public LazyNickedMessage errNoNickNameGiven() {
        return numeric(ERR_NONICKNAMEGIVEN, "No nickname given");
    }

    public static final int ERR_ERRORNEUSNICKNAME = 432;

    public LazyNickedMessage errErrorneusNickname(String nick) {
        return numeric(ERR_ERRORNEUSNICKNAME, nick, "Erreneus nickname");
    }

    public static final int ERR_NICKNAMEINUSE = 433;

    public LazyNickedMessage errNicknameInUse(String nick) {
        return numeric(ERR_NICKNAMEINUSE, nick, "Nickname is already in use");
    }

    public static final int ERR_NICKCOLLISION = 436;

    public LazyNickedMessage errNickCollision(String nick) {
        return numeric(ERR_NICKCOLLISION, nick, "Nickname collision KILL");
    }

    public static final int ERR_NEEDMOREPARAMS = 461;

    public LazyNickedMessage errNeedMoreParams(String command) {
        return numeric(ERR_NEEDMOREPARAMS, command, "Not enough parameters");
    }

    public static final int ERR_ALREADYREGISTERED = 461;

    public LazyNickedMessage errAlreadyRegistered() {
        return numeric(ERR_ALREADYREGISTERED, "You may not reregister");
    }
}
