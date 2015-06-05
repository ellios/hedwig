package me.ellios.hedwig.memcached.resource.args.parser;

import me.ellios.hedwig.memcached.resource.args.ArgParser;

/**
 * double parser.
 *
 * @author gaofeng
 * @since: 14-3-20
 */
public class DoubleParser implements ArgParser {

    @Override
    public Object parse(String value) {
        return Double.parseDouble(value);
    }
}
