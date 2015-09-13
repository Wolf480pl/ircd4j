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

import java.util.regex.Pattern;

public class IRCRegexes {

    public static final String REGEX_HOSTNAME = "(?:[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?\\.)*[a-zA-Z](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?";
    public static final String REGEX_IPV4 = "[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}";
    public static final String REGEX_IPV6 = "\\[(?:[0-9a-fA-F]{1,4}(?::[0-9a-fA-F]{1,4})*|(?:[0-9a-fA-F]{1,4}(?::[0-9a-fA-F]{1,4})*)?::(?:[0-9a-fA-F]{1,4}(?::[0-9a-fA-F]{1,4})*)?)(?::" + REGEX_IPV4 + ")?\\]";
    public static final String REGEX_HOST = "(?:" + REGEX_HOSTNAME + ")|(?:" + REGEX_IPV4 + ")|(?:" + REGEX_IPV6 + ")";
    public static final String REGEX_NICK = "[a-zA-Z][a-zA-Z0-9{}\\[\\]|\\\\^`-]*";
    public static final String REGEX_USER = "[^ \\x00\\r\\n]*";
    public static final String REGEX_USER_HOSTMASK = REGEX_NICK + "(?:!" + REGEX_USER + ")?(?:@" + REGEX_HOST + ")?";
    public static final String REGEX_PARAM = "[^ \\x00\\r\\n:][^ \\x00\\r\\n]*";
    public static final Pattern REGEX_PATTERN_PARAM = Pattern.compile(" +(?<arg>" + REGEX_PARAM + ")");
    public static final String REGEX_PARAMS = "(?: +" + REGEX_PARAM + ")*";
    public static final String REGEX_PARAM_TRAILING = "[^\\x00\\r\\n]*";
    public static final Pattern REGEX_PATTERN_MESSAGE = Pattern.compile("(?::(?<prefix>" + REGEX_HOST + "|" + REGEX_USER_HOSTMASK + ") +)?(?<command>[0-9]{3}|[a-zA-Z]+)(?<args>" + REGEX_PARAMS + ")(?: :(?<trailing>" + REGEX_PARAM_TRAILING + "))?");
    public static final Pattern REGEX_PREFIX = Pattern.compile("(?<server>" + REGEX_HOST + ")|(?<nick>" + REGEX_NICK + ")" + "(?:!(?<user>" + REGEX_USER + "))?(?:@(?<host>" + REGEX_HOST + "))?");

    private IRCRegexes() {
    }

}
