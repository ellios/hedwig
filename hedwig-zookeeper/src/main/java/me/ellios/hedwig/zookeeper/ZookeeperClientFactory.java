package me.ellios.hedwig.zookeeper;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import me.ellios.hedwig.zookeeper.config.ZookeeperConfig;
import me.ellios.hedwig.zookeeper.curator.CuratorZookeeperClient;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.utils.ZKPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * Author: ellios
 * Date: 12-10-29 Time: 下午7:09
 */
public class ZookeeperClientFactory {
    private static final Logger LOG = LoggerFactory.getLogger(ZookeeperClientFactory.class);

    /**
     * The full connection string consists of server address and a {@code chrootPath}. And we
     * define the {@code chrootPath} by a specific prefix and a type string.
     * e.g. server address=0.0.0.0:2148,0.0.0.0:1236, prefix=/hedwig/dev, type=service, the
     * resulting connection string is 0.0.0.0:2148,0.0.0.0:1236/hedwig/dev/service
     *
     * @return the full connection string
     */
    private static String createFullConnectionString(String type) {
        String rootPath = ZookeeperConfig.getRootPath();
        return ZookeeperConfig.getConnectString() + ZKPaths.makePath(rootPath, type);
    }

    private static ZookeeperClient buildClient(String type) {
        String namespace = ZookeeperConfig.getNamespaceString();
        return buildClient(type, namespace);
    }

    private static ZookeeperClient buildClient(String type, String namespace) {
        String connectString = createFullConnectionString(type);

        if (!(ZooType.SERVICE.getType().equalsIgnoreCase(type) || ZooType.REDIS.getType().equalsIgnoreCase(type)
                || ZooType.MEMCACHED.getType().equalsIgnoreCase(type))) {
            // clear out namespace if it's not of service/redis type.
            namespace = "";
        }
        LOG.info("[HEDWIG-REGISTRY] connectionString: {}, group: {}", connectString, namespace);
        return new CuratorZookeeperClient(connectString, namespace);
    }

    /**
     * We maintain an singleton instance per type.
     */
    private static final ConcurrentHashMap<ZooKey, FutureTask<ZookeeperClient>>  CONCURRENT_HASH_MAP
            = new ConcurrentHashMap<>();

    /**
     * Create a {@link ZookeeperClient}.
     *
     * @param type  the {@link ZooType}
     * @param group the group
     * @return the zookeeper client instance
     */
    public static ZookeeperClient getZookeeperClient(final String type, final String group) {
        Preconditions.checkArgument(StringUtils.isNotBlank(type), "type parameter cannot be null or empty.");
        final String typeInLowercase = type.toLowerCase();
        final String groupInLowercase = Strings.nullToEmpty(group).toLowerCase();
        ZooKey key = new ZooKey(typeInLowercase, groupInLowercase);
        FutureTask<ZookeeperClient> future = CONCURRENT_HASH_MAP.get(key);
        if (null != future) {
            return runFuture(future, key);
        } else {
            FutureTask<ZookeeperClient> task = new FutureTask<>(new Callable<ZookeeperClient>() {
                @Override
                public ZookeeperClient call() throws Exception {
                    return buildClient(typeInLowercase, groupInLowercase);
                }
            });
            FutureTask<ZookeeperClient> old = CONCURRENT_HASH_MAP.putIfAbsent(key, task);
            if (old != null) {
                return runFuture(old, key);
            } else {
                return runFuture(task, key);
            }
        }
    }

    /**
     * Get specific {@link ZookeeperClient}.
     *
     * @param type specified type {@link ZooType}
     * @return the zookeeper client instance
     */
    public static ZookeeperClient getZookeeperClient(final String type) {
        String namespace = ZookeeperConfig.getNamespaceString();
        return getZookeeperClient(type, namespace);
    }

    /**
     * First call {@link java.util.concurrent.FutureTask#run()}  then fetch the result.
     *
     * @param future the future
     * @param zooKey service type and namespace.
     * @return the zookeeper client.
     */
    private static ZookeeperClient runFuture(FutureTask<ZookeeperClient> future, ZooKey zooKey) {
        if (!future.isDone()) {
            // one time thing
            future.run();
        }
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new IllegalStateException("Create ZookeeperClient of type " + zooKey, e);
        }
    }

    /**
     * Get the default {@link ZookeeperClient} whose type is specified by
     * {@link me.ellios.hedwig.zookeeper.config.ZookeeperConfig#getTypeString()}
     *
     * @return the default zookeeper client.
     */
    public static ZookeeperClient getZookeeperClient() {
        return getZookeeperClient(ZookeeperConfig.getTypeString());
    }

    /**
     * Get {@link ZookeeperClient} by {@link ZooType}.
     *
     * @param type supported {@link ZooType}
     * @return teh zookeeper client.
     */
    public static ZookeeperClient getZookeeperClient(ZooType type) {
        return getZookeeperClient(type.getType());
    }

    /**
     * only usered on test case.
     */
    protected static void clear() {
        CONCURRENT_HASH_MAP.clear();
    }
}
