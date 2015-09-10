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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;

public class Message {
    private String prefix;
    private String command;
    private List<String> params;

    public Message() {
        this(null);
    }

    public Message(String command) {
        this(command, Collections.<String>emptyList());
    }

    public Message(String command, List<String> params) {
        this(null, command, params);
    }

    public Message(String prefix, String command, List<String> params) {
        this.prefix = prefix;
        this.command = command;
        this.params = params;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public List<String> getParams() {
        return Collections.unmodifiableList(params);
    }

    public void setParams(List<String> params) {
        this.params = params;
    }

    @Override
    public String toString() {
        return "{prefix=" + prefix + ",command=" + command + ",params=" + params + "}";
    }

    public static Message withPrefix(String prefix, String command, String... params) {
        return new Message(prefix, command, Arrays.asList(params));
    }

    public static Message withoutPrefix(String command, String... params) {
        return new Message(command, Arrays.asList(params));
    }

    public static class Prefix {
        private String nick;
        private String username;
        private String host;

        public Prefix(String nick, String username, String host) {
            this.nick = nick;
            this.username = username;
            this.host = host;
        }

        public String nick() {
            return nick;
        }

        public String username() {
            return username;
        }

        public String host() {
            return host;
        }

        @Override
        public String toString() {
            if (nick == null) {
                return host;
            }
            StringBuilder sb = new StringBuilder(nick);
            if (username != null) {
                sb.append('!').append(username);
            }
            if (host != null) {
                sb.append('@').append(host);
            }
            return sb.toString();
        }

        public static Prefix parse(String prefix) {
            Matcher m = IRCRegexes.REGEX_PREFIX.matcher(prefix);
            if (!m.matches()) {
                return null;
            }
            return new Prefix(m.group("nick"), m.group("user"), m.group("host"));
        }

        public static Prefix ofServer(String hostname) {
            return new Prefix(null, null, hostname);
        }
    }
}
