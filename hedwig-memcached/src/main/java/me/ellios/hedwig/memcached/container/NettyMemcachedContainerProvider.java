package me.ellios.hedwig.memcached.container;

import org.glassfish.jersey.server.ApplicationHandler;
import org.glassfish.jersey.server.spi.ContainerProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.Application;

/**
 * Say something?
 *
 * @author George Cao
 * @since 4/15/13 2:15 PM
 */
public class NettyMemcachedContainerProvider implements ContainerProvider {
    private static final Logger LOG = LoggerFactory.getLogger(NettyMemcachedContainerProvider.class);


    @Override
    public <T> T createContainer(Class<T> type, Application app) throws ProcessingException {
        if (type != NettyMemcachedContainer.class) {
            LOG.warn("Unsupported type {}", type);
            return null;
        }
        return type.cast(new NettyMemcachedContainer(new ApplicationHandler(app)));
    }
}
