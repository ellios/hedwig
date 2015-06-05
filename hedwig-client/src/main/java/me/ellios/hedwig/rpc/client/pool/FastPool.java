package me.ellios.hedwig.rpc.client.pool;

import com.google.common.base.FinalizableReferenceQueue;
import com.google.common.base.FinalizableWeakReference;
import me.ellios.hedwig.common.exceptions.HedwigException;
import me.ellios.hedwig.rpc.proxy.pool.HedwigPoolConfig;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.PoolableObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.Reference;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Implements for fast pooling.
 *
 * @author gaofeng
 * @since: 13-11-20
 */
public class FastPool<T> implements ObjectPool<T> {
    private static final Logger LOG = LoggerFactory.getLogger(FastPool.class);
    private PoolableObjectFactory<T> factory;
    private final HedwigPoolConfig config;

    private static final int MAX_RETRIES = 3;
    private volatile boolean running = true;
    /**
     * Keep track of resources tied to thread.
     */
    final FinalizableReferenceQueue finalizableRefQueue = new FinalizableReferenceQueue();

    /**
     * Keep track of resources tied to thread.
     */
    final ConcurrentMap<T, Reference<Thread>> threadFinalizableRefs = new ConcurrentHashMap<>();

    // Take care of the possible of resource leak.
    private final ThreadLocal<T> threadLocal = new ThreadLocal<T>() {

        @Override
        protected T initialValue() {
            try {
                T obj = factory.makeObject();
                threadWatch(obj);
                return obj;
            } catch (Exception e) {
                throw new HedwigException(e);
            }
        }
    };

    public FastPool(HedwigPoolConfig config, PoolableObjectFactory<T> factory) {
        this.config = config;
        this.factory = factory;
    }

    @Override
    public T borrowObject() {
        T obj = threadLocal.get();
        int i = 0;
        while (config.isTestOnBorrow() && i++ < MAX_RETRIES) {
            if (!factory.validateObject(obj)) {
                threadLocal.remove();
                try {
                    factory.destroyObject(obj);
                } catch (Exception e) {
                    LOG.error("Cannot destroy invalid resource {} ", obj, e);
                }
                obj = threadLocal.get();
            } else {
                break;
            }
        }
        return obj;
    }

    @Override
    public void returnObject(T obj) {
        if (config.isTestOnReturn()) {
            if (!factory.validateObject(obj)) {
                threadLocal.remove();
            }
        }
    }

    /**
     * Keep track of this resource tied to which thread so that if the thread is terminated
     * we can reclaim our resource.
     *
     * @param r resource to track.
     */
    protected void threadWatch(final T r) {
        if (null != r) {
            threadFinalizableRefs.put(r, new FinalizableWeakReference<Thread>(Thread.currentThread(), finalizableRefQueue) {
                @Override
                public void finalizeReferent() {
                    try {
                        if (!running) {
                            LOG.debug("Monitored thread is dead, closing off allocated resource.");
                        }
                        invalidateObject(r);
                    } catch (Exception e) {
                        LOG.error("Cannot reclaim this resource {}", r, e);
                    }
                    threadFinalizableRefs.remove(r);
                }
            });
        }
    }

    @Override
    public void invalidateObject(T obj) throws Exception {
        threadLocal.remove();
        factory.destroyObject(obj);
    }

    @Override
    public void addObject() {
        borrowObject();
    }

    @Override
    public int getNumIdle() {
        throw new UnsupportedOperationException("fast pool is unsupported getNumIdle() operation");
    }

    @Override
    public int getNumActive() {
        throw new UnsupportedOperationException("fast pool is unsupported getNumActive() operation");
    }

    @Override
    public void clear() {
        for (T resource : threadFinalizableRefs.keySet()) {
            try {
                invalidateObject(resource);
            } catch (Exception e) {
                LOG.error("Cannot destroy resource {}", resource, e);
            }
        }
        threadFinalizableRefs.clear();
    }

    @Override
    public void close() {
        running = false;
        clear();
    }

    @Override
    public void setFactory(PoolableObjectFactory<T> factory) {
        this.factory = factory;
    }
}
