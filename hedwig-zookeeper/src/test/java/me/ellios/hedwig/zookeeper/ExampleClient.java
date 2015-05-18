package me.ellios.hedwig.zookeeper;

import me.ellios.hedwig.zookeeper.curator.CuratorRecipes;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListener;
import org.apache.curator.framework.state.ConnectionState;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * An example leader selector client
 */
public class ExampleClient implements Closeable, LeaderSelectorListener
{
    private final String name;
    private final LeaderSelector leaderSelector;
    private final AtomicInteger leaderCount = new AtomicInteger();

    private volatile Thread ourThread = null;

    public ExampleClient(CuratorFramework client, String bizName, String leaderName, String name)
    {
        this.name = name;

        // create a leader selector using the given path for management
        // all participants in a given leader selection must use the same path
        // ExampleClient here is also a LeaderSelectorListener but this isn't required
        leaderSelector = CuratorRecipes.getLeaderSelector(client, bizName, leaderName, this);

        // for most cases you will want your instance to requeue when it relinquishes leadership
        leaderSelector.autoRequeue();
    }

    public void start() throws IOException
    {
        // the selection for this instance doesn't start until the leader selector is started
        // leader selection is done in the background so this call to leaderSelector.start() returns immediately
        leaderSelector.start();
    }

    @Override
    public void close() throws IOException
    {
        leaderSelector.close();
    }

    @Override
    public void takeLeadership(CuratorFramework client) throws Exception
    {
        // we are now the leader. This method should not return until we want to relinquish leadership

        final int         waitSeconds = (int)(5 * Math.random()) + 1;

        ourThread = Thread.currentThread();
        System.out.println(name + " is now the leader. Waiting " + waitSeconds + " seconds...");
        System.out.println(name + " has been leader " + leaderCount.getAndIncrement() + " time(s) before.");
        try
        {
            Thread.sleep(TimeUnit.SECONDS.toMillis(waitSeconds));
        }
        catch ( InterruptedException e )
        {
            System.err.println(name + " was interrupted.");
            Thread.currentThread().interrupt();
        }
        finally
        {
            ourThread = null;
            System.out.println(name + " relinquishing leadership.\n");
        }
    }

    @Override
    public void stateChanged(CuratorFramework client, ConnectionState newState)
    {
        // you MUST handle connection state changes. This WILL happen in production code.

        if ( (newState == ConnectionState.LOST) || (newState == ConnectionState.SUSPENDED) )
        {
            if ( ourThread != null )
            {
                ourThread.interrupt();
            }
        }
    }
}
