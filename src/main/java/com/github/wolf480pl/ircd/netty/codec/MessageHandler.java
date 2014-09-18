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
package com.github.wolf480pl.ircd.netty.codec;

import java.util.concurrent.atomic.AtomicReference;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;

import com.github.wolf480pl.ircd.Message;
import com.github.wolf480pl.ircd.Session;
import com.github.wolf480pl.ircd.SessionHandler;
import com.github.wolf480pl.ircd.netty.NettySession;

public class MessageHandler extends SimpleChannelInboundHandler<Message> {
    public static final AttributeKey<Session> ATTR_SESSION = AttributeKey.valueOf(MessageHandler.class.getName() + ".SESSION");
    public static final AttributeKey<SessionHandler> ATTR_SESSION_HANDLER = AttributeKey.valueOf(MessageHandler.class.getName() + ".SESSION_HANDLER");

    private final AtomicReference<NettySession> session = new AtomicReference<>(null);
    private final SessionHandler handler;

    public MessageHandler(SessionHandler handler) {
        this.handler = handler;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        final Channel ch = ctx.channel();
        NettySession s = new NettySession(ch, handler);
        if (!session.compareAndSet(null, s)) {
            throw new IllegalStateException("Session was set before channel was activated");
        }
        ch.attr(ATTR_SESSION).set(s);
        ch.attr(ATTR_SESSION_HANDLER).set(handler);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        handler.onInboundThrowable(session.get(), cause);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        NettySession s = session.get();
        s.validate(ctx.channel());
        handler.messageReceived(s, msg);
    }

}
