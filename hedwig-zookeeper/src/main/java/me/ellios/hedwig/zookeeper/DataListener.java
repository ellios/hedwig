package me.ellios.hedwig.zookeeper;

/**
 * 数据监听
 * Author: ellios
 * Date: 13-1-16 Time: 上午11:03
 */
public interface DataListener {

    void dataChanged(String path, byte[] data);
}
