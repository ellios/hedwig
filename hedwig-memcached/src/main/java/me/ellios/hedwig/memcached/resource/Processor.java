package me.ellios.hedwig.memcached.resource;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 * processor
 *
 * @author gaofeng@qiyi.com
 * @since: 14-3-24
 */
public interface Processor {

    Object process(Map<String, String> parameters) throws InvocationTargetException, IllegalAccessException;
}
