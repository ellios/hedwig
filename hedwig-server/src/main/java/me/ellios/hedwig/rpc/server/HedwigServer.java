package me.ellios.hedwig.rpc.server;

import com.google.common.base.Preconditions;
import me.ellios.hedwig.common.config.HedwigConfig;
import me.ellios.hedwig.common.instances.ServiceImplFactory;
import me.ellios.hedwig.common.spring.SpringContainer;
import me.ellios.hedwig.http.server.HttpServerFactory;
import me.ellios.hedwig.memcached.server.MemcachedServerFactory;
import me.ellios.hedwig.rpc.core.ServiceConfig;
import me.ellios.hedwig.rpc.core.ServiceSchema;
import me.ellios.hedwig.rpc.core.ServiceType;
import me.ellios.hedwig.rpc.server.support.SimpleRpcServerFactoryChain;
import me.ellios.hedwig.rpc.thrift.server.ThriftRpcServerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author ellios
 * @author George Cao
 *         Date: 12-11-1 Time: 下午12:14
 */
public class HedwigServer {

    private static final Logger LOG = LoggerFactory.getLogger(HedwigServer.class);

    private static final HedwigConfig hc = HedwigConfig.getInstance();

    private boolean registered = false;
    private List<ServiceConfig> configs = new LinkedList<>();
    private Map<ServerKey, RpcServer> activeServerTrack = new HashMap<>();
    private RpcServerFactoryChain chain;

    private HedwigServer() {
        initServer();
    }

    private void initServer() {
        if (hc.getBoolean("hedwig.spring.enable", false)) {
            LOG.info("begin to load service from spring");
            ServiceImplFactory.setContainer(new SpringContainer());
        }
        chain = new SimpleRpcServerFactoryChain(new ThriftRpcServerFactory());
        chain.addFactory(new HttpServerFactory());
        chain.addFactory(new MemcachedServerFactory());
    }

    /**
     * Stop all the servers started.
     */
    public void stop() {
        for (Map.Entry<ServerKey, RpcServer> entry : activeServerTrack.entrySet()) {
            LOG.info("Stop {}", entry.getKey(), entry.getValue());
            entry.getValue().stop();
        }
        activeServerTrack.clear();
    }

    private class ServerKey {
        ServiceSchema schema;
        ServiceType type;

        private ServerKey(ServiceSchema schema, ServiceType type) {
            this.schema = schema;
            this.type = type;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ServerKey serverKey = (ServerKey) o;

            if (schema != serverKey.schema) return false;
            if (type != serverKey.type) return false;

            return true;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("ServerKey{");
            sb.append("schema=").append(schema);
            sb.append(", type=").append(type);
            sb.append('}');
            return sb.toString();
        }

        @Override
        public int hashCode() {
            int result = schema != null ? schema.hashCode() : 0;
            result = 31 * result + (type != null ? type.hashCode() : 0);
            return result;
        }
    }

    /**
     * 启动服务
     */
    public void start() {
        Preconditions.checkArgument(registered, "You MUST at least register one service before starting the server.");
        for (ServiceConfig config : configs) {
            ServerKey key = new ServerKey(config.getSchema(), config.getType());
            RpcServer server = activeServerTrack.get(key);
            if (null == server) {
                server = chain.create(config.getSchema(), config.getType());
                if (null == server) {
                    throw new IllegalStateException("Need RpcServerFactory to create server with " + config);
                }
                activeServerTrack.put(key, server);
            }
            server.registerService(config);
        }
        for (RpcServer server : activeServerTrack.values()) {
            start(server);
        }
    }

    private void start(final RpcServer server) {
        if (null != server) {
            server.start();
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    LOG.info("Stop rpc server: {}", server);
                    server.stop();
                }
            });
        }
    }

    /**
     * 注册服务
     *
     * @param serviceConfig service config.
     */

    public void registerService(ServiceConfig serviceConfig) {
        Preconditions.checkNotNull(serviceConfig, "ServiceConfig cannot be null.");
        configs.add(serviceConfig);
        registered = true;
    }

    public static HedwigServer getServer() {
        return new HedwigServer();
    }
}
