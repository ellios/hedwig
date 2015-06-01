package me.ellios.jedis;

import redis.clients.jedis.Jedis;

/**
 * Author: ellios
 * Date: 13-1-29 Time: 下午7:02
 */
public interface JedisCallback<T> {

    public T doWithJedis(Jedis jedis);

}
