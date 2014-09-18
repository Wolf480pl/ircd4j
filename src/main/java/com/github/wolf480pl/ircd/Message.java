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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
}
