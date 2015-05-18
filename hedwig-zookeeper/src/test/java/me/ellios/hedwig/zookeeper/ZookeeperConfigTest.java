package me.ellios.hedwig.zookeeper;

import me.ellios.hedwig.zookeeper.config.ZookeeperConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.fail;

/**
 * Say something?
 *
 * @author George Cao
 * @since 13-5-23 上午10:41
 */

public class ZookeeperConfigTest {
    private static final Logger LOG = LoggerFactory.getLogger(ZookeeperConfigTest.class);

    @Test
    public void testSetTypeString() throws Exception {
        try {
            ZookeeperConfig.setTypeString("");
            fail();
        } catch (Exception ignored) {
        }

        try {
            ZookeeperConfig.setTypeString(null);
            fail();
        } catch (Exception ignored) {
        }
        String type = "type";
        ZookeeperConfig.setTypeString(type);
        assertEquals(type, ZookeeperConfig.getTypeString());
    }

    @Test
    public void testSetNamespaceString() throws Exception {
        try {
            ZookeeperConfig.setNamespaceString("");
            fail();
        } catch (Exception ignored) {
        }

        try {
            ZookeeperConfig.setNamespaceString(null);
            fail();
        } catch (Exception ignored) {
        }

        String ns = "ns";
        ZookeeperConfig.setNamespaceString(ns);
        assertEquals(ns, ZookeeperConfig.getNamespaceString());
    }
}
