package me.ellios.jedis;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import me.ellios.jedis.config.Config;
import me.ellios.jedis.config.ServerMode;
import me.ellios.jedis.support.AbstractRedisClient;
import me.ellios.jedis.transcoders.CachedData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.Protocol;
import redis.clients.jedis.Tuple;
import redis.clients.util.SafeEncoder;

import java.util.*;

import static me.ellios.jedis.OpType.READ;
import static me.ellios.jedis.OpType.WRITE;

/**
 * Author: ellios
 * Date: 13-1-29 Time: 下午9:22
 */
public class RedisClient extends AbstractRedisClient implements RedisOp {

    private static final Logger LOG = LoggerFactory.getLogger(RedisClient.class);

    public RedisClient(Config config) {
        super(config);
    }

    @Override
    public byte[] getBytes(final String key) {
        if (getServerMode() == ServerMode.CLUSTER) {
            return executeWithJedisCluster(new JedisClusterCallback<byte[]>() {
                @Override
                public byte[] doWithJedisCluster(JedisCluster cluster) {
                    return cluster.getBytes(key);
                }
            });
        } else {
            return executeWithJedis(READ, new JedisCallback<byte[]>() {
                @Override
                public byte[] doWithJedis(Jedis jedis) {
                    return jedis.get(SafeEncoder.encode(key));
                }
            });
        }
    }

    @Override
    public Object getObject(final String key) {
        if (getServerMode() == ServerMode.CLUSTER) {
            return executeWithJedisCluster(new JedisClusterCallback<Object>() {
                @Override
                public Object doWithJedisCluster(JedisCluster cluster) {
                    return transcoder.decode(new CachedData(cluster.getBytes(key)));
                }
            });
        } else {
            return executeWithJedis(READ, new JedisCallback<Object>() {
                @Override
                public Object doWithJedis(Jedis jedis) {
                    byte[] data = jedis.get(SafeEncoder.encode(key));
                    if(data == null || data.length <= 0){
                        return null;
                    }
                    return transcoder.decode(new CachedData(data));
                }
            });
        }
    }

    @Override
    public String get(final String key) {
        if (getServerMode() == ServerMode.CLUSTER) {
            return executeWithJedisCluster(new JedisClusterCallback<String>() {
                @Override
                public String doWithJedisCluster(JedisCluster cluster) {
                    return cluster.get(key);
                }
            });
        } else {
            return executeWithJedis(READ, new JedisCallback<String>() {
                @Override
                public String doWithJedis(Jedis jedis) {
                    return jedis.get(key);
                }
            });
        }

    }

    @Override
    public String hget(final String key, final String field) {
        if (getServerMode() == ServerMode.CLUSTER) {
            return executeWithJedisCluster(new JedisClusterCallback<String>() {
                @Override
                public String doWithJedisCluster(JedisCluster cluster) {
                    return cluster.hget(key, field);
                }
            });
        } else {
            return executeWithJedis(READ, new JedisCallback<String>() {
                @Override
                public String doWithJedis(Jedis jedis) {
                    return jedis.hget(key, field);
                }
            });
        }
    }

    @Override
    public Boolean del(final String key) {
        if (getServerMode() == ServerMode.CLUSTER) {
            return executeWithJedisCluster(new JedisClusterCallback<Boolean>() {
                @Override
                public Boolean doWithJedisCluster(JedisCluster cluster) {
                    return cluster.del(key) >= 0;
                }
            });
        } else {
            return executeWithJedis(WRITE, new JedisCallback<Boolean>() {
                @Override
                public Boolean doWithJedis(Jedis jedis) {
                    LOG.info("begin to delete key : {} from : {}:{}", key, jedis.getClient().getHost(), jedis.getClient().getPort());
                    return jedis.del(SafeEncoder.encode(key)) >= 0;
                }
            });
        }
    }

    @Override
    public Boolean set(final String key, final byte[] data) {
        if (getServerMode() == ServerMode.CLUSTER) {
            return executeWithJedisCluster(new JedisClusterCallback<Boolean>() {
                @Override
                public Boolean doWithJedisCluster(JedisCluster cluster) {
                    String status = cluster.setBytes(key, data);

                    return RedisReply.OK.equalsIgnoreCase(status);
                }
            });
        } else {
            return executeWithJedis(WRITE, new JedisCallback<Boolean>() {
                @Override
                public Boolean doWithJedis(Jedis jedis) {
                    String status = jedis.set(SafeEncoder.encode(key), data);

                    return RedisReply.OK.equalsIgnoreCase(status);
                }
            });
        }
    }

    @Override
    public Boolean hmset(final String key, final Map map) {
        if (getServerMode() == ServerMode.CLUSTER) {
            return executeWithJedisCluster(new JedisClusterCallback<Boolean>() {
                @Override
                public Boolean doWithJedisCluster(JedisCluster cluster) {
                    String status = cluster.hmset(key, map);

                    return RedisReply.OK.equalsIgnoreCase(status);
                }
            });
        } else {
            return executeWithJedis(WRITE, new JedisCallback<Boolean>() {
                @Override
                public Boolean doWithJedis(Jedis jedis) {
                    String status = jedis.hmset(key, map);

                    return RedisReply.OK.equalsIgnoreCase(status);
                }
            });
        }
    }


    @Override
    public Long hset(final String key, final String field, final String value) {
        if (getServerMode() == ServerMode.CLUSTER) {
            return executeWithJedisCluster(new JedisClusterCallback<Long>() {
                @Override
                public Long doWithJedisCluster(JedisCluster cluster) {
                    return cluster.hset(key, field, value);
                }
            });
        } else {
            return executeWithJedis(WRITE, new JedisCallback<Long>() {
                @Override
                public Long doWithJedis(Jedis jedis) {
                    return jedis.hset(key, field, value);
                }
            });
        }
    }

    @Override
    public List<String> hmget(final String key, final String... fileds) {
        if (getServerMode() == ServerMode.CLUSTER) {
            return executeWithJedisCluster(new JedisClusterCallback<List<String>>() {
                @Override
                public List<String> doWithJedisCluster(JedisCluster cluster) {
                    return cluster.hmget(key, fileds);
                }
            });
        } else {
            return executeWithJedis(READ, new JedisCallback<List<String>>() {
                @Override
                public List<String> doWithJedis(Jedis jedis) {
                    return jedis.hmget(key, fileds);
                }
            });
        }
    }

    @Override
    public Boolean set(final String key, final byte[] data, final int exp) {
        if (getServerMode() == ServerMode.CLUSTER) {
            return executeWithJedisCluster(new JedisClusterCallback<Boolean>() {
                @Override
                public Boolean doWithJedisCluster(JedisCluster cluster) {
                    String status = "";
                    if (exp > 0) {
                        status = cluster.setexBytes(key, exp, data);
                    } else {
                        status = cluster.setBytes(key, data);
                    }
                    return Protocol.Keyword.OK.name().equalsIgnoreCase(status);
                }
            });
        } else {
            return executeWithJedis(WRITE, new JedisCallback<Boolean>() {
                @Override
                public Boolean doWithJedis(Jedis jedis) {
                    String status = "";
                    if (exp > 0) {
                        status = jedis.setex(SafeEncoder.encode(key), exp, data);
                    } else {
                        status = jedis.set(SafeEncoder.encode(key), data);
                    }
                    return Protocol.Keyword.OK.name().equalsIgnoreCase(status);
                }
            });
        }
    }

    @Override
    public Boolean setObject(final String key, final Object data, final int exp) {
        if (getServerMode() == ServerMode.CLUSTER) {
            return executeWithJedisCluster(new JedisClusterCallback<Boolean>() {
                @Override
                public Boolean doWithJedisCluster(JedisCluster cluster) {
                    String status = "";
                    if (exp > 0) {
                        status = cluster.setexBytes(key, exp, transcoder.encode(data).getFullData());
                    } else {
                        status = cluster.setBytes(key, transcoder.encode(data).getFullData());
                    }
                    return Protocol.Keyword.OK.name().equalsIgnoreCase(status);
                }
            });
        } else {
            return executeWithJedis(WRITE, new JedisCallback<Boolean>() {
                @Override
                public Boolean doWithJedis(Jedis jedis) {
                    String status = "";
                    if (exp > 0) {
                        status = jedis.setex(SafeEncoder.encode(key), exp, transcoder.encode(data).getFullData());
                    } else {
                        status = jedis.set(SafeEncoder.encode(key), transcoder.encode(data).getFullData());
                    }
                    return Protocol.Keyword.OK.name().equalsIgnoreCase(status);
                }
            });
        }
    }

    @Override
    public Set<String> smembers(final String key) {
        if (getServerMode() == ServerMode.CLUSTER) {
            return executeWithJedisCluster(new JedisClusterCallback<Set<String>>() {
                @Override
                public Set<String> doWithJedisCluster(JedisCluster cluster) {
                    return cluster.smembers(key);
                }
            });
        } else {
            return executeWithJedis(READ, new JedisCallback<Set<String>>() {
                @Override
                public Set<String> doWithJedis(Jedis jedis) {
                    return jedis.smembers(key);
                }
            });
        }
    }

    @Override
    public Long sadd(final String key, final String... members) {
        if (getServerMode() == ServerMode.CLUSTER) {
            return executeWithJedisCluster(new JedisClusterCallback<Long>() {
                @Override
                public Long doWithJedisCluster(JedisCluster cluster) {
                    return cluster.sadd(key, members);
                }
            });
        } else {
            return executeWithJedis(WRITE, new JedisCallback<Long>() {
                @Override
                public Long doWithJedis(Jedis jedis) {
                    return jedis.sadd(key, members);
                }
            });
        }
    }

    @Override
    public Long srem(final String key, final String... members) {
        if (getServerMode() == ServerMode.CLUSTER) {
            return executeWithJedisCluster(new JedisClusterCallback<Long>() {
                @Override
                public Long doWithJedisCluster(JedisCluster cluster) {
                    return cluster.srem(key, members);
                }
            });
        } else {
            return executeWithJedis(WRITE, new JedisCallback<Long>() {
                @Override
                public Long doWithJedis(Jedis jedis) {
                    return jedis.srem(key, members);
                }
            });
        }
    }

    @Override
    public Long scard(final String key) {
        if (getServerMode() == ServerMode.CLUSTER) {
            return executeWithJedisCluster(new JedisClusterCallback<Long>() {
                @Override
                public Long doWithJedisCluster(JedisCluster cluster) {
                    return cluster.scard(key);
                }
            });
        } else {
            return executeWithJedis(READ, new JedisCallback<Long>() {
                @Override
                public Long doWithJedis(Jedis jedis) {
                    return jedis.scard(key);
                }
            });
        }
    }

    @Override
    public Boolean mset(final Map<String, byte[]> keyDatas) {
        if (getServerMode() == ServerMode.CLUSTER) {
            throw new UnsupportedOperationException();
        } else {
            return executeWithJedis(WRITE, new JedisCallback<Boolean>() {
                @Override
                public Boolean doWithJedis(Jedis jedis) {
                    String replyCode = jedis.mset(getKeysValuesByMap(keyDatas));
                    if (!RedisReply.OK.equalsIgnoreCase(replyCode)) {
                        return false;
                    }
                    return true;
                }
            });
        }
    }

    @Override
    public Map<String, byte[]> mget(final Collection<String> keys) {
        if (getServerMode() == ServerMode.CLUSTER) {
            throw new UnsupportedOperationException();
        } else {
            return executeWithJedis(READ, new JedisCallback<Map<String, byte[]>>() {
                @Override
                public Map<String, byte[]> doWithJedis(Jedis jedis) {
                    List<byte[]> keyBytes = Lists.transform(new ArrayList<String>(keys), new Function<String, byte[]>() {
                        @Override
                        public byte[] apply(String input) {
                            return SafeEncoder.encode(input);
                        }
                    });
                    List<byte[]> datas = jedis.mget(keyBytes.toArray(new byte[][]{}));
                    if (datas == null || datas.isEmpty()) {
                        return null;
                    }
                    Map<String, byte[]> result = new HashMap<>();
                    for (int i = 0; i < keyBytes.size(); i++) {
                        byte[] data = datas.get(i);
                        if (data != null) {
                            result.put(SafeEncoder.encode(keyBytes.get(i)), data);
                        }
                    }
                    return result;
                }
            });
        }
    }

    @Override
    public Long lpush(final String key, final String members) {
        if (getServerMode() == ServerMode.CLUSTER) {
            return executeWithJedisCluster(new JedisClusterCallback<Long>() {
                @Override
                public Long doWithJedisCluster(JedisCluster cluster) {
                    return cluster.lpush(key, members);
                }
            });
        } else {
            return executeWithJedis(WRITE, new JedisCallback<Long>() {
                @Override
                public Long doWithJedis(Jedis jedis) {
                    return jedis.lpush(key, members);
                }
            });
        }
    }

    @Override
    public Boolean exists(final String key) {
        if (getServerMode() == ServerMode.CLUSTER) {
            return executeWithJedisCluster(new JedisClusterCallback<Boolean>() {
                @Override
                public Boolean doWithJedisCluster(JedisCluster cluster) {
                    return cluster.exists(key);
                }
            });
        } else {
            return executeWithJedis(READ, new JedisCallback<Boolean>() {
                @Override
                public Boolean doWithJedis(Jedis jedis) {
                    return jedis.exists(key);
                }
            });
        }
    }

    @Override
    public Long incr(final String key) {
        if (getServerMode() == ServerMode.CLUSTER) {
            return executeWithJedisCluster(new JedisClusterCallback<Long>() {
                @Override
                public Long doWithJedisCluster(JedisCluster cluster) {
                    return cluster.incr(key);
                }
            });
        } else {
            return executeWithJedis(WRITE, new JedisCallback<Long>() {
                @Override
                public Long doWithJedis(Jedis jedis) {
                    return jedis.incr(key);
                }
            });
        }
    }

    @Override
    public Boolean zadd(final String key, final double score, final String member) {
        if (getServerMode() == ServerMode.CLUSTER) {
            return executeWithJedisCluster(new JedisClusterCallback<Boolean>() {
                @Override
                public Boolean doWithJedisCluster(JedisCluster cluster) {
                    return cluster.zadd(key, score, member) > 0;
                }
            });
        } else {
            return executeWithJedis(WRITE, new JedisCallback<Boolean>() {
                @Override
                public Boolean doWithJedis(Jedis jedis) {
                    return jedis.zadd(key, score, member) > 0;
                }
            });
        }
    }

    @Override
    public Long zadd(final String key, final Map<String, Double> scoreMembers) {
        if (getServerMode() == ServerMode.CLUSTER) {
            return executeWithJedisCluster(new JedisClusterCallback<Long>() {
                @Override
                public Long doWithJedisCluster(JedisCluster cluster) {
                    return cluster.zadd(key, scoreMembers);
                }
            });
        } else {
            return executeWithJedis(WRITE, new JedisCallback<Long>() {
                @Override
                public Long doWithJedis(Jedis jedis) {
                    return jedis.zadd(key, scoreMembers);
                }
            });
        }
    }

    @Override
    public Set<String> zrange(final String key, final long start, final long end) {

        if (getServerMode() == ServerMode.CLUSTER) {
            return executeWithJedisCluster(new JedisClusterCallback<Set<String>>() {
                @Override
                public Set<String> doWithJedisCluster(JedisCluster cluster) {
                    return cluster.zrange(key, start, end);
                }
            });
        } else {
            return executeWithJedis(READ, new JedisCallback<Set<String>>() {
                @Override
                public Set<String> doWithJedis(Jedis jedis) {
                    return jedis.zrange(key, start, end);
                }
            });
        }
    }

    @Override
    public Set<String> zrangeByScore(final String key, final double min, final double max) {
        if (getServerMode() == ServerMode.CLUSTER) {
            return executeWithJedisCluster(new JedisClusterCallback<Set<String>>() {
                @Override
                public Set<String> doWithJedisCluster(JedisCluster cluster) {
                    return cluster.zrangeByScore(key, min, max);
                }
            });
        } else {
            return executeWithJedis(READ, new JedisCallback<Set<String>>() {
                @Override
                public Set<String> doWithJedis(Jedis jedis) {
                    return jedis.zrangeByScore(key, min, max);
                }
            });
        }
    }

    @Override
    public Set<String> zrangeByScore(final String key, final double min, final double max, final int offset, final int count) {
        if (getServerMode() == ServerMode.CLUSTER) {
            return executeWithJedisCluster(new JedisClusterCallback<Set<String>>() {
                @Override
                public Set<String> doWithJedisCluster(JedisCluster cluster) {
                    return cluster.zrangeByScore(key, min, max, offset, count);
                }
            });
        } else {
            return executeWithJedis(READ, new JedisCallback<Set<String>>() {
                @Override
                public Set<String> doWithJedis(Jedis jedis) {
                    return jedis.zrangeByScore(key, min, max, offset, count);
                }
            });
        }
    }

    @Override
    public Set<Tuple> zrangeByScoreWithScores(final String key, final double min, final double max, final int offset, final int count) {
        if (getServerMode() == ServerMode.CLUSTER) {
            return executeWithJedisCluster(new JedisClusterCallback<Set<Tuple>>() {
                @Override
                public Set<Tuple> doWithJedisCluster(JedisCluster cluster) {
                    return cluster.zrangeByScoreWithScores(key, min, max, offset, count);
                }
            });
        } else {
            return executeWithJedis(READ, new JedisCallback<Set<Tuple>>() {
                @Override
                public Set<Tuple> doWithJedis(Jedis jedis) {
                    return jedis.zrangeByScoreWithScores(key, min, max, offset, count);
                }
            });
        }
    }

    @Override
    public Long zcard(final String key) {
        if (getServerMode() == ServerMode.CLUSTER) {
            return executeWithJedisCluster(new JedisClusterCallback<Long>() {
                @Override
                public Long doWithJedisCluster(JedisCluster cluster) {
                    return cluster.zcard(key);
                }
            });
        } else {
            return executeWithJedis(READ, new JedisCallback<Long>() {
                @Override
                public Long doWithJedis(Jedis jedis) {
                    return jedis.zcard(key);
                }
            });
        }
    }

    @Override
    public Long zrem(final String key, final String... members) {
        if (getServerMode() == ServerMode.CLUSTER) {
            return executeWithJedisCluster(new JedisClusterCallback<Long>() {
                @Override
                public Long doWithJedisCluster(JedisCluster cluster) {
                    return cluster.zrem(key, members);
                }
            });
        } else {
            return executeWithJedis(WRITE, new JedisCallback<Long>() {
                @Override
                public Long doWithJedis(Jedis jedis) {
                    return jedis.zrem(key, members);
                }
            });
        }
    }

    @Override
    public String rpop(final String key) {
        if (getServerMode() == ServerMode.CLUSTER) {
            return executeWithJedisCluster(new JedisClusterCallback<String>() {
                @Override
                public String doWithJedisCluster(JedisCluster cluster) {
                    return cluster.rpop(key);
                }
            });
        } else {
            return executeWithJedis(WRITE, new JedisCallback<String>() {
                @Override
                public String doWithJedis(Jedis jedis) {
                    return jedis.rpop(key);
                }
            });
        }
    }

    @Override
    public Long llen(final String key) {
        if (getServerMode() == ServerMode.CLUSTER) {
            return executeWithJedisCluster(new JedisClusterCallback<Long>() {
                @Override
                public Long doWithJedisCluster(JedisCluster cluster) {
                    return cluster.llen(key);
                }
            });
        } else {
            return executeWithJedis(READ, new JedisCallback<Long>() {
                @Override
                public Long doWithJedis(Jedis jedis) {
                    return jedis.llen(key);
                }
            });
        }
    }

    @Override
    public Long expire(final String key, final int seconds) {
        if (getServerMode() == ServerMode.CLUSTER) {
            return executeWithJedisCluster(new JedisClusterCallback<Long>() {
                @Override
                public Long doWithJedisCluster(JedisCluster cluster) {
                    return cluster.expire(key, seconds);
                }
            });
        } else {
            return executeWithJedis(WRITE, new JedisCallback<Long>() {
                @Override
                public Long doWithJedis(Jedis jedis) {
                    return jedis.expire(key, seconds);
                }
            });
        }
    }

    @Override
    public Boolean setnx(final String key, final String data) {
        if (getServerMode() == ServerMode.CLUSTER) {
            return executeWithJedisCluster(new JedisClusterCallback<Boolean>() {
                @Override
                public Boolean doWithJedisCluster(JedisCluster cluster) {
                    Long status = cluster.setnx(key, data);
                    if (status > 0) {
                        return Boolean.TRUE;
                    } else
                        return Boolean.FALSE;
                }
            });
        } else {
            return executeWithJedis(WRITE, new JedisCallback<Boolean>() {
                @Override
                public Boolean doWithJedis(Jedis jedis) {
                    Long status = jedis.setnx(key, data);
                    if (status > 0) {
                        return Boolean.TRUE;
                    } else
                        return Boolean.FALSE;
                }
            });
        }
    }


    private byte[][] getKeysValuesByMap(Map<String, byte[]> keyDatas) {
        List<byte[]> values = new ArrayList<byte[]>();
        for (Map.Entry<String, byte[]> entry : keyDatas.entrySet()) {
            values.add(SafeEncoder.encode(entry.getKey()));
            values.add(entry.getValue());
        }
        return values.toArray(new byte[][]{});
    }

}
