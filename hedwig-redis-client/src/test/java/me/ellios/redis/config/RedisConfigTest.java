package me.ellios.redis.config;

import me.ellios.jedis.config.Config;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * User: ellios
 * Time: 13-8-6 : 下午10:24
 */
public class RedisConfigTest {

    @Test
    public void testCreateConfig5Format() throws Exception {
        String formatString = "127.0.0.1:6379,127.0.0.1:6380:3,127.0.0.1:6381:5/6";
        Config redisConfig = Config.parseConnString("test", formatString);
        System.out.println(redisConfig);
        assertNotNull(redisConfig);
    }
}
