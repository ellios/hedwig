package me.ellios.hedwig.memcached.resource.args.parser;

import me.ellios.hedwig.memcached.resource.args.ArgParser;

/**
 * long parser.
 *
 * @author gaofeng
 * @since: 14-3-20
 */
public class LongParser implements ArgParser {

    @Override
    public Object parse(String value) {
        return Long.parseLong(value);
    }
}
