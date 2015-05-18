package me.ellios.hedwig.http.server;

import me.ellios.hedwig.rpc.core.ServiceSchema;
import me.ellios.hedwig.rpc.core.ServiceType;
import me.ellios.hedwig.rpc.server.RpcServer;
import me.ellios.hedwig.rpc.server.RpcServerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * We use this factory to create rpc server.
 * And the server's schema is {@link ServiceSchema#HTTP}. We do NOT respect the type {@link ServiceType}, because the
 * way we implement the HTTP schema can support them both at the same time.
 *
 * @author George Cao
 * @since 4/26/13 4:53 PM
 */
public class HttpServerFactory implements RpcServerFactory {
    private static final Logger LOG = LoggerFactory.getLogger(HttpServerFactory.class);

    @Override
    public boolean accept(ServiceSchema schema, ServiceType type) {
        LOG.info("We can safely ignore the type {}, because we can support them all at the same time.", type);
        return schema == ServiceSchema.HTTP;
    }

    @Override
    public RpcServer create() {
        return new HttpServer();
    }
}
