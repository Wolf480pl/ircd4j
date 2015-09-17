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

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class SingleUserChannelRegistry implements ChannelRegistry {
    private ConcurrentMap<String, Channel> chanMap = new ConcurrentHashMap<>();

    @Override
    public Channel getChannel(String name) {
        return chanMap.computeIfAbsent(name, SingleUserChannel::new);
    }

    private class SingleUserChannel implements Channel {
        private final String name;
        private User member;

        public SingleUserChannel(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getTopic() {
            return null;
        }

        @Override
        public CompletableFuture<Void> join(User user, String key) {
            member = user;
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public CompletableFuture<Collection<User>> getMembers() {
            return CompletableFuture.completedFuture(Arrays.asList(member));
        }

        @Override
        public CompletableFuture<Boolean> part(User user) {
            boolean success = false;
            if (member == user) {
                member = null;
                success = true;
            }
            return CompletableFuture.completedFuture(success);
        }

    }

}
