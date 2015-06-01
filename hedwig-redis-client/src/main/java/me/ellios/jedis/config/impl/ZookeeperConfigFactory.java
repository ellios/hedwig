package me.ellios.jedis.config.impl;

import me.ellios.hedwig.zookeeper.DataListener;
import me.ellios.hedwig.zookeeper.ZooType;
import me.ellios.hedwig.zookeeper.ZookeeperClient;
import me.ellios.hedwig.zookeeper.ZookeeperClientFactory;
import me.ellios.jedis.config.ConfigListener;
import me.ellios.jedis.config.support.BaseConfigFactory;
import me.ellios.jedis.util.RedisZooPath;

/**
 * User: ellios
 * Time: 13-9-24 : 下午4:15
 */
public class ZookeeperConfigFactory extends BaseConfigFactory {

    private static final ZookeeperClient zkClient = ZookeeperClientFactory.getZookeeperClient(ZooType.REDIS);

    public ZookeeperConfigFactory(String configName) {
        super(configName);
    }

    @Override
    protected String doGetServers(String configName) {
        //获取数据，并监听数据变化
        byte[] data = zkClient.getData(RedisZooPath.getRedisZooPath(getConfigName()));

        if(data == null || data.length <= 0){
            throw new IllegalStateException("redis : " + getConfigName() + " not config in zookeeper.");
        }
        return new String(data);
    }

    @Override
    protected void doAttachChangeListener(final ConfigListener listener) {
        zkClient.getData(RedisZooPath.getRedisZooPath(getConfigName()), new DataListener() {
            @Override
            public void dataChanged(String path, byte[] data) {
                if(data == null || data.length <= 0){
                    LOG.warn("redis : {} not config in zookeeper.", getConfigName());
                    return;
                }

                refreshServers(getConfigName());
                listener.onChange(getConfig());
                LOG.info("finish refreshing config from zookeeper. configName : {}", getConfigName());
            }
        });
    }

}
