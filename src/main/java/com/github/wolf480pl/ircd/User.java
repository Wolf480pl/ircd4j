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

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import com.github.wolf480pl.ircd.util.AttributeKey;
import com.github.wolf480pl.ircd.util.EventExecutor;
import com.github.wolf480pl.ircd.util.Util;

public interface User {

    String getNick();

    String getUsername();

    String getRealName();

    String getHostname();

    Session getSession();

    void send(Message msg);

    void maybeSend(Message msgOrNull);

    String getHostmask();

    String getServer();

    boolean isRegistered();

    boolean isQuitted();

    EventExecutor executor();

    default CompletableFuture<Void> enqueueRun(Runnable runnable) {
        if (executor().inEventLoop()) {
            runnable.run();
            return CompletableFuture.completedFuture(null);
        }
        return CompletableFuture.runAsync(runnable, executor());
    }

    default <T> CompletableFuture<T> enqueueSupply(Supplier<T> supplier) {
        if (executor().inEventLoop()) {
            return CompletableFuture.completedFuture(supplier.get());
        }
        return CompletableFuture.supplyAsync(supplier, executor());
    }

    /**
     * Returns a future that completes in the {@link User#executor()}'s thread
     * when the provided {@code future} completes.
     * <p>
     * IMPORTANT: stages appended to the resulting future <strong>may be
     * executed in the caller's thread</strong> if the future is
     * already done at the time the next stage is appended, so <strong>don't use
     * this for thread-safety unless you're already
     * {@link EventExecutor#inEventLoop() in the event loop}</strong> at the
     * time of adding next stages.
     *
     * @param future
     * @return
     */
    default <T> CompletableFuture<T> maybeEnqueue(CompletableFuture<T> future) {
        return future.handleAsync(Util.passthruHandler(), executor());
    }

    <T extends UserAPI> T api(Class<T> clazz);

    default UserAPI api() {
        return api(UserAPI.class);
    }

    <T> T attr(AttributeKey<T> key);

    <T> T attr(AttributeKey<T> key, T putIfAbsent);

    <T> T attr(AttributeKey<T> key, Supplier<T> factory);

}
