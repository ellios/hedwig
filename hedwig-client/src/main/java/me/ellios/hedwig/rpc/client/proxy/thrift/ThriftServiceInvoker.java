package me.ellios.hedwig.rpc.client.proxy.thrift;

import com.google.common.base.Preconditions;
import me.ellios.hedwig.common.exceptions.HedwigException;
import me.ellios.hedwig.common.exceptions.HedwigTransportException;
import me.ellios.hedwig.common.utils.StopWatch;
import me.ellios.hedwig.registry.CallbackWatcher;
import me.ellios.hedwig.rpc.core.ServiceNode;
import me.ellios.hedwig.rpc.loadbalancer.factory.LoadBalancerFactory;
import me.ellios.hedwig.rpc.proxy.support.AbstractServiceInvoker;
import org.apache.thrift.TApplicationException;
import org.apache.thrift.TServiceClient;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import static org.slf4j.helpers.MessageFormatter.arrayFormat;

/**
 * Author: ellios
 * Date: 12-11-3 Time: 下午5:02
 */
public class ThriftServiceInvoker<T extends TServiceClient> extends AbstractServiceInvoker<ThriftClientPool<T>> {
    private static final Logger LOG = LoggerFactory.getLogger(ThriftServiceInvoker.class);

    public ThriftServiceInvoker(String serviceGroup, String serviceName, LoadBalancerFactory loadBalancerFactory) {
        super(serviceGroup, serviceName, loadBalancerFactory);
    }

    @Override
    protected void destroy(ThriftClientPool<T> resource) {
        resource.destroy();
    }

    @Override
    protected Object doInvoke(Object target, Method method, Object[] args) {
        StopWatch watch = new StopWatch("doInvoke method : " + method);
        watch.start("[[get client]]");
        ThriftClientPool<T> pool = getPool();
        Preconditions.checkNotNull(pool, "pool is null");
        boolean isBroken = false;
        T client = pool.getResource();
        watch.stop();
        watch.start("[[call method]]");
        try {
            LOG.debug("call method: {} with args: {}, transport: {}", method, args, client.getInputProtocol().getTransport());
            return method.invoke(client, args);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            // Handle application exception
            if (cause instanceof TApplicationException) {
                TApplicationException thriftEx = (TApplicationException) cause;
                if (thriftEx.getType() == TApplicationException.MISSING_RESULT) {
                    LOG.warn("Thrift missing result error. method : {}, args : {}", method, args);
                    return null;
                }
            }
            // This should not happen any way.
            isBroken = true;
            // Handle network error.
            if (cause instanceof TTransportException) {
                String message = arrayFormat("Transport fail. transport: {}, method: {}, args: {}",
                        new Object[]{client.getInputProtocol().getTransport(), method, args}).getMessage();
                throw new HedwigTransportException(message, e);
            }
            // Now we do  NOT know what happened
            String message = arrayFormat("Call method fail. transport: {}, method: {}, args: {}",
                    new Object[]{client.getInputProtocol().getTransport(), method, args}).getMessage();
            throw new HedwigException(message, e);
        } catch (Exception e) {
            String message = arrayFormat("Call method fail. Transport: {}, method: {}, args: {}, message: {}",
                    new Object[]{client.getInputProtocol().getTransport(), method, args, e.getMessage()}).getMessage();
            isBroken = true;
            throw new HedwigException(message, e);
        } finally {
            if (isBroken) {
                pool.returnBrokenResource(client);
            } else {
                pool.returnResource(client);
            }
            watch.stop();
            if (watch.getTotalTimeMillis() > 100) {
                LOG.warn(watch.toString());
            }
        }
    }

    @Override
    protected CallbackWatcher getCallbackWatcher() {
        return new ThriftServiceCallbackWatcher();
    }

    @Override
    protected ThriftClientPool<T> createPool(ServiceNode node) {
        return ThriftClientPoolFactory.createPool(node);
    }

    private class ThriftServiceCallbackWatcher implements CallbackWatcher {

        @Override
        public void doCallback(List<ServiceNode> serviceNodeList) {
            LOG.warn("Try to refresh thrift services : {}", serviceNodeList);
            buildLoadBalancer(serviceNodeList);
        }
    }
}
