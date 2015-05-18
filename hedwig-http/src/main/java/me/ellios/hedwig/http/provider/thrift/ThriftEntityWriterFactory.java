package me.ellios.hedwig.http.provider.thrift;

import me.ellios.hedwig.http.provider.EntityWriter;
import me.ellios.hedwig.http.provider.WriterFactory;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static me.ellios.hedwig.http.provider.thrift.JavaToThriftTypeMapping.getThriftType;

/**
 * Say something?
 *
 * @author George Cao
 * @since 4/23/13 5:20 PM
 */
public class ThriftEntityWriterFactory implements WriterFactory<TProtocol> {
    private static final Logger LOG = LoggerFactory.getLogger(ThriftEntityWriterFactory.class);

    @Override
    public EntityWriter<TProtocol> getEntityWriter(Class<?> clazz) {
        byte type = getThriftType(clazz);
        switch (type) {
            case TType.DOUBLE:
                return new EntityWriter<TProtocol>() {
                    @Override
                    public void write(Object obj, TProtocol op) throws TException {
                        Double d = (Double) obj;
                        op.writeDouble(d);
                    }
                };
            case TType.BYTE:
                return new EntityWriter<TProtocol>() {
                    @Override
                    public void write(Object obj, TProtocol op) throws TException {
                        Byte b = (Byte) obj;
                        op.writeByte(b);
                    }
                };
            case TType.I16:
                return new EntityWriter<TProtocol>() {
                    @Override
                    public void write(Object obj, TProtocol op) throws TException {
                        Short d = (Short) obj;
                        op.writeI16(d);
                    }
                };
            case TType.I32:
                return new EntityWriter<TProtocol>() {
                    @Override
                    public void write(Object obj, TProtocol op) throws TException {
                        Integer d = (Integer) obj;
                        op.writeI32(d);
                    }
                };
            case TType.I64:
                return new EntityWriter<TProtocol>() {
                    @Override
                    public void write(Object obj, TProtocol op) throws TException {
                        Long d = (Long) obj;
                        op.writeI64(d);
                    }
                };
            case TType.STRUCT:
                return new EntityWriter<TProtocol>() {
                    @Override
                    public void write(Object obj, TProtocol op) throws TException {
                        TBase d = (TBase) obj;
                        d.write(op);
                    }
                };

            case TType.STRING:
                return new EntityWriter<TProtocol>() {
                    @Override
                    public void write(Object obj, TProtocol op) throws TException {
                        String d = (String) obj;
                        op.writeString(d);
                    }
                };
            default:
                throw new IllegalStateException("Unsupported type " + type);
        }
    }
}
