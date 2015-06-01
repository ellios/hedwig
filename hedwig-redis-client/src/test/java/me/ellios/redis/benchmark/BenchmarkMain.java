package me.ellios.redis.benchmark;


import me.ellios.jedis.RedisClientFactory;
import me.ellios.jedis.RedisOp;

import java.util.concurrent.*;

/**
 * User: ellios
 * Time: 13-9-12 : 下午4:28
 */
public class BenchmarkMain {

    public static void main(String... args) throws Exception{
        RedisOp redisOp = RedisClientFactory.getRedisClient("test");
        redisOp.set("ellios", "sddddsdsdfdflajsdlfjlasjdlfjalksjdfljalsdjflajsldfjlajsdlfalsdjflajsdl;fjafd".getBytes());
        System.out.println("Warn Up Begin");
        for(int i=1; i<500;){
            startThreadTest(i, 0);
            i=i+50;
        }
        System.out.println("Warn Up End");
        System.out.println("time take : " + startThreadTest(2000, 0));
        Thread.sleep(1000000000);
    }

    /**
     * Helper function.
     *
     * @param threads
     * @param workDelay
     * @return time taken
     * @throws InterruptedException
     */
    public static long startThreadTest(int threads, int workDelay) throws InterruptedException {
        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch doneSignal = new CountDownLatch(threads);

        ExecutorService pool = Executors.newFixedThreadPool(threads);
        ExecutorCompletionService<Long> ecs = new ExecutorCompletionService<Long>(pool);
        for (int i = 0; i <= threads; i++){ // create and start threads
            ecs.submit(new ThreadTesterUtil(startSignal, doneSignal, workDelay));
        }

        startSignal.countDown(); // START TEST!
        doneSignal.await();
        long time = 0;
        for (int i = 0; i <= threads; i++){
            try {
                time = time + ecs.take().get();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        pool.shutdown();
        return time;
    }
}
