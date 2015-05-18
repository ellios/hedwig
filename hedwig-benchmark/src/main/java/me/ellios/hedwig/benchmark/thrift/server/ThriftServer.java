package me.ellios.hedwig.benchmark.thrift.server;

import me.ellios.hedwig.benchmark.thrift.Benchmark;
import me.ellios.hedwig.benchmark.thrift.service.BenchmarkServiceImpl;
import me.ellios.hedwig.rpc.core.ServiceConfig;
import me.ellios.hedwig.rpc.core.ServiceType;
import me.ellios.hedwig.rpc.server.HedwigServer;

/**
 * Author: ellios
 * Date: 12-11-14 Time: 下午5:01
 */
public class ThriftServer {

    public static void main(String... args) {
        HedwigServer server = HedwigServer.getServer();
        ServiceConfig config = ServiceConfig.newBuilder()
                .serviceFace(Benchmark.Iface.class)
                .serviceImpl(BenchmarkServiceImpl.class)
                .port(8113)
                .type(ServiceType.THRIFT)
                .build();
        server.registerService(config);


        server.start();
    }
}
