package me.ellios.hedwig.http.provider.thrift;

import me.ellios.hedwig.http.provider.EntityReader;
import me.ellios.hedwig.http.provider.ReaderFactory;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TType;

import static me.ellios.hedwig.http.provider.thrift.JavaToThriftTypeMapping.getThriftType;
import static me.ellios.hedwig.http.provider.util.ProviderUtils.createDefaultObject;

/**
 * Say something?
 *
 * @author George Cao
 * @since 4/23/13 5:09 PM
 */
public class ThriftEntityReaderFactory implements ReaderFactory<TProtocol, Object> {
    @Override
    public EntityReader<TProtocol, Object> getEntityReader(final Class<?> elementClass) {
        byte type = getThriftType(elementClass);
        switch (type) {
            case TType.DOUBLE:
                return new EntityReader<TProtocol, Object>() {
                    @Override
                    public Object read(TProtocol ip) throws TException {
                        return ip.readDouble();
                    }
                };
            case TType.BYTE:
                return new EntityReader<TProtocol, Object>() {
                    @Override
                    public Object read(TProtocol ip) throws TException {
                        return ip.readByte();
                    }
                };
            case TType.I16:
                return new EntityReader<TProtocol, Object>() {
                    @Override
                    public Object read(TProtocol ip) throws TException {
                        return ip.readI16();
                    }
                };
            case TType.I32:
                return new EntityReader<TProtocol, Object>() {
                    @Override
                    public Object read(TProtocol ip) throws TException {
                        return ip.readI32();
                    }
                };
            case TType.I64:
                return new EntityReader<TProtocol, Object>() {
                    @Override
                    public Object read(TProtocol ip) throws TException {
                        return ip.readI64();
                    }
                };
            case TType.STRUCT:
                return new EntityReader<TProtocol, Object>() {
                    @Override
                    public Object read(TProtocol ip) throws TException {
                        TBase base = (TBase) createDefaultObject(elementClass);
                        base.read(ip);
                        return base;
                    }
                };
            case TType.STRING:
                return new EntityReader<TProtocol, Object>() {
                    @Override
                    public Object read(TProtocol ip) throws TException {
                        return ip.readString();
                    }
                };
            default:
                throw new IllegalStateException("Unsupported type " + type);
        }
    }
}
