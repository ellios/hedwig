package me.ellios.jedis.support;

import com.google.common.base.Preconditions;
import me.ellios.jedis.JedisCallback;
import me.ellios.jedis.JedisClusterCallback;
import me.ellios.jedis.OpType;
import me.ellios.jedis.config.Config;
import me.ellios.jedis.config.ServerMode;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.util.Pool;

/**
 * 和云平台整合的redis客户端
 * Author: ellios
 * Date: 13-1-29 Time: 上午11:49
 */
abstract public class AbstractRedisClient {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractRedisClient.class);

    //redis数据库信息
    private volatile Config config = null;

    private volatile JedisContainer jedisContainer = null;

    private final ThreadLocal<Pair<Jedis, Pool<Jedis>>> jedisLocal = new ThreadLocal<>();


    private volatile boolean isShutdownHookCalled = false;
    private final Thread shutdownHookThread;


    public AbstractRedisClient(me.ellios.jedis.config.Config config) {
        Preconditions.checkNotNull(config);

        refresh(config);

        shutdownHookThread = new Thread(new Runnable() {
            @Override
            public void run() {
                isShutdownHookCalled = true;
                destroy();
            }
        });
        Runtime.getRuntime().addShutdownHook(shutdownHookThread);
    }

    /**
     * 刷新redis节点
     *
     * @param config
     */
    public void refresh(Config config) {

        if (config == null) {
            LOG.warn("redis config is null, will not refresh.");
            return;
        }
        LOG.info("trying to refresh with redis config : {}", config);
        this.config = config;

        JedisContainer container = JedisContainerFactory.createContainer(config);
        if (container == null) {
            LOG.info("fail to refresh jedis container with redis config : {}", config);
            return;
        }

        this.jedisContainer = container;
    }


    /**
     * 销毁客户端
     */
    public void destroy() {
        jedisContainer.destroy();
        //消除hook，如果hook thread没有在运行的话
        if (!isShutdownHookCalled) {
            LOG.info("remove hook thread");
            Runtime.getRuntime().removeShutdownHook(shutdownHookThread);
        }
        LOG.info("finish destroy redis client.");
    }

    /**
     * Template Method
     *
     * @param type
     * @param callback
     * @param <T>
     * @return
     */
    public <T> T executeWithJedis(OpType type, JedisCallback<T> callback) {
        boolean broken = false;
        Jedis jedis = null;
        try {
            jedis = getJedis(type);
            if (LOG.isDebugEnabled()) {
                LOG.debug("execute from redis {}:{}", jedis.getClient().getHost(),
                        jedis.getClient().getPort());
            }
            return callback.doWithJedis(jedis);
        } catch (JedisConnectionException e) {
            //Redis Connection is broken
            broken = true;
            LOG.error("fail to execute callback in redis:{}, error : {}", jedisLocal.get(), e.getMessage(), e);
        } finally {
            cleanContext(broken);
        }
        return null;
    }

    /**
     * Template Method
     *
     * @param callback
     * @param <T>
     * @return
     */
    public <T> T executeWithJedisCluster(JedisClusterCallback<T> callback) {
        JedisCluster cluster = null;
        try {
            cluster = jedisContainer.getJedisCluster();
            return callback.doWithJedisCluster(cluster);
        } catch (JedisConnectionException e) {
            //Redis Connection is broken
            LOG.error("fail to execute callback in redis cluster, error : {}", config.getNodes(), e.getMessage(), e);
        }
        return null;
    }

    /**
     * 获取jedis.
     *
     * @param type
     * @return Redis客户端
     */
    public Jedis getJedis(OpType type) {

        Pair<Jedis, Pool<Jedis>> pair = jedisLocal.get();
        if (pair != null) {
            return pair.getKey();
        }

        Pool<Jedis> pool = jedisContainer.getJedisPool(type);
        Jedis jedis = pool.getResource();
        jedisLocal.set(new ImmutablePair<>(jedis, pool));
        return jedis;
    }

    /**
     * 释放资源，将redis链接释放回连接池
     */
    protected void cleanContext(boolean broken) {
        Pair<Jedis, Pool<Jedis>> pair = jedisLocal.get();
        if (pair != null) {
            jedisLocal.remove();
            Jedis jedis = pair.getKey();
            Pool<Jedis> pool = pair.getValue();
            if (pool != null) {
                if (broken) {
                    pool.returnBrokenResource(jedis);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("return broken jedis : {} to pool : {}", jedis, pool);
                    }
                } else {
                    pool.returnResource(jedis);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("return jedis : {} to pool : {}", jedis, pool);
                    }
                }
            }
        }
    }

    protected ServerMode getServerMode() {
        return this.config.getServerMode();
    }

}
