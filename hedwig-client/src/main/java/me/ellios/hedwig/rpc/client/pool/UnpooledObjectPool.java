package me.ellios.hedwig.rpc.client.pool;

import org.apache.commons.pool.BaseObjectPool;
import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Say something?
 *
 * @author George
 * @since: 13-11-21
 */
public class UnpooledObjectPool<T> extends BaseObjectPool<T> {
    private static final Logger LOG = LoggerFactory.getLogger(UnpooledObjectPool.class);
    private PoolableObjectFactory<T> factory;
    private GenericObjectPool.Config config;

    public UnpooledObjectPool(GenericObjectPool.Config poolConfig, PoolableObjectFactory<T> factory) {
        this.factory = factory;
        this.config = poolConfig;
    }


    @Override
    public T borrowObject() throws Exception {
        return factory.makeObject();
    }

    @Override
    public void returnObject(T obj) throws Exception {
        factory.destroyObject(obj);
    }

    @Override
    public void invalidateObject(T obj) throws Exception {
        factory.destroyObject(obj);
    }
}
