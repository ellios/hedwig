package me.ellios.memcached;

import com.beust.jcommander.internal.Maps;
import org.junit.Test;

import java.io.Serializable;
import java.util.Map;

/**
 * User: ellios
 * Time: 15-6-29 : 上午11:19
 */
public class HedwigMemcachedClientTest {

    private MemcachedOp memcachedOp = HedwigMemcachedClientFactory.getMemcachedClient("web_old_sis");

    @Test
    public void test(){
        Map<String, String> bingo = Maps.newHashMap();
        bingo.put("bili", "bili");
//        memcachedOp.set("hihi", 1000, bingo);
        Map<String, String> bb = memcachedOp.get("hihi");
        User user = new User();
        user.setId("zzzzzzz");
        user.setName("gogogogo");
        memcachedOp.set("gogo", 1000, user);
        System.out.println("=========================================");
        System.out.println(bb);
        System.out.println(memcachedOp.get("gogo"));
        System.out.println("=========================================");
    }

    @Test
    public void testAdd(){
        boolean result = memcachedOp.add("zzzz", 10, "hello");
        boolean result1 = memcachedOp.add("zzzz", 1, "hello");
        System.out.println("========================================");
        System.out.println(result);
        System.out.println(result1);
        System.out.println("========================================");
    }

    public static class User implements Serializable{



        private String id;
        private String name;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("User{");
            sb.append("id='").append(id).append('\'');
            sb.append(", name='").append(name).append('\'');
            sb.append('}');
            return sb.toString();
        }
    }
}
