package me.ellios.hedwig.zookeeper;

import com.google.common.collect.Lists;
import com.google.common.io.Closeables;
import me.ellios.hedwig.zookeeper.curator.CuratorRecipes;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListener;
import org.apache.curator.framework.state.ConnectionState;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

public class LeaderSelectorExample
{
    private static final int        CLIENT_QTY = 10;

    private static final String PATH = "/examples/leader";

    public static void main(String[] args) throws Exception
    {
        // all of the useful sample code is in ExampleClient.java

        System.out.println("Create " + CLIENT_QTY + " clients, have each negotiate for leadership and then wait a random number of seconds before letting another leader election occur.");
        System.out.println("Notice that leader election is fair: all clients will become leader and will do so the same number of times.");

        List<CuratorFramework> clients = Lists.newArrayList();
        List<ExampleClient> examples = Lists.newArrayList();
        try
        {
            for ( int i = 0; i < CLIENT_QTY; ++i )
            {
                CuratorFramework client = CuratorRecipes.getCuratorClient();
                clients.add(client);

                ExampleClient example = new ExampleClient(client, "examples", "leader", "Client #" + i);
                examples.add(example);

                example.start();
            }

            System.out.println("Press enter/return to quit\n");
            new BufferedReader(new InputStreamReader(System.in)).readLine();
        }
        finally
        {
            System.out.println("Shutting down...");

            for ( ExampleClient exampleClient : examples )
            {
                Closeables.closeQuietly(exampleClient);
            }
            for ( CuratorFramework client : clients )
            {
                Closeables.closeQuietly(client);
            }
        }
    }

    public void pp(){
        CuratorFramework client = CuratorRecipes.getCuratorClient();
        LeaderSelector leaderSelector = CuratorRecipes.getLeaderSelector(client, "examples", "leader", new LeaderSelectorListener() {
            @Override
            public void takeLeadership(CuratorFramework client) throws Exception {
                System.out.println(Thread.currentThread() + " is the leader.");
            }

            @Override
            public void stateChanged(CuratorFramework curatorFramework, ConnectionState connectionState) {
                System.out.println("state change.");
            }
        });
        try {
            leaderSelector.autoRequeue();
            leaderSelector.start();
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } finally {
            Closeables.closeQuietly(client);
            Closeables.closeQuietly(leaderSelector);
        }
    }
}
