package me.ellios.hedwig.rpc.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import static org.testng.Assert.assertNotNull;

/**
 * Say something?
 *
 * @author George Cao
 * @since 3/21/13 5:39 PM
 */
public class ServiceHelperTest {
    private static final Logger LOG = LoggerFactory.getLogger(ServiceHelperTest.class);

    @Test
    public void testGetThriftService() throws Exception {
        ServiceFace face = ServiceHelper.getThriftService("serviceFace", ServiceFace.class);
        assertNotNull(face);
    }
}
