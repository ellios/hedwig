package me.ellios.hedwig.registry.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Say something?
 *
 * @author George Cao
 * @since 13-3-13 上午11:31
 */

public class ZPathUtilsTest {
    private static final Logger LOG = LoggerFactory.getLogger(ZPathUtilsTest.class);

    private String znode;
    private String service;

    @BeforeMethod
    public void setUp() throws Exception {
        znode = "znode";
        service = "service";
    }

    private int countSlash(String path) {
        int start = 0;
        int count = 0;
        while (0 != (start = path.indexOf("/", start) + 1)) {
            count++;
        }
        return count;
    }

    private void emptyGroupPath(String group, String name) throws Exception {
        String path = ZPathUtils.buildServicePath(name);
        int count = countSlash(path);
        assertEquals(count, 1);
    }

    @Test
    public void testBuildServicePath() throws Exception {
        emptyGroupPath(null, znode);
        emptyGroupPath("", znode);
        emptyGroupPath("  ", znode);
        emptyGroupPath(" ", znode);
    }

    @Test
    public void testBuildWithDefaultGroup() throws Exception {
        String path = ZPathUtils.buildServicePath(service);
        int count = countSlash(path);
        assertEquals(count, 1);
    }

    @Test
    public void testPathContainBlank() throws Exception {
        String path = ZPathUtils.buildZNodeFullPath(service, znode);
        assertEquals(path.length(), service.length() + znode.length() + 2);
        assertEquals(countSlash(path), 2);
    }

    @Test
    public void testBuildZNodeFullPath() throws Exception {
        String path = ZPathUtils.buildZNodeFullPath(service, znode);
        assertEquals(countSlash(path), 2);
    }

    @Test
    public void testAppendToServicePath() throws Exception {
        String[] nodes = new String[]{"", null, " ", "/2", "2/", "/"};
        for (String znode : nodes) {
            boolean yes = false;
            try {
                ZPathUtils.appendToServicePath("", znode);
            } catch (IllegalArgumentException | NullPointerException e) {
                yes = true;
            }
            assertTrue(yes);
        }
        String path = ZPathUtils.appendToServicePath("/parent", "znode");
        assertNotNull(path);
    }
}
