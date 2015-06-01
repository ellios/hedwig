package me.ellios.jedis.support;

import com.google.common.collect.Sets;
import me.ellios.jedis.OpType;
import me.ellios.jedis.config.Config;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisSentinelPool;
import redis.clients.util.Pool;

import java.util.Collections;
import java.util.Set;

/**
 * User: ellios
 * Time: 15-5-29 : 下午8:32
 */
public class SentinelJedisContainer extends AbstractJedisContainer{

    private final JedisSentinelPool sentinelPool;

    public SentinelJedisContainer(Config config) {
        super(config);
        Set<String> sentinels = Sets.newHashSet();
        for (Config.RedisNode node : config.getNodes()) {
            sentinels.add(node.getHost() + ":" + node.getPort());
        }

        sentinelPool = new JedisSentinelPool(config.getSentinelName(), sentinels);
    }


    @Override
    public Pool<Jedis> getJedisPool(OpType opMode) {
        return sentinelPool;
    }

    @Override
    public JedisCluster getJedisCluster() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void destroy() {
        sentinelPool.destroy();
    }
}
