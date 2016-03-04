package me.ellios.jedis.support;

import me.ellios.jedis.config.Config;
import me.ellios.jedis.util.RedisConfigFileParsers;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

/**
 * User: ellios
 * Time: 15-5-29 : 下午8:53
 */
abstract public class AbstractJedisContainer implements JedisContainer{

    protected final Config config;

    protected AbstractJedisContainer(Config config) {
        this.config = config;
    }

    /**
     * 获取连接池配置信息
     *
     * @return
     */
    protected GenericObjectPoolConfig getPoolConfig() {
        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        poolConfig.setLifo(RedisConfigFileParsers.getPoolLifo());
        poolConfig.setMaxTotal(RedisConfigFileParsers.getPoolMaxActive());
        poolConfig.setMaxIdle(RedisConfigFileParsers.getPoolMaxIdle());
        poolConfig.setMinIdle(RedisConfigFileParsers.getPoolMinIdle());
        poolConfig.setMaxWaitMillis(RedisConfigFileParsers.getPoolMaxWait());
        poolConfig.setTestOnBorrow(RedisConfigFileParsers.getPoolTestOnBorrow());
        poolConfig.setTestOnReturn(RedisConfigFileParsers.getPoolTestOnReturn());
        poolConfig.setBlockWhenExhausted(RedisConfigFileParsers.getBlockWhenExhausted());
        poolConfig.setTestWhileIdle(RedisConfigFileParsers.getPoolTestWhileIdle());
        poolConfig.setMinEvictableIdleTimeMillis(RedisConfigFileParsers.getPoolMinEvictableIdleTimeMillis());
        poolConfig.setTimeBetweenEvictionRunsMillis(RedisConfigFileParsers.getPoolTimeBetweenEvictionRunsMillis());
        poolConfig.setNumTestsPerEvictionRun(RedisConfigFileParsers.getPoolNumTestsPerEvictionRun());
        poolConfig.setSoftMinEvictableIdleTimeMillis(RedisConfigFileParsers.getPoolSoftMinEvictableIdleTimeMillis());
        return poolConfig;
    }
}
