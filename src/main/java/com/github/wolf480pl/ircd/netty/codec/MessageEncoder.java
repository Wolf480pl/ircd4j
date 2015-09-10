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

import java.util.Iterator;
import java.util.List;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import com.github.wolf480pl.ircd.Message;

public class MessageEncoder extends MessageToMessageEncoder<Message> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, List<Object> out) throws Exception {
        StringBuilder builder = new StringBuilder();
        String prefix = msg.getPrefix();
        if (prefix != null && !prefix.isEmpty()) {
            builder.append(':').append(prefix).append(' ');
        }

        builder.append(msg.getCommand());

        Iterator<String> it = msg.getParams().iterator();
        while (it.hasNext()) {
            String param = it.next();
            if (param == null) {
                //TODO: Should this happen at all?
                continue;
            }
            if (param.contains(" ") || param.isEmpty()) {
                if (it.hasNext()) {
                    throw new IllegalArgumentException("IRC command parameter \"" + param + "\" contains space (or is empty) and isn't the last parameter");
                } else {
                    builder.append(" :").append(param);
                }
            } else {
                builder.append(' ').append(param);
            }
        }
        builder.append("\r\n");
        out.add(builder.toString());
    }

}
