package me.ellios.hedwig.benchmark.thrift.client;

import me.ellios.hedwig.benchmark.AbstractBenchmarkClient;
import me.ellios.hedwig.benchmark.ClientRunnable;
import me.ellios.hedwig.benchmark.thrift.Benchmark;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TMultiplexedProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

/**
 * @author wangweiping
 */
public class OioThriftBenchmarkClient extends AbstractBenchmarkClient {
    private static final Logger LOG = LoggerFactory.getLogger(OioThriftBenchmarkClient.class);

    private String host;
    private int port;

    public OioThriftBenchmarkClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public Benchmark.Iface create() {
        TTransport transport = new TSocket(host, port);
        if (!transport.isOpen()) {
            try {
                transport.open();
            } catch (TTransportException e) {
                LOG.warn("", e);
            }
        }
        TTransport framed = new TFramedTransport.Factory().getTransport(transport);
        TProtocolFactory factory = new TCompactProtocol.Factory();
        TMultiplexedProtocol protocol = new TMultiplexedProtocol(factory.getProtocol(framed), "T_Benchmark");
        return new Benchmark.Client.Factory().getClient(protocol);
    }

    public static void main(String[] args) throws Exception {
        String host = "localhost";
        int port = 8113;
        if (args.length > 0) {
            host = args[0];
        }
        if (args.length > 1) {
            port = Integer.valueOf(args[1]);
        }
        new OioThriftBenchmarkClient(host, port).run(new String[]{"100", "120", "5"});
    }

    @Override
    public ClientRunnable getClientRunnable(int clientNums, CyclicBarrier barrier,
                                            CountDownLatch latch, long endTime, long startTime) {
        return new ThriftBenchmarkClientRunnable(barrier, latch, startTime, endTime, create());
    }

}
