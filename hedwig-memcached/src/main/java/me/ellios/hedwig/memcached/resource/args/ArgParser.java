package me.ellios.hedwig.memcached.resource.args;

/**
 * args parse.
 *
 * @author gaofeng
 * @since: 14-3-20
 */
public interface ArgParser {

    Object parse(String value);
}
