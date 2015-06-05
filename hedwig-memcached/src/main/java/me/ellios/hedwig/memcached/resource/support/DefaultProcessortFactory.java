package me.ellios.hedwig.memcached.resource.support;

import me.ellios.hedwig.common.instances.ServiceImplFactory;
import me.ellios.hedwig.memcached.resource.Processor;
import me.ellios.hedwig.memcached.resource.ProcessorFactory;
import me.ellios.hedwig.memcached.resource.args.ArgProperty;
import me.ellios.hedwig.memcached.resource.args.ArgType;
import me.ellios.hedwig.memcached.resource.impl.Resource;

import javax.ws.rs.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * default processor factory.
 *
 * @author gaofeng
 * @since: 14-3-24
 */
public class DefaultProcessortFactory implements ProcessorFactory {

    private static final Map<String, Processor> processorMap = new ConcurrentHashMap<>();

    @Override
    public Map<String, Processor> getProcessorMap(List<Class<?>> classes) {
        if (processorMap.isEmpty()) {
            synchronized (DefaultProcessortFactory.class) {
                if (processorMap.isEmpty()) {
                    initProcessorMap(classes);
                }
            }
        }
        return processorMap;
    }

    private static void initProcessorMap(List<Class<?>> classes) {
        for (Class<?> clasz : classes) {
            Method[] methods = clasz.getDeclaredMethods();
            for (Method method : methods) {
                if (method.getAnnotation(GET.class) != null) {
                    Path path = method.getAnnotation(Path.class);
                    if (path != null) {
                        String uri = path.value();
                        Resource resource = (Resource) processorMap.get(uri);
                        if (resource == null) {
                            resource = new Resource();
                            resource.setMethod(method);
                            resource.setService(ServiceImplFactory.getServiceImpl(clasz));
                            processorMap.put(uri, resource);
                        }
                        initResource(resource, method);
                    }
                }
            }
        }
    }

    private static void initResource(Resource resource, Method methed) {
        List<ArgProperty> argsProperties = getArgProperties(methed);
        initArgsType(methed, argsProperties);
        resource.setArgsProperties(argsProperties);
    }

    private static List<ArgProperty> getArgProperties(Method methed) {
        List<ArgProperty> argsProperties = new ArrayList<>();
        Annotation[][] annotations = methed.getParameterAnnotations();
        for (Annotation[] annotation : annotations) {
            ArgProperty argProperty = new ArgProperty();
            for (Annotation value : annotation) {
                if (value instanceof PathParam) {
                    PathParam pathParam = (PathParam) value;
                    argProperty.setName(pathParam.value());
                } else if (value instanceof QueryParam) {
                    QueryParam queryParam = (QueryParam) value;
                    argProperty.setName(queryParam.value());
                } else if (value instanceof DefaultValue) {
                    DefaultValue defaultValue = (DefaultValue) value;
                    argProperty.setDefaultValue(defaultValue.value());
                }
            }
            argsProperties.add(argProperty);
        }
        return argsProperties;
    }

    private static void initArgsType(Method methed, List<ArgProperty> argsProperties) {
        Class<?>[] parameterTypes = methed.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            String type = parameterTypes[i].getSimpleName();
            argsProperties.get(i).setType(ArgType.getArgType(type));
        }
    }
}
