package me.ellios.hedwig.zookeeper;

/**
 * Say something?
 *
 * @author George Cao
 * @since 3/26/13 8:15 PM
 */
public enum ZooType {
    REDIS("redis"),
    JMS("jms"),
    RECIPES("recipes"),
    SERVICE("service"),
    CONFIG("config");

    private String type;

    private ZooType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
