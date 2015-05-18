package me.ellios.hedwig.memcached.resource.args.parser;

import me.ellios.hedwig.memcached.resource.args.ArgParser;

/**
 * int parser.
 *
 * @author gaofeng@qiyi.com
 * @since: 14-3-20
 */
public class IntParser implements ArgParser {

    @Override
    public Object parse(String value) {
        return Integer.parseInt(value);
    }
}
