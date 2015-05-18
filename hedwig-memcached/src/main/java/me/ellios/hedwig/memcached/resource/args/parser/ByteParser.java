package me.ellios.hedwig.memcached.resource.args.parser;

import me.ellios.hedwig.memcached.resource.args.ArgParser;

/**
 * byte parser.
 *
 * @author gaofeng@qiyi.com
 * @since: 14-3-20
 */
public class ByteParser implements ArgParser {

    @Override
    public Object parse(String value) {
        return Byte.parseByte(value);
    }
}
