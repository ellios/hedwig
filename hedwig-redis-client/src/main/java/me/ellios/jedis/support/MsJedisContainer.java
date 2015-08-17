package me.ellios.jedis.support;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import me.ellios.jedis.OpType;
import me.ellios.jedis.RedisReply;
import me.ellios.jedis.config.Config;
import me.ellios.jedis.util.RedisConfigFileParsers;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisDataException;
import redis.clients.util.Pool;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

/**
 * User: ellios
 * Time: 15-5-28 : 下午1:09
 */
public class MsJedisContainer extends AbstractJedisContainer{

    private static final Logger LOG = LoggerFactory.getLogger(MsJedisContainer.class);

    private final ScheduledExecutorService brokenNodeCheckExecutor;
    private final BrokenSlaveRedisCheckTask brokenSlaveRedisCheckTask;

    private final ConcurrentMap<Config.RedisNode, JedisPool> nodeJedisPoolMap = Maps.newConcurrentMap();

    private volatile JedisPool masterPool;

    private final CopyOnWriteArrayList<JedisPool> slaveJedisPools = new CopyOnWriteArrayList<>();


    public MsJedisContainer(Config config) {
        super(config);

        for (Config.RedisNode node : config.getNodes()) {
            if (node.isMaster()) {
                masterPool = buildJedisPool(node);
            } else {
                slaveJedisPools.add(buildJedisPool(node));
            }
        }

        //定期检查slave节点
        brokenNodeCheckExecutor = Executors.newScheduledThreadPool(1, new ThreadFactoryBuilder().
                setNameFormat(config.getName() + "-broken-redis-check-task" + "-%d").setDaemon(true).build());
        brokenSlaveRedisCheckTask = new BrokenSlaveRedisCheckTask();
        brokenNodeCheckExecutor.scheduleWithFixedDelay(brokenSlaveRedisCheckTask,
                2000, 10000, TimeUnit.MILLISECONDS);
    }



    @Override
    public Pool<Jedis> getJedisPool(OpType opMode) {
        Pool<Jedis> pool = masterPool;
        if (opMode == OpType.READ) {
            if (CollectionUtils.isNotEmpty(slaveJedisPools)) {
                int index = ThreadLocalRandom.current().nextInt(0, slaveJedisPools.size());
                pool = slaveJedisPools.get(index);
            }
        }
        return pool;
    }

    @Override
    public JedisCluster getJedisCluster() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void destroy() {
        if (masterPool != null) {
            Config.RedisNode node = getRedisNodeByPool(masterPool);
            LOG.info("destroy master node. node : {}, pool : {}", node, masterPool);
            masterPool.destroy();
        }
        if (CollectionUtils.isNotEmpty(slaveJedisPools)) {
            for (JedisPool pool : slaveJedisPools) {
                Config.RedisNode node = getRedisNodeByPool(pool);
                LOG.info("begin to destroy slave pool. node : {}, pool : {}", node, pool);
                pool.destroy();
            }
            slaveJedisPools.clear();
        }
        nodeJedisPoolMap.clear();
        brokenSlaveRedisCheckTask.cleanBrokens();
        brokenNodeCheckExecutor.shutdown();
        LOG.info("finish destroy redis client config : {}", this.config);
    }


    private Config.RedisNode getRedisNodeByPool(JedisPool pool) {
        if (MapUtils.isEmpty(nodeJedisPoolMap)) {
            return null;
        }
        for (Map.Entry<Config.RedisNode, JedisPool> entry : nodeJedisPoolMap.entrySet()) {
            if (entry.getValue().equals(pool)) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * 构建jedis连接池,避免重复创建连接池
     *
     * @param node
     * @return
     */
    private JedisPool buildJedisPool(Config.RedisNode node) {
        Preconditions.checkNotNull(node);
        JedisPool pool = this.nodeJedisPoolMap.get(node);
        if (pool == null) {
            int timeout = RedisConfigFileParsers.getTimeout();
            GenericObjectPoolConfig poolConfig = getPoolConfig();
            pool = new JedisPool(poolConfig, node.getHost(), node.getPort(), timeout, config.getPassword(), config.getDb());
            this.nodeJedisPoolMap.put(node, pool);
        }
        return this.nodeJedisPoolMap.get(node);
    }


    /**
     * 定期检查down掉的redis节点
     */
    private class BrokenSlaveRedisCheckTask implements Runnable {

        private final Set<Config.RedisNode> brokenNodes = Sets.newHashSet();

        @Override
        public void run() {
            checkSlaveRedisNode();
            repairBrokenNode();
        }

        private void checkSlaveRedisNode() {
            if (MapUtils.isNotEmpty(nodeJedisPoolMap)) {
                for (Map.Entry<Config.RedisNode, JedisPool> entry : nodeJedisPoolMap.entrySet()) {
                    Config.RedisNode node = entry.getKey();
                    if (node.isMaster()) {
                        //master就直接跳过了
                        continue;
                    }

                    JedisPool pool = entry.getValue();
                    Jedis jedis = null;
                    boolean broken = false;
                    try {
                        jedis = pool.getResource();
                        String pingResult = jedis.ping();
                        if (RedisReply.PONG.equals(pingResult)) {
                            node.setFail(0);
                            if (LOG.isDebugEnabled()) {
                                LOG.debug("success to check redis : {}, pool : {}", node, pool);
                            }
                        } else {
                            LOG.error("fail to check redis node : {}, pool : {}, add fail count, pingResult is : {}",
                                    node, pool, pingResult);
                            node.incFail();
                        }
                    } catch (JedisConnectionException | JedisDataException e) {
                        LOG.error("fail to check redis node : {}, pool : {}, add fail count, error : {}",
                                node, pool, e.getMessage());
                        node.incFail();
                        broken = true;
                    } finally {
                        if (jedis != null) {
                            if (broken) {
                                if (LOG.isDebugEnabled()) {
                                    LOG.debug("slave checker return broken jedis : {} to pool : {}", jedis, pool);
                                }
                                pool.returnBrokenResource(jedis);
                            } else {
                                if (LOG.isDebugEnabled()) {
                                    LOG.debug("slave checker return jedis : {} to pool : {}", jedis, pool);
                                }
                                pool.returnResource(jedis);
                            }
                        }
                    }
                    if (node.getFail() > 2) {
                        LOG.error("redis node : {} fail count  >2, try to move to broken", node);
                        destroyBrokenRedisAndPool(node, pool);
                    }
                }
            }
        }

        private void repairBrokenNode() {
            if (CollectionUtils.isNotEmpty(brokenNodes)) {
                for (me.ellios.jedis.config.Config.RedisNode node : brokenNodes) {
                    LOG.warn("being to validate broken node : {}", node);
                    Jedis jedis = new Jedis(node.getHost(), node.getPort());
                    try {
                        jedis.connect();
                        if (jedis.isConnected() && "PONG".equals(jedis.ping())) {
                            slaveJedisPools.add(buildJedisPool(node));
                            brokenNodes.remove(node);
                            node.setFail(0);
                            LOG.warn("redis node : {} is ok now", node);
                        } else {
                            node.incFail();
                            LOG.warn("!!!!!!!!!!!!! redis node : {} still broken", node);
                        }
                    } catch (JedisConnectionException | JedisDataException e) {
                        node.incFail();
                        LOG.warn("!!!!!!!!!!!!! redis node : {} still broken", node);
                    } catch (Exception e) {
                        LOG.error(e.getMessage(), e);
                    } finally {
                        if (jedis != null && jedis.isConnected()) {
                            jedis.disconnect();
                        }
                    }
                }
            }
        }

        private void destroyBrokenRedisAndPool(Config.RedisNode node, JedisPool pool) {
            JedisPool brokenPool = nodeJedisPoolMap.remove(node);
            if (brokenPool != null) {
                LOG.warn("begin to destroy broken pool. node : {}, pool : {}", node, pool);
                slaveJedisPools.remove(brokenPool);
                brokenPool.destroy();
            }
            brokenNodes.add(node);
        }

        public void cleanBrokens() {
            this.brokenNodes.clear();
        }
    }
}
