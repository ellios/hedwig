package me.ellios.jedis.support;

import com.google.common.base.Preconditions;
import me.ellios.jedis.JedisCallback;
import me.ellios.jedis.JedisClusterCallback;
import me.ellios.jedis.OpType;
import me.ellios.jedis.config.Config;
import me.ellios.jedis.config.ServerMode;
import me.ellios.jedis.transcoders.SerializingTranscoder;
import me.ellios.jedis.transcoders.Transcoder;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.util.Pool;

import java.net.SocketException;

/**
 * 和云平台整合的redis客户端
 * Author: ellios
 * Date: 13-1-29 Time: 上午11:49
 */
abstract public class AbstractRedisClient {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractRedisClient.class);

    protected Transcoder transcoder = new SerializingTranscoder();

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
        int tryCount = 1;
        while (tryCount <= 3){
            //乐视的redis太不靠谱，这里增加了重试机制
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
                if(tryCount < 3){
                    if(jedisLocal.get() != null){
                        LOG.warn("fail to execute callback in redis host : {} port : {}, error : {}, try again tryCount : {}",
                                jedisLocal.get().getKey().getClient().getHost(),
                                jedisLocal.get().getKey().getClient().getPort(),
                                e.getMessage(), tryCount, e);
                    }else{
                        Pool<Jedis> pool = jedisContainer.getJedisPool(type);

                        LOG.warn("fail to execute callback in redis for redis not found. error : {}, try again tryCount : {}. active num : {}, wait num : {}, idel num : {}",
                                e.getMessage(), pool.getNumActive(), pool.getNumWaiters(), pool.getNumIdle(), tryCount, e);
                    }

                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e1) {
                    }
                }else{
                    LOG.warn("wooooo fail to execute callback in redis host : {} port : {}, error : {} after tryCount : {}",
                            jedisLocal.get().getKey().getClient().getHost(),
                            jedisLocal.get().getKey().getClient().getPort(),
                            e.getMessage(), tryCount, e);
                    throw e;
                }

            } finally {
                cleanContext(broken);
                tryCount++;
            }
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
                    jedis.close();
                    LOG.info("return broken jedis host : {} port : {} to pool : {}",
                            jedis.getClient().getHost(), jedis.getClient().getPort(), pool);
                }else{
                    pool.returnResource(jedis);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("return jedis host : {} port : {} to pool : {}",
                                jedis.getClient().getHost(), jedis.getClient().getPort(), pool);
                    }
                }

            }
        }
    }

    protected ServerMode getServerMode() {
        return this.config.getServerMode();
    }

}
