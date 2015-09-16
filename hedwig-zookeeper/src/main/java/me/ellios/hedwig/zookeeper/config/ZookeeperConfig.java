package me.ellios.hedwig.zookeeper.config;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import me.ellios.hedwig.common.config.HedwigConfig;
import me.ellios.hedwig.common.config.HedwigEnv;
import org.apache.commons.lang3.StringUtils;

/**
 * Author: ellios
 * Date: 12-12-19 Time: 上午10:23
 */
public class ZookeeperConfig {

    private static final HedwigConfig hc = HedwigConfig.getInstance();

    public static final String ROOT_PATH_PREFIX = "/hedwig";
    public static final String DEFAULT_NAMESPACE = "";
    public static final String DEFAULT_TYPE = "service";

    /**
     * 获取hedwig根路径
     *
     * @return
     */
    public static String getRootPath() {
        String rootPath = hc.getString("hedwig.zk.rootpath", null);
        if (rootPath != null) {
            return rootPath;
        }
        String envAbbr = hc.getString("hedwig.env", "dev");
        HedwigEnv env = HedwigEnv.getEnvByAbbreviation(envAbbr);
        return ROOT_PATH_PREFIX + "/" + env.getAbbreviation();
    }

    /**
     * 获取连接信息
     *
     * @return
     */
    public static String getConnectString() {
        return hc.getString("hedwig.zk.ips",//
                "10.154.28.253:2181,10.154.28.254:2181,10.154.28.255:2181,10.176.30.42:2181,10.135.30.47:2181");
    }

    private static String type = DEFAULT_TYPE;

    /**
     * Gets the type.
     *
     * @return service type.
     */
    public static String getTypeString() {
        if (DEFAULT_TYPE.equals(type)) {
            // Fetch from config file.
            return hc.getString("hedwig.zk.type", type);
        } else {
            return type;
        }
    }

    /**
     * Give the user a chance to override the type value from config file before starting the zookeeper.
     *
     * @param type the expected type.
     */
    public static void setTypeString(String type) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(type), "type parameter cannot be empty or null.");
        ZookeeperConfig.type = type;
    }

    /**
     * Gets the namespace.
     * Read property by key {@code hedwig.zk.group}. Please note that
     * this implementation only support one namespace in the same process.
     *
     * @return {@link #DEFAULT_NAMESPACE} or a not-empty string.
     */
    public static String getNamespaceString() {
        if (DEFAULT_NAMESPACE.equals(namespace)) {
            // Read value from config file.
            return hc.getString("hedwig.zk.group", DEFAULT_NAMESPACE);
        } else {
            return namespace;
        }
    }

    public static String getServiceGroup(String serviceName) {
        // Read value from config file.
        return hc.getString("hedwig.zk.group." + serviceName, getNamespaceString());
    }

    /**
     * Give the user a chance to override the namespace value from config file before starting the zookeeper.
     *
     * @param group the expected group.
     */
    public static void setNamespaceString(String group) {
        Preconditions.checkArgument(StringUtils.isNotBlank(group),
                "namespace parameter cannot be set to blank or null.");
        ZookeeperConfig.namespace = group;
    }

    private static String namespace = DEFAULT_NAMESPACE;

    /**
     * Gets the acl user string
     *
     * @return
     */
    public static String getAclUserString() {
        return hc.getString("hedwig.zk.acl.user", "");
    }

    /**
     * Gets the acl password string
     *
     * @return
     */
    public static String getAclPasswordString() {
        return hc.getString("hedwig.zk.acl.password", "");
    }
}
