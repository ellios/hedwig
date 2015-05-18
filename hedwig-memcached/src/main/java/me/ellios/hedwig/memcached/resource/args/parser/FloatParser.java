package me.ellios.hedwig.memcached.resource.args.parser;

import me.ellios.hedwig.memcached.resource.args.ArgParser;

/**
 * float parser.
 *
 * @author gaofeng@qiyi.com
 * @since: 14-3-20
 */
public class FloatParser implements ArgParser {

    @Override
    public Object parse(String value) {
        return Float.parseFloat(value);
    }
}
