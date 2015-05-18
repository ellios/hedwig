package me.ellios.hedwig.memcached.resource.args.parser;

import me.ellios.hedwig.memcached.resource.args.ArgParser;

/**
 * short parser.
 *
 * @author gaofeng@qiyi.com
 * @since: 14-3-20
 */
public class ShortParser implements ArgParser {

    @Override
    public Object parse(String value) {
        return Short.parseShort(value);
    }
}
