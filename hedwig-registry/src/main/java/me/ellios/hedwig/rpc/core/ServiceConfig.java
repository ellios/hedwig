package me.ellios.hedwig.rpc.core;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import me.ellios.hedwig.common.utils.NetworkUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Author: ellios
 * Date: 12-10-31 Time: 下午3:56
 */
public class ServiceConfig {

    public static final int DEFAULT_SERVICE_PORT = 8888;
    public static final int DEFAULT_MAX_FRAME_SIZE = 2 * 1024 * 1024; //2M
    public static final String DEFAULT_SERVICE_GROUP = "";
    private int maxFrameSize = DEFAULT_MAX_FRAME_SIZE;
    private String name;
    private String host;
    private int port;
    private ServiceSchema schema;
    private ServiceType type;
    private Class<?> serviceFace;
    private Class<?> serviceImpl;
    private String serviceGroup;
    private String processor;

    ServiceConfig(String name, int port,
                  ServiceSchema schema, ServiceType type, String group,
                  Class<?> serviceFace, Class<?> serviceImpl) {
        this(name, NetworkUtils.getEth0Address(), port, DEFAULT_MAX_FRAME_SIZE,
                schema, type, group,
                serviceFace, serviceImpl);
    }

    ServiceConfig(String name, String host, int port, int maxFrameSize,
                  ServiceSchema schema, ServiceType type, String group,
                  Class<?> serviceFace, Class<?> serviceImpl) {
        this.name = name;
        this.host = host;
        this.port = port < 0 ? DEFAULT_SERVICE_PORT : port;
        this.maxFrameSize = maxFrameSize <= 0 ? DEFAULT_MAX_FRAME_SIZE : maxFrameSize;
        this.serviceFace = serviceFace;
        this.serviceImpl = serviceImpl;
        this.type = type;
        this.schema = schema;
        // NetworkUtils.getEth0Address(); //取第一块网卡的地址，作为host地址
        this.serviceGroup = group;
    }

    ServiceConfig(String name, int port,
                  ServiceSchema schema, ServiceType type, Class<?> serviceFace, Class<?> serviceImpl) {
        this(name, port, schema, type, DEFAULT_SERVICE_GROUP, serviceFace, serviceImpl);
    }

    public int getMaxFrameSize() {
        return maxFrameSize;
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

    public Class<?> getServiceFace() {
        return serviceFace;
    }

    public Class<?> getServiceImpl() {
        return serviceImpl;
    }

    public String getServiceGroup() {
        return serviceGroup;
    }

    public String getProcessor() {
        String thriftClassName = StringUtils.substringBeforeLast(getServiceFace().getName(), "$");
        return thriftClassName + "$Processor";
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("ServiceConfig");
        sb.append("{maxFrameSize=").append(maxFrameSize);
        sb.append(", name='").append(name).append('\'');
        sb.append(", host='").append(host).append('\'');
        sb.append(", port=").append(port);
        sb.append(", schema=").append(schema);
        sb.append(", type=").append(type);
        sb.append(", serviceFace=").append(serviceFace);
        sb.append(", serviceImpl=").append(serviceImpl);
        sb.append(", serviceGroup=").append(serviceGroup);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServiceConfig that = (ServiceConfig) o;
        return !(name != null ? !name.equals(that.name) : that.name != null);
    }

    @Override
    public int hashCode() {
        return (name != null ? name.hashCode() : 0);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {
        private int maxFrameSize;
        private String name;
        private String host;
        private int port;
        private ServiceSchema schema;
        private ServiceType type;
        private Class<?> serviceFace;
        private Class<?> serviceImpl;
        private String serviceGroup;

        public Builder maxFrameSize(int maxFrameSize) {
            this.maxFrameSize = maxFrameSize;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder host(String host) {
            this.host = host;
            return this;
        }

        public Builder port(int port) {
            this.port = port;
            return this;
        }

        public Builder schema(ServiceSchema schema) {
            this.schema = schema;
            return this;
        }

        public Builder type(ServiceType type) {
            this.type = type;
            return this;
        }

        public Builder serviceFace(Class<?> serviceFace) {
            this.serviceFace = serviceFace;
            return this;
        }

        public Builder serviceImpl(Class<?> serviceImpl) {
            this.serviceImpl = serviceImpl;
            return this;
        }

        public Builder serviceGroup(String groupName) {
            this.serviceGroup = groupName;
            return this;
        }

        public ServiceConfig build() {
            Preconditions.checkNotNull(serviceFace, "Service interface cannot be null.");
            Preconditions.checkNotNull(type, "Service type cannot be null.");
            Preconditions.checkNotNull(serviceImpl, "Service implementation cannot be null.");
            Preconditions.checkArgument(serviceFace.isAssignableFrom(serviceImpl),
                    "%s must implement the interface %s", serviceImpl.getName(), serviceFace.getName());
            // TODO do we need check if serviceFace is an interface?
            if (port < 1) {
                port(DEFAULT_SERVICE_PORT);
            }
            if (Strings.isNullOrEmpty(serviceGroup)) {
                serviceGroup(DEFAULT_SERVICE_GROUP);
            }
            if (Strings.isNullOrEmpty(host)) {
                host(NetworkUtils.getEth0Address());
            }
            if (maxFrameSize < 1) {
                maxFrameSize(DEFAULT_MAX_FRAME_SIZE);
            }
            if (Strings.isNullOrEmpty(name)) {
                name(ServiceConfigHelper.buildDefaultServiceName(type, serviceFace));
            }
            if (null == schema) {
                schema(ServiceSchema.TCP);
            }
            return new ServiceConfig(name, host, port, maxFrameSize,
                    schema, type, serviceGroup,
                    serviceFace, serviceImpl);
        }
    }
}


