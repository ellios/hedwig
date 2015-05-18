package me.ellios.hedwig.rpc.thrift.client;

import me.ellios.hedwig.common.config.RpcConfig;
import org.apache.thrift.transport.TIOStreamTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;

/**
 * Plain old socket for thrift client.
 *
 * @author George
 * @since 13-11-14
 */
public class ThriftSocket extends TIOStreamTransport implements Closeable {

    private static final Logger LOG = LoggerFactory.getLogger(TSocket.class.getName());
    /**
     * Wrapped Socket object.
     */
    private Socket socket;
    /**
     * Remote address.
     */
    private final InetSocketAddress address;
    /**
     * The socket config info.
     */
    private final RpcConfig config;

    /**
     * Creates a new unconnected socket that will connect to the given
     * {@link java.net.InetSocketAddress}.
     *
     * @param address remote socket address.
     * @param config  socket config.
     */
    public ThriftSocket(InetSocketAddress address, RpcConfig config) {
        this.config = config;
        this.address = address;
        initSocket();
    }

    /**
     * Initializes the socket object
     */
    private void initSocket() {
        socket = new Socket();
        try {
            socket.setTcpNoDelay(config.tcpNoDelay());
            setSocketTimeout(getSocketTimeoutInMillis());
            socket.setReuseAddress(config.reuseAddress());
            socket.setReceiveBufferSize(config.receiveBufferSize());
            socket.setSendBufferSize(config.sendBufferSize());
        } catch (SocketException sx) {
            LOG.error("Could not configure socket.", sx);
        }
    }

    public int getConnectTimeoutInMillis() {
        return config.connectTimeoutInMillis();
    }

    public int getSocketTimeoutInMillis() {
        return Math.max(config.readTimeoutInMillis(), config.writeTimeoutInMillis());
    }

    /**
     * Sets the socket timeout
     *
     * @param timeoutInMillis Milliseconds timeout
     */
    public void setSocketTimeout(int timeoutInMillis) {
        try {
            socket.setSoTimeout(timeoutInMillis);
        } catch (SocketException sx) {
            LOG.warn("Could not set socket timeout.", sx);
        }
    }

    /**
     * Returns a reference to the underlying socket.
     *
     * @return the underlying socket.
     */
    public Socket getSocket() {
        if (socket == null) {
            initSocket();
        }
        return socket;
    }

    /**
     * Checks whether the socket is connected.
     *
     * @return {@link java.net.Socket#isConnected() }
     */
    @Override
    public boolean isOpen() {
        if (socket == null || socket.isClosed()) {
            return false;
        }
        return socket.isConnected();
    }

    /**
     * Connects the socket, creating a new socket object if necessary.
     *
     * @throws org.apache.thrift.transport.TTransportException
     */
    @Override
    public void open() throws TTransportException {
        if (isOpen()) {
            throw new TTransportException(TTransportException.ALREADY_OPEN, "Socket already connected.");
        }
        if (null == address) {
            throw new TTransportException(TTransportException.NOT_OPEN, "Cannot open null host.");
        }
        if (socket == null) {
            initSocket();
        }
        try {
            socket.connect(address, getConnectTimeoutInMillis());
            LOG.info("You just opened a connection {} with configuration {}", socket, config);
            inputStream_ = new BufferedInputStream(socket.getInputStream(), config.receiveBufferSize());
            outputStream_ = new BufferedOutputStream(socket.getOutputStream(), config.sendBufferSize());
        } catch (IOException iox) {
            close();
            throw new TTransportException(TTransportException.NOT_OPEN, iox);
        }
    }

    /**
     * Closes the socket.
     */
    @Override
    public void close() {
        // Close the underlying streams
        super.close();
        // Close the socket
        if (socket != null) {
            try {
                socket.close();
                LOG.info("You just closed an open connection {}", socket);
            } catch (IOException iox) {
                LOG.warn("Could not close socket {}.", socket, iox);
            }
            socket = null;
        }
    }
}
