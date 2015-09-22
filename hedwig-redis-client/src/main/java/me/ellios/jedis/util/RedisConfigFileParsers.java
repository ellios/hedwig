package me.ellios.jedis.util;

import me.ellios.hedwig.common.config.HedwigConfig;
import me.ellios.hedwig.common.utils.ClassLoaderUtils;

import java.net.URL;

/**
 * User: ellios
 * Time: 13-9-25 : 上午10:17
 */
public class RedisConfigFileParsers {

    private static final HedwigConfig HEDWIG_CONFIG = HedwigConfig.getInstance();

    private static final String REDIS_SERVERS_KEY_PREFIX = "redis.servers.";


    public static String getRedisServers(String name) {
        return HEDWIG_CONFIG.getString(REDIS_SERVERS_KEY_PREFIX + name, "");
    }

    public static boolean getPoolLifo() {
        return HEDWIG_CONFIG.getBoolean("redis.pool.lifo", true);
    }

    public static int getPoolMaxActive() {
        return HEDWIG_CONFIG.getInt("redis.pool.maxActive", 200);
    }                       

    public static int getPoolMaxIdle() {
        return HEDWIG_CONFIG.getInt("redis.pool.maxIdle", 100);
    }

    public static int getPoolMinIdle() {
        return HEDWIG_CONFIG.getInt("redis.pool.minIdle", 20);
    }

    public static int getPoolMaxWait() {
        return HEDWIG_CONFIG.getInt("redis.pool.maxWait", 1000);
    }

    public static boolean getPoolTestWhileIdle() {
        return HEDWIG_CONFIG.getBoolean("redis.pool.testWhileIdle", true);
    }

    public static boolean getPoolTestOnBorrow() {
        return HEDWIG_CONFIG.getBoolean("redis.pool.testOnBorrow", true);
    }

    public static boolean getPoolTestOnReturn() {
        return HEDWIG_CONFIG.getBoolean("redis.pool.testOnReturn", false);
    }

    public static int getPoolMinEvictableIdleTimeMillis() {
        return HEDWIG_CONFIG.getInt("redis.pool.minEvictableIdleTimeMillis", 1200000);
    }

    public static int getPoolTimeBetweenEvictionRunsMillis() {
        return HEDWIG_CONFIG.getInt("redis.pool.timeBetweenEvictionRunsMillis", 10000);
    }

    public static int getPoolNumTestsPerEvictionRun() {
        return HEDWIG_CONFIG.getInt("redis.pool.numTestsPerEvictionRun", 5);
    }

    public static long getPoolSoftMinEvictableIdleTimeMillis() {
        return HEDWIG_CONFIG.getInt("redis.pool.softMinEvictableIdleTimeMillis", 120000);
    }

    public static int getTimeout() {
        return HEDWIG_CONFIG.getInt("redis.timeout", 1000);
    }

    /**
     * 获取配置文件的绝对路径
     *
     * @return
     */
    public static String getConfigFileAbsolutePath() {

        String classPathFilename = "properties/hedwig.properties";
        URL url = ClassLoaderUtils.getURL(classPathFilename);
        if (url == null) {
            return null;
        }
        return url.getPath();
    }
}
