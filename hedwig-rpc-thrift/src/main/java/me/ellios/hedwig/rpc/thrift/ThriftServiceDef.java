package me.ellios.hedwig.rpc.thrift;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import me.ellios.hedwig.common.config.HedwigConfig;
import me.ellios.hedwig.common.config.RpcConfig;
import me.ellios.hedwig.common.exceptions.HedwigException;
import me.ellios.hedwig.common.instances.ServiceImplFactory;
import me.ellios.hedwig.common.utils.ClassLoaderUtils;
import me.ellios.hedwig.rpc.core.ServiceConfig;
import me.ellios.hedwig.rpc.tracer.TracerDriver;
import me.ellios.hedwig.rpc.tracer.impl.SimpleLogTracer;
import org.apache.thrift.*;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocolFactory;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Defines the thrift service. One service per port.
 *
 * @author ellios
 * @author George Cao
 */
public class ThriftServiceDef {
    private final int serverPort;
    private final String serverHost;
    private final int maxFrameSize;
    private final int lengthFieldLength;
    private final TProcessor processor;
    private final TProtocolFactory inProtocolFact;
    private final TProtocolFactory outProtocolFact;
    private final boolean headerTransport;
    private final boolean enableConnectionCheck;
    private final Executor executor;
    private final String name;
    private final int maxConcurrentRequests;
    private final int openConnectionsThresholds;
    private final int hostConnectionsThresholds;
    private final int hostConnectionMaxIdleTime;
    private final int hostConnectionMaxLifeTime;
    private final TracerDriver tracer;
    private final Map<String, Set<String>> mapping;

    public ThriftServiceDef(String serverHost, int serverPort, int maxFrameSize, int lengthFieldLength,
                            TProcessor processor,
                            TProtocolFactory inProtocolFact, TProtocolFactory outProtocolFact,
                            boolean headerTransport, boolean enableConnectionCheck,
                            Executor executor, String name,
                            int maxConcurrentRequests,
                            int hostConnectionMaxIdleTime, int hostConnectionMaxLifeTime,
                            int openConnectionsThresholds, int hostConnectionsThresholds,
                            TracerDriver tracer) {
        this(serverHost, serverPort, maxFrameSize, lengthFieldLength,
                processor,
                inProtocolFact, outProtocolFact,
                headerTransport, enableConnectionCheck,
                executor, name,
                maxConcurrentRequests, hostConnectionMaxIdleTime,
                hostConnectionMaxLifeTime, openConnectionsThresholds,
                hostConnectionsThresholds, tracer, Collections.<String, Set<String>>emptyMap());
    }

    public ThriftServiceDef(String serverHost, int serverPort, int maxFrameSize, int lengthFieldLength,
                            TProcessor processor,
                            TProtocolFactory inProtocolFact, TProtocolFactory outProtocolFact,
                            boolean headerTransport, boolean enableConnectionCheck,
                            Executor executor, String name,
                            int maxConcurrentRequests,
                            int hostConnectionMaxIdleTime, int hostConnectionMaxLifeTime,
                            int openConnectionsThresholds, int hostConnectionsThresholds,
                            TracerDriver tracer, Map<String, Set<String>> mapping) {
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        this.maxFrameSize = maxFrameSize;
        this.lengthFieldLength = lengthFieldLength;
        this.processor = processor;
        this.inProtocolFact = inProtocolFact;
        this.outProtocolFact = outProtocolFact;
        this.headerTransport = headerTransport;
        this.enableConnectionCheck = enableConnectionCheck;
        this.executor = executor;
        this.name = name;
        this.maxConcurrentRequests = maxConcurrentRequests;
        this.hostConnectionMaxIdleTime = hostConnectionMaxIdleTime;
        this.hostConnectionMaxLifeTime = hostConnectionMaxLifeTime;
        this.openConnectionsThresholds = openConnectionsThresholds;
        this.hostConnectionsThresholds = hostConnectionsThresholds;
        this.tracer = (null == tracer) ? TracerDriver.DISCARD_TRACER : tracer;
        this.mapping = mapping;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    /**
     * Creates ThriftServerConfig from ServiceConfig.
     *
     * @param serviceConfig service config info.
     * @return The final {@link me.ellios.hedwig.rpc.thrift.ThriftServiceDef}
     * @throws Exception thrift processor creation exception.
     * @see #createBuilder(me.ellios.hedwig.rpc.core.ServiceConfig)
     */
    public static ThriftServiceDef create(ServiceConfig serviceConfig) {
        return createBuilder(serviceConfig).build();
    }

    public static Builder createBuilder(ServiceConfig serviceConfig) {
        return createBuilder(serviceConfig, HedwigConfig.getInstance());
    }

    /**
     * In case you want to change the config info in the {@link ServiceConfig}, here comes your chances.
     *
     * @param serviceConfig service config.
     * @return The builder that you can override values.
     * @throws Exception thrift processor creation exception.
     */
    public static Builder createBuilder(ServiceConfig serviceConfig, RpcConfig config) {
        Builder builder = ThriftServiceDef.newBuilder()
                .serverPort(serviceConfig.getPort())
                .serverHost(serviceConfig.getHost())
                .maxFrameSize(serviceConfig.getMaxFrameSize())
                .inProtocolFact(new TCompactProtocol.Factory())
                .outProtocolFact(new TCompactProtocol.Factory())
                .headerTransport(false)
                .name(serviceConfig.getName());
        // TODO refactor out this thread pool.
        // Construct the business thread pool
        ThreadFactory bossThreadFactory = new ThreadFactoryBuilder()
                .setNameFormat("hedwig-thrift-rpc-#%d").build();
        Executor executor = Executors.newCachedThreadPool(bossThreadFactory);
        // Construct the Thrift processor.
        TProcessor processor = initialProcessor(serviceConfig);
        // Check if the connection limits is enabled.
        return builder
                .processor(processor)
                .enableConnectionCheck(config.enableConnectionCheck())
                .executor(executor)
                .maxConcurrentRequests(Integer.MAX_VALUE)
                .hostConnectionMaxIdleTime(config.connectionsThresholdsPerHost())
                .hostConnectionMaxLifeTime(Integer.MAX_VALUE)
                .hostConnectionsThresholds(config.connectionsThresholdsPerHost())
                .openConnectionsThresholds(config.globalConnectionsThresholds());
    }

    private static TProcessor initialProcessor(ServiceConfig serviceConfig) {
        String name = serviceConfig.getProcessor();
        Class processClass = ClassLoaderUtils.loadClass(name);
        try {
            Constructor cons = processClass.getConstructor(new Class<?>[]{serviceConfig.getServiceFace()});
            Object serviceImpl = ServiceImplFactory.getServiceImpl(serviceConfig.getServiceImpl());
            return (TProcessor) cons.newInstance(new Object[]{serviceImpl});
        } catch (Exception e) {
            throw new HedwigException("Cannot initial class " + name, e);
        }
    }

    /**
     * Aggregate these services to one service on the same port.
     *
     * @param services all services on the same port.
     * @return the server config.
     */
    public static ThriftServiceDef aggregate(Collection<ServiceConfig> services) {
        Map<String, Set<String>> mapping = new HashMap<>(services.size());
        ThriftServiceDef.Builder builder = ThriftServiceDef.newBuilder();
        TMultiplexedProcessor processor = new TMultiplexedProcessor();
        for (ServiceConfig service : services) {
            ThriftServiceDef config = create(service);
            // Register the concrete processor.
            TBaseProcessor concrete = (TBaseProcessor) config.getProcessor();
            Map<String, ProcessFunction> map = concrete.getProcessMapView();
            if (null != map) {
                Set<String> names = map.keySet();
                if (names.size() > 0) {
                    mapping.put(config.getName(), map.keySet());
                }
            }
            processor.registerProcessor(config.getName(), concrete);
            // Copy the properties.
            builder.enableConnectionCheck(config.isEnableConnectionCheck())
                    .hostConnectionMaxIdleTime(config.getHostConnectionMaxIdleTime())
                    .hostConnectionMaxLifeTime(config.getHostConnectionMaxLifeTime())
                    .hostConnectionsThresholds(config.getHostConnectionsThresholds())
                    .openConnectionsThresholds(config.getOpenConnectionsThresholds())
                    .maxConcurrentRequests(config.getMaxConcurrentRequests())

                    .lengthFieldLength(config.getLengthFieldLength())
                    .maxFrameSize(Math.max(builder.maxFrameSize, config.getMaxFrameSize()))
                    .executor(config.getExecutor())
                    .inProtocolFact(config.getInProtocolFact())
                    .outProtocolFact(config.getOutProtocolFact())
                    .name("NameDoesNotMatter")
                    .headerTransport(config.isHeaderTransport())
                    .tracer(config.getTracer())
                    .serverHost(config.getServerHost())
                    .serverPort(config.getServerPort());
        }
        return builder.processor(processor).mapping(mapping).tracer(new SimpleLogTracer()).build();
    }

    public Map<String, Set<String>> getMapping() {
        return mapping;
    }

    public String getServerHost() {
        return serverHost;
    }

    public TracerDriver getTracer() {
        return tracer;
    }

    public TProtocolFactory getInProtocolFact() {
        return inProtocolFact;
    }

    public TProtocolFactory getOutProtocolFact() {
        return outProtocolFact;
    }

    public int getMaxConcurrentRequests() {
        return maxConcurrentRequests;
    }

    public int getHostConnectionMaxIdleTime() {
        return hostConnectionMaxIdleTime;
    }

    public int getHostConnectionMaxLifeTime() {
        return hostConnectionMaxLifeTime;
    }

    public int getOpenConnectionsThresholds() {
        return openConnectionsThresholds;
    }

    public int getHostConnectionsThresholds() {
        return hostConnectionsThresholds;
    }

    public boolean isEnableConnectionCheck() {
        return enableConnectionCheck;
    }

    public int getServerPort() {
        return serverPort;
    }

    public int getMaxFrameSize() {
        return maxFrameSize;
    }

    public int getLengthFieldLength() {
        return lengthFieldLength;
    }

    public TProcessorFactory getProcessorFactory() {
        return new TProcessorFactory(processor);
    }

    public TProcessor getProcessor() {
        return processor;
    }

    public TProtocolFactory getInProtocolFactory() {
        return inProtocolFact;
    }

    public TProtocolFactory getOutProtocolFactory() {
        return outProtocolFact;
    }

    public boolean isHeaderTransport() {
        return headerTransport;
    }

    public Executor getExecutor() {
        return executor;
    }

    public String getName() {
        return name;
    }

    public static class Builder {
        private int serverPort;
        private String serverHost;
        private int maxFrameSize = 1024;
        private int lengthFieldLength = 4;
        private TProcessor processor;
        private TProtocolFactory inProtocolFact;
        private TProtocolFactory outProtocolFact;
        private boolean headerTransport = false;
        private boolean enableConnectionCheck;
        private Executor executor;
        private String name;
        private int maxConcurrentRequests;
        private int hostConnectionMaxIdleTime;
        private int hostConnectionMaxLifeTime;
        private int openConnectionsThresholds;
        private int hostConnectionsThresholds;
        private TracerDriver tracer;
        private Map<String, Set<String>> mapping = Collections.emptyMap();

        public Builder mapping(Map<String, Set<String>> mapping) {
            this.mapping = mapping;
            return this;
        }

        public Builder serverPort(int serverPort) {
            this.serverPort = serverPort;
            return this;
        }

        public Builder maxFrameSize(int maxFrameSize) {
            this.maxFrameSize = maxFrameSize;
            return this;
        }

        public Builder lengthFieldLength(int lengthFieldLength) {
            this.lengthFieldLength = lengthFieldLength;
            return this;
        }

        public Builder processor(TProcessor processor) {
            this.processor = processor;
            return this;
        }

        public Builder inProtocolFact(TProtocolFactory inProtocolFact) {
            this.inProtocolFact = inProtocolFact;
            return this;
        }

        public Builder outProtocolFact(TProtocolFactory outProtocolFact) {
            this.outProtocolFact = outProtocolFact;
            return this;
        }

        public Builder enableConnectionCheck(boolean enableConnectionCheck) {
            this.enableConnectionCheck = enableConnectionCheck;
            return this;
        }

        public Builder headerTransport(boolean headerTransport) {
            this.headerTransport = headerTransport;
            return this;
        }

        public Builder executor(Executor executor) {
            this.executor = executor;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder maxConcurrentRequests(int maxConcurrentRequests) {
            this.maxConcurrentRequests = maxConcurrentRequests;
            return this;
        }

        public Builder hostConnectionMaxIdleTime(int hostConnectionMaxIdleTime) {
            this.hostConnectionMaxIdleTime = hostConnectionMaxIdleTime;
            return this;
        }

        public Builder hostConnectionMaxLifeTime(int hostConnectionMaxLifeTime) {
            this.hostConnectionMaxLifeTime = hostConnectionMaxLifeTime;
            return this;
        }

        public Builder openConnectionsThresholds(int openConnectionsThresholds) {
            this.openConnectionsThresholds = openConnectionsThresholds;
            return this;
        }

        public Builder hostConnectionsThresholds(int hostConnectionsThresholds) {
            this.hostConnectionsThresholds = hostConnectionsThresholds;
            return this;
        }

        public Builder tracer(TracerDriver tracer) {
            this.tracer = tracer;
            return this;
        }

        public Builder serverHost(String serverHost) {
            this.serverHost = serverHost;
            return this;
        }

        public ThriftServiceDef build() {
            // TODO check required arguments and set optional values first.
            return new ThriftServiceDef(serverHost, serverPort, maxFrameSize, lengthFieldLength,
                    processor,
                    inProtocolFact, outProtocolFact,
                    headerTransport, enableConnectionCheck,
                    executor, name,
                    maxConcurrentRequests,
                    hostConnectionMaxIdleTime, hostConnectionMaxLifeTime,
                    openConnectionsThresholds, hostConnectionsThresholds,
                    tracer, mapping
            );
        }
    }
}

