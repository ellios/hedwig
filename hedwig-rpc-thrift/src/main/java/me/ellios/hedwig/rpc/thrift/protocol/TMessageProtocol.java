package me.ellios.hedwig.rpc.thrift.protocol;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TMessage;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolDecorator;

/**
 * Another {@link org.apache.thrift.protocol.TProtocol} decorator.
 * We can read the begin message then reuse it.
 *
 * @author George Cao
 * @since 13-10-11 下午3:46
 */
public class TMessageProtocol extends TProtocolDecorator {

    private TMessage messageBegin;

    public TMessageProtocol(TProtocol protocol, TMessage messageBegin) {
        super(protocol);
        this.messageBegin = messageBegin;
    }

    @Override
    public TMessage readMessageBegin() throws TException {
        return messageBegin;
    }
}
