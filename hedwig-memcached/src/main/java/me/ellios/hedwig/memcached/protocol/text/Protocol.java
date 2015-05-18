package me.ellios.hedwig.memcached.protocol.text;

import org.jboss.netty.channel.ChannelHandlerContext;

import java.nio.charset.Charset;

/**
 * GET command only.
 * <p/>
 * more detail: https://github.com/memcached/memcached/blob/master/doc/protocol.txt
 * <p/>
 *
 * @author gaofeng@qiyi.com
 * @since 14-3-20
 */
public interface Protocol {
    Charset UTF_8 = Charset.forName("UTF-8");
    byte SPACE = ' ';
    byte[] NEW_LINE = "\r\n".getBytes(UTF_8);
    byte[] END = "END".getBytes(UTF_8);
    byte[] VALUE = "VALUE".getBytes(UTF_8);
    byte[] ERROR = "ERROR".getBytes(UTF_8);
    byte[] CLIENT_ERROR = "CLIENT_ERROR".getBytes(UTF_8);
    byte[] SERVER_ERROR = "SERVER_ERROR".getBytes(UTF_8);
    int FLAGS = 0;
    byte[] EMPTY_ARRAY = new byte[0];

    enum Commend {
        GET
    }

    enum ErrorType {
        ERROR, CLIENT_ERROR, SERVER_ERROR;
    }

    void handleCommand(ChannelHandlerContext ctx, String command);

    void error(ChannelHandlerContext ctx, ErrorResponse error);

}
