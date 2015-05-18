package me.ellios.hedwig.rpc.server.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

/**
 * Say something?
 *
 * @author George Cao
 * @since 4/26/13 4:42 PM
 */
public class AbstractRpcServerTest {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractRpcServerTest.class);

    @Test
    public void testNewCachedThreadPool() throws Exception {
        //System.out.println(AbstractRpcServer.newEventLoopGroup("{0}-%d", 2));
    }
}
