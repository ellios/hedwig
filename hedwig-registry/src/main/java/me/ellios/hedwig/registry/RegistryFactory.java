package me.ellios.hedwig.registry;


public interface RegistryFactory {

    Registry getRegistry();

    Registry getRegistry(String type, String group);

    Registry getRegistry(String group);

}