package me.ellios.hedwig.common.instances;

import me.ellios.hedwig.common.exceptions.HedwigException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Author: ellios
 * Date: 12-11-14 Time: 上午10:22
 */
public class DefaultServiceImplContainer implements ServiceImplContainer {

    private static final Logger logger = LoggerFactory.getLogger(DefaultServiceImplContainer.class);
    private volatile ConcurrentMap<Class<?>, Object> classInstanceMap = new ConcurrentHashMap<>();

    @Override
    public <T> T getServiceImpl(Class<T> clazz) {
        T instance = (T) classInstanceMap.get(clazz);
        if (instance != null)
            return instance;
        if (logger.isInfoEnabled()) {
            logger.info("instance class : " + clazz.getName());
        }
        try {
            instance = clazz.newInstance();
            T old = (T) classInstanceMap.putIfAbsent(clazz, instance);
            if (null != old) {
                return old;
            }
        } catch (Exception e) {
            throw new HedwigException("fail to instance class : " + clazz.getName(), e);
        }
        return instance;
    }
}
