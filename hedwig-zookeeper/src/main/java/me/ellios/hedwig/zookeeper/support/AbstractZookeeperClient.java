package me.ellios.hedwig.zookeeper.support;

import me.ellios.hedwig.zookeeper.ChildListener;
import me.ellios.hedwig.zookeeper.DataListener;
import me.ellios.hedwig.zookeeper.StateListener;
import me.ellios.hedwig.zookeeper.ZookeeperClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;

public abstract class AbstractZookeeperClient<TargetListener> implements ZookeeperClient {

    protected static final Logger logger = LoggerFactory.getLogger(AbstractZookeeperClient.class);

    private final String connectString;

    private final Set<StateListener> stateListeners = new CopyOnWriteArraySet<>();

    private final ConcurrentMap<String, ConcurrentMap<ChildListener, TargetListener>> childListeners
            = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, ConcurrentMap<DataListener, TargetListener>> dataListeners
            = new ConcurrentHashMap<>();

    private volatile boolean closed = false;

    public AbstractZookeeperClient(String connectString) {
        this.connectString = connectString;
    }

    public void create(String path, boolean ephemeral) {
        int i = path.lastIndexOf('/');
        if (i > 0) {
            create(path.substring(0, i), false);
        }
        if (ephemeral) {
            createEphemeral(path);
        } else {
            createPersistent(path);
        }
    }

    public void create(String path, boolean ephemeral, byte[] data) {
        int i = path.lastIndexOf('/');
        if (i > 0) {
            create(path.substring(0, i), false);
        }
        if (ephemeral) {
            createEphemeral(path, data);
        } else {
            createPersistent(path, data);
        }
    }

    public void addStateListener(StateListener listener) {
        stateListeners.add(listener);
    }

    @Override
    public Set<StateListener> getStateListeners() {
        return stateListeners;
    }

    public void removeStateListener(StateListener listener) {
        stateListeners.remove(listener);
    }

    public Set<StateListener> getSessionListeners() {
        return stateListeners;
    }

    @Override
    public List<String> addChildListener(String path, final ChildListener listener) {
        ConcurrentMap<ChildListener, TargetListener> listeners = childListeners.get(path);
        if (listeners == null) {
            childListeners.putIfAbsent(path, new ConcurrentHashMap<ChildListener, TargetListener>());
            listeners = childListeners.get(path);
        }
        TargetListener targetListener = listeners.get(listener);
        if (targetListener == null) {
            listeners.putIfAbsent(listener, createTargetChildListener(path, listener));
            targetListener = listeners.get(listener);
        }
        return addTargetChildListener(path, targetListener);
    }


    @Override
    public void removeChildListener(String path, ChildListener listener) {
        ConcurrentMap<ChildListener, TargetListener> listeners = childListeners.get(path);
        if (listeners != null) {
            TargetListener targetListener = listeners.remove(listener);
            if (targetListener != null) {
                removeTargetChildListener(path, targetListener);
            }
        }
    }

    @Override
    public Set<ChildListener> getChildListeners(String path) {
        ConcurrentMap<ChildListener, TargetListener> listeners = childListeners.get(path);
        if (listeners != null) {
            return listeners.keySet();
        }
        return Collections.emptySet();
    }

    @Override
    public void removeDataListener(String path, DataListener listener) {
        ConcurrentMap<DataListener, TargetListener> listeners = dataListeners.get(path);
        if (listeners != null) {
            TargetListener targetListener = listeners.remove(listener);
            if (targetListener != null) {
                removeTargetDataListener(path, targetListener);
            }
        }
    }

    @Override
    public Set<DataListener> getDataListeners(String path) {
        ConcurrentMap<DataListener, TargetListener> listeners = dataListeners.get(path);
        if (null != listeners) {
            return listeners.keySet();
        }
        return Collections.emptySet();
    }

    protected void stateChanged(int state) {
        for (StateListener sessionListener : getSessionListeners()) {
            sessionListener.stateChanged(state);
        }
    }

    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        try {
            doClose();
        } catch (Throwable t) {
            logger.warn(t.getMessage(), t);
        }
    }

    @Override
    public byte[] getData(String path, DataListener listener) {
        ConcurrentMap<DataListener, TargetListener> listeners = dataListeners.get(path);
        if (listeners == null) {
            dataListeners.putIfAbsent(path, new ConcurrentHashMap<DataListener, TargetListener>());
            listeners = dataListeners.get(path);
        }
        TargetListener targetListener = listeners.get(listener);
        if (targetListener == null) {
            listeners.putIfAbsent(listener, createTargetDataListener(path, listener));
            targetListener = listeners.get(listener);
        }
        return addTargetDataListener(path, targetListener);
    }

    protected abstract void doClose();

    protected abstract void createPersistent(String path, byte[] data);

    protected abstract void createEphemeral(String path, byte[] data);

    protected abstract void createPersistent(String path);

    protected abstract void createEphemeral(String path);

    protected abstract TargetListener createTargetChildListener(String path, ChildListener listener);

    protected abstract List<String> addTargetChildListener(String path, TargetListener listener);

    protected abstract void removeTargetChildListener(String path, TargetListener listener);

    protected abstract TargetListener createTargetDataListener(String path, DataListener listener);

    protected abstract byte[] addTargetDataListener(String path, TargetListener listener);

    protected abstract void removeTargetDataListener(String path, TargetListener listener);

}
