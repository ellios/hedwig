package me.ellios.hedwig.benchmark;

import me.ellios.hedwig.benchmark.thrift.Benchmark;
import me.ellios.hedwig.benchmark.thrift.service.BenchmarkServiceImpl;
import me.ellios.hedwig.rpc.core.ServiceConfigFactory;
import me.ellios.hedwig.rpc.server.HedwigServer;

/**
 * Author: ellios
 * Date: 12-11-14 Time: 下午6:14
 */
public class BenchmarkServer {

    public static void main(String... args){
        HedwigServer server = HedwigServer.getServer();
        server.registerService(ServiceConfigFactory.createThriftServiceConfig(Benchmark.Iface.class,
                BenchmarkServiceImpl.class, 8113));
        server.start();
    }
}
