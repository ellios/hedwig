package me.ellios.hedwig.rpc.proxy;

/**
 * Author: ellios
 * Date: 12-10-31 Time: 上午11:38
 */
public interface ServiceInvokerFactory {

    public ServiceInvoker getServiceInvoker(String serviceName);
}
