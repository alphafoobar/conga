/*
 * Copyright 2018 FIX Protocol Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package io.fixprotocol.conga.server.io;

import java.security.Principal;

import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;

import io.fixprotocol.conga.buffer.RingBufferSupplier;
import io.fixprotocol.conga.server.io.callback.BinaryExchangeSocket;
import io.fixprotocol.conga.server.io.callback.TextExchangeSocket;
import io.fixprotocol.conga.server.session.ServerSessions;

/**
 * WebSocket creator only accepts requests for binary subprotocol
 * 
 * Todo: register FIX as a subprotocol
 * 
 * @author Don Mendelson
 *
 */
public class ExchangeSocketCreator implements WebSocketCreator {

  private final RingBufferSupplier ringBuffer;
  private final ServerSessions sessions;

  /**
   * Constructor
   * @param sessions associates sessions to transports
   * @param ringBuffer provides buffers to persist received messages
   */
  public ExchangeSocketCreator(ServerSessions sessions, RingBufferSupplier ringBuffer) {
    this.sessions = sessions;
    this.ringBuffer = ringBuffer;
  }

  @Override
  public Object createWebSocket(ServletUpgradeRequest request, ServletUpgradeResponse response) {
    Principal principal = request.getUserPrincipal();
    String source;
    source = (null != principal) ? principal.getName() : request.getRemoteAddress();
    for (String subprotocol : request.getSubProtocols()) {
      if ("binary".equals(subprotocol)) {
        response.setAcceptedSubProtocol(subprotocol);
        return new BinaryExchangeSocket(sessions, ringBuffer, source);
      } else if ("text".equals(subprotocol)) {
        response.setAcceptedSubProtocol(subprotocol);
        return new TextExchangeSocket(sessions, ringBuffer, source);
      }
    }

    return null;
  }

}
