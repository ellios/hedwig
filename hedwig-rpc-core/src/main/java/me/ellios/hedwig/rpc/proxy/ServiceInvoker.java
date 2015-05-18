package me.ellios.hedwig.rpc.proxy;

import java.lang.reflect.Method;

/**
 * Author: ellios
 * Date: 12-10-31 Time: 上午10:23
 */
public interface ServiceInvoker {

    public Object invoke(Method method, Object[] args);

    public void destroy();
}
