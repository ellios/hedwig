package me.ellios.jedis;

import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.Tuple;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * redis的命令接口
 * Author: ellios
 * Date: 13-1-29 Time: 下午9:09
 */
public interface RedisOp{

    Object getObject(String key);

    /**
     * Get the value of the specified key
     *
     * @param key
     * @return
     */
    String get(String key);

    /**
     * Get the value of the specified key
     *
     * @param key
     * @return
     */
    byte[] getBytes(String key);

    /**
     * hget
     *
     * @param key               i
     * @param field
     * @return
     */
    String hget(String key, String field);

    /**
     * Set the bytes value as value of the key
     *
     * @param key
     * @param data
     * @return
     */
    Boolean set(String key, byte[] data);

    Boolean hmset(String key, Map map);

    Long hset(String key, String field, String value);

    List<String> hmget(String key, String... fileds);

    /**
     * Set the bytes value as value of the key with expire time
     *
     * @param key
     * @param data
     * @param exp  0 means no expire time. above zero is expire time. unit
     *             second
     * @return
     */
    Boolean set(String key, byte[] data, int exp);

    /**
     * Remove the specified keys.
     *
     * @param key
     * @return
     */
    Boolean del(String key);

    /**
     * Add the specified member to the set value stored at key
     *
     * @param key
     * @param members
     * @return
     */
    Long sadd(String key, String... members);

    Boolean setObject(String key, Object data, int exp);

    /**
     * Return all the members (elements) of the set value stored at key
     *
     * @param key
     * @return
     */
    Set<String> smembers(String key);

    /**
     * Set the the respective keys to the respective values.
     *
     * @param keyDatas
     * @return
     */
    Boolean mset(Map<String, byte[]> keyDatas);

    /**
     * Get the values of all the specified keys.
     *
     * @param keys
     * @return
     */
    Map<String, byte[]> mget(Collection<String> keys);

    /**
     * left add
     *
     * @param key
     * @param members
     * @return
     */
    Long lpush(String key, String members);

    Map<String, String> hgetAll(String key);

    /**
     * expire time
     *
     * @param key
     * @param seconds
     * @return
     */
    Long expire(String key, int seconds);

    /**
     * rerurn size
     *
     * @param key
     * @return
     */
    Long llen(String key);

    /**
     * return right
     *
     * @param key
     * @return
     */
    String rpop(String key);

    /**
     * Set the string value as value of the key
     *
     * @param key
     * @param data
     * @return
     */
    Boolean setnx(String key, String data);

    /**
     * Return the set cardinality (number of elements). If the key does not
     * exist 0 is returned, like for empty sets.
     *
     * @param key
     * @return
     */
    Long scard(String key);

    /**
     * Remove the specified member from the set value stored at key. If member
     * was not a member of the set no operation is performed. If key does not
     * hold a set value an error is returned.
     * <p/>
     * Time complexity O(1)
     *
     * @param key
     * @param members
     * @return
     */
    Long srem(String key, String... members);

    /**
     * Trim an existing list so that it will contain only the specified range of elements specified.
     * Start and end are zero-based indexes. 0 is the first element of the list (the list head), 1 the
     * next element and so on.
     * <p>
     * For example LTRIM foobar 0 2 will modify the list stored at foobar key so that only the first
     * three elements of the list will remain.
     * <p>
     * start and end can also be negative numbers indicating offsets from the end of the list. For
     * example -1 is the last element of the list, -2 the penultimate element and so on.
     * <p>
     * Indexes out of range will not produce an error: if start is over the end of the list, or start
     * > end, an empty list is left as value. If end over the end of the list Redis will threat it
     * just like the last element of the list.
     * <p>
     * Hint: the obvious use of LTRIM is together with LPUSH/RPUSH. For example:
     * <p>
     * {@code lpush("mylist", "someelement"); ltrim("mylist", 0, 99); * }
     * <p>
     * The above two commands will push elements in the list taking care that the list will not grow
     * without limits. This is very useful when using Redis to store logs for example. It is important
     * to note that when used in this way LTRIM is an O(1) operation because in the average case just
     * one element is removed from the tail of the list.
     * <p>
     * Time complexity: O(n) (with n being len of list - len of range)
     * @param key
     * @param start
     * @param end
     * @return true: success false:fail
     */
    Boolean ltrim(String key, int start, int end);

    /**
     * @param key
     * @return
     */
    Boolean exists(String key);

    /**
     * @param key
     * @return
     */
    Long incr(String key);

    /**
     * @param key
     * @param step
     * @return
     */
    Long incrBy(String key, long step);

    Boolean zadd(String key, double score, String member);

    Long zadd(String key, Map<String, Double> scoreMembers);

    Set<String> zrange(String key, long start, long end);

    Set<String> zrangeByScore(String key, double min, double max);

    Set<String> zrangeByScore(String key, double min, double max, int offset, int count);

    Set<Tuple> zrangeByScoreWithScores(String key, double min, double max, int offset, int count);

    Long zcard(String key);

    Long zrem(String key, String... members);

    List<String> lrange(String key, long start, long end);

    /**
     * 发布命令到channel
     * @param channel
     * @param message
     * @return
     */
    Long publish(String channel, String message);

    /**
     * 订阅channel
     * @param jedisPubSub
     * @param channels
     * @return
     */
    Boolean subscribe(JedisPubSub jedisPubSub, String... channels);

    /**
     * 按pattern订阅
     * @param jedisPubSub
     * @param patterns
     * @return
     */
    Boolean psubscribe(JedisPubSub jedisPubSub, String... patterns);

}
