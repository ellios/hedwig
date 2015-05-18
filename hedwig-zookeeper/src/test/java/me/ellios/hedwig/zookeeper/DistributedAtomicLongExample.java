package me.ellios.hedwig.zookeeper;

import com.google.common.io.Closeables;
import me.ellios.hedwig.zookeeper.curator.CuratorRecipes;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.atomic.DistributedAtomicLong;

/**
 * Author: ellios
 * Date: 12-12-19 Time: 下午6:15
 */
public class DistributedAtomicLongExample {

    private static final int T_COUNT = 10;

    private static final int INC_COUNT = 1000;

    public static void main(String[] args) throws Exception
    {

        Thread t = null;
        for(int i=0; i<T_COUNT; i++){
            final CuratorFramework client = CuratorRecipes.getCuratorClient();
            t = new Thread(new CounterTask(client));
            t.start();
        }

        t.join();
        final CuratorFramework client = CuratorRecipes.getCuratorClient();
        DistributedAtomicLong number = CuratorRecipes.getDistributedAtomicLong(client, "examples", "counter");
        System.out.println("the number is " + number.get().postValue());
    }


    static class CounterTask implements Runnable{

        private CuratorFramework client;

        CounterTask(CuratorFramework client) {
            this.client = client;
        }

        @Override
        public void run() {
            DistributedAtomicLong number = CuratorRecipes.getDistributedAtomicLong(client, "examples", "counter");
            for(int i=0; i<INC_COUNT; i++){
                try {
                    System.out.println(Thread.currentThread() +  " begin to inc data");
                    System.out.println(number.increment().succeeded());
                    System.out.println(Thread.currentThread() +  " the data is " + number.get().postValue());
                } catch (Exception e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
            System.out.println(Thread.currentThread() +  "finish inc " + INC_COUNT + " times");
        }
    }


    public void pp(){
        CuratorFramework client = CuratorRecipes.getCuratorClient();
        DistributedAtomicLong seq = CuratorRecipes.getDistributedAtomicLong(client, "examples", "counter");
        try {
            System.out.println("before inc :  " + seq.get().postValue());
            System.out.println("after inc : " + seq.increment().postValue());
        } catch (Exception ignored) {

        }finally {
            Closeables.closeQuietly(client);
        }
    }
}
