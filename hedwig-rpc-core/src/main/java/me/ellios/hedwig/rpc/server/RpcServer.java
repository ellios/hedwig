package me.ellios.hedwig.rpc.server;


import me.ellios.hedwig.rpc.core.ServiceConfig;

/**
 * Author: ellios
 * Date: 12-11-1 Time: 下午3:02
 */
public interface RpcServer {

    public void start();

    public void stop();

    public void registerService(ServiceConfig serviceConfig);
}
