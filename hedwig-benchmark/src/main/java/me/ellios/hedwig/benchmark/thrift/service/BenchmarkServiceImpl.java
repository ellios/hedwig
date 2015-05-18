package me.ellios.hedwig.benchmark.thrift.service;

import me.ellios.hedwig.benchmark.thrift.Benchmark;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;

/**
 * Author: ellios
 * Date: 12-11-14 Time: 下午4:59
 */
@Service("thriftBenchmarkService")
public class BenchmarkServiceImpl implements Benchmark.Iface{
    private static Logger logger = LoggerFactory.getLogger(BenchmarkServiceImpl.class);


    @Override
    public String ping(String ping) throws TException {
        logger.info("get message : " + ping);
        return ping;
    }

    @Override
    public ByteBuffer benchmark(ByteBuffer data) throws TException {
        return data;
    }
}
