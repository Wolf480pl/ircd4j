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
package com.github.wolf480pl.ircd.util;

import java.util.concurrent.CompletionException;
import java.util.function.BiFunction;

@FunctionalInterface
public interface BiHandler<T, R> extends BiFunction<T, Throwable, R> {

    R handle(T res, Throwable ex) throws Throwable;

    @Override
    default R apply(T res, Throwable t) {
        if (t instanceof CompletionException) {
            t = t.getCause();
        }
        try {
            return handle(res, t);
        } catch (Throwable e) {
            throw new CompletionException(e);
        }
    }
}
