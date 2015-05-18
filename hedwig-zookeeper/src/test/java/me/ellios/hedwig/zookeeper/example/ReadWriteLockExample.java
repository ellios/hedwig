package me.ellios.hedwig.zookeeper.example;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.framework.recipes.locks.InterProcessReadWriteLock;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.util.concurrent.TimeUnit;

/**
 * Author: ellios
 * Date: 12-12-18 Time: 下午8:15
 */
public class ReadWriteLockExample {

    private static final String PATH = "/examples/locks";

    public static void main(String[] args) throws Exception
    {
        final CuratorFramework client = CuratorFrameworkFactory.newClient("10.11.50.34:2181,10.11.50.34:2171,10.11.50.34:2161,10.11.50.73:2181,10.11.50.73:2171/hedwig/dev",
                new ExponentialBackoffRetry(1000, 3));
        client.start();

        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                InterProcessReadWriteLock rwLock = new InterProcessReadWriteLock(client, PATH);
                final InterProcessMutex writeLock = rwLock.writeLock();
                try {

                    if(writeLock.acquire(1, TimeUnit.SECONDS)){
                        System.out.println("get write lock.");
                        Thread.sleep(10000);
                        System.out.println("get write lock end.");
                    }else{
                        System.out.println("get write lock fail");
                    }
                    InterProcessMutex readLock = rwLock.readLock();
                    if(readLock.acquire(1, TimeUnit.SECONDS)){
                        System.out.println(Thread.currentThread() +  "get read lock.");
                        Thread.sleep(10000);
                    }else {
                        System.out.println(Thread.currentThread() +  "get read lock fail");
                    }
                    readLock.release();
                } catch (Exception ignored) {

                }finally {
                    try {
                        writeLock.release();
                    } catch (Exception e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }
            }
        });
        t1.start();

        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                InterProcessReadWriteLock rwLock = new InterProcessReadWriteLock(client, PATH);
                InterProcessMutex readLock = rwLock.readLock();
                try {

                    while (true){
                        if(readLock.acquire(1, TimeUnit.SECONDS)){
                            System.out.println(Thread.currentThread() +  "get read lock.");
                            Thread.sleep(1000);
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
        t2.start();

        t1.join();
        t2.join();
    }
}
