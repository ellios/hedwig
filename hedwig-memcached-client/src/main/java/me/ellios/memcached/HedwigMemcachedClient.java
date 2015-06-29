package me.ellios.memcached;

import me.ellios.hedwig.common.exceptions.HedwigException;
import me.ellios.memcached.config.Config;
import me.ellios.memcached.exception.HedwigMemcachedException;
import me.ellios.memcached.support.AbstractMemcachedClient;
import net.rubyeye.xmemcached.exception.MemcachedException;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * User: ellios
 * Time: 15-6-28 : 下午11:31
 */
public class HedwigMemcachedClient extends AbstractMemcachedClient{

    private long timeout = 1000L;

    public HedwigMemcachedClient(Config config) {
        super(config);
    }

    @Override
    public long decr(String key, long delta) {
        return decr(key, delta, 0);
    }

    @Override
    public long decr(String key, long delta, long initValue) {
        return decr(key, delta, initValue, getTimeout());
    }

    @Override
    public long decr(String key, long delta, long initValue, long timeout) {
        return decr(key, delta, initValue, timeout, 0);
    }

    @Override
    public boolean delete(String key) {
        return delete(key, getTimeout());
    }

    @Override
    public void destroy() {
        //ignore
    }

    @Override
    public <T> Map<String, T> get(Collection<String> keyCollections) {
        return get(keyCollections, getTimeout());
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(String key) {
        return (T) get(key, getTimeout());
    }

    public long getTimeout() {
        return timeout;
    }

    @Override
    public long incr(String key, long delta) {
        return incr(key, delta, 0);
    }

    @Override
    public long incr(String key, long delta, long initValue) {
        return incr(key, delta, initValue, getTimeout());
    }

    @Override
    public long incr(String key, long delta, long initValue, long timeout) {
        return incr(key, delta, initValue, timeout, 0);
    }

    @Override
    public boolean set(String key, int exp, Object value) {
        return set(key, exp, value, getTimeout());
    }

    @Override
    public long decr(String key, long delta, long initValue, long timeout, int exp) {
        try {
            return getMemcachedClient().decr(key, delta, initValue, timeout, exp);
        } catch (TimeoutException|InterruptedException|MemcachedException e) {
            throw new HedwigMemcachedException(e);
        }
    }

    @Override
    public int delete(Collection<String> keys) {
        int cnt = 0;
        for (String key : keys) {
            if (delete(key, getTimeout())) {
                cnt++;
            }
        }
        return cnt;
    }

    @Override
    public boolean delete(String key, long opTimeout) {
        try {
            return getMemcachedClient().delete(key, opTimeout);
        } catch (TimeoutException|InterruptedException|MemcachedException e) {
            throw new HedwigMemcachedException(e);
        }
    }

    @Override
    public void decrWithNoReply(String key, long delta) {
        try {
            getMemcachedClient().decrWithNoReply(key, delta);
        } catch (InterruptedException|MemcachedException e) {
            throw new HedwigMemcachedException(e);
        }
    }

    @Override
    public void deleteWithNoReply(String key) {
        try {
            getMemcachedClient().deleteWithNoReply(key);
        }  catch (InterruptedException|MemcachedException e) {
            throw new HedwigMemcachedException(e);
        }
    }

    @Override
    public <T> Map<String, T> get(Collection<String> keyCollections, long timeout) {
        try {
            return getMemcachedClient().get(keyCollections, timeout);
        } catch (TimeoutException|InterruptedException|MemcachedException e) {
            throw new HedwigMemcachedException(e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(String key, long timeout) {
        try {
            return (T) getMemcachedClient().get(key, timeout);
        }  catch (TimeoutException|InterruptedException|MemcachedException e) {
            throw new HedwigMemcachedException(e);
        }
    }

    @Override
    public long incr(String key, long delta, long initValue, long timeout, int exp) {
        try {
            return getMemcachedClient().incr(key, delta, initValue, timeout, exp);
        }  catch (TimeoutException|InterruptedException|MemcachedException e) {
            throw new HedwigMemcachedException(e);
        }
    }

    @Override
    public void incrWithNoReply(String key, long delta) {
        try {
            getMemcachedClient().incrWithNoReply(key, delta);
        }  catch (InterruptedException|MemcachedException e) {
            throw new HedwigMemcachedException(e);
        }
    }

    @Override
    public boolean set(String key, int exp, Object value, long timeout) {
        try {
            return getMemcachedClient().set(key, exp, value, timeout);
        }  catch (TimeoutException|InterruptedException|MemcachedException e) {
            throw new HedwigMemcachedException(e);
        }
    }

    @Override
    public boolean add(String key, int exp, Object value) {
        return add(key, exp, value, getTimeout());
    }

    @Override
    public boolean add(String key, int exp, Object value, long timeout) {
        try {
            return getMemcachedClient().add(key, exp, value, timeout);
        }  catch (TimeoutException|InterruptedException|MemcachedException e) {
            throw new HedwigMemcachedException(e);
        }
    }

    @Override
    public void addWithNoReply(String key, int exp, Object value) {
        try {
            getMemcachedClient().addWithNoReply(key, exp, value);
        }  catch (InterruptedException|MemcachedException e) {
            throw new HedwigMemcachedException(e);
        }
    }

    @Override
    public boolean append(String key, Object value) {
        return append(key, value, getTimeout());
    }

    @Override
    public boolean append(String key, Object value, long timeout) {
        try {
            return getMemcachedClient().append(key, value, timeout);
        }  catch (TimeoutException|InterruptedException|MemcachedException e) {
            throw new HedwigMemcachedException(e);
        }
    }

    @Override
    public void appendWithNoReply(String key, Object value) {
        try {
            getMemcachedClient().appendWithNoReply(key, value);
        }  catch (InterruptedException|MemcachedException e) {
            throw new HedwigMemcachedException(e);
        }
    }

    @Override
    public boolean cas(String key, int exp, Object value, long cas) {
        return cas(key, exp, value, getTimeout(), cas);
    }

    @Override
    public boolean cas(String key, int exp, Object value, long timeout, long cas) {
        try {
            return getMemcachedClient().cas(key, exp, value, timeout, cas);
        } catch (TimeoutException|InterruptedException|MemcachedException e) {
            throw new HedwigMemcachedException(e);
        }
    }

    @Override
    public boolean prepend(String key, Object value) {
        return prepend(key, value, getTimeout());
    }

    @Override
    public boolean prepend(String key, Object value, long timeout) {
        try {
            return getMemcachedClient().prepend(key, value, timeout);
        } catch (TimeoutException | InterruptedException | MemcachedException e) {
            throw new HedwigMemcachedException(e);
        }
    }

    @Override
    public void prependWithNoReply(String key, Object value) {
        try {
            getMemcachedClient().prependWithNoReply(key, value);
        } catch (InterruptedException|MemcachedException e) {
            throw new HedwigMemcachedException(e);
        }
    }

    @Override
    public boolean replace(String key, int exp, Object value) {
        return replace(key, exp, value, getTimeout());
    }

    @Override
    public boolean replace(String key, int exp, Object value, long timeout) {
        try {
            return getMemcachedClient().replace(key, exp, value, timeout);
        } catch (TimeoutException|InterruptedException|MemcachedException e) {
            throw new HedwigMemcachedException(e);
        }
    }

    @Override
    public void replaceWithNoReply(String key, int exp, Object value) {
        try {
            getMemcachedClient().replaceWithNoReply(key, exp, value);
        } catch (InterruptedException|MemcachedException e) {
            throw new HedwigMemcachedException(e);
        }
    }

    @Override
    public void setWithNoReply(String key, int exp, Object value) {
        try {
            getMemcachedClient().setWithNoReply(key, exp, value);
        } catch (InterruptedException|MemcachedException e) {
            throw new HedwigMemcachedException(e);
        }
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }
}
