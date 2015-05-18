package me.ellios.hedwig.memcached.resource.args;

/**
 * args parse.
 *
 * @author gaofeng@qiyi.com
 * @since: 14-3-20
 */
public interface ArgParser {

    Object parse(String value);
}
