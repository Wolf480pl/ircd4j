package com.github.wolf480pl.ircd.netty.codec;

import java.util.List;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.util.AttributeKey;

import com.github.wolf480pl.ircd.Message;
import com.github.wolf480pl.ircd.Message.Prefix;

public class NickTracker extends MessageToMessageEncoder<Message> {
    protected static final AttributeKey<String> ATTR_NICK = AttributeKey.valueOf(NickTracker.class.getCanonicalName() + ".NICK");

    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, List<Object> out) throws Exception {
        out.add(msg);
        if (msg.getCommand().equalsIgnoreCase("NICK")) {
            if (Prefix.parse(msg.getPrefix()).nick() == ctx.channel().attr(ATTR_NICK).get()) {
                String nick = msg.getParams().get(0);
                ctx.channel().attr(ATTR_NICK).set(nick);
            }
        }
    }

}
