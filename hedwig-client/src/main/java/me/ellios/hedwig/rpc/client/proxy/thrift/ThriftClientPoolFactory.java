package me.ellios.hedwig.rpc.client.proxy.thrift;

import me.ellios.hedwig.common.config.HedwigConfig;
import me.ellios.hedwig.rpc.core.ServiceNode;
import me.ellios.hedwig.rpc.proxy.pool.HedwigPoolConfig;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.thrift.TServiceClient;

/**
 * Author: ellios
 * Date: 12-12-7 Time: 下午3:09
 */
public class ThriftClientPoolFactory {


    private static HedwigPoolConfig getDefaultPoolConfig() {
        HedwigConfig hc = HedwigConfig.getInstance();

        HedwigPoolConfig config = new HedwigPoolConfig();
        config.lifo = hc.getBoolean("hedwig.rpc.pool.lifo", true);
        config.setMaxActive(hc.getInt("hedwig.rpc.pool.maxActive", 200));
        config.setMaxIdle(hc.getInt("hedwig.rpc.pool.maxIdle", 50));
        config.setMinIdle(hc.getInt("hedwig.rpc.pool.minIdle", 20));
        config.setMaxWait(hc.getInt("hedwig.rpc.pool.maxWait", 1000));
        config.setTestOnBorrow(hc.getBoolean("hedwig.rpc.pool.testOnBorrow", true));
        config.setTestOnReturn(hc.getBoolean("hedwig.rpc.pool.testOnReturn", false));
        config.setWhenExhaustedAction((byte) hc.getInt("hedwig.rpc.pool.whenExhaustedAction", GenericObjectPool.WHEN_EXHAUSTED_FAIL));
        config.setTestWhileIdle(true);
        config.setMinEvictableIdleTimeMillis(hc.getLong("hedwig.rpc.pool.minEvictableIdleTimeMillis", config.getMinEvictableIdleTimeMillis()));
        config.setTimeBetweenEvictionRunsMillis(hc.getLong("hedwig.rpc.pool.timeBetweenEvictionRunsMillis", config.getTimeBetweenEvictionRunsMillis()));
        config.setNumTestsPerEvictionRun(-1);
        return config;
    }

    /**
     * 传教client代理池
     *
     * @param node
     * @return
     */
    public static <T extends TServiceClient> ThriftClientPool<T> createPool(ServiceNode node) {
        return new ThriftClientPool<>(getDefaultPoolConfig(), node);
    }
}
