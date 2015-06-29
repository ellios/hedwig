package me.ellios.jedis;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import me.ellios.jedis.config.Config;
import me.ellios.jedis.config.ConfigFactory;
import me.ellios.jedis.config.ConfigListener;
import me.ellios.jedis.config.support.ConfigFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

/**
 * Author: ellios
 * Date: 13-1-29 Time: 下午5:30
 */
public class RedisClientFactory {

    private static final Logger LOG = LoggerFactory.getLogger(RedisClientFactory.class);

    private static ConcurrentMap<String, FutureTask<me.ellios.jedis.RedisClient>> clients = new ConcurrentHashMap<>();


    /**
     * 获取redis 客户端
     *
     * @param name
     * @return
     */
    public static RedisClient getRedisClient(final String name) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(name));

        //use futuretask to reduce redundancy create big object
        FutureTask<RedisClient> future = clients.get(name);
        if (null != future) {
            return runFuture(future);
        } else {
            FutureTask<RedisClient> task = new FutureTask<>(new Callable<me.ellios.jedis.RedisClient>() {
                @Override
                public me.ellios.jedis.RedisClient call() throws Exception {
                    return createRedisClient(name);
                }
            });
            FutureTask<RedisClient> old = clients.putIfAbsent(name, task);
            if (old != null) {
                return runFuture(old);
            } else {
                return runFuture(task);
            }
        }
    }

    /**
     * 创建redis client
     *
     * @param name
     * @return
     */
    private static RedisClient createRedisClient(final String name) {

        ConfigFactory configFactory = ConfigFactoryBuilder.getConfigFactory(name);
        Config config = configFactory.getConfig();
        if (config == null) {
            LOG.error("redis : {} not config, can not build redis client.");
            return null;
        }

        final RedisClient redisClient = new RedisClient(config);
        configFactory.attachChangeListener(new ConfigListener() {
            @Override
            public void onChange(Config config) {
                try {
                    redisClient.refresh(config);
                } catch (Exception e) {
                    LOG.error("fail to create redis client : {}, error : {}", name, e.getMessage(), e);
                }
            }
        });

        return redisClient;
    }

    /**
     * First call {@link java.util.concurrent.FutureTask#run()}  then fetch the result.
     *
     * @param future the future
     * @return the zookeeper client.
     */
    private static <T> T runFuture(FutureTask<T> future) {
        if (!future.isDone()) {
            // one time thing
            future.run();
        }
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new IllegalStateException("fail to get future result.", e);
        }
    }
}
