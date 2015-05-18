package me.ellios.hedwig.http.provider.thrift;

import me.ellios.hedwig.http.provider.CollectionReader;
import me.ellios.hedwig.http.provider.CollectionWriter;
import me.ellios.hedwig.http.provider.util.ProviderUtils;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.transport.TIOStreamTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static me.ellios.hedwig.http.provider.util.ProviderUtils.*;
import static me.ellios.hedwig.http.provider.util.ThriftProviderUtils.getProtocolFactory;

/**
 * Say something?
 *
 * @author George Cao
 * @since 4/15/13 5:40 PM
 */
public abstract class ThriftCollectProvider extends ThriftEntityProvider
        implements CollectionReader<TProtocol>, CollectionWriter<TProtocol> {
    private static final Logger LOG = LoggerFactory.getLogger(ThriftCollectProvider.class);


    @Override
    public Class<?>[] getCollectionImplementations() {
        return new Class<?>[0];
    }

    public boolean verifyCollectionSubclass(Class<?> type) {
        return ProviderUtils.verifyCollectionSubclass(type, getCollectionImplementations());
    }

    public Collection createCollection(Class<?> type) {
        return ProviderUtils.createCollection(type, getCollectionImplementations());
    }

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        if (verifyCollectionSubclass(type)) {
            return verifyGenericType(genericType, getTypeChecker()) && isCompatible(mediaType);
        } else if (type.isArray()) {
            return verifyArrayType(type, getTypeChecker()) && isCompatible(mediaType);
        } else {
            return false;
        }
    }

    @Override
    public Object readFrom(Class<Object> type, Type genericType,
                           Annotation[] annotations,
                           MediaType mediaType, MultivaluedMap<String, String> httpHeaders,
                           InputStream entityStream) throws IOException, WebApplicationException {
        final Class elementType = getElementClass(type, genericType);
        Collection collect = createCollection(type);
        TProtocolFactory factory = getProtocolFactory(mediaType);
        TProtocol ip = factory.getProtocol(new TIOStreamTransport(entityStream));
        try {
            read(collect, elementType, ip);
        } catch (Exception e) {
            LOG.warn("Something wrong when reading {}, {}", type, genericType, e);
            throw new WebApplicationException(e, Response.Status.BAD_REQUEST);
        }
        return (type.isArray()) ? createArray((List) collect, elementType) : collect;
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        if (Collection.class.isAssignableFrom(type)) {
            return verifyGenericType(genericType, getTypeChecker()) && isCompatible(mediaType);
        } else if (type.isArray()) {
            return verifyArrayType(type, getTypeChecker()) && isCompatible(mediaType);
        } else {
            return false;
        }
    }

    @Override
    public void writeTo(Object obj, Class<?> type, Type genericType,
                        Annotation[] annotations,
                        MediaType mediaType, MultivaluedMap<String, Object> httpHeaders,
                        OutputStream entityStream) throws IOException, WebApplicationException {
        try {
            final Collection c = (type.isArray())
                    ? Arrays.asList((Object[]) obj) : (Collection) obj;
            Class<?> elementClass = getElementClass(type, genericType);
            TProtocolFactory factory = getProtocolFactory(mediaType);
            TProtocol op = factory.getProtocol(new TIOStreamTransport(entityStream));
            write(c, elementClass, op);
        } catch (Exception ex) {
            throw new WebApplicationException(ex, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }
}
