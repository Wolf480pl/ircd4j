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

import java.util.concurrent.CompletableFuture;

public class Util {

    private Util() {
    }

    public static RuntimeException ensureUnchecked(Throwable t) {
        if (t instanceof Error) {
            throw (Error) t;
        }
        if (t instanceof RuntimeException) {
            return (RuntimeException) t;
        }
        return new IllegalStateException("Unexpected checked exception", t);
    }

    public static <T> T passthruHandle(T res, Throwable ex) throws Throwable {
        if (ex != null) {
            throw ex;
        }
        return res;
    }

    public static <T> BiHandler<T, T> passthruHandler() {
        return Util::passthruHandle;
    }

    public static <T> BiHandlingConsumer<T> passthruJumper(CompletableFuture<T> dst) {
        return (res, ex) -> {
            if (ex != null) {
                dst.completeExceptionally(ex);
            } else {
                dst.complete(res);
            }
        };
    }

    public static <T> void jumpFuture(CompletableFuture<T> src, CompletableFuture<T> dst) {
        dst.whenComplete(passthruJumper(dst));
    }
}
