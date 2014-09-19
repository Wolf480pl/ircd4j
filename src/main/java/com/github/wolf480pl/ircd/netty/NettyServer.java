package com.github.wolf480pl.ircd.netty;

import java.net.SocketAddress;
import java.util.concurrent.atomic.AtomicBoolean;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import com.github.wolf480pl.ircd.SessionHandler;

public class NettyServer {
    private SocketAddress bindAddress;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private SessionHandler handler;
    private final AtomicBoolean started = new AtomicBoolean(false);
    private final AtomicBoolean stopped = new AtomicBoolean(false);
    private Channel channel;

    public NettyServer(SocketAddress bindAddress, SessionHandler handler) {
        this.bindAddress = bindAddress;
        this.handler = handler;
    }

    public ChannelFuture start() {
        if (!started.compareAndSet(false, true)) {
            return null;
        }

        ServerBootstrap bootstrap = new ServerBootstrap();
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();

        IRCChannelInitializer initializer = new IRCChannelInitializer(handler);

        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(initializer)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true);

        ChannelFuture future =  bootstrap.bind(bindAddress);
        this.channel = future.channel();
        return future;
    }

    public ChannelFuture stop() {
        if (!started.get()) {
            return null; //TODO: Failed future?
        }
        if (!stopped.compareAndSet(false, true)) {
            return null; //TODO: Failed future?
        }
        return channel.close().addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                workerGroup.shutdownGracefully();
                bossGroup.shutdownGracefully();
            }
        });
    }

}
