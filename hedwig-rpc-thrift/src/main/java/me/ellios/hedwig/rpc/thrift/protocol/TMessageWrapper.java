package me.ellios.hedwig.rpc.thrift.protocol;

import org.apache.thrift.protocol.TMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TMessage header and message body.
 *
 * @author George Cao(caozhangzhi@qiyi.com)
 * @since 2014-01-13 17
 */
public class TMessageWrapper {
    private static final Logger LOG = LoggerFactory.getLogger(TMessageWrapper.class);
    TMessage message;
    TMessageHeader header;

    public TMessage getMessage() {
        return message;
    }

    public void setMessage(TMessage message) {
        this.message = message;
    }

    public TMessageHeader getHeader() {
        return header;
    }

    public void setHeader(TMessageHeader header) {
        this.header = header;
    }
}
