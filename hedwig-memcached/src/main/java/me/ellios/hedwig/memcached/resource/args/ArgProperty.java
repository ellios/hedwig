package me.ellios.hedwig.memcached.resource.args;

/**
 * arg property.
 *
 * @author gaofeng@qiyi.com
 * @since: 14-3-18
 */
public class ArgProperty {

    private String name;

    private ArgType type;

    private String defaultValue;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArgType getType() {
        return type;
    }

    public void setType(ArgType type) {
        this.type = type;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Override
    public String toString() {
        return "ArgProperty{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", defaultValue='" + defaultValue + '\'' +
                '}';
    }
}
