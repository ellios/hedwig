package me.ellios.hedwig.registry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Say something?
 *
 * @author George Cao(caozhangzhi@iqiyi.com)
 * @since 5/21/13 10:49 AM
 */
public class ServiceImpl implements ServiceFace {
    private static final Logger LOG = LoggerFactory.getLogger(ServiceImpl.class);

    @Override
    public void ping(String pong) {
        LOG.info("{}", pong);
    }
}
