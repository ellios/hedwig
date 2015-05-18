package me.ellios.hedwig.zookeeper.curator;

import com.google.common.base.Preconditions;
import me.ellios.hedwig.common.Constants;
import me.ellios.hedwig.zookeeper.ZooType;
import me.ellios.hedwig.zookeeper.ZookeeperClientFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.atomic.DistributedAtomicLong;
import org.apache.curator.framework.recipes.barriers.DistributedBarrier;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListener;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.framework.recipes.locks.InterProcessReadWriteLock;
import org.apache.curator.retry.RetryNTimes;

/**
 * Author: ellios
 * Date: 12-12-18 Time: 下午8:54
 */
public class CuratorRecipes {

    public static CuratorFramework getCuratorClient(){
        CuratorZookeeperClient client = (CuratorZookeeperClient) ZookeeperClientFactory.getZookeeperClient(ZooType.RECIPES);
        return client.getCuratorClient();
    }

    public static InterProcessMutex getInterProcessMutex(CuratorFramework client, String bizName, String nodeName){
        checkArgument(client, bizName, nodeName);
        String path = StringUtils.join(new String[]{"/lock", bizName, nodeName}, Constants.PATH_SEPARATOR);
        return new InterProcessMutex(client, path);
    }

    public static InterProcessReadWriteLock getInterProcessReadWriteLock(CuratorFramework client, String bizName, String nodeName){
        checkArgument(client, bizName, nodeName);
        String path = StringUtils.join(new String[]{"/lock", bizName, nodeName}, Constants.PATH_SEPARATOR);
        return new InterProcessReadWriteLock(client, path);
    }

    public static LeaderSelector getLeaderSelector(CuratorFramework client, String bizName, String nodeName,
                                                   LeaderSelectorListener listener){
        checkArgument(client, bizName, nodeName);
        Preconditions.checkNotNull(listener, "listener is null");
        String path = StringUtils.join(new String[]{"/leader", bizName, nodeName}, Constants.PATH_SEPARATOR);
        return new LeaderSelector(client, path, listener);
    }

    public static DistributedBarrier getDistributedBarrier(CuratorFramework client, String bizName, String nodeName){
        checkArgument(client, bizName, nodeName);
        String path = StringUtils.join(new String[]{"/barrier", bizName, nodeName}, Constants.PATH_SEPARATOR);
        return new DistributedBarrier(client, path);
    }

    public static DistributedAtomicLong getDistributedAtomicLong(CuratorFramework client, String bizName, String nodeName){
        checkArgument(client, bizName, nodeName);
        String path = StringUtils.join(new String[]{"/counter", bizName, nodeName}, Constants.PATH_SEPARATOR);
        //重试100次，每次重试的间隔时间为10毫秒
        RetryPolicy retryPolicy = new RetryNTimes(100, 10);
        return new DistributedAtomicLong(client, path, retryPolicy);
    }

    private static boolean checkArgument(CuratorFramework client, String... nodes){
        Preconditions.checkNotNull(client, "curator client is null.");
        if(nodes != null && nodes.length > 0){
            for(String node : nodes){
                Preconditions.checkArgument(StringUtils.isNotEmpty(node), "node is empty.");
                Preconditions.checkArgument(!StringUtils.startsWith(node, Constants.PATH_SEPARATOR), "illegal node");
            }
        }
        return true;
    }
}
