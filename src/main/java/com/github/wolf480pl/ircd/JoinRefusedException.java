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

public class JoinRefusedException extends Exception {
    private static final long serialVersionUID = -3917619542388566374L;

    private final Reason reason;

    public JoinRefusedException(Reason reason) {
        super();
        this.reason = reason;
    }

    public JoinRefusedException(Reason reason, String message) {
        super(message);
        this.reason = reason;
    }

    public JoinRefusedException(Reason reason, Throwable cause) {
        super(cause);
        this.reason = reason;
    }

    public JoinRefusedException(Reason reason, String message, Throwable cause) {
        super(message, cause);
        this.reason = reason;
    }

    public Reason reason() {
        return reason;
    }

    public enum Reason {
        BANNED, NEED_INVITE, WRONG_PASSWORD, CHANNEL_FULL, TOO_MANY_CHANNELS
    }
}
