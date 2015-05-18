package me.ellios.hedwig.rpc.server;


import me.ellios.hedwig.rpc.core.ServiceSchema;
import me.ellios.hedwig.rpc.core.ServiceType;

/**
 * RPC server factory to initiate the required {@link me.ellios.hedwig.rpc.server.RpcServer}.
 *
 * @author George Cao
 * @since 4/26/13 11:50 AM
 */
public interface RpcServerFactory {

    /**
     * Checks if the current factory to handle the required server.
     *
     * @param schema the required server schema
     * @param type   the required server type
     * @return true or false.
     */
    boolean accept(ServiceSchema schema, ServiceType type);

    /**
     * Creates a new {@link me.ellios.hedwig.rpc.server.RpcServer}.
     * This method is only invoked when
     * {@link #accept(me.ellios.hedwig.rpc.core.ServiceSchema, me.ellios.hedwig.rpc.core.ServiceType)}
     * returns true.
     *
     * @return new {@link me.ellios.hedwig.rpc.server.RpcServer} instance.
     * @see #accept(me.ellios.hedwig.rpc.core.ServiceSchema, me.ellios.hedwig.rpc.core.ServiceType)
     */
    RpcServer create();
}
