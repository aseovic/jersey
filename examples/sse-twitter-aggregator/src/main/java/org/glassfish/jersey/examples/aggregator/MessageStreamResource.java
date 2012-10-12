/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package org.glassfish.jersey.examples.aggregator;

import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.media.sse.EventChannel;
import org.glassfish.jersey.media.sse.OutboundEvent;
import org.glassfish.jersey.media.sse.SseBroadcaster;
import org.glassfish.jersey.server.ChunkedResponse;

/**
 * Resource that aggregates incoming messages and broadcasts them
 * to the registered Server-Sent Even (SSE) client streams.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
@Path("message/stream")
public class MessageStreamResource {
    private static final Logger LOGGER = Logger.getLogger(MessageStreamResource.class.getName());

    private static SseBroadcaster broadcaster = new SseBroadcaster() {
        @Override
        public void onException(ChunkedResponse<OutboundEvent> chunkedResponse, Exception exception) {
            LOGGER.log(Level.SEVERE, "Error broadcasting message.", exception);
        }
    };
    private static AtomicLong nextMessageId = new AtomicLong(0);

    /**
     * Put a new message to the stream.
     *
     * The message will be broadcast to all registered SSE clients.
     *
     * @param message message to be broadcast.
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public void putMessage(Message message) {
        LOGGER.info("--> Message received.");

        OutboundEvent event = new OutboundEvent.Builder()
                .id(String.valueOf(nextMessageId.getAndIncrement()))
                .mediaType(MediaType.APPLICATION_JSON_TYPE)
                .data(Message.class, message)
                .build();

        broadcaster.broadcast(event);
    }

    /**
     * Get the new SSE message stream channel.
     *
     * @return new SSE message stream channel.
     */
    @GET
    @Produces(EventChannel.SERVER_SENT_EVENTS)
    public EventChannel getMessageStream() {
        LOGGER.info("--> SSE connection received.");
        final EventChannel eventChannel = new EventChannel();
        broadcaster.add(eventChannel);
        return eventChannel;
    }

}