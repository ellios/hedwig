package me.ellios.hedwig.rpc.thrift.protocol;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import static me.ellios.hedwig.rpc.thrift.protocol.MultiplexHelper.join;
import static me.ellios.hedwig.rpc.thrift.protocol.MultiplexHelper.split;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Say something please.
 *
 * @author George Cao
 */
public class MultiplexHelperTest {
    private static final Logger LOG = LoggerFactory.getLogger(MultiplexHelperTest.class);

    @Test
    public void testJoin() throws Exception {
        String service = "S";
        String api = "a";
        String n = join(service, api);
        assertEquals("S:a", n);

        service = "S";
        api = null;
        n = join(service, api);
        assertEquals("S:", n);
    }

    @Test
    public void testSplit() throws Exception {
        String name = "getVideoById";
        String[] names = split(name);
        assertTrue(Strings.isNullOrEmpty(names[0]));

        name = "A:b";
        names = split(name);
        assertTrue(!Strings.isNullOrEmpty(names[0]));

        name = ":b";
        names = split(name);
        assertTrue(Strings.isNullOrEmpty(names[0]));
        assertTrue(!Strings.isNullOrEmpty(names[1]));
    }
}
