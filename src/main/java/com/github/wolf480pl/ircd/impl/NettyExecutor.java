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
package com.github.wolf480pl.ircd.impl;

import io.netty.util.concurrent.EventExecutor;

public class NettyExecutor implements com.github.wolf480pl.ircd.util.EventExecutor {
    private final EventExecutor executor;

    public NettyExecutor(EventExecutor executor) {
        this.executor = executor;
    }

    @Override
    public void execute(Runnable command) {
        executor.execute(command);
    }

    @Override
    public boolean inEventLoop(Thread thread) {
        return executor.inEventLoop(thread);
    }

    @Override
    public boolean inEventLoop() {
        return executor.inEventLoop();
    }
}
