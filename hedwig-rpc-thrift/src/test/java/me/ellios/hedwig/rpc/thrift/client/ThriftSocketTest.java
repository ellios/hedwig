package me.ellios.hedwig.rpc.thrift.client;

import me.ellios.hedwig.common.config.HedwigConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.*;

/**
 * Say something?
 *
 * @author George Cao
 * @since 2014-01-27 10
 */
public class ThriftSocketTest {
    private static final Logger LOG = LoggerFactory.getLogger(ThriftSocketTest.class);

    int port = 9010;
    Thread thread;


    public void createServer() throws Exception {
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ServerSocket serverSocket = new ServerSocket(port);
                    while (true) {
                        Socket socket = serverSocket.accept();
                        LOG.info("Client {}", socket.getInetAddress());

                    }
                } catch (IOException e) {
                    LOG.error("Handle exception please", e);
                }

            }
        });
        thread.start();
    }

    @Test
    public void testGetConnectTimeoutInMillis() throws Exception {

    }

    @Test
    public void testGetSocketTimeoutInMillis() throws Exception {

    }

    @Test
    public void testSetSocketTimeout() throws Exception {

    }

    ThriftSocket thriftSocket;

    public void setUp() throws Exception {
        thriftSocket = new ThriftSocket(new InetSocketAddress(port), HedwigConfig.getInstance());
        thriftSocket.open();
    }


    public void tearDown() throws Exception {
        thriftSocket.close();
    }

    @Test
    public void testGetSocket() throws Exception {
        Socket socket = thriftSocket.getSocket();
        assertNotNull(socket);
    }

    @Test
    public void testIsOpen() throws Exception {
        assertTrue(thriftSocket.isOpen());
    }

    @Test
    public void testOpen() throws Exception {
        try {
            thriftSocket.open();
            fail("already open.");
        } catch (Exception e) {

        }
    }

    @Test
    public void testClose() throws Exception {
        thriftSocket.close();
        assertTrue(!thriftSocket.isOpen());
    }

    @Test
    public void testServerClose() throws Exception {
        final int localPort = port + 1;
        final List<Socket> sockets = new ArrayList<>();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ServerSocket serverSocket = new ServerSocket(localPort);
                    while (true) {
                        final Socket socket = serverSocket.accept();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                LOG.info("Client {}", socket.getInetAddress());
                                sockets.add(socket);
                                try {
                                    InputStream in = socket.getInputStream();
                                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                                    String line;
                                    while (null != (line = reader.readLine())) {
                                        System.out.println(line);
                                    }
                                } catch (IOException e) {
                                    LOG.error("Handle exception please", e);
                                }
                            }
                        }).start();
                    }
                } catch (IOException e) {
                    LOG.error("Error", e);
                }
            }
        }).start();
        ThriftSocket thriftSocket = new ThriftSocket(new InetSocketAddress(localPort), HedwigConfig.getInstance());
        thriftSocket.open();
        assertTrue(thriftSocket.isOpen());
        String val = "test\r\n";
        thriftSocket.write(val.getBytes());
        thriftSocket.write(val.getBytes());
        thriftSocket.write(val.getBytes());
        thriftSocket.flush();
        for (Socket socket : sockets) {
            socket.close();
            assertTrue(socket.isClosed());
        }
        TimeUnit.SECONDS.sleep(1);

        int count = 100;
        int i = 0;
        try {
            while (i++ < count) {
                thriftSocket.write(val.getBytes());
                thriftSocket.flush();
            }
            fail("");
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(thriftSocket.isOpen());
        }
    }

    @Test
    public void testClientClose() throws Exception {
        int localPort = port + 2;
        final ServerSocket serverSocket = new ServerSocket(localPort);
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        final Socket socket = serverSocket.accept();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    OutputStream out = socket.getOutputStream();
                                    InputStream in = socket.getInputStream();
                                    byte[] buffer = new byte[16];
                                    int count = in.read(buffer);
                                    out.write("xx".getBytes());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

        ThriftSocket client = new ThriftSocket(new InetSocketAddress("10.1.212.161", localPort), HedwigConfig.getInstance());
        client.open();
        TimeUnit.SECONDS.sleep(1);
        client.write(UUID.randomUUID().toString().getBytes());
        client.close();

        TimeUnit.SECONDS.sleep(10);
    }
}
