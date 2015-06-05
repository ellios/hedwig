package me.ellios.hedwig.memcached.resource.args.parser;

import me.ellios.hedwig.memcached.resource.args.ArgParser;

/**
 * boolean parser.
 *
 * @author gaofeng
 * @since: 14-3-20
 */
public class BooleanParser implements ArgParser {

    @Override
    public Object parse(String value) {
        return Boolean.parseBoolean(value);
    }
}
