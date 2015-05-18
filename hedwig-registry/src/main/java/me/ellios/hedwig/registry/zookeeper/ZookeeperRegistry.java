package me.ellios.hedwig.registry.zookeeper;

import com.google.common.base.Preconditions;
import me.ellios.hedwig.rpc.core.ServiceNode;
import me.ellios.hedwig.common.exceptions.HedwigException;
import me.ellios.hedwig.registry.CallbackWatcher;
import me.ellios.hedwig.registry.support.AbstractRegistry;
import me.ellios.hedwig.registry.utils.ZPathUtils;
import me.ellios.hedwig.zookeeper.ChildListener;
import me.ellios.hedwig.zookeeper.StateListener;
import me.ellios.hedwig.zookeeper.ZookeeperClient;
import me.ellios.hedwig.zookeeper.ZookeeperClientFactory;
import me.ellios.hedwig.zookeeper.config.ZookeeperConfig;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * ZookeeperRegistry
 * Author: ellios
 * Date: 12-10-29 Time: 下午2:10
 */
public class ZookeeperRegistry extends AbstractRegistry {
    private final static Logger LOG = LoggerFactory.getLogger(ZookeeperRegistry.class);
    private final ConcurrentMap<String, ConcurrentMap<CallbackWatcher, ChildListener>> zkListeners =
            new ConcurrentHashMap<>();
    private final ZookeeperClient zkClient;

    public ZookeeperRegistry(String type, String group) {
        zkClient = ZookeeperClientFactory.getZookeeperClient(type, group);
        zkClient.addStateListener(new StateListener() {
            public void stateChanged(int state) {
                if (state == RECONNECTED) {
                    try {
                        recover();
                    } catch (Exception e) {
                        LOG.error("Got Reconnected event, failed to recover", e);
                    }
                }
            }
        });
    }

    public ZookeeperRegistry() {
        this(ZookeeperConfig.getTypeString(), ZookeeperConfig.getNamespaceString());
    }

    public boolean isAvailable() {
        return zkClient.isConnected();
    }

    @Override
    public void destroy() {
        super.destroy();
        try {
            zkClient.close();
        } catch (Exception e) {
            LOG.warn("Failed to close zookeeper client, cause: ", e);
        }
    }

    protected void doRegister(ServiceNode serviceNode) {
        try {
            //删除来不及过期的节点
            if (zkClient.exist(serviceNode.getZnodePath())) {
                zkClient.delete(serviceNode.getZnodePath());
            }
            zkClient.create(serviceNode.getZnodePath(), true, serviceNode.toJson());
        } catch (Throwable e) {
            throw new HedwigException("Failed to register " + serviceNode + " to zookeeper, cause: " + e.getMessage(), e);
        }
    }

    protected void doUnregister(ServiceNode serviceNode) {
        try {
            zkClient.delete(serviceNode.getZnodePath());
        } catch (Throwable e) {
            throw new HedwigException("Failed to unregister " + serviceNode + " to zookeeper, cause: " + e.getMessage(), e);
        }
    }

    protected void doSubscribe(final String serviceName, final CallbackWatcher watcher) {
        try {
            final String servicePath = ZPathUtils.buildServicePath(serviceName);
            ConcurrentMap<CallbackWatcher, ChildListener> listenerMap = zkListeners.get(servicePath);
            if (listenerMap == null) {
                zkListeners.putIfAbsent(servicePath, new ConcurrentHashMap<CallbackWatcher, ChildListener>());
                listenerMap = zkListeners.get(servicePath);
            }
            ChildListener zkListener = listenerMap.get(watcher);
            if (zkListener == null) {
                listenerMap.putIfAbsent(watcher, new ChildListener() {
                    @Override
                    public void childChanged(String path, List<String> children) {
                        LOG.info("Client path {} has children {}", path, children);
                        if (servicePath.equals(path)) {
                            watcher.doCallback(getServiceNodesByPath(serviceName, children));
                        }
                    }
                });
                zkListener = listenerMap.get(watcher);
            }
            List<String> children = zkClient.addChildListener(servicePath, zkListener);
            watcher.doCallback(getServiceNodesByPath(serviceName, children));
        } catch (Throwable e) {
            throw new HedwigException("Failed to subscribe " + serviceName + " to zookeeper , cause: " + e.getMessage(), e);
        }
    }

    protected void doUnsubscribe(final String serviceName, CallbackWatcher watcher) {
        try {
            final String servicePath = ZPathUtils.buildServicePath(serviceName);
            ConcurrentMap<CallbackWatcher, ChildListener> listeners = zkListeners.get(servicePath);
            if (listeners != null) {
                // We need remove the watcher here
                ChildListener zkListener = listeners.remove(watcher);
                if (zkListener != null) {
                    zkClient.removeChildListener(servicePath, zkListener);
                }
            }
        } catch (Exception e) {
            throw new HedwigException("Failed to unsubscribe " + serviceName + " to zookeeper , cause: " + e.getMessage(), e);
        }
    }

    public List<ServiceNode> doLookup(String serviceName) {
        try {
            if (StringUtils.isEmpty(serviceName)) {
                LOG.error("serviceName is null;");
                return null;
            }
            final String servicePath = ZPathUtils.buildServicePath(serviceName);
            List<String> children = zkClient.getChildren(servicePath);
            return getServiceNodesByPath(serviceName, children);
        } catch (Exception e) {
            throw new HedwigException("Failed to lookup service " + serviceName + ", cause: ", e);
        }
    }

    public List<ServiceNode> getServiceNodesByPath(String serviceName, List<String> nodes) {
        Preconditions.checkNotNull(nodes, "pathList is null");
        List<ServiceNode> serviceNodeList = new ArrayList<>();
        for (String nodeName : nodes) {
            byte[] data = zkClient.getData(ZPathUtils.buildZNodeFullPath(serviceName, nodeName));
            ServiceNode serviceNode = ServiceNode.fromJson(data);
            serviceNodeList.add(serviceNode);
        }
        return serviceNodeList;
    }

}