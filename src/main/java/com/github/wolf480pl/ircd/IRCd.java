/*
 * This file is part of java-lib-ircd.
 *
 * Copyright (c) 2014 Wolf480pl <wolf480@interia.pl>
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

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.wolf480pl.ircd.netty.IRCChannelInitializer;

public class IRCd {
	private SocketAddress bindAddress;
	private EventLoopGroup bossGroup = new NioEventLoopGroup();
	private EventLoopGroup workerGroup = new NioEventLoopGroup();
	private SessionHandler handler;

	public IRCd(SocketAddress bindAddress, SessionHandler handler) {
		this.bindAddress = bindAddress;
		this.handler = handler;
	}

	public ChannelFuture start() {
		ServerBootstrap bootstrap = new ServerBootstrap();
		bossGroup = new NioEventLoopGroup(1);
		workerGroup = new NioEventLoopGroup();

		IRCChannelInitializer initializer = new IRCChannelInitializer(handler);

		bootstrap
				.group(bossGroup, workerGroup)
				.channel(NioServerSocketChannel.class)
				.childHandler(initializer)
				.childOption(ChannelOption.TCP_NODELAY, true)
				.childOption(ChannelOption.SO_KEEPALIVE, true);

		return bootstrap.bind(bindAddress);
	}

	public static void main(String[] args) throws InterruptedException {
	    Logger logger = LogManager.getLogger(IRCd.class);
	    logger.info("Starting IRCd");
		ChannelFuture f = new IRCd(new InetSocketAddress(6667), new IRCSessionHandler()).start();
		f.sync(); // Wait for it to bind
		logger.info("IRCd started");
		
		f.channel().closeFuture().sync(); // Wait for it to close
	}

}
