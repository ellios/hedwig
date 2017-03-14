package me.ellios.hedwig.benchmark;

import me.ellios.hedwig.benchmark.thrift.Benchmark;
import me.ellios.hedwig.benchmark.thrift.client.OioThriftBenchmarkClient;
import me.ellios.hedwig.rpc.client.ServiceHelper;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Say something?
 * User: George
 * Date: 13-3-5
 * Time: 上午11:47
 */

public class AbstractBenchmarkClientTest {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractBenchmarkClientTest.class);

    private Benchmark.Iface service = ServiceHelper.getThriftService(Benchmark.Iface.class);


    @Test
    public void testOut() throws Exception {
        System.out.println("===============================");
        System.out.println(service.ping("ping"));
        System.out.println("===============================");
//        OioThriftBenchmarkClient client = new OioThriftBenchmarkClient("",2);
//        client.out(0,0,32,23,32);
//        client.out(0,200,32,23,32);
//        client.out(200,1000,0,2000003,3000002);
//        client.out(1000,5000,100, 0,32);
    }
}
