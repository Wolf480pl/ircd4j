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
package com.github.wolf480pl.ircd.netty;

import java.nio.charset.Charset;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;

import com.github.wolf480pl.ircd.SessionHandler;
import com.github.wolf480pl.ircd.netty.codec.MessageDecoder;
import com.github.wolf480pl.ircd.netty.codec.MessageEncoder;
import com.github.wolf480pl.ircd.netty.codec.MessageHandler;

public class IRCChannelInitializer extends ChannelInitializer<SocketChannel> {
    public static final int MAX_LINE_LENGTH = 512;
    public static final int IDLE_TIMEOUT = 30;
    public static final Charset CHARSET = Charset.forName("UTF-8");

    private final SessionHandler handler;

    public IRCChannelInitializer(SessionHandler handler) {
        this.handler = handler;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        LineBasedFrameDecoder lineDecoder = new LineBasedFrameDecoder(MAX_LINE_LENGTH);
        StringDecoder stringDecoder = new StringDecoder(CHARSET); //FIXME: Should only split on CRLF, not on LF alone
        MessageDecoder messageDecoder = new MessageDecoder();
        MessageHandler messageHandler = new MessageHandler(handler);

        StringEncoder stringEncoder = new StringEncoder(CHARSET);
        MessageEncoder messageEncoder = new MessageEncoder();

        IdleStateHandler idleHandler = new IdleStateHandler(IDLE_TIMEOUT, 0, 0);

        // Inbound goes from first to last, outbound goes from last to first.
        // i.e. the outside is on the left/top, the inside is on the right/bottom
        ch.pipeline().addLast(lineDecoder).addLast(stringDecoder).addLast(messageDecoder).addLast(idleHandler).addLast(messageHandler)
                .addLast(stringEncoder).addLast(messageEncoder);

    }

}
