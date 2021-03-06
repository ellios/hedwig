package me.ellios.jedis.config.support;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import me.ellios.jedis.config.ConfigFactory;
import me.ellios.jedis.config.impl.RedisPropertiesFileConfigFactory;
import me.ellios.jedis.config.impl.ZookeeperConfigFactory;
import me.ellios.jedis.util.RedisConfigFileParsers;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User: ellios
 * Time: 13-9-24 : 下午4:21
 */
public class ConfigFactoryBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(ConfigFactoryBuilder.class);

    /**
     * 获取配置创建工厂
     *
     * @param name
     * @return
     */
    public static ConfigFactory getConfigFactory(String name) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(name));
        String servers = RedisConfigFileParsers.getRedisServers(name);
        if (StringUtils.isNotEmpty(servers)) {
            //优先从配置文件里面读redis服务信息
            LOG.info("read redis config : {} from : {}", name, RedisConfigFileParsers.getConfigFileAbsolutePath());
            return new RedisPropertiesFileConfigFactory(name);
        }
        LOG.info("read redis config : {} from zookeeper", name);
        //从zookeeper里面读取配置信息
        return new ZookeeperConfigFactory(name);
    }
}
