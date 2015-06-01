package me.ellios.jedis.support;

import me.ellios.jedis.config.Config;
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
//        poolConfig.lifo = RedisConfigFileParsers.getPoolLifo();
//        poolConfig.maxActive = RedisConfigFileParsers.getPoolMaxActive();
//        poolConfig.maxIdle = me.ellios.jedis.util.RedisConfigFileParsers.getPoolMaxIdle();
//        poolConfig.minIdle = me.ellios.jedis.util.RedisConfigFileParsers.getPoolMinIdle();
//        poolConfig.maxWait = me.ellios.jedis.util.RedisConfigFileParsers.getPoolMaxWait();
//        poolConfig.testOnBorrow = me.ellios.jedis.util.RedisConfigFileParsers.getPoolTestOnBorrow();
//        poolConfig.testOnReturn = me.ellios.jedis.util.RedisConfigFileParsers.getPoolTestOnReturn();
//        poolConfig.whenExhaustedAction = (byte) me.ellios.jedis.util.RedisConfigFileParsers.getPoolWhenExhaustedAction();
//        poolConfig.testWhileIdle = me.ellios.jedis.util.RedisConfigFileParsers.getPoolTestWhileIdle();
//        poolConfig.minEvictableIdleTimeMillis = me.ellios.jedis.util.RedisConfigFileParsers.getPoolMinEvictableIdleTimeMillis();
//        poolConfig.timeBetweenEvictionRunsMillis = me.ellios.jedis.util.RedisConfigFileParsers.getPoolTimeBetweenEvictionRunsMillis();
//        poolConfig.numTestsPerEvictionRun = me.ellios.jedis.util.RedisConfigFileParsers.getPoolNumTestsPerEvictionRun();
//        poolConfig.softMinEvictableIdleTimeMillis = RedisConfigFileParsers.getPoolSoftMinEvictableIdleTimeMillis();
        return poolConfig;
    }
}
