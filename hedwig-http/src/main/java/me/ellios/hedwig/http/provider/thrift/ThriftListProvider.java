package me.ellios.hedwig.http.provider.thrift;

import me.ellios.hedwig.http.provider.EntityReader;
import me.ellios.hedwig.http.provider.EntityWriter;
import org.apache.thrift.protocol.TList;
import org.apache.thrift.protocol.TProtocol;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.ext.Provider;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import static me.ellios.hedwig.http.mediatype.ExtendedMediaType.APPLICATION_X_THRIFT;
import static me.ellios.hedwig.http.provider.thrift.JavaToThriftTypeMapping.getThriftType;

/**
 * Say something?
 *
 * @author George Cao
 * @since 4/23/13 3:36 PM
 */
@Provider
@Consumes({APPLICATION_X_THRIFT})
@Produces({APPLICATION_X_THRIFT})
public class ThriftListProvider extends ThriftCollectProvider {
    private static final Class[] DEFAULT_IMPLEMENTATIONS = new Class[]{
            ArrayList.class,
            LinkedList.class,
    };

    @Override
    public Class[] getCollectionImplementations() {
        return DEFAULT_IMPLEMENTATIONS;
    }

    @Override
    public void read(Collection collect, Class<?> elementType, TProtocol ip) throws Exception {
        TList list = ip.readListBegin();
        EntityReader<TProtocol, Object> reader = getEntityReader(elementType);
        for (int i = 0; i < list.size; i++) {
            Object obj = reader.read(ip);
            collect.add(obj);
        }
        ip.readListEnd();
    }


    @Override
    public void write(Collection collect, Class<?> elementClass, TProtocol op) throws Exception {
        byte type = getThriftType(elementClass);
        TList list = new TList(type, collect.size());
        op.writeListBegin(list);
        Iterator it = collect.iterator();
        EntityWriter<TProtocol> writer = getEntityWriter(elementClass);
        while (it.hasNext()) {
            writer.write(it.next(), op);
        }
        op.writeListEnd();
    }
}
