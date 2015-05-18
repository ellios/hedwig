package me.ellios.hedwig.memcached.resource.impl;

import me.ellios.hedwig.memcached.resource.Processor;
import me.ellios.hedwig.memcached.resource.args.ArgProperty;
import me.ellios.hedwig.memcached.resource.args.ArgType;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * resource defined.
 *
 * @author gaofeng@qiyi.com
 * @since: 14-3-24
 */
public class Resource implements Processor {

    private Object service;

    private Method method;

    private List<ArgProperty> argsProperties;

    public Resource() {
    }

    public Resource(Object service, Method method, List<ArgProperty> argsProperties) {
        this.service = service;
        this.method = method;
        this.argsProperties = argsProperties;
    }

    public Object getService() {
        return service;
    }

    public void setService(Object service) {
        this.service = service;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public List<ArgProperty> getArgsProperties() {
        return argsProperties;
    }

    public void setArgsProperties(List<ArgProperty> argsProperties) {
        this.argsProperties = argsProperties;
    }

    @Override
    public Object process(Map<String, String> parameters) throws InvocationTargetException, IllegalAccessException {
        Object[] args = getArgs(parameters);
        return method.invoke(service, args);
    }

    private Object[] getArgs(Map<String, String> parameters) {
        List<Object> args = new ArrayList<>();
        for (ArgProperty argProperty : argsProperties) {
            String name = argProperty.getName();
            ArgType argType = argProperty.getType();
            String value = parameters.get(name);
            if (value == null) {
                if (argProperty.getDefaultValue() != null) {
                    value = argProperty.getDefaultValue();
                } else {
                    value = argType == ArgType.String ? "" : "0";
                }
            }
            args.add(argType.parseValue(value));
        }
        return args.toArray();
    }

    @Override
    public String toString() {
        return "Resource{" +
                "service=" + service +
                ", method=" + method +
                ", argsProperties=" + argsProperties +
                '}';
    }
}
