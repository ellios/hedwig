package me.ellios.hedwig.rpc.client.proxy.thrift;

import com.google.common.reflect.AbstractInvocationHandler;
import me.ellios.hedwig.rpc.proxy.ServiceInvoker;

import java.lang.reflect.Method;

/**
 * Author: ellios
 * Date: 12-10-31 Time: 上午10:36
 */
public class ThriftServiceInvocationHandler extends AbstractInvocationHandler {

    private final ServiceInvoker invoker;

    public ThriftServiceInvocationHandler(ServiceInvoker invoker) {
        this.invoker = invoker;
    }

    @Override
    protected Object handleInvocation(Object proxy, Method method, Object[] args) throws Throwable {
        return invoker.invoke(method, args);
    }
}
