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

public class DropMessageException extends Exception {
    private static final long serialVersionUID = -465543809261640492L;

    private final boolean silently;

    public DropMessageException() {
        this(false);
    }

    public DropMessageException(boolean silently) {
        this.silently = silently;
    }

    public DropMessageException(String message) {
        this(false, message);
    }

    public DropMessageException(boolean silently, String message) {
        super(message);
        this.silently = silently;
    }

    public DropMessageException(Throwable cause) {
        this(false, cause);
    }

    public DropMessageException(boolean silently, Throwable cause) {
        super(cause);
        this.silently = silently;
    }

    public DropMessageException(String message, Throwable cause) {
        this(false, message, cause);
    }

    public DropMessageException(boolean silently, String message, Throwable cause) {
        super(message, cause);
        this.silently = silently;
    }

    public boolean silently() {
        return silently;
    }
}
