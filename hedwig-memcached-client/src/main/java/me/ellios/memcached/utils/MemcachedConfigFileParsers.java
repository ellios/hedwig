package me.ellios.memcached.utils;

import me.ellios.hedwig.common.config.HedwigConfig;
import me.ellios.hedwig.common.utils.ClassLoaderUtils;

import java.net.URL;

/**
 * User: ellios
 * Time: 13-9-25 : 上午10:17
 */
public class MemcachedConfigFileParsers {

    private static final HedwigConfig HEDWIG_CONFIG = HedwigConfig.getInstance();

    private static final String MEMCACHED_SERVERS_KEY_PREFIX = "memcached.servers.";


    public static String getMemcachedServers(String name) {
        return HEDWIG_CONFIG.getString(MEMCACHED_SERVERS_KEY_PREFIX + name, "");
    }


    /**
     * 获取配置文件的绝对路径
     *
     * @return
     */
    public static String getConfigFileAbsolutePath() {

        String classPathFilename = "properties/hedwig.properties";
        URL url = ClassLoaderUtils.getURL(classPathFilename);
        if (url == null) {
            return null;
        }
        return url.getPath();
    }
}
