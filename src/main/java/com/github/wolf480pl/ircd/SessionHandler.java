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

import org.apache.logging.log4j.Logger;

public interface SessionHandler {

    void messageReceived(Session session, Message msg);

    void onInboundThrowable(Session session, Throwable t);

    void onOutboundThrowable(Session session, Throwable t);

    void onInboundIdle(Session session);

    void onDisconnect(Session session);

    Logger getLogger(Session session);
}
