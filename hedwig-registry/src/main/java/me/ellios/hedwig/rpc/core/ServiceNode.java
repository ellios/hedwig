package me.ellios.hedwig.rpc.core;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONCreator;
import com.alibaba.fastjson.annotation.JSONField;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import me.ellios.hedwig.common.Constants;
import org.apache.commons.lang3.StringUtils;

import java.net.InetSocketAddress;


import static me.ellios.hedwig.registry.utils.ZPathUtils.appendToServicePath;
import static me.ellios.hedwig.registry.utils.ZPathUtils.buildServicePath;


/**
 * Author: ellios
 * Date: 12-10-30 Time: 下午8:16
 */
public class ServiceNode {

    private static final Joiner JOINER = Joiner.on(Constants.ZNODE_NAME_SEPARATOR);
    private String znodeName;
    private String znodePath;
    private String znodeParentPath;
    // TODO remove duplicated properties with ServiceConfig
    private String name;
    private String host;
    private int port;
    private ServiceSchema schema;
    private ServiceType type;
    private String serviceFace;
    private String serviceImpl;
    private String serviceGroup;
    /**
     * For later use.*
     */
    public static final int DEFAULT_WEIGHT = 1;
    private int weight = 1;
    public static final int DEFAULT_CONNECTIONS = 0;
    private int connections = 0;

    private ServiceNode(String name, String host, int port,
                        ServiceSchema schema, ServiceType type,
                        int weight, int connections,
                        String serviceFace, String serviceImpl) {
        this.name = name;
        this.host = host;
        this.port = port;
        this.schema = schema;
        this.type = type;
        this.weight = weight;
        this.connections = connections;
        this.serviceFace = serviceFace;
        this.serviceImpl = serviceImpl;
        this.znodeParentPath = buildServicePath(name);
        this.znodeName = createZNodeName(schema, host, port, type);
        this.znodePath = appendToServicePath(znodeParentPath, znodeName);
    }

    public String getName() {
        return name;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public ServiceSchema getSchema() {
        return schema;
    }

    public ServiceType getType() {
        return type;
    }

    public String getServiceFace() {
        return serviceFace;
    }

    public String getServiceImpl() {
        return serviceImpl;
    }

    public int getWeight() {
        return weight;
    }

    public int getConnections() {
        return connections;
    }

    public String getZnodeName() {
        return znodeName;
    }


    public String getZnodeParentPath() {
        return znodeParentPath;
    }

    public String getZnodePath() {
        return znodePath;
    }

    public String getServiceGroup() {
        return serviceGroup;
    }

    @JSONField(serialize = false)
    public void setZnodeName(String znodeName) {
        this.znodeName = znodeName;
    }

    @JSONField(serialize = false)
    public void setServiceGroup(String serviceGroup) {
        this.serviceGroup = serviceGroup;
    }

    @JSONField(serialize = false)
    public void setZnodePath(String znodePath) {
        this.znodePath = znodePath;
    }


    @JSONField(serialize = false)
    public void setZnodeParentPath(String znodeParentPath) {
        this.znodeParentPath = znodeParentPath;
    }

    @JSONField(serialize = false)
    public InetSocketAddress getSocketAddress() {
        return new InetSocketAddress(host, port);
    }

    @JSONField(serialize = false)
    public void setConnections(int connections) {
        this.connections = connections;
    }

    @JSONField(serialize = false)
    public void setWeight(int weight) {
        this.weight = weight;
    }

    public byte[] toJson() {
        return JSON.toJSONBytes(this);
    }


    public static ServiceNode fromJson(byte[] json) {
        Preconditions.checkNotNull(json, "argument json is empty");
        return JSON.parseObject(json, ServiceNode.class);
    }

    @JSONCreator
    public static ServiceNode createServiceNode(@JSONField(name = "name") String name,
                                                @JSONField(name = "host") String host,
                                                @JSONField(name = "port") int port,
                                                @JSONField(name = "schema") ServiceSchema schema,
                                                @JSONField(name = "type") ServiceType type,
                                                @JSONField(name = "serviceFace") String serviceFace,
                                                @JSONField(name = "serviceImpl") String serviceImpl,
                                                @JSONField(name = "serviceGroup") String serviceGroup,
                                                @JSONField(name = "weight") int weight,
                                                @JSONField(name = "connections") int connections) {
        Preconditions.checkArgument(StringUtils.isNotEmpty(name), "name is empty.");
        Preconditions.checkArgument(StringUtils.isNotEmpty(host), "host is empty.");
        Preconditions.checkNotNull(schema, "schema is null.");
        Preconditions.checkNotNull(type, "type is null.");
        Preconditions.checkArgument(StringUtils.isNotEmpty(serviceFace), "serviceFace is empty.");
        Preconditions.checkArgument(StringUtils.isNotEmpty(serviceImpl), "serviceImpl is empty.");
        return new ServiceNode(name, host, port,
                schema, type,
                weight, connections,
                serviceFace, serviceImpl);
    }

    public static ServiceNode createServiceNode(ServiceConfig config) {
        return createServiceNode(config, DEFAULT_WEIGHT, DEFAULT_CONNECTIONS);
    }

    public static ServiceNode createServiceNode(ServiceConfig config, int weight, int connections) {
        Preconditions.checkNotNull(config, "serviceConfig is null");
        String name = config.getName();
        String host = config.getHost();
        int port = config.getPort();
        ServiceSchema schema = config.getSchema();
        ServiceType type = config.getType();
        return new ServiceNode(name, host, port,
                schema, type,
                weight, connections,
                config.getServiceFace().getName(), config.getServiceImpl().getName());
    }

    private static String createZNodeName(ServiceSchema schema, String host, int port,
                                          ServiceType type) {
        return JOINER.join(schema.name(), type.name(), host, port);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("ServiceNode");
        sb.append("{znodeName='").append(znodeName).append('\'');
        sb.append(", znodePath='").append(znodePath).append('\'');
        sb.append(", znodeParentPath='").append(znodeParentPath).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append(", host='").append(host).append('\'');
        sb.append(", port=").append(port);
        sb.append(", schema=").append(schema);
        sb.append(", type=").append(type);
        sb.append(", serviceFace='").append(serviceFace).append('\'');
        sb.append(", serviceImpl='").append(serviceImpl).append('\'');
        sb.append(", serviceGroup='").append(serviceGroup).append('\'');
        sb.append(", weight=").append(weight);
        sb.append(", connections=").append(connections);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ServiceNode that = (ServiceNode) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        return !(znodeName != null ? !znodeName.equals(that.znodeName) : that.znodeName != null);

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (znodeName != null ? znodeName.hashCode() : 0);
        return result;
    }
}
