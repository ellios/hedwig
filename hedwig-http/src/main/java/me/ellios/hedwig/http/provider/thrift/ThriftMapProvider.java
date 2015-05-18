package me.ellios.hedwig.http.provider.thrift;

import me.ellios.hedwig.http.provider.EntityReader;
import me.ellios.hedwig.http.provider.EntityWriter;
import me.ellios.hedwig.http.provider.MapProvider;
import me.ellios.hedwig.http.provider.util.ProviderUtils;
import org.apache.thrift.protocol.TMap;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.transport.TIOStreamTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.TreeMap;

import static me.ellios.hedwig.http.mediatype.ExtendedMediaType.APPLICATION_X_THRIFT;
import static me.ellios.hedwig.http.provider.thrift.JavaToThriftTypeMapping.getThriftType;
import static me.ellios.hedwig.http.provider.util.ProviderUtils.*;
import static me.ellios.hedwig.http.provider.util.ThriftProviderUtils.getProtocolFactory;

/**
 * Say something?
 *
 * @author George Cao
 * @since 4/15/13 5:30 PM
 */
@Provider
@Consumes({APPLICATION_X_THRIFT})
@Produces({APPLICATION_X_THRIFT})
public class ThriftMapProvider extends ThriftEntityProvider implements MapProvider {
    private static final Logger LOG = LoggerFactory.getLogger(ThriftMapProvider.class);

    private static final Class[] DEFAULT_IMPLEMENTATIONS = new Class[]{
            HashMap.class,
            TreeMap.class,
            Hashtable.class
    };

    @Override
    public Class[] getMapImplementations() {
        return DEFAULT_IMPLEMENTATIONS;
    }

    @Override
    public boolean verifyMapSubclass(Class<?> type) {
        return ProviderUtils.verifyMapSubclass(type, getMapImplementations());
    }

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        if (verifyMapSubclass(type)) {
            return verifyMapGenericType(genericType, getTypeChecker()) && isCompatible(mediaType);
        } else {
            return false;
        }
    }

    @Override
    public Object readFrom(Class<Object> type, Type genericType,
                           Annotation[] annotations,
                           MediaType mediaType, MultivaluedMap<String, String> httpHeaders,
                           InputStream entityStream) throws IOException, WebApplicationException {
        Map map = createMap(type, getMapImplementations());
        Class<?>[] classes = getMapGenericTypes(genericType);
        if (classes.length != 2) {
            throw new IllegalArgumentException("Unknown Map key and value type.");
        }
        Class<?> keyClass = classes[0];
        Class<?> valueClass = classes[1];
        EntityReader<TProtocol, Object> keyReader = getEntityReader(keyClass);
        EntityReader<TProtocol, Object> valueReader = getEntityReader(valueClass);
        TProtocolFactory factory = getProtocolFactory(mediaType);
        TProtocol ip = factory.getProtocol(new TIOStreamTransport(entityStream));
        try {
            TMap tmap = ip.readMapBegin();
            for (int i = 0; i < tmap.size; i++) {
                Object key = keyReader.read(ip);
                Object value = valueReader.read(ip);
                map.put(key, value);
            }
            ip.readMapEnd();
        } catch (Exception e) {
            throw new WebApplicationException(e, Response.Status.BAD_REQUEST);
        }
        return map;
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        if (Map.class.isAssignableFrom(type)) {
            return verifyMapGenericType(genericType, getTypeChecker()) && isCompatible(mediaType);
        } else {
            return false;
        }
    }

    @Override
    public void writeTo(Object o,
                        Class<?> type, Type genericType,
                        Annotation[] annotations,
                        MediaType mediaType, MultivaluedMap<String, Object> httpHeaders,
                        OutputStream entityStream) throws IOException, WebApplicationException {
        Class<?>[] classes = getMapGenericTypes(genericType);
        if (classes.length != 2) {
            throw new IllegalArgumentException("Unknown Map key and value type.");
        }
        Class<?> keyClass = classes[0];
        Class<?> valueClass = classes[1];
        TProtocolFactory factory = getProtocolFactory(mediaType);
        TProtocol op = factory.getProtocol(new TIOStreamTransport(entityStream));
        Map entityMap = (Map) o;
        byte kType = getThriftType(keyClass);
        byte vType = getThriftType(valueClass);
        EntityWriter<TProtocol> keyWriter = getEntityWriter(keyClass);
        EntityWriter<TProtocol> valueWriter = getEntityWriter(valueClass);
        TMap map = new TMap(kType, vType, entityMap.size());
        try {
            op.writeMapBegin(map);
            for (Object entry : entityMap.entrySet()) {
                Map.Entry e = (Map.Entry) entry;
                keyWriter.write(e.getKey(), op);
                valueWriter.write(e.getValue(), op);
            }
            op.writeMapEnd();
        } catch (Exception e) {
            throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }
}
