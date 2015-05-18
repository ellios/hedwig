package me.ellios.hedwig.http.provider.util;

import me.ellios.hedwig.http.provider.TypeChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.WebApplicationException;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Say something?
 *
 * @author George Cao
 * @since 4/15/13 7:38 PM
 */
public class ProviderUtils {
    private static final Logger LOG = LoggerFactory.getLogger(ProviderUtils.class);

    private ProviderUtils() {
    }

    public static Class getElementClass(Class<?> type, Type genericType) {
        Type ta;
        if (genericType instanceof ParameterizedType) {
            // List case
            ta = ((ParameterizedType) genericType).getActualTypeArguments()[0];
        } else if (genericType instanceof GenericArrayType) {
            // GenericArray case
            ta = ((GenericArrayType) genericType).getGenericComponentType();
        } else {
            // Array case
            ta = type.getComponentType();
        }
        if (ta instanceof ParameterizedType) {
            // JAXBElement case
            ta = ((ParameterizedType) ta).getActualTypeArguments()[0];
        }
        return (Class) ta;
    }

    public static Object createArray(List l, Class componentType) {
        Object array = Array.newInstance(componentType, l.size());
        for (int i = 0; i < l.size(); i++) {
            Array.set(array, i, l.get(i));
        }
        return array;
    }

    /**
     * The method could be used to check if given type is an array of Thrift supported type.
     *
     * @param type    the array to be checked
     * @param checker only allows Thrift supported types.
     * @return true if given type is an array of Thrift data
     */
    public static boolean verifyArrayType(Class type, TypeChecker checker) {
        type = type.getComponentType();
        return checker.isSupported(type);
    }

    /**
     * The method could be used to check if given type is a collection of Thrift supported data.
     *
     * @param genericType the type to be checked
     * @param checker     only allows Thrift supported types.
     * @return true if given type is a collection of Thrift data.
     */
    public static boolean verifyGenericType(Type genericType, TypeChecker checker) {
        if (!(genericType instanceof ParameterizedType)) {
            return false;
        }
        final ParameterizedType pt = (ParameterizedType) genericType;
        if (pt.getActualTypeArguments().length > 1) {
            return false;
        }
        final Type ta = pt.getActualTypeArguments()[0];
        return verify(ta, checker);
    }

    private static boolean verify(final Type ta, TypeChecker checker) {
        Class<?> klass = typeToClass(ta);
        if (null == klass) {
            return false;
        }
        return checker.isSupported(klass);
    }

    private static Class<?> typeToClass(final Type ta) {
        if (ta instanceof ParameterizedType) {
            ParameterizedType lpt = (ParameterizedType) ta;
            if (lpt.getRawType() instanceof Class) {
                return (Class<?>) lpt.getRawType();
            }
        }
        if (!(ta instanceof Class)) {
            return null;
        }
        return (Class<?>) ta;
    }

    public static boolean verifyMapGenericType(Type genericType, TypeChecker checker) {
        if (!(genericType instanceof ParameterizedType)) {
            return false;
        }
        final ParameterizedType pt = (ParameterizedType) genericType;
        if (pt.getActualTypeArguments().length > 2) {
            return false;
        }
        final Type keyType = pt.getActualTypeArguments()[0];
        final Type valueType = pt.getActualTypeArguments()[1];
        return verify(keyType, checker) && verify(valueType, checker);
    }

    public static Class<?>[] getMapGenericTypes(Type genericType) {
        Class<?>[] classes = new Class<?>[0];
        if (!(genericType instanceof ParameterizedType)) {
            return classes;
        }
        final ParameterizedType pt = (ParameterizedType) genericType;
        if (pt.getActualTypeArguments().length > 2) {
            return classes;
        }
        final Class<?> keyClass = typeToClass(pt.getActualTypeArguments()[0]);
        final Class<?> valueClass = typeToClass(pt.getActualTypeArguments()[1]);
        if (null != keyClass && null != valueClass) {
            classes = new Class<?>[]{keyClass, valueClass};
        }
        return classes;
    }

    public static Object createDefaultObject(Class<?> type) {
        try {
            Constructor<?> constructor = type.getConstructor();
            return constructor.newInstance();
        } catch (NoSuchMethodException e) {
            LOG.warn("Thrift Class {} must have a default constructor", type, e);
            throw new WebApplicationException(e);
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            LOG.warn("Cannot instantiate {}", type, e);
            throw new WebApplicationException(e);
        }
    }

    public static Collection createCollection(Class<?> type, Class[] implementations) {
        Collection collect = null;
        if (type.isArray()) {
            collect = new ArrayList();
        } else {
            try {
                collect = (Collection) type.newInstance();
            } catch (Exception e) {
                for (Class c : implementations) {
                    if (type.isAssignableFrom(c)) {
                        try {
                            collect = (Collection) c.newInstance();
                            break;
                        } catch (InstantiationException | IllegalAccessException ignored) {
                            // NO-OP
                        }
                    }
                }
            }
        }
        return collect;
    }

    public static Map createMap(Class<?> type) {
        return createMap(type, new Class<?>[]{});
    }

    public static Map createMap(Class<?> type, Class<?>[] implementations) {
        Map map = null;
        try {
            map = (Map) type.newInstance();
        } catch (Exception e) {
            for (Class c : implementations) {
                if (type.isAssignableFrom(c)) {
                    try {
                        map = (Map) c.newInstance();
                        break;
                    } catch (InstantiationException | IllegalAccessException ignored) {
                        // NO-OP
                    }
                }
            }
        }
        return map;
    }

    public static boolean verifyMapSubclass(Class<?> type, Class[] implementations) {
        try {
            if (Map.class.isAssignableFrom(type)) {
                for (Class c : implementations) {
                    if (type.isAssignableFrom(c)) {
                        return true;
                    }
                }
                return !Modifier.isAbstract(type.getModifiers())
                        && Modifier.isPublic(type.getConstructor().getModifiers());
            }
        } catch (NoSuchMethodException | SecurityException ex) {
            LOG.warn("Something is wrong for map type {}", type, ex);
        }
        return false;
    }

    public static boolean verifyCollectionSubclass(Class<?> type, Class[] implementations) {
        try {
            if (Collection.class.isAssignableFrom(type)) {
                for (Class c : implementations) {
                    if (type.isAssignableFrom(c)) {
                        return true;
                    }
                }
                return !Modifier.isAbstract(type.getModifiers())
                        && Modifier.isPublic(type.getConstructor().getModifiers());
            }
        } catch (NoSuchMethodException | SecurityException ex) {
            LOG.warn("Something is wrong for collection type {}", type, ex);
        }
        return false;
    }
}
