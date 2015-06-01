package me.ellios.jedis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;

/**
 * User: ellios
 * Time: 15-5-29 : 下午10:23
 */
public interface JedisClusterCallback<T> {

    public T doWithJedisCluster(JedisCluster cluster);
}
