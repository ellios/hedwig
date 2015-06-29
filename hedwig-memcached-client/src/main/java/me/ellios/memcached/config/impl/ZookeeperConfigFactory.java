package me.ellios.memcached.config.impl;

import me.ellios.hedwig.zookeeper.DataListener;
import me.ellios.hedwig.zookeeper.ZooType;
import me.ellios.hedwig.zookeeper.ZookeeperClient;
import me.ellios.hedwig.zookeeper.ZookeeperClientFactory;
import me.ellios.memcached.config.ConfigListener;
import me.ellios.memcached.config.support.BaseConfigFactory;
import me.ellios.memcached.utils.ZooPath;

/**
 * User: ellios
 * Time: 13-9-24 : 下午4:15
 */
public class ZookeeperConfigFactory extends BaseConfigFactory {

    private static final ZookeeperClient zkClient = ZookeeperClientFactory.getZookeeperClient(ZooType.MEMCACHED);

    public ZookeeperConfigFactory(String configName) {
        super(configName);
    }

    @Override
    protected String doGetServers(String configName) {
        //获取数据，并监听数据变化
        byte[] data = zkClient.getData(ZooPath.getZooPath(getConfigName()));

        if (data == null || data.length <= 0) {
            throw new IllegalStateException("memcached : " + getConfigName() + " not config in zookeeper.");
        }
        return new String(data);
    }

    @Override
    protected void doAttachChangeListener(final ConfigListener listener) {
        zkClient.getData(ZooPath.getZooPath(getConfigName()), new DataListener() {
            @Override
            public void dataChanged(String path, byte[] data) {
                if (data == null || data.length <= 0) {
                    LOG.warn("memcached : {} not config in zookeeper.", getConfigName());
                    return;
                }

                refreshServers(getConfigName());
                listener.onChange(getConfig());
                LOG.info("finish refreshing config from zookeeper. configName : {}", getConfigName());
            }
        });
    }

}
