package me.ellios.hedwig.http.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Jersey does not have providers for the java primitive/wrapper class.
 * So Jersey does not known how to write them to the underlying network. We should at least convert the basic types
 * that used in thrift type system.
 * <pre>
 * bool: A boolean value (true or false)
 * byte: An 8-bit signed integer
 * i16: A 16-bit signed integer
 * i32: A 32-bit signed integer
 * i64: A 64-bit signed integer
 * double: A 64-bit floating point number
 * string: A text string encoded using UTF-8 encoding
 * binary: a sequence of unencoded bytes
 * </pre>
 * The last two type <strong>string</strong> and <strong>binary</strong> are supported by Jersey natively.
 *
 * @author George Cao
 * @since 4/19/13 11:34 AM
 */

public class PrimitivesResponseFilter implements ContainerResponseFilter {
    private static final Logger LOG = LoggerFactory.getLogger(PrimitivesResponseFilter.class);
    private static final Set<Class<?>> WHITE_LIST = new HashSet<>();

    static {
        WHITE_LIST.add(double.class);
        WHITE_LIST.add(Double.class);
        WHITE_LIST.add(boolean.class);
        WHITE_LIST.add(Boolean.class);
        WHITE_LIST.add(byte.class);
        WHITE_LIST.add(Byte.class);
        WHITE_LIST.add(short.class);
        WHITE_LIST.add(Short.class);
        WHITE_LIST.add(int.class);
        WHITE_LIST.add(Integer.class);
        WHITE_LIST.add(long.class);
        WHITE_LIST.add(Long.class);
    }

    @Override
    public void filter(ContainerRequestContext request, ContainerResponseContext response) throws IOException {
        Object entity = response.getEntity();
        if (null == entity) {
            return;
        }
        Class<?> clazz = entity.getClass();
        if (WHITE_LIST.contains(clazz)) {
            LOG.info("Convert primitive/wrapper {} to string.", clazz);
            response.setEntity(entity.toString());
        }
    }
}
