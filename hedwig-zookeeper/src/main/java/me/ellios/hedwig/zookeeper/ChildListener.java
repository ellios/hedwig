package me.ellios.hedwig.zookeeper;

import java.util.List;

public interface ChildListener {

    /**
     * Process child changed event.
     *
     * @param path     the parent path.
     * @param children all the children znode under the parent {@code path}
     */
    void childChanged(String path, List<String> children);
}
