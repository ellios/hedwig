package me.ellios.jedis.support;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import me.ellios.jedis.OpType;
import me.ellios.jedis.config.Config;
import me.ellios.jedis.util.RedisConfigFileParsers;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.util.Pool;

import java.io.IOException;
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

        Set<HostAndPort> clusterNodes = Sets.newHashSet();

        for (Config.RedisNode node : config.getNodes()) {
            clusterNodes.add(new HostAndPort(node.getHost(), node.getPort()));
        }


        int timeout = RedisConfigFileParsers.getTimeout();
        jedisCluster = new JedisCluster(clusterNodes, timeout, timeout, 3, config.getPassword(), getPoolConfig());
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
        try {
            jedisCluster.close();
        } catch (IOException e) {
        }
    }
}
