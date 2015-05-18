package me.ellios.hedwig.zookeeper;

import org.apache.zookeeper.data.ACL;

import java.util.List;
import java.util.Set;

/**
 * Zookeeper客户端
 * Author: ellios
 * Date: 13-1-16 Time: 上午11:03
 */
public interface ZookeeperClient {

    /**
     * 创建节点
     *
     * @param path
     * @param ephemeral
     */
    void create(String path, boolean ephemeral);

    /**
     * 创建节点并添加数据
     *
     * @param path
     * @param ephemeral
     * @param data
     */
    void create(String path, boolean ephemeral, byte[] data);

    /**
     * 删除节点
     *
     * @param path
     */
    void delete(String path);

    /**
     * 获取该节点下的所有子节点
     *
     * @param path
     * @return
     */
    List<String> getChildren(String path);

    /**
     * 添加子节点监听器
     *
     * @param path
     * @param listener
     * @return
     */
    List<String> addChildListener(String path, ChildListener listener);

    /**
     * 益处子节点监听器
     *
     * @param path
     * @param listener
     */
    void removeChildListener(String path, ChildListener listener);

    Set<ChildListener> getChildListeners(String path);

    /**
     * 添加状态监听器
     *
     * @param listener
     */
    void addStateListener(StateListener listener);

    Set<StateListener> getStateListeners();

    /**
     * 移除状态监听器
     *
     * @param listener
     */
    void removeStateListener(StateListener listener);


    /**
     * 是否和zookeeper连接
     *
     * @return
     */
    boolean isConnected();

    /**
     * 关闭连接
     */
    void close();

    /**
     * 获取数据
     *
     * @param path
     * @return
     */
    byte[] getData(String path);

    /**
     * 获取数据并添加监听器
     *
     * @param path
     * @param listener
     * @return
     */
    byte[] getData(String path, DataListener listener);

    /**
     * 向节点写数据
     *
     * @param path
     * @param data
     */
    void setData(String path, byte[] data);

    /**
     * 设置ACL
     *
     * @param path    the path
     * @param aclList th acl list
     */
    public void withACL(String path, List<ACL> aclList);

    /**
     * 移除数据监听器
     *
     * @param path
     * @param listener
     */
    void removeDataListener(String path, DataListener listener);

    /**
     * Get all data listeners, in case you want to remove them.
     *
     * @param path the znode path
     * @return a set of {@link DataListener}
     */
    Set<DataListener> getDataListeners(String path);

    /**
     * 同步数据
     *
     * @param path
     */
    void sync(String path);

    /**
     * 节点是否存在
     *
     * @param path
     * @return
     */
    boolean exist(String path);

}
