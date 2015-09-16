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

import java.util.concurrent.CompletableFuture;

import com.github.wolf480pl.ircd.IRCCommands;
import com.github.wolf480pl.ircd.IRCUser;
import com.github.wolf480pl.ircd.UserAPI;
import com.github.wolf480pl.ircd.util.Util;

public class UserAPIImpl implements UserAPI {
    protected final IRCUser user;
    protected final IRCCommands ircCommands;

    public UserAPIImpl(IRCUser user, IRCCommands ircCommands) {
        this.user = user;
        this.ircCommands = ircCommands;
    }

    @Override
    public CompletableFuture<String> nick(String nick) {
        CompletableFuture<String> resultFuture = new CompletableFuture<>();
        user.enqueueRun(() -> {
            /*ircCommands.changeNick(user, nick).whenComplete((BiHandlingConsumer<String>) (newNick, ex) -> {
                if (ex != null) {
                    resultFuture.completeExceptionally(ex);
                } else {
                    resultFuture.complete(newNick);
                }
            });*/
            Util.jumpFuture(ircCommands.changeNick(user, nick), resultFuture);
        });
        return resultFuture;
    }

}
