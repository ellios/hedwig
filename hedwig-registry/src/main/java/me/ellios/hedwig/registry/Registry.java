package me.ellios.hedwig.registry;

import me.ellios.hedwig.rpc.core.ServiceNode;

import java.util.List;

/**
 * Author: ellios
 * Date: 12-10-29 Time: 下午2:10
 */
public interface Registry {

    void register(ServiceNode serviceNode);


    void unregister(ServiceNode serviceNode);


    void subscribe(String serviceName, CallbackWatcher listener);


    void unsubscribe(String serviceName, CallbackWatcher listener);

    List<ServiceNode> lookup(String serviceName);

    void destroy();
}
