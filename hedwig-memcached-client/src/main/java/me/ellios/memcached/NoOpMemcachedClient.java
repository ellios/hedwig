package me.ellios.memcached;

import me.ellios.memcached.config.Config;
import me.ellios.memcached.support.AbstractMemcachedClient;

import java.util.Collection;
import java.util.Map;

/**
 * User: ellios
 * Time: 15-6-29 : 上午11:27
 */
public class NoOpMemcachedClient extends AbstractMemcachedClient {
    public NoOpMemcachedClient(Config config) {
        super(config);
    }

    @Override
    public long decr(String key, long delta) {
        return 0;
    }

    @Override
    public long decr(String key, long delta, long initValue) {
        return 0;
    }

    @Override
    public long decr(String key, long delta, long initValue, long timeout) {
        return 0;
    }

    @Override
    public long decr(String key, long delta, long initValue, long timeout, int exp) {
        return 0;
    }

    @Override
    public boolean delete(String key) {
        return false;
    }

    @Override
    public boolean delete(String key, long opTimeout) {
        return false;
    }

    @Override
    public int delete(Collection<String> keys) {
        return 0;
    }

    @Override
    public <T> Map<String, T> get(Collection<String> keyCollections) {
        return null;
    }

    @Override
    public <T> Map<String, T> get(Collection<String> keyCollections, long timeout) {
        return null;
    }

    @Override
    public <T> T get(String key) {
        return null;
    }

    @Override
    public <T> T get(String key, long timeout) {
        return null;
    }

    @Override
    public long incr(String key, long delta) {
        return 0;
    }

    @Override
    public long incr(String key, long delta, long initValue) {
        return 0;
    }

    @Override
    public long incr(String key, long delta, long initValue, long timeout) {
        return 0;
    }

    @Override
    public long incr(String key, long delta, long initValue, long timeout, int exp) {
        return 0;
    }

    @Override
    public boolean set(String key, int exp, Object value) {
        return false;
    }

    @Override
    public boolean set(String key, int exp, Object value, long timeout) {
        return false;
    }

    @Override
    public boolean add(String key, int exp, Object value) {
        return false;
    }

    @Override
    public boolean add(String key, int exp, Object value, long timeout) {
        return false;
    }

    @Override
    public void addWithNoReply(String key, int exp, Object value) {

    }

    @Override
    public boolean append(String key, Object value) {
        return false;
    }

    @Override
    public boolean append(String key, Object value, long timeout) {
        return false;
    }

    @Override
    public void appendWithNoReply(String key, Object value) {

    }

    @Override
    public boolean cas(String key, int exp, Object value, long cas) {
        return false;
    }

    @Override
    public boolean cas(String key, int exp, Object value, long timeout, long cas) {
        return false;
    }

    @Override
    public void deleteWithNoReply(String key) {

    }

    @Override
    public void decrWithNoReply(String key, long delta) {

    }

    @Override
    public void incrWithNoReply(String key, long delta) {

    }

    @Override
    public boolean prepend(String key, Object value) {
        return false;
    }

    @Override
    public boolean prepend(String key, Object value, long timeout) {
        return false;
    }

    @Override
    public void prependWithNoReply(String key, Object value) {

    }

    @Override
    public boolean replace(String key, int exp, Object value) {
        return false;
    }

    @Override
    public boolean replace(String key, int exp, Object value, long timeout) {
        return false;
    }

    @Override
    public void replaceWithNoReply(String key, int exp, Object value) {

    }

    @Override
    public void setWithNoReply(String key, int exp, Object value) {

    }
}
