package me.ellios.hedwig.zookeeper.lock;

import me.ellios.hedwig.zookeeper.curator.CuratorRecipes;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.framework.recipes.locks.InterProcessReadWriteLock;

import java.util.concurrent.TimeUnit;

/**
 * Author: ellios
 * Date: 12-12-18 Time: 下午8:15
 */
public class ReadWriteLockExample {

    public static void main(String[] args) throws Exception
    {

        final CuratorFramework client = CuratorRecipes.getCuratorClient();
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                InterProcessReadWriteLock rwLock = CuratorRecipes.getInterProcessReadWriteLock(client, "examples", "locks");
                final InterProcessMutex writeLock = rwLock.writeLock();
                try {

                    if(writeLock.acquire(1, TimeUnit.SECONDS)){
                        System.out.println(Thread.currentThread() +  "get write lock.");
                        Thread.sleep(10000);
                        System.out.println(Thread.currentThread() +  "get write lock end.");
                    }else{
                        System.out.println(Thread.currentThread() +  "get write lock fail");
                    }
                    writeLock.release();
                    InterProcessMutex readLock = rwLock.readLock();
                    while (1 > 0){

                        if(readLock.acquire(1, TimeUnit.SECONDS)){
                            System.out.println(Thread.currentThread() +  "get read lock.");
                            Thread.sleep(100000);
                            System.out.println(Thread.currentThread() +  "get read lock end.");
                        }else {
                            System.out.println(Thread.currentThread() +  "get read lock fail");
                        }
                        readLock.release();
                    }

                } catch (Exception ignored) {

                }
            }
        });


        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                InterProcessReadWriteLock rwLock = CuratorRecipes.getInterProcessReadWriteLock(client, "examples", "locks");
                InterProcessMutex readLock = rwLock.readLock();
                try {

                    while (true){
                        if(readLock.acquire(1, TimeUnit.SECONDS)){
                            System.out.println(Thread.currentThread() +  "get read lock.");
                            Thread.sleep(1000);
                            System.out.println(Thread.currentThread() +  "get read lock end.");
                        }else {
                            System.out.println(Thread.currentThread() +  "get read lock fail");
                        }
                    }
                } catch (Exception ignored) {

                }finally {
                    try {
                        readLock.release();
                    } catch (Exception e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }
            }
        });
        t1.start();
        t2.start();

        t1.join();
        t2.join();
    }
}
