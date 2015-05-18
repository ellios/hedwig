package me.ellios.hedwig.memcached.text;

import me.ellios.hedwig.memcached.protocol.text.GetResponse;
import me.ellios.hedwig.memcached.protocol.text.Protocol;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * Say something?
 *
 * @author George Cao(caozhangzhi@qiyi.com)
 * @since 2014-03-26 19:04
 */
public class GetResponseTest {
    private static final Logger LOG = LoggerFactory.getLogger(GetResponseTest.class);

    @Test
    public void testGetSerializedSize() throws Exception {
        GetResponse response = GetResponse.EMPTY_RESPONSE;
        assertEquals(response.size(), 0);
    }

    @Test
    public void testWriteTo() throws Exception {

    }

    @Test
    public void testBuffer() throws Exception {
        GetResponse response = new GetResponse("key", ChannelBuffers.copiedBuffer("value and data together.".getBytes(Protocol.UTF_8)));
        ChannelBuffer buffer = response.buffer();
        System.out.println(buffer.toString(Protocol.UTF_8));
    }
}
