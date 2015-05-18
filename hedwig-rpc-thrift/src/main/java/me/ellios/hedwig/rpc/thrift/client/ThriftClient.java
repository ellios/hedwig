package me.ellios.hedwig.rpc.thrift.client;

import me.ellios.hedwig.common.config.HedwigConfig;
import me.ellios.hedwig.common.config.RpcConfig;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.apache.thrift.transport.TTransportFactory;

import java.io.Closeable;
import java.net.InetSocketAddress;

public class ThriftClient implements Closeable {
    private TTransportFactory factory;
    private TTransport transport;

    public ThriftClient(int maxFrameSize) {
        // Take care of the network order.
        this(new TFramedTransport.Factory(maxFrameSize));
    }

    public ThriftClient(TTransportFactory factory) {
        this.factory = factory;
    }

    public TTransport connectSync(InetSocketAddress address) throws TTransportException {
        return connectSync(address, HedwigConfig.getInstance());
    }

    public TTransport connectSync(InetSocketAddress address, RpcConfig config) throws TTransportException {
        ThriftSocket socket = new ThriftSocket(address, config);
        socket.open();
        transport = factory.getTransport(socket);
        return transport;
    }

    @Override
    public void close() {
        if (null != transport) {
            transport.close();
            transport = null;
        }
    }
}
