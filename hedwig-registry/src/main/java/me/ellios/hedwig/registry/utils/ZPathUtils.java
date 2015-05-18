package me.ellios.hedwig.registry.utils;

import com.google.common.base.Preconditions;
import me.ellios.hedwig.common.Constants;
import org.apache.commons.lang3.StringUtils;

/**
 * zookeeper的路径处理类
 *
 * @author ellios
 * @since 12-11-5 上午12:04
 */
public class ZPathUtils {

    /**
     * Create the zookeeper service path.
     *
     * @param serviceName service name.
     * @return zookeeper path.
     */
    public static String buildServicePath(String serviceName) {
        return servicePathBuilder(serviceName).toString();
    }

    private static StringBuilder servicePathBuilder(String serviceName) {
        Preconditions.checkArgument(StringUtils.isNotEmpty(serviceName), "serviceName is empty");
        return append(Constants.PATH_SEPARATOR).append(serviceName);
    }

    private static StringBuilder append(String value) {
        return append(new StringBuilder(), value);
    }

    private static StringBuilder append(StringBuilder builder, String value) {
        if (null == builder) {
            builder = new StringBuilder();
        }
        return builder.append(value.trim());
    }

    /**
     * Create the full zookeeper path, including the leaf node.
     *
     * @param serviceName service name
     * @param nodeName    node name
     * @return full path
     */
    public static String buildZNodeFullPath(String serviceName, String nodeName) {
        StringBuilder servicePath = servicePathBuilder(serviceName);
        return appendToServicePath(servicePath, nodeName);
    }

    /**
     * Add leaf node to the service path.
     *
     * @param servicePath service path.
     * @param nodeName    node name
     * @return full path.
     */
    public static String appendToServicePath(String servicePath, String nodeName) {
        Preconditions.checkArgument(StringUtils.isNotBlank(servicePath), "servicePath cannot be empty or null.");
        return appendToServicePath(append(servicePath), nodeName);
    }

    private static String appendToServicePath(StringBuilder servicePath, String nodeName) {
        Preconditions.checkArgument(StringUtils.isNotBlank(nodeName), "nodeName cannot be empty or null.");
        Preconditions.checkArgument(!nodeName.contains("/"), "nodeName cannot contains slash /.");
        return append(servicePath, Constants.PATH_SEPARATOR).append(nodeName).toString();
    }
}
