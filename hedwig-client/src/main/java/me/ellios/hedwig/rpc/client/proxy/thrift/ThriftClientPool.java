package me.ellios.hedwig.rpc.client.proxy.thrift;

import me.ellios.hedwig.common.utils.ClassHelper;
import me.ellios.hedwig.rpc.core.ServiceConfig;
import me.ellios.hedwig.rpc.core.ServiceNode;
import me.ellios.hedwig.rpc.loadbalancer.ConnectionResource;
import me.ellios.hedwig.rpc.proxy.pool.HedwigPoolConfig;
import me.ellios.hedwig.rpc.proxy.pool.Pool;
import me.ellios.hedwig.rpc.thrift.client.ThriftClient;
import me.ellios.hedwig.rpc.thrift.protocol.THeaderName;
import me.ellios.hedwig.rpc.thrift.protocol.TMessageHeader;
import me.ellios.hedwig.zookeeper.config.ZookeeperConfig;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool.BasePoolableObjectFactory;
import org.apache.thrift.TServiceClient;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TMultiplexedProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.transport.TTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Author: ellios Date: 12-11-16 Time: 下午6:59
 *
 * @param <T> a sub class of {@link org.apache.thrift.TServiceClient}.
 */
public class ThriftClientPool<T extends TServiceClient> extends Pool<T> implements ConnectionResource {
    private static final Logger LOG = LoggerFactory.getLogger(ThriftClientPool.class);

    private AtomicInteger connections = new AtomicInteger(0);
    private AtomicInteger weight = new AtomicInteger(1);

    public ThriftClientPool(final HedwigPoolConfig config, final ServiceNode serviceNode) {
        super(config, new ThriftClientFactory<T>(serviceNode));
        connections = new AtomicInteger(Math.max(0, serviceNode.getConnections()));
        weight = new AtomicInteger(Math.max(1, serviceNode.getWeight()));
    }

    @Override
    public int connections() {
        return connections.get();
    }

    @Override
    public int addAndGetConnections(int delta) {
        return connections.addAndGet(delta);
    }

    @Override
    public int weight() {
        return weight.get();
    }

    @Override
    public int addAndGetWeight(int delta) {
        return weight.addAndGet(delta);
    }

    private static class ThriftClientFactory<T extends TServiceClient> extends BasePoolableObjectFactory<T> {

        private static final ThriftClient thriftClient = new ThriftClient(ServiceConfig.DEFAULT_MAX_FRAME_SIZE);
        private final ServiceNode serviceNode;
        private final TProtocolFactory factory;
        private final ConcurrentMap<T, TTransport> transports = new ConcurrentHashMap<>();
        private final String header;

        public ThriftClientFactory(ServiceNode serviceNode) {
            this(serviceNode, new TCompactProtocol.Factory());
        }

        public ThriftClientFactory(ServiceNode serviceNode, TProtocolFactory factory) {
            this.serviceNode = serviceNode;
            this.factory = factory;
            this.header = new TMessageHeader()
                    .add(THeaderName.USER, ZookeeperConfig.getAclUserString())
                    .add(THeaderName.SERVICE, serviceNode.getName())
                    .encode();
        }

        @Override
        public T makeObject() throws Exception {
            String thriftClassName = StringUtils.split(serviceNode.getServiceFace(), '$')[0];
            Class<T> clientClazz = (Class<T>) ClassHelper.forName(thriftClassName + "$Client");
            TTransport transport = thriftClient.connectSync(serviceNode.getSocketAddress());

            TMultiplexedProtocol protocol = new TMultiplexedProtocol(factory.getProtocol(transport), header);
            Constructor<T> cons = clientClazz.getConstructor(TProtocol.class);
            T client = cons.newInstance(protocol);
            transports.put(client, transport);
            LOG.debug("ServiceNode: {}, finish makeObject client : {}, transport : {}",
                    serviceNode.getZnodeName(), client, transport);
            return client;
        }

        @Override
        public void destroyObject(final T client) throws Exception {
            TTransport transport = transports.get(client);
            if (transport != null && transport.isOpen()) {
                LOG.info("Closing transport. client : {}, transport : {}, serviceNode :{}",
                        client, transport, serviceNode.getZnodeName());
                transport.close();
            }
            transports.remove(client);
        }

        @Override
        public boolean validateObject(final T client) {
            TTransport transport = transports.get(client);
            LOG.debug("ServiceNode: {}, trying to validateObject client : {}, transport : {}",
                    serviceNode.getZnodeName(), client, transport);
            return transport != null && transport.isOpen();
        }
    }

}
