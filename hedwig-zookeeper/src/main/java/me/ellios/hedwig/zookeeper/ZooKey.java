package me.ellios.hedwig.zookeeper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

/**
 * A zookeeper client key.
 *
 * @author George Cao
 * @since 2013-12-26 16
 */
public class ZooKey implements Serializable {
    private static final Logger LOG = LoggerFactory.getLogger(ZooKey.class);
    private String type;
    private String group;

    public ZooKey(String type, String group) {
        this.type = type;
        this.group = group;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ZooKey zooKey = (ZooKey) o;

        if (group != null ? !group.equals(zooKey.group) : zooKey.group != null) return false;
        if (type != null ? !type.equals(zooKey.type) : zooKey.type != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (group != null ? group.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ZooKey{" +
                "type='" + type + '\'' +
                ", group='" + group + '\'' +
                '}';
    }
}
