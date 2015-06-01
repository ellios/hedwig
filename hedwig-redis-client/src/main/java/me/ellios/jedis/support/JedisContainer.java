package me.ellios.jedis.support;

import me.ellios.jedis.OpType;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.util.Pool;

/**
 * User: ellios
 * Time: 15-5-28 : 下午12:42
 */
public interface JedisContainer {

    public Pool<Jedis> getJedisPool(OpType opMode);

    public JedisCluster getJedisCluster();

    public void destroy();
}
