package me.ellios.hedwig.rpc.thrift.server;

import com.google.common.io.Closeables;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import me.ellios.hedwig.rpc.thrift.ThriftServiceDef;
import me.ellios.hedwig.rpc.tracer.impl.SampleLogTracer;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.testng.Assert.assertTrue;

/**
 * Say something?
 *
 * @author George Cao
 * @since 13-3-8 上午11:20
 */

public class ConnectionLimitFilterHandlerTest {
    private static final Logger LOG = LoggerFactory.getLogger(ConnectionLimitFilterHandlerTest.class);
    Charset charset = Charset.forName("UTF-8");

    final AtomicInteger localPort = new AtomicInteger(ThreadLocalRandom.current().nextInt(2048, 65535));
    ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(),
            new ThreadFactoryBuilder().setNameFormat("Test-Thread-#%d").build());

    @BeforeMethod
    public void setUp() throws Exception {
    }

    @Test
    public void testGlobalLimits() throws Exception {
        for (int i = 1; i < 4; i++) {
            testCloseConnections(i, i - 1);
        }
    }

    @Test
    public void testHostLimits() throws Exception {
        for (int i = 0; i < 4; i++) {
            testCloseConnections(i);
        }
    }

    public void testCloseConnections(int n) throws Exception {
        testCloseConnections(n, n);
    }

    public void testCloseConnections(int n, int m) throws Exception {
        final ThriftServiceDef def = ThriftServiceDef.newBuilder()
                .serverPort(2333)
                .hostConnectionsThresholds(n)
                .openConnectionsThresholds(m)
                .tracer(new SampleLogTracer(LOG))
                .enableConnectionCheck(true)
                .build();
        Future<ServerBootstrap> future = start(def);
        ServerBootstrap bootstrap = future.get();
        final AtomicBoolean yes = new AtomicBoolean(false);

        final int t = def.getHostConnectionsThresholds() + 1;

        final CountDownLatch latch = new CountDownLatch(t);
        for (int i = 0; i < t; i++) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    Socket socket = null;
                    int port = localPort.getAndIncrement();
                    try {
                        socket = new Socket("0.0.0.0", 2333, null, port);
                        OutputStream out = socket.getOutputStream();
                        for (; ; ) {
                            out.write(("data" + Thread.currentThread().getName()).getBytes(charset));
                            if (latch.getCount() < t) {
                                break;
                            }
                            Thread.sleep(200);
                        }
                    } catch (SocketException e) {
                        yes.compareAndSet(false, true);
                        LOG.warn("Socket error on port {}", port, e);
                    } catch (IOException | InterruptedException ignored) {
                        ignored.printStackTrace();
                        // ignored
                    } finally {
                        latch.countDown();
                        try {
                            socket.close();
                        } catch (IOException e) {

                        }
                    }
                }
            });
        }
        latch.await();
        assertTrue(yes.get());
        bootstrap.shutdown();
    }

    private Future<ServerBootstrap> start(final ThriftServiceDef def) {
        final ConnectionLimitFilterHandler handler = new ConnectionLimitFilterHandler(def);
        return executor.submit(new Callable<ServerBootstrap>() {
            @Override
            public ServerBootstrap call() throws Exception {
                ServerBootstrap bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory());
                bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
                    @Override
                    public ChannelPipeline getPipeline() throws Exception {
                        ChannelPipeline pipeline = Channels.pipeline();
                        pipeline.addLast("limit", handler);
                        pipeline.addLast("out", new SimpleChannelHandler() {
                            @Override
                            public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
                                if (e.getMessage() instanceof ChannelBuffer) {
                                    ChannelBuffer buffer = (ChannelBuffer) e.getMessage();
                                    LOG.info("{}", buffer.toString(charset));
                                }
                            }
                        });
                        return pipeline;
                    }
                });
                bootstrap.setOption("soLinger", 0);
                bootstrap.bind(new InetSocketAddress("0.0.0.0", def.getServerPort()));
                return bootstrap;
            }
        });
    }

    @Test
    public void testAfterBlocked() throws Exception {
        final ThriftServiceDef def = ThriftServiceDef.newBuilder()
                .serverPort(8192)
                .hostConnectionsThresholds(7)
                .openConnectionsThresholds(7)
                .enableConnectionCheck(true)
                .tracer(new SampleLogTracer(LOG))
                .build();
        ServerBootstrap bootstrap = start(def).get();
        List<Socket> list = new LinkedList<>();
        for (int i = 0; i < def.getHostConnectionsThresholds(); i++) {
            int port = localPort.getAndIncrement();
            Socket socket = new Socket("0.0.0.0", def.getServerPort(), null, port);
            socket.setKeepAlive(true);
            socket.setTcpNoDelay(true);
            int n = 0;
            OutputStream out = socket.getOutputStream();
            while (n++ < 2) {
                out.write(String.valueOf(port).concat(":").getBytes(charset));
            }
            list.add(socket);
        }

        // Wait for all the connections are established.
        Thread.sleep(100);
        int port = localPort.getAndIncrement();
        // This will not work.
        Socket socket = new Socket("0.0.0.0", def.getServerPort(), null, port);
        LOG.info("This will fail {}", port);
        list.add(socket);
        try (OutputStream out = socket.getOutputStream()) {
            for (; ; ) {

                out.write("WTF".getBytes(charset));
                Thread.sleep(500);
            }
        } catch (SocketException e) {
            LOG.warn("port:{}", port, e);
        }
        for (Socket s : list) {
            LOG.info("{}", s.isClosed());
            s.close();
        }
        bootstrap.shutdown();
    }

    @Test
    public void testAccept() throws Exception {
        final ThriftServiceDef def = ThriftServiceDef.newBuilder()
                .serverPort(8192)
                .hostConnectionsThresholds(10)
                .openConnectionsThresholds(300)
                .enableConnectionCheck(true)
                .tracer(new SampleLogTracer(LOG))
                .build();
        ServerBootstrap bootstrap = start(def).get();
        List<Socket> list = new LinkedList<>();
        for (int i = 0; i < def.getHostConnectionsThresholds(); i++) {
            try (Socket socket = new Socket("localhost", def.getServerPort())) {
                socket.getOutputStream().write("test".getBytes());
                list.add(socket);
            }
        }
        bootstrap.shutdown();
    }
}
