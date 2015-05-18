package me.ellios.hedwig.http.provider.thrift;

import org.apache.thrift.protocol.TType;

import java.util.HashMap;
import java.util.Map;

/**
 * Say something?
 *
 * @author George Cao
 * @since 4/15/13 6:03 PM
 */
public class JavaToThriftTypeMapping {
    private static final Map<Class<?>, Byte> CLASS_TYPE_MAP = new HashMap<>();

    static {
        CLASS_TYPE_MAP.put(Double.class, TType.DOUBLE);
        CLASS_TYPE_MAP.put(Boolean.class, TType.BOOL);
        CLASS_TYPE_MAP.put(Byte.class, TType.BYTE);
        CLASS_TYPE_MAP.put(Short.class, TType.I16);
        CLASS_TYPE_MAP.put(Integer.class, TType.I32);
        CLASS_TYPE_MAP.put(Long.class, TType.I64);
        CLASS_TYPE_MAP.put(String.class, TType.STRING);
    }

    public static boolean contains(Class<?> clazz) {
        return CLASS_TYPE_MAP.containsKey(clazz);
    }

    public static byte getThriftType(Class<?> clazz) {
        if (CLASS_TYPE_MAP.containsKey(clazz)) {
            return CLASS_TYPE_MAP.get(clazz);
        }
        return TType.STRUCT;
    }
}
