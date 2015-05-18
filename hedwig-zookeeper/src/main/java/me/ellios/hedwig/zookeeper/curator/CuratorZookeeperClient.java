package me.ellios.hedwig.zookeeper.curator;

import com.google.common.base.Strings;
import me.ellios.hedwig.common.exceptions.HedwigException;
import me.ellios.hedwig.zookeeper.ChildListener;
import me.ellios.hedwig.zookeeper.DataListener;
import me.ellios.hedwig.zookeeper.StateListener;
import me.ellios.hedwig.zookeeper.config.ZookeeperConfig;
import me.ellios.hedwig.zookeeper.support.AbstractZookeeperClient;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.CuratorFrameworkFactory.Builder;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException.NoNodeException;
import org.apache.zookeeper.KeeperException.NodeExistsException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class CuratorZookeeperClient extends AbstractZookeeperClient<CuratorWatcher> {
    private static final Logger LOG = LoggerFactory.getLogger(CuratorZookeeperClient.class);
    private final CuratorFramework client;

    /**
     * We need the {@code namespace} parameter to implement group service.
     *
     * @param connectString the connection string
     * @param namespace     namespace specified, can be null or empty.
     *                      If the namespace is null or empty, just ignore it.
     */
    public CuratorZookeeperClient(String connectString, @Nullable String namespace) {
        super(connectString);
        final CountDownLatch latch = new CountDownLatch(1);
        try {
            Builder builder = CuratorFrameworkFactory.builder()
                    .connectString(connectString)
                    .retryPolicy(new ExponentialBackoffRetry(500, Integer.MAX_VALUE, 3000))
                    .sessionTimeoutMs(20000)       // Session timeouts after 20 seconds.
                    .connectionTimeoutMs(5000);    // Max connection timeout set to 5 seconds.
            if (!Strings.isNullOrEmpty(namespace)) {
                builder.namespace(namespace);
            }
            client = builder.build();
            client.getConnectionStateListenable().addListener(new ConnectionStateListener() {
                public void stateChanged(CuratorFramework client, ConnectionState state) {
                    try {
                        if (state == ConnectionState.LOST) {
                            CuratorZookeeperClient.this.stateChanged(StateListener.DISCONNECTED);
                        } else if (state == ConnectionState.CONNECTED) {
                            CuratorZookeeperClient.this.stateChanged(StateListener.CONNECTED);
                        } else if (state == ConnectionState.RECONNECTED) {
                            CuratorZookeeperClient.this.stateChanged(StateListener.RECONNECTED);
                        }
                    } finally {
                        latch.countDown();
                    }
                }
            });
            client.start();
            long start = System.nanoTime();
            latch.await();
            LOG.info("Create connection to {} takes {}(ms).", connectString,
                    TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start));
            // support zookeeper acl
            addAuth(this);
        } catch (Exception e) {
            throw new IllegalStateException("Create CuratorZookeeperClient with connection string " + connectString, e);
        }
    }

    private void addAuth(CuratorZookeeperClient client) {
        String user = ZookeeperConfig.getAclUserString();
        String password = ZookeeperConfig.getAclPasswordString();
        if (StringUtils.isEmpty(user) || StringUtils.isEmpty(password)) {
            return;
        }
        String aclId = user + ":" + password;
        try {
            ZooKeeper zooKeeper = client.getCuratorClient().getZookeeperClient().getZooKeeper();
            zooKeeper.addAuthInfo("digest", aclId.getBytes());
            //add self to client
            //client.create("client/" + InetAddress.getLocalHost().getHostAddress(), true);
        } catch (Exception e) {
            LOG.error("[HEDWIG-REGISTRY] authentication is error, user:{}, password:{}", user, password);
            throw new HedwigException(e);
        }
        LOG.info("[HEDWIG-REGISTRY] authentication, user:{}, password:{}", user, password);
    }

    /**
     * 获取curator的client
     *
     * @return
     */
    public CuratorFramework getCuratorClient() {
        return client;
    }

    public void createPersistent(String path, byte[] data) {
        try {
            client.create().forPath(path, data);
        } catch (NodeExistsException ignored) {
        } catch (Exception e) {
            throw new IllegalStateException("Create " + path + " with data", e);
        }
    }

    public void createPersistent(String path) {
        try {
            client.create().creatingParentsIfNeeded().forPath(path);
        } catch (NodeExistsException ignored) {
            // ignore this exception
        } catch (Exception e) {
            throw new IllegalStateException("Create " + path, e);
        }
    }

    public void createEphemeral(String path, byte[] data) {
        try {
            client.create().withMode(CreateMode.EPHEMERAL).forPath(path, data);
        } catch (NodeExistsException ignored) {
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public void createEphemeral(String path) {
        try {
            client.create().withMode(CreateMode.EPHEMERAL).forPath(path);
        } catch (NodeExistsException ignored) {
        } catch (Exception e) {
            throw new IllegalStateException("Create " + path, e);
        }
    }

    public void delete(String path) {
        try {
            client.delete().forPath(path);
        } catch (NoNodeException ignored) {
        } catch (Exception e) {
            throw new IllegalStateException("Delete " + path, e);
        }
    }

    public byte[] getData(String path) {
        try {
            return client.getData().forPath(path);
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public void setData(String path, byte[] data) {
        try {
            client.setData().forPath(path, data);
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public void withACL(String path, List<ACL> aclList) {
        try {
            client.setACL().withACL(aclList).forPath(path);
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public List<String> getChildren(String path) {
        try {
            return client.getChildren().forPath(path);
        } catch (NoNodeException e) {
            return null;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public boolean isConnected() {
        return client.getZookeeperClient().isConnected();
    }

    public void doClose() {
        client.close();
    }


    public void sync(String path) {
        client.sync(path, null);
    }

    public boolean exist(String path) {
        try {
            Stat stat = client.checkExists().forPath(path);
            return stat != null;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public CuratorWatcher createTargetChildListener(String path, ChildListener listener) {
        return new CuratorChildWatcherImpl(listener);
    }

    public List<String> addTargetChildListener(String path, CuratorWatcher listener) {
        try {
            return client.getChildren().usingWatcher(listener).forPath(path);
        } catch (NoNodeException e) {
            return null;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public void removeTargetChildListener(String path, CuratorWatcher listener) {
        ((CuratorChildWatcherImpl) listener).unwatch();
    }

    public CuratorWatcher createTargetDataListener(String path, DataListener listener) {
        return new CuratorDataWatcherImpl(listener);
    }

    public byte[] addTargetDataListener(String path, CuratorWatcher listener) {
        try {
            return client.getData().usingWatcher(listener).forPath(path);
        } catch (NoNodeException e) {
            return null;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public void removeTargetDataListener(String path, CuratorWatcher listener) {
        ((CuratorDataWatcherImpl) listener).unwatch();
    }

    /**
     * Curator子节点监听
     */
    private class CuratorChildWatcherImpl implements CuratorWatcher {

        private volatile ChildListener listener;

        public CuratorChildWatcherImpl(ChildListener listener) {
            this.listener = listener;
        }

        public void unwatch() {
            this.listener = null;
        }

        @Override
        public void process(WatchedEvent event) throws Exception {
            if (listener != null) {
                LOG.info("Got children Event: {}", event);
                listener.childChanged(event.getPath(), client.getChildren().usingWatcher(this).forPath(event.getPath()));
            }
        }
    }

    /**
     * Curator数据监听
     */
    private class CuratorDataWatcherImpl implements CuratorWatcher {

        private volatile DataListener listener;

        public CuratorDataWatcherImpl(DataListener listener) {
            this.listener = listener;
        }

        public void unwatch() {
            this.listener = null;
        }

        @Override
        public void process(WatchedEvent event) throws Exception {
            if (listener != null) {
                LOG.info("Got data Event: {}", event);
                listener.dataChanged(event.getPath(), client.getData().usingWatcher(this).forPath(event.getPath()));
            }
        }
    }

}
