package me.ellios.hedwig.benchmark.thrift.client;

import me.ellios.hedwig.benchmark.AbstractBenchmarkClient;
import me.ellios.hedwig.benchmark.ClientRunnable;
import me.ellios.hedwig.benchmark.thrift.Benchmark;
import me.ellios.hedwig.rpc.client.ServiceHelper;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

/**
 * @author wangweiping
 */
public class ThriftBenchmarkClient extends AbstractBenchmarkClient {

    private Benchmark.Iface service = ServiceHelper.getThriftService(Benchmark.Iface.class);

    public static void main(String[] args) throws Exception {
        new ThriftBenchmarkClient().run(new String[]{"100", "120", "5"});
    }

    @Override
    public ClientRunnable getClientRunnable(int clientNums, CyclicBarrier barrier,
                                            CountDownLatch latch, long endTime, long startTime) {
        return new ThriftBenchmarkClientRunnable(barrier, latch, startTime, endTime, service);
    }

}
