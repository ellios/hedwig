package me.ellios.jedis.support;

import com.google.common.base.Preconditions;
import me.ellios.jedis.config.Config;

/**
 * User: ellios
 * Time: 15-5-29 : 下午9:12
 */
public class JedisContainerFactory {

    public static JedisContainer createContainer(Config config){
        Preconditions.checkNotNull(config);

        switch (config.getServerMode()){
            case MASTER_SLAVE:return new MsJedisContainer(config);
            case SENTINEL:return new SentinelJedisContainer(config);
            case CLUSTER:return new JedisClusterContainer(config);
        }
        return null;
    }
}
