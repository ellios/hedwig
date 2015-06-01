package me.ellios.jedis.config;

/**
 * User: ellios
 * Time: 13-9-24 : 下午3:09
 */
public interface ConfigListener {

    void onChange(Config config);
}
