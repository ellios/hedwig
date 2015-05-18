package me.ellios.hedwig.common.instances;

/**
 * Author: ellios
 * Date: 12-11-14 Time: 上午10:19
 */
public interface ServiceImplContainer {

    public <T> T getServiceImpl(Class<T> clazz);
}
