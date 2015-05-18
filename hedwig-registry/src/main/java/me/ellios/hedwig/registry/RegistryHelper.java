package me.ellios.hedwig.registry;

import me.ellios.hedwig.rpc.core.ServiceNode;
import me.ellios.hedwig.registry.zookeeper.ZookeeperRegistryFactory;

import java.util.List;

/**
 * Author: ellios
 * Date: 12-11-3 Time: 下午2:57
 */
public class RegistryHelper {

    private static Registry registry = ZookeeperRegistryFactory.getInstance().getRegistry();

    public static void register(ServiceNode serviceNode){
        registry.register(serviceNode);
    }

    public static void unregister(ServiceNode serviceNode){
        registry.unregister(serviceNode);
    }

    public static void subscribe(String serviceName, CallbackWatcher listener){
        registry.subscribe(serviceName, listener);
    }

    public static void unsubscribe(String serviceName, CallbackWatcher listener){
        registry.unsubscribe(serviceName, listener);
    }

    public static List<ServiceNode> lookup(String serviceName){
        return registry.lookup(serviceName);
    }

    public static void destroy(){
        registry.destroy();
    }
}
