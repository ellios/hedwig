package me.ellios.hedwig.zookeeper;

import me.ellios.hedwig.zookeeper.curator.CuratorRecipes;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.barriers.DistributedBarrier;

/**
 * Author: ellios
 * Date: 12-12-19 Time: 下午2:41
 */
public class DistributedBarrierExample {

    public static void main(String[] args) throws Exception
    {

        final CuratorFramework client = CuratorRecipes.getCuratorClient();
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                DistributedBarrier barrier = CuratorRecipes.getDistributedBarrier(client, "examples", "barrier");

                System.out.println(Thread.currentThread() +  "to be barrier");
                try {
                    barrier.setBarrier();
                    barrier.waitOnBarrier();
                } catch (Exception e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
                System.out.println(Thread.currentThread() +  "after barrier");
            }
        });


        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                DistributedBarrier barrier = CuratorRecipes.getDistributedBarrier(client, "examples", "barrier");
                System.out.println(Thread.currentThread() +  "to be barrier");
                try {
                    barrier.setBarrier();
                    barrier.waitOnBarrier();
                } catch (Exception e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
                System.out.println(Thread.currentThread() +  "after barrier");
            }
        });
        t1.start();
        t2.start();

        Thread.sleep(100000);
        DistributedBarrier barrier = CuratorRecipes.getDistributedBarrier(client, "examples", "barrier");
        System.out.println(Thread.currentThread() +  "remove barrier");
        barrier.removeBarrier();
        t1.join();
        t2.join();
    }
}
