package me.ellios.hedwig.registry.support;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import me.ellios.hedwig.rpc.core.ServiceNode;
import me.ellios.hedwig.registry.CallbackWatcher;
import me.ellios.hedwig.registry.Registry;
import me.ellios.hedwig.common.utils.ConcurrentHashSet;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;

import static me.ellios.hedwig.common.Constants.REGISTRY_RETRY_INTERVAL;


public abstract class AbstractRegistry implements Registry {

    private static Logger LOG = LoggerFactory.getLogger(AbstractRegistry.class);
    private final Set<ServiceNode> registered = new ConcurrentHashSet<>();
    private final ConcurrentMap<String, Set<CallbackWatcher>> subscribed = new ConcurrentHashMap<>();

    // 定时任务执行器
    private final ScheduledExecutorService retryExecutor = Executors.newScheduledThreadPool(1, new ThreadFactoryBuilder().
            setNameFormat("hedwig-registry-failed-retry" + "-%d").setDaemon(true).build());

    // 失败重试定时器，定时检查是否有请求失败，如有，无限次重试
    private final ScheduledFuture<?> retryFuture;


    public AbstractRegistry() {
        this.retryFuture = retryExecutor.scheduleWithFixedDelay(new Runnable() {
            public void run() {
                // 检测并连接注册中心
                try {
                    retry();
                } catch (Throwable t) { // 防御性容错
                    LOG.error("Unexpected error occur at failed retry, cause: ", t);
                }
            }
        }, REGISTRY_RETRY_INTERVAL, REGISTRY_RETRY_INTERVAL, TimeUnit.MILLISECONDS);
    }

    @Override
    public void register(ServiceNode serviceNode) {
        Preconditions.checkNotNull(serviceNode, "serviceNode is Null");
        if (LOG.isInfoEnabled()) {
            LOG.info("Register service : " + serviceNode.getName() + " node : " + serviceNode);
        }
        registered.add(serviceNode);
        try {
            // 向服务器端发送注册请求
            doRegister(serviceNode);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to register " + serviceNode + " to registry, cause: ", e);
        }
    }

    @Override
    public void unregister(ServiceNode serviceNode) {
        Preconditions.checkNotNull(serviceNode, "serviceNode is Null");
        if (LOG.isInfoEnabled()) {
            LOG.info("Unregister service : {}, node: {}", serviceNode.getName(), serviceNode);
        }
        registered.remove(serviceNode);
        try {
            // 向服务器端发送取消注册请求
            doUnregister(serviceNode);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to unregister " + serviceNode + " to registry, cause: ", e);
        }
    }

    @Override
    public void subscribe(String serviceName, CallbackWatcher callback) {
        Preconditions.checkArgument(StringUtils.isNotEmpty(serviceName), "serviceName is empty.");
        Preconditions.checkNotNull(callback, "callbak is null.");
        if (LOG.isInfoEnabled()) {
            LOG.info("Subscribe service : " + serviceName);
        }
        Set<CallbackWatcher> watchers = subscribed.get(serviceName);
        if (watchers == null) {
            subscribed.putIfAbsent(serviceName, new ConcurrentHashSet<CallbackWatcher>());
            watchers = subscribed.get(serviceName);
        }
        watchers.add(callback);
        try {
            // 向服务器端发送订阅请求
            doSubscribe(serviceName, callback);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to subscribe " + serviceName + ", cause: ", e);
        }
    }

    @Override
    public void unsubscribe(String serviceName, CallbackWatcher callback) {
        Preconditions.checkArgument(StringUtils.isNotEmpty(serviceName), "serviceName is empty.");
        Preconditions.checkNotNull(callback, "callbak is null.");
        if (LOG.isInfoEnabled()) {
            LOG.info("Unsubscribe service : " + serviceName);
        }
        Set<CallbackWatcher> watchers = subscribed.get(serviceName);
        if (watchers != null) {
            watchers.remove(callback);
        }
        try {
            // 向服务器端发送取消订阅请求
            doUnsubscribe(serviceName, callback);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to unsubscribe " + serviceName + " to registry, cause: ", e);
        }
    }

    @Override
    public List<ServiceNode> lookup(String serviceName) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(serviceName), "lookup serviceName is empty");
        try {
            return doLookup(serviceName);
        } catch (Throwable e) {
            LOG.error("Failed to lookup " + serviceName + " from zookeeper, cause: " + e.getMessage(), e);
            return null;
        }
    }

    @Override
    public void destroy() {
        LOG.info("Destroy registry");
        //unregister
        Set<ServiceNode> recoverRegistered = new HashSet<>(registered);
        if (!recoverRegistered.isEmpty()) {
            for (ServiceNode node : recoverRegistered) {
                try {
                    unregister(node);
                    LOG.info("Destroy register serviceNode {}", node);
                } catch (Throwable t) {
                    LOG.warn("Failed to unregister service :{} node :{} to registry on destroy, cause: ",
                            node.getName(), node, t);
                }
            }
        }
        // unsubscribe
        Map<String, Set<CallbackWatcher>> recoverSubscribed = new HashMap<>(subscribed);
        if (!recoverSubscribed.isEmpty()) {
            for (Map.Entry<String, Set<CallbackWatcher>> entry : recoverSubscribed.entrySet()) {
                String name = entry.getKey();
                for (CallbackWatcher watcher : entry.getValue()) {
                    try {
                        unsubscribe(name, watcher);
                        LOG.info("Destroy subscribe service :{}", name);
                    } catch (Throwable t) {
                        LOG.warn("Failed to unsubscribe service {} to registry on destroy, cause: ", name, t);
                    }
                }
            }
        }
        try {
            retryFuture.cancel(true);
            retryExecutor.shutdownNow();
        } catch (Throwable t) {
            LOG.warn("Cancel retry future error", t);
        }
    }

    // 重试失败的动作
    protected void retry() {
        // TODO fix this
    }

    protected void recover() throws Exception {
        Set<ServiceNode> recoverRegistered = new HashSet<>(registered);
        if (!recoverRegistered.isEmpty()) {
            for (ServiceNode node : recoverRegistered) {
                try {
                    if (LOG.isInfoEnabled()) {
                        LOG.info("Recover register node " + node);
                    }
                    register(node);
                } catch (Throwable t) {
                    LOG.error("Failed to register service : " + node.getName() + " node : " + node + " to registry on recover, cause: " + t.getMessage(), t);
                }
            }
        }
        // subscribe
        Map<String, Set<CallbackWatcher>> recoverSubscribed = new HashMap<>(subscribed);
        if (!recoverSubscribed.isEmpty()) {

            for (Map.Entry<String, Set<CallbackWatcher>> entry : recoverSubscribed.entrySet()) {
                String name = entry.getKey();
                for (CallbackWatcher watcher : entry.getValue()) {
                    try {
                        if (LOG.isInfoEnabled()) {
                            LOG.info("Recover subscribe service :  " + name);
                        }
                        subscribe(name, watcher);
                    } catch (Throwable t) {
                        LOG.error("Failed to subscribe service : " + name + " to registry on recover, cause: " + t.getMessage(), t);
                    }
                }
            }
        }
    }

    // ==== 模板方法 ====

    protected abstract void doRegister(ServiceNode serviceNode);

    protected abstract void doUnregister(ServiceNode serviceNode);

    protected abstract void doSubscribe(String serviceName, CallbackWatcher listener);

    protected abstract void doUnsubscribe(String serviceName, CallbackWatcher listener);

    protected abstract List<ServiceNode> doLookup(String serviceName);

}