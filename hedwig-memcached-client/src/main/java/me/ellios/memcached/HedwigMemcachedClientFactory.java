package me.ellios.memcached;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import me.ellios.memcached.config.Config;
import me.ellios.memcached.config.ConfigFactory;
import me.ellios.memcached.config.ConfigListener;
import me.ellios.memcached.config.support.ConfigFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

/**
 * User: ellios
 * Time: 15-6-28 : 下午11:33
 */
public class HedwigMemcachedClientFactory {

    private static final Logger LOG = LoggerFactory.getLogger(HedwigMemcachedClientFactory.class);

    private static ConcurrentMap<String, FutureTask<MemcachedOp>> clients = new ConcurrentHashMap<>();


    /**
     * 获取redis 客户端
     *
     * @param name
     * @return
     */
    public static MemcachedOp getMemcachedClient(final String name) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(name));

        //use futuretask to reduce redundancy create big object
        FutureTask<MemcachedOp> future = clients.get(name);
        if (null != future) {
            return runFuture(future);
        } else {
            FutureTask<MemcachedOp> task = new FutureTask<>(new Callable<MemcachedOp>() {
                @Override
                public MemcachedOp call() throws Exception {
                    return createMemcachedClient(name);
                }
            });
            FutureTask<MemcachedOp> old = clients.putIfAbsent(name, task);
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
    private static MemcachedOp createMemcachedClient(final String name) {

        ConfigFactory configFactory = ConfigFactoryBuilder.getConfigFactory(name);
        Config config = configFactory.getConfig();
        if (config == null) {
            LOG.error("memcached : {} not config, can not build redis client.");
            return null;
        }

        if(config.getNoop()){
            final NoOpMemcachedClient memcachedClient = new NoOpMemcachedClient(config);
            configFactory.attachChangeListener(new ConfigListener() {
                @Override
                public void onChange(Config config) {
                    try {
                        memcachedClient.refresh(config);
                    } catch (Exception e) {
                        LOG.error("fail to create memcached client : {}, error : {}", name, e.getMessage(), e);
                    }
                }
            });
            return memcachedClient;
        }else {
            final HedwigMemcachedClient memcachedClient = new HedwigMemcachedClient(config);
            configFactory.attachChangeListener(new ConfigListener() {
                @Override
                public void onChange(Config config) {
                    try {
                        memcachedClient.refresh(config);
                    } catch (Exception e) {
                        LOG.error("fail to create memcached client : {}, error : {}", name, e.getMessage(), e);
                    }
                }
            });
            return memcachedClient;
        }

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
