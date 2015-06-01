package me.ellios.jedis.config;

/**
 * User: ellios
 * Time: 13-9-24 : 下午3:00
 */
public interface ConfigFactory {

    /**
     * 获取配置内容
     *
     * @return
     */
    Config getConfig();

    /**
     * 附加配置变化监听
     *
     * @param listener
     */
    void attachChangeListener(ConfigListener listener);
}


