package me.ellios.hedwig.rpc.thrift.server;

import me.ellios.hedwig.rpc.core.ServiceSchema;
import me.ellios.hedwig.rpc.core.ServiceType;
import me.ellios.hedwig.rpc.server.RpcServer;
import me.ellios.hedwig.rpc.server.RpcServerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * We use this factory to create rpc server.
 * And the server's schema is {@link ServiceSchema#TCP}, type is {@link ServiceType#THRIFT} .
 *
 * @author George Cao
 * @since 4/26/13 11:52 AM
 */
public class ThriftRpcServerFactory implements RpcServerFactory {
    private static final Logger LOG = LoggerFactory.getLogger(ThriftRpcServerFactory.class);

    @Override
    public boolean accept(ServiceSchema schema, ServiceType type) {
        return schema == ServiceSchema.TCP && type == ServiceType.THRIFT;
    }

    @Override
    public RpcServer create() {
        LOG.info("Creating new instance of ThriftRpcServer.");
        return new ThriftRpcServer();
    }
}
