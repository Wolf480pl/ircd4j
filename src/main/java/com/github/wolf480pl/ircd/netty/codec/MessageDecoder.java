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
package com.github.wolf480pl.ircd.netty.codec;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.wolf480pl.ircd.Message;
import com.github.wolf480pl.ircd.Session;
import com.github.wolf480pl.ircd.SessionHandler;

public class MessageDecoder extends MessageToMessageDecoder<String> {
    private static final String REGEX_HOSTNAME = "(?:[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?\\.)*[a-zA-Z](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?";
    private static final String REGEX_IPV4 = "[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}";
    private static final String REGEX_IPV6 = "\\[(?:[0-9a-fA-F]{1,4}(?::[0-9a-fA-F]{1,4})*|(?:[0-9a-fA-F]{1,4}(?::[0-9a-fA-F]{1,4})*)?::(?:[0-9a-fA-F]{1,4}(?::[0-9a-fA-F]{1,4})*)?)(?::" + REGEX_IPV4 + ")?\\]";
    private static final String REGEX_HOST = "(?:" + REGEX_HOSTNAME + ")|(?:" + REGEX_IPV4 + ")|(?:" + REGEX_IPV6 + ")";
    private static final String REGEX_NICK = "[a-zA-Z][a-zA-Z0-9{}\\[\\]|\\\\^`-]*";
    private static final String REGEX_USER = "[^ \\x00\\r\\n]*";
    private static final String REGEX_USER_HOSTMASK = REGEX_NICK + "(?:!" + REGEX_USER + ")?(?:@" + REGEX_HOST + ")?";
    private static final String REGEX_PARAM = "[^ \\x00\\r\\n:][^ \\x00\\r\\n]*";
    private static final Pattern REGEX_PATTERN_PARAM = Pattern.compile(" +(?<arg>" + REGEX_PARAM + ")");
    private static final String REGEX_PARAMS = "(?: +" + REGEX_PARAM + ")*";
    private static final String REGEX_PARAM_TRAILING = "[^\\x00\\r\\n]*";
    private static final Pattern REGEX_PATTERN_MESSAGE = Pattern.compile("(?::(?<prefix>" + REGEX_HOST + "|" + REGEX_USER_HOSTMASK + ") +)?(?<command>[0-9]{3}|[a-zA-Z]+)(?<args>" + REGEX_PARAMS + ")(?: :(?<trailing>" + REGEX_PARAM_TRAILING + "))?");

    private static Logger logger = LogManager.getLogger(MessageDecoder.class);

    @Override
    protected void decode(ChannelHandlerContext ctx, String msg, List<Object> out) throws Exception {
        Matcher matcher = REGEX_PATTERN_MESSAGE.matcher(msg);
        if (!matcher.matches()) {
            SessionHandler handler = ctx.channel().attr(MessageHandler.ATTR_SESSION_HANDLER).get();
            Session session = ctx.channel().attr(MessageHandler.ATTR_SESSION).get();
            handler.getLogger(session).debug("Received incorrect message:" + msg);
            return;
        }

        String args = matcher.group("args");
        List<String> params = new ArrayList<>();
        Matcher argMatcher = REGEX_PATTERN_PARAM.matcher(args);
        while (argMatcher.find()) {
            params.add(argMatcher.group("arg"));
        }
        String trailing = matcher.group("trailing");
        if (trailing != null) {
            params.add(trailing);
        }

        Message ircmsg = new Message(matcher.group("prefix"), matcher.group("command"), params);
        out.add(ircmsg);
    }
}
