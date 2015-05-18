package me.ellios.hedwig.registry;

import me.ellios.hedwig.rpc.core.ServiceNode;

import java.util.List;

public interface CallbackWatcher {

    /**
     * 当收到服务变更通知时触发。
     */
    void doCallback(List<ServiceNode> urls);

}