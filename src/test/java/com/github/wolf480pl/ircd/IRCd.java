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

import java.net.InetSocketAddress;

import io.netty.channel.ChannelFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.wolf480pl.ircd.impl.IRCSessionHandler;
import com.github.wolf480pl.ircd.netty.NettyServer;

public class IRCd {
    public static void main(String[] args) throws InterruptedException {
        Logger logger = LoggerFactory.getLogger(IRCd.class);
        logger.info("Starting IRCd");
        ChannelFuture f = new NettyServer(new InetSocketAddress(6667), new IRCSessionHandler()).start();
        f.sync(); // Wait for it to bind
        logger.info("IRCd started");

        f.channel().closeFuture().sync(); // Wait for it to close
    }

}
