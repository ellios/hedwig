package me.ellios.hedwig.memcached.resource.args.parser;

import me.ellios.hedwig.memcached.resource.args.ArgParser;

/**
 * char parser.
 *
 * @author gaofeng
 * @since: 14-3-20
 */
public class CharParser implements ArgParser {

    @Override
    public Object parse(String value) {
        return value.charAt(0);
    }
}
