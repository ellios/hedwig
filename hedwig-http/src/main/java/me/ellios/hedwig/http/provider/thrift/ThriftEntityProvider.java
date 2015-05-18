package me.ellios.hedwig.http.provider.thrift;

import me.ellios.hedwig.http.mediatype.ExtendedMediaType;
import me.ellios.hedwig.http.provider.*;
import org.apache.thrift.TBase;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.transport.TIOStreamTransport;
import org.glassfish.jersey.message.internal.AbstractMessageReaderWriterProvider;

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

import static me.ellios.hedwig.http.mediatype.ExtendedMediaType.APPLICATION_X_THRIFT;
import static me.ellios.hedwig.http.provider.thrift.JavaToThriftTypeMapping.contains;
import static me.ellios.hedwig.http.provider.util.ThriftProviderUtils.getProtocolFactory;

/**
 * Thrift Entity provider.
 *
 * @author George Cao
 * @since 4/15/13 5:34 PM
 */
@Provider
@Consumes({APPLICATION_X_THRIFT})
@Produces({APPLICATION_X_THRIFT})
public class ThriftEntityProvider extends AbstractMessageReaderWriterProvider<Object> implements EntityProvider {
    private WriterFactory<TProtocol> writerFactory = new ThriftEntityWriterFactory();
    private ReaderFactory<TProtocol, Object> readerFactory = new ThriftEntityReaderFactory();

    public WriterFactory<TProtocol> getWriterFactory() {
        return writerFactory;
    }

    public ReaderFactory<TProtocol, Object> getReaderFactory() {
        return readerFactory;
    }

    @Override
    public boolean isCompatible(MediaType mediaType) {
        return mediaType.isCompatible(ExtendedMediaType.APPLICATION_X_THRIFT_TYPE);
    }

    public EntityReader<TProtocol, Object> getEntityReader(final Class<?> elementClass) {
        return getReaderFactory().getEntityReader(elementClass);
    }

    public EntityWriter<TProtocol> getEntityWriter(Class<?> type) {
        return getWriterFactory().getEntityWriter(type);
    }

    private TypeChecker checker = new TypeChecker() {
        @Override
        public boolean isSupported(Class<?> type) {
            return contains(type) || TBase.class.isAssignableFrom(type);
        }
    };

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return checker.isSupported(type);
    }

    @Override
    public Object readFrom(Class<Object> type, Type genericType, Annotation[] annotations,
                           MediaType mediaType, MultivaluedMap<String, String> httpHeaders,
                           InputStream entityStream) throws IOException, WebApplicationException {
        TProtocolFactory factory = getProtocolFactory(mediaType);
        TProtocol in = factory.getProtocol(new TIOStreamTransport(entityStream));
        EntityReader<TProtocol, Object> reader = getEntityReader(type);
        try {
            return reader.read(in);
        } catch (Exception e) {
            throw new WebApplicationException(e, Response.Status.BAD_REQUEST);
        }
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return checker.isSupported(type);
    }

    @Override
    public void writeTo(Object entity, Class<?> type, Type genericType,
                        Annotation[] annotations,
                        MediaType mediaType, MultivaluedMap<String, Object> httpHeaders,
                        OutputStream entityStream) throws IOException, WebApplicationException {
        TProtocolFactory factory = getProtocolFactory(mediaType);
        TProtocol op = factory.getProtocol(new TIOStreamTransport(entityStream));
        EntityWriter<TProtocol> writer = getEntityWriter(type);
        try {
            writer.write(entity, op);
        } catch (Exception e) {
            throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public TypeChecker getTypeChecker() {
        return checker;
    }
}
