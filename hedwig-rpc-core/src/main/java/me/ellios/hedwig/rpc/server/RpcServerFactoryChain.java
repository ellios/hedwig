package me.ellios.hedwig.rpc.server;


import me.ellios.hedwig.rpc.core.ServiceSchema;
import me.ellios.hedwig.rpc.core.ServiceType;

/**
 * Implements Chain of Responsibility pattern to create rpc server.
 *
 * @author George Cao
 * @since 4/26/13 5:51 PM
 */
public interface RpcServerFactoryChain {

    /**
     * Just a util method to add the specified {@link me.ellios.hedwig.rpc.server.RpcServerFactory} factory.
     *
     * @param factory new rpc server factory
     * @see #addNext(me.ellios.hedwig.rpc.server.RpcServerFactoryChain)
     */
    void addFactory(RpcServerFactory factory);

    /**
     * Gets the current factory.
     *
     * @return {@link me.ellios.hedwig.rpc.server.RpcServerFactory}
     */
    RpcServerFactory getFactory();

    /**
     * Gets the next node in the chain.
     * Please invoke {@link #hasNext()} to check if there has any more node.
     *
     * @return RpcServerFactoryChain
     * @see #hasNext()
     */
    RpcServerFactoryChain getNext();

    /**
     * Adds new factory to the chain.
     *
     * @param next {@link me.ellios.hedwig.rpc.server.RpcServerFactoryChain}
     */
    void addNext(RpcServerFactoryChain next);

    /**
     * Checks if there are more nodes.
     *
     * @return true or false.
     */
    boolean hasNext();

    /**
     * Creates the {@link me.ellios.hedwig.rpc.server.RpcServer} instance. if the current rpc factory supports the specified {@link ServiceSchema} schema
     * and {@link me.ellios.hedwig.rpc.core.ServiceType} type.
     *
     * @param schema rpc server schema
     * @param type   rpc server type
     * @return An instance of {@link me.ellios.hedwig.rpc.server.RpcServer}, maybe null.
     */
    RpcServer create(ServiceSchema schema, ServiceType type);
}
