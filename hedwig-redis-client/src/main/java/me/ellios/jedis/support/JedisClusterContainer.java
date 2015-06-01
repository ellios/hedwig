package me.ellios.jedis.support;

import me.ellios.jedis.OpType;
import me.ellios.jedis.config.Config;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.util.Pool;

import java.util.Collections;
import java.util.Set;

/**
 * User: ellios
 * Time: 15-5-29 : 下午8:57
 */
public class JedisClusterContainer extends AbstractJedisContainer {

    private final JedisCluster jedisCluster;

    protected JedisClusterContainer(Config config) {
        super(config);

        Set<HostAndPort> clusterNodes = Collections.emptySet();

        for (Config.RedisNode node : config.getNodes()) {
            clusterNodes.add(new HostAndPort(node.getHost(), node.getPort()));
        }

        jedisCluster = new JedisCluster(clusterNodes, getPoolConfig());
    }

    @Override
    public Pool<Jedis> getJedisPool(OpType opMode) {
        throw new UnsupportedOperationException();
    }

    @Override
    public JedisCluster getJedisCluster() {
        return jedisCluster;
    }

    @Override
    public void destroy() {
        jedisCluster.close();
    }
}
