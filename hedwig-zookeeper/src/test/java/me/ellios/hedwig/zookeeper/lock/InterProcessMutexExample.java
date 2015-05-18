package me.ellios.hedwig.zookeeper.lock;

import com.google.common.io.Closeables;
import me.ellios.hedwig.zookeeper.curator.CuratorRecipes;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;

import java.util.concurrent.TimeUnit;

/**
 * Author: ellios
 * Date: 12-12-19 Time: 下午4:02
 */
public class InterProcessMutexExample {

    private static final int COUNT = 10;

    public static void main(String[] args) throws Exception
    {


        for(int i=0; i<1; i++){
            final CuratorFramework client = CuratorRecipes.getCuratorClient();
            Thread t = new Thread(new LockTask(client));
            t.start();
        }

        Thread.sleep(1000000);
    }


    static class LockTask implements Runnable{

        private CuratorFramework client;

        LockTask(CuratorFramework client) {
            this.client = client;
        }

        @Override
        public void run() {
            InterProcessMutex lock = CuratorRecipes.getInterProcessMutex(client, "examples", "locks");
            try {
                while (!lock.acquire(1, TimeUnit.SECONDS)){
                    System.out.println(Thread.currentThread() +  "get lock fail. try again.");
                    Thread.sleep(1000);
                }
                System.out.println(Thread.currentThread() +  "successfully get lock.");
                Thread.sleep(10000);
            } catch (Exception e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }finally {
                try {
                    lock.release();
                } catch (Exception ignored) {

                }
            }
        }

        public void test(){
            final CuratorFramework client = CuratorRecipes.getCuratorClient();
            InterProcessMutex lock = CuratorRecipes.getInterProcessMutex(client, "examples", "locks");
            try {
                while (!lock.acquire(1, TimeUnit.SECONDS)){
                    System.out.println(Thread.currentThread() +  "get lock fail. try again.");
                    Thread.sleep(1000);
                }
                System.out.println(Thread.currentThread() +  "successful get lock.");
                Thread.sleep(10000);
            } catch (Exception e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }finally {
                try {
                    lock.release();
                } catch (Exception ignored) {

                }
                Closeables.closeQuietly(client);
            }
        }
    }
}
