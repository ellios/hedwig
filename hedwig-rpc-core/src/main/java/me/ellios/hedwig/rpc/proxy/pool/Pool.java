package me.ellios.hedwig.rpc.proxy.pool;

import me.ellios.hedwig.common.exceptions.HedwigException;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;

public abstract class Pool<T> {

    private final ObjectPool<T> internalPool;

    protected Pool(ObjectPool<T> concretePool) {
        this.internalPool = concretePool;
    }

    protected Pool(final GenericObjectPool.Config poolConfig, PoolableObjectFactory<T> factory) {
        this(new GenericObjectPool<T>(factory, poolConfig));
    }

    public T getResource() {
        try {
            return internalPool.borrowObject();
        } catch (Exception e) {
            throw new HedwigException("Could not get a resource from the pool", e);
        }
    }

    public void returnResourceObject(final T resource) {
        try {
            internalPool.returnObject(resource);
        } catch (Exception e) {
            throw new HedwigException("Could not return the resource to the pool " + resource, e);
        }
    }

    public void returnBrokenResource(final T resource) {
        returnBrokenResourceObject(resource);
    }

    public void returnResource(final T resource) {
        returnResourceObject(resource);
    }

    protected void returnBrokenResourceObject(final T resource) {
        try {
            internalPool.invalidateObject(resource);
        } catch (Exception e) {
            throw new HedwigException("Could not return the broken resource to the pool " + resource, e);
        }
    }

    public void destroy() {
        try {
            internalPool.close();
        } catch (Exception e) {
            throw new HedwigException("Could not destroy the pool", e);
        }
    }
}