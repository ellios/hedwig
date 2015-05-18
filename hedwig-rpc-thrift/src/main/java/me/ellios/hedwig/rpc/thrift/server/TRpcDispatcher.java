/**
 * Copyright 2012 Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     client://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package me.ellios.hedwig.rpc.thrift.server;

import me.ellios.hedwig.rpc.thrift.ThriftServiceDef;
import me.ellios.hedwig.rpc.thrift.processor.TTraceableProcessor;
import org.apache.thrift.TException;
import org.apache.thrift.TProcessor;
import org.apache.thrift.TProcessorFactory;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.transport.TTransport;
import org.jboss.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.util.concurrent.Executor;

/**
 * Dispatch TNiftyTransport to the TProcessor and write output back.
 */
public class TRpcDispatcher extends SimpleChannelUpstreamHandler {

    private static final Logger logger = LoggerFactory.getLogger(TRpcDispatcher.class);

    private final TProcessorFactory processorFactory;
    private final TProtocolFactory inProtocolFactory;
    private final TProtocolFactory outProtocolFactory;
    private final Executor exec;
    private ThriftServiceDef def;

    public TRpcDispatcher(ThriftServiceDef serviceDef) {
        this.processorFactory = serviceDef.getProcessorFactory();
        this.inProtocolFactory = serviceDef.getInProtocolFactory();
        this.outProtocolFactory = serviceDef.getOutProtocolFactory();
        this.exec = serviceDef.getExecutor();
        this.def = serviceDef;
    }

    @Override
    public void messageReceived(final ChannelHandlerContext ctx, final MessageEvent e) throws Exception {
        if (e.getMessage() instanceof TTransport) {
            // Because the client works in synchronous mode, which means the client first sends one request
            // then waits for the response from the server, after that, the client sends next request.
            // And from the server's point of view, the requests just come in one by one within one channel.
            // So we can do this without causing any problems and the meantime do not block the I/O thread.
            // TODO refactoring this with ExecutionHandler to support async client request or pipeline requests.
            exec.execute(new Runnable() {
                @Override
                public void run() {
                    TTransport transport = (TTransport) e.getMessage();
                    TProtocol inProtocol = inProtocolFactory.getProtocol(transport);
                    TProtocol outProtocol = outProtocolFactory.getProtocol(transport);
                    try {
                        TProcessor concrete = processorFactory.getProcessor(transport);
                        TTraceableProcessor processor = new TTraceableProcessor(ctx, concrete, def.getTracer(), def.getMapping());
                        processor.process(inProtocol, outProtocol);
                    } catch (TException e) {
                        logger.error("Exception while invoking!", e);
                        closeChannel(ctx);
                    }
                }
            });
        } else {
            ctx.sendUpstream(e);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        // Any out of band exception are caught here and we tear down the socket
        Channel channel = ctx.getChannel();
        SocketAddress remote = null;
        SocketAddress local = null;
        if (null != channel) {
            remote = channel.getRemoteAddress();
            local = channel.getLocalAddress();
        }
        logger.error("On channel remote:{}, local:{}", remote, local, e.getCause());
        closeChannel(ctx);
    }

    private void closeChannel(ChannelHandlerContext ctx) {
        if (ctx.getChannel().isOpen()) {
            ctx.getChannel().close();
        }
    }
}
