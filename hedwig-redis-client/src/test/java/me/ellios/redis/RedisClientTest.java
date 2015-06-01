package me.ellios.redis;

import me.ellios.jedis.RedisClient;
import me.ellios.jedis.RedisClientFactory;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Author: ellios
 * Date: 13-2-17 Time: 下午3:57
 */
public class RedisClientTest {

    RedisClient client = RedisClientFactory.getRedisClient("test");

    @Test
    public void testGet() throws Exception {
        client.set("ellios1", "hello".getBytes());
        String result = client.get("ellios1");

        System.out.println("====================================");
        System.out.println(result);
        System.out.println("====================================");
    }

    @Test
    public void testMGet() {
        Map<String, String> expect = new HashMap<>();
        expect.put("test", "hello");
        expect.put("test1", "hello1");
        expect.put("test2", "hello2");
        expect.put("test3", "hello3");
        expect.put("test4", "hello4");
        expect.put("test5", "hello5");

        Map<String, byte[]> input = new HashMap<>();
        for (Map.Entry<String, String> entry : expect.entrySet()) {
            input.put(entry.getKey(), entry.getValue().getBytes());
        }
        client.mset(input);

        Map<String, byte[]> bytesResult = client.mget(Arrays.asList("test", "test1", "test6", "test3", "test4", "test5", "test2"));

        Map<String, String> result = new HashMap<>();
        for (Map.Entry<String, byte[]> entry : bytesResult.entrySet()) {
            result.put(entry.getKey(), new String(entry.getValue()));
        }

        System.out.println("====================================");
        System.out.println(result);
        System.out.println("====================================");
        Assert.assertEquals(expect, result);
    }

    @Test
    public void testDel() {
        client.set("test", "hello".getBytes());
        Assert.assertTrue(client.del("test"));
    }

    @Test
    public void testConcrrentGet() {
        for (int j = 0; j < 100; j++) {
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < 10000; i++) {
                        try {
                            Thread.sleep(10L);
                        } catch (InterruptedException e) {

                        }
                        client.get("VIS_RATE_TF_06156f9bfee54293b64eccc2c6d139f3");
                    }
                }
            });
            t.start();
        }
        try {
            Thread.sleep(1000000L);
        } catch (InterruptedException e) {
        }
    }

    @Test
    public void testSadd() {
        Long result = client.sadd("st", "1213", "323");
        System.out.println(result);
        Set<String> members = client.smembers("st");
        Long rem = client.srem("st", "1213");
        Assert.assertTrue(result > 0);
        Assert.assertTrue(members.size() == 2);
        Assert.assertTrue(rem > 0);
    }
}
