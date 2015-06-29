package me.ellios.memcached;

import me.ellios.memcached.config.Config;

import java.util.Collection;
import java.util.Map;

/**
 * User: ellios
 * Time: 15-6-28 : 下午11:31
 */
public interface MemcachedOp {


    long decr(String key, long delta);


    long decr(String key, long delta, long initValue);


    long decr(String key, long delta, long initValue, long timeout);


    long decr(String key, long delta, long initValue, long timeout, int exp);


    boolean delete(String key);


    boolean delete(String key, long opTimeout);


    int delete(Collection<String> keys);


    <T> Map<String, T> get(Collection<String> keyCollections);


    <T> Map<String, T> get(Collection<String> keyCollections, long timeout);


    <T> T get(String key);


    <T> T get(String key, long timeout);


    long incr(String key, long delta);


    long incr(String key, long delta, long initValue);


    long incr(String key, long delta, long initValue, long timeout);


    long incr(String key, long delta, long initValue, long timeout, int exp);


    boolean set(String key, int exp, Object value);


    boolean set(String key, int exp, Object value, long timeout);

    boolean add(final String key, final int exp, final Object value);

    boolean add(final String key, final int exp, final Object value, final long timeout);

    void addWithNoReply(final String key, final int exp, final Object value);

    boolean append(final String key, final Object value);

    boolean append(final String key, final Object value, final long timeout);

    void appendWithNoReply(final String key, final Object value);

    boolean cas(final String key, final int exp, final Object value, final long cas);

    boolean cas(final String key, final int exp, final Object value, final long timeout, final long cas);

    void deleteWithNoReply(final String key);

    void decrWithNoReply(final String key, final long delta);

    void incrWithNoReply(final String key, final long delta);

    boolean prepend(final String key, final Object value);

    boolean prepend(final String key, final Object value, final long timeout);

    void prependWithNoReply(final String key, final Object value);

    boolean replace(final String key, final int exp, final Object value);

    boolean replace(final String key, final int exp, final Object value, final long timeout);

    void replaceWithNoReply(final String key, final int exp, final Object value);

    void setWithNoReply(final String key, final int exp, final Object value);

    void refresh(Config config);

}
