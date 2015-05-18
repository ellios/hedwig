package me.ellios.hedwig.memcached.resource;

import java.util.List;
import java.util.Map;

/**
 * processor factory.
 *
 * @author gaofeng@qiyi.com
 * @since: 14-3-24
 */
public interface ProcessorFactory {

    Map<String, Processor> getProcessorMap(List<Class<?>> classes);
}
