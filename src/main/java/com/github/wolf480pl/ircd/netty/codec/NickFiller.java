package com.github.wolf480pl.ircd.netty.codec;

import java.util.List;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import com.github.wolf480pl.ircd.LazyNickedMessage;

public class NickFiller extends MessageToMessageEncoder<LazyNickedMessage> {

    @Override
    protected void encode(ChannelHandlerContext ctx, LazyNickedMessage msg, List<Object> out) throws Exception {
        String nick = ctx.attr(NickTracker.ATTR_NICK).get();
        out.add(msg.fill(nick));
    }

}
