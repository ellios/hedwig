package me.ellios.hedwig.http.container;

import org.glassfish.jersey.server.spi.ContainerResponseWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Say something?
 *
 * @author George Cao(caozhangzhi@qiyi.com)
 * @since 2014-03-24 18:36
 */
public abstract class JerseyResponseWriter<T> implements ContainerResponseWriter {
    private static final Logger LOG = LoggerFactory.getLogger(JerseyResponseWriter.class);

    protected T response;

    public JerseyResponseWriter(T response) {
        this.response = response;
    }

    @Override
    public void commit() {
    }

    @Override
    public boolean enableResponseBuffering() {
        System.out.println("enableResponseBuffering");
        return true;
    }

    @Override
    public boolean suspend(long timeOut, TimeUnit timeUnit, TimeoutHandler timeoutHandler) {
        return false;
    }

    @Override
    public void setSuspendTimeout(long timeOut, TimeUnit timeUnit) throws IllegalStateException {

    }

    @Override
    public void failure(Throwable error) {

    }
}