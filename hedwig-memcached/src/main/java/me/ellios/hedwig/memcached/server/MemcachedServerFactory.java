package me.ellios.hedwig.memcached.server;

import me.ellios.hedwig.rpc.core.ServiceSchema;
import me.ellios.hedwig.rpc.core.ServiceType;
import me.ellios.hedwig.rpc.server.RpcServer;
import me.ellios.hedwig.rpc.server.RpcServerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A memcached server factory that is responsible for server creation.
 *
 * @author George Cao
 * @since 2014-01-26 14
 */
public class MemcachedServerFactory implements RpcServerFactory {
    private static final Logger LOG = LoggerFactory.getLogger(MemcachedServerFactory.class);

    @Override
    public boolean accept(ServiceSchema schema, ServiceType type) {
        LOG.info("We can safely ignore the type {}, because we can support them all at the same time.", type);
        return schema == ServiceSchema.MEMCACHED;
    }

    @Override
    public RpcServer create() {
        return new MemcachedServer();
    }
}
