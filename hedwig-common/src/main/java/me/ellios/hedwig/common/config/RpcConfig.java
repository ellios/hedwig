package me.ellios.hedwig.common.config;

/**
 * Say something?
 *
 * @author George Cao
 * @since 13-10-11 上午10:53
 */
public interface RpcConfig {

    /**
     * By default, HedwigConfig just search the current working directory for
     * the specific property file. If the first attempt failed, then stop. If
     * you want to find all property file with the same name, please set this
     * system property to true before you try to get the config. One option may
     * be like this {@code -Dhedwig.properties.load.aggressive=true}
     */
    String PROPERTY_LOAD_AGGRESSIVE = "hedwig.properties.load.aggressive";
    String HEDWIG_RPC_READ_TIMEOUT_IN_MILLIS = "hedwig.rpc.read.timeout.in.millis";
    String HEDWIG_RPC_CONNECT_TIMEOUT_IN_MILLIS = "hedwig.rpc.connect.timeout.in.millis";
    String HEDWIG_RPC_WRITE_TIMEOUT_IN_MILLIS = "hedwig.rpc.write.timeout.in.millis";
    String HEDWIG_RPC_READ_TIMEOUT_IN_SECONDS = "hedwig.rpc.read.timeout.in.seconds";
    String HEDWIG_RPC_CONNECT_TIMEOUT_IN_SECONDS = "hedwig.rpc.connect.timeout.in.seconds";
    String HEDWIG_RPC_WRITE_TIMEOUT_IN_SECONDS = "hedwig.rpc.write.timeout.in.seconds";
    String HEDWIG_RPC_HOST_CONNECTIONS_THRESHOLDS = "hedwig.rpc.host.connections.thresholds";
    String HEDWIG_RPC_GLOBAL_CONNECTIONS_THRESHOLDS = "hedwig.rpc.global.connections.thresholds";
    String HEDWIG_RPC_CONNECTION_CHECK = "hedwig.rpc.connection.check";

    String HEDWIG_RPC_BACKLOG = "hedwig.server.backlog";
    String HEDWIG_RPC_TCP_NO_DELAY = "hedwig.server.tcp.no.delay";
    String HEDWIG_RPC_REUSE_ADDRESS = "hedwig.server.reuse.address";
    String HEDWIG_RPC_KEEP_ALIVE = "hedwig.server.keep.alive";
    String HEDWIG_RPC_SEND_BUFFER_SIZE = "hedwig.server.send.buffer.size";
    String HEDWIG_RPC_RECEIVE_BUFFER_SIZE = "hedwig.server.receive.buffer.size";
    String HEDWIG_RPC_MAX_SERVICE_THRESHOLD = "hedwig.server.max.service.threshold";

    String HEDWIG_ZK_ROOT_PATH = "hedwig.zk.rootpath";
    String HEDWIG_ZK_ENV = "hedwig.env";

    int backlog();

    boolean tcpNoDelay();

    boolean reuseAddress();

    boolean keepAlive();

    int sendBufferSize();

    int receiveBufferSize();

    int maxNumberOfServices();

    boolean loadAggressive();

    int globalConnectionsThresholds();

    int connectionsThresholdsPerHost();

    boolean enableConnectionCheck();

    int connectTimeoutInMillis();

    int readTimeoutInMillis();

    int writeTimeoutInMillis();
}
