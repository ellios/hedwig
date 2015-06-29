package me.ellios.memcached.support;

import com.google.common.base.Preconditions;
import me.ellios.memcached.MemcachedOp;
import me.ellios.memcached.config.Config;
import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.MemcachedClientBuilder;
import net.rubyeye.xmemcached.XMemcachedClientBuilder;
import net.rubyeye.xmemcached.command.BinaryCommandFactory;
import net.rubyeye.xmemcached.impl.KetamaMemcachedSessionLocator;
import net.rubyeye.xmemcached.utils.AddrUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Author: ellios
 * Date: 13-1-29 Time: 上午11:49
 */
abstract public class AbstractMemcachedClient implements MemcachedOp{

    private static final Logger LOG = LoggerFactory.getLogger(AbstractMemcachedClient.class);

    //redis数据库信息
    private volatile Config config = null;

    private volatile MemcachedClient memcachedClient = null;


    private volatile boolean isShutdownHookCalled = false;
    private final Thread shutdownHookThread;


    public AbstractMemcachedClient(Config config) {
        Preconditions.checkNotNull(config);

        refresh(config);

        shutdownHookThread = new Thread(new Runnable() {
            @Override
            public void run() {
                isShutdownHookCalled = true;
                destroy();
            }
        });
        Runtime.getRuntime().addShutdownHook(shutdownHookThread);
    }

    /**
     * 刷新memcached节点
     *
     * @param config
     */
    public void refresh(Config config) {

        if (config == null) {
            LOG.warn("redis config is null, will not refresh.");
            return;
        }
        LOG.info("trying to refresh with redis config : {}", config);
        this.config = config;

        final String address = config.getAddress();
        if (StringUtils.isEmpty(address)) {
            LOG.warn("address is empty, will not refresh");
            return;
        }
        final boolean failover = config.getFailover();
        final boolean textmode = config.getTextmode();
        MemcachedClientBuilder builder = null;
        if (failover) {
            builder = new XMemcachedClientBuilder(AddrUtil.getAddressMap(address));
        } else {
            builder = new XMemcachedClientBuilder(AddrUtil.getAddresses(address));
        }
        builder.setFailureMode(failover);
        if (!textmode) {
            builder.setCommandFactory(new BinaryCommandFactory());
        }
        builder.setSessionLocator(new KetamaMemcachedSessionLocator());
        try {
            memcachedClient = builder.build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 销毁客户端
     */
    public void destroy() {
        try {
            memcachedClient.shutdown();
        } catch (IOException e) {
        }
        //消除hook，如果hook thread没有在运行的话
        if (!isShutdownHookCalled) {
            LOG.info("remove hook thread");
            Runtime.getRuntime().removeShutdownHook(shutdownHookThread);
        }
        LOG.info("finish destroy redis client.");
    }

    protected MemcachedClient getMemcachedClient() {
        return memcachedClient;
    }

}
