package me.ellios.hedwig.registry.zookeeper;

import me.ellios.hedwig.registry.Registry;
import me.ellios.hedwig.registry.RegistryFactory;
import me.ellios.hedwig.zookeeper.ZooKey;
import me.ellios.hedwig.zookeeper.config.ZookeeperConfig;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


public class ZookeeperRegistryFactory implements RegistryFactory {

    private static final ZookeeperRegistryFactory instance = new ZookeeperRegistryFactory();
    private static final ConcurrentMap<ZooKey, Registry> CONCURRENT_MAP = new ConcurrentHashMap<>();

    private ZookeeperRegistryFactory() {
    }

    public static RegistryFactory getInstance() {
        return instance;
    }

    @Override
    public Registry getRegistry() {
        return getRegistry(ZookeeperConfig.getTypeString(), ZookeeperConfig.getNamespaceString());
    }

    @Override
    public Registry getRegistry(String type, String group) {
        String typeInLowercase = type.toLowerCase();
        String groupInLowercase = group.toLowerCase();
        ZooKey key = new ZooKey(typeInLowercase, groupInLowercase);
        Registry registry = CONCURRENT_MAP.get(key);
        if (registry == null) {
            registry = createRegistry(typeInLowercase, groupInLowercase);
            if (null != registry) {
                Registry old = CONCURRENT_MAP.putIfAbsent(key, registry);
                if (null != old) {
                    registry = old;
                }
            }
        }
        return registry;
    }

    @Override
    public Registry getRegistry(String group) {
        return getRegistry(ZookeeperConfig.getTypeString(), group);
    }

    private Registry createRegistry(String type, String group) {
        return new ZookeeperRegistry(type, group);
    }
}