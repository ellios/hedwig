package me.ellios.hedwig.rpc.server.support;

import com.google.common.base.Preconditions;
import me.ellios.hedwig.rpc.core.ServiceSchema;
import me.ellios.hedwig.rpc.core.ServiceType;
import me.ellios.hedwig.rpc.server.RpcServer;
import me.ellios.hedwig.rpc.server.RpcServerFactory;
import me.ellios.hedwig.rpc.server.RpcServerFactoryChain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements Chain-of-responsibility pattern to create RPC server.
 * We do NOT want the {@link me.ellios.hedwig.rpc.server.RpcServerFactory} become too complex, so we implements the factory chain separately.
 *
 * @author George Cao
 * @since 4/26/13 5:51 PM
 */
public class SimpleRpcServerFactoryChain implements RpcServerFactoryChain {
    private static final Logger LOG = LoggerFactory.getLogger(SimpleRpcServerFactoryChain.class);
    private RpcServerFactoryChain next;
    private RpcServerFactory factory;

    public SimpleRpcServerFactoryChain(RpcServerFactory factory) {
        Preconditions.checkNotNull(factory, "RpcServerFactory cannot be NULL.");
        this.factory = factory;
    }

    @Override
    public boolean hasNext() {
        return next != null;
    }

    public void addNext(RpcServerFactoryChain next) {
        Preconditions.checkNotNull(next, "RpcServerFactoryChain(next) cannot be NULL.");
        if (hasNext()) {
            getNext().addNext(next);
        } else {
            this.next = next;
        }
    }

    @Override
    public RpcServerFactoryChain getNext() {
        return next;
    }

    @Override
    public void addFactory(RpcServerFactory factory) {
        addNext(new SimpleRpcServerFactoryChain(factory));
    }

    @Override
    public RpcServerFactory getFactory() {
        return factory;
    }

    public RpcServer create(ServiceSchema schema, ServiceType type) {
        if (factory.accept(schema, type)) {
            return factory.create();
        } else if (hasNext()) {
            return getNext().create(schema, type);
        }
        return null;
    }
}
