package me.ellios.hedwig.http.provider.thrift;

import me.ellios.hedwig.http.provider.EntityReader;
import me.ellios.hedwig.http.provider.EntityWriter;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.ext.Provider;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeSet;

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
public class ThriftSetProvider extends ThriftCollectProvider {
    private static final Logger LOG = LoggerFactory.getLogger(ThriftSetProvider.class);
    private static final Class[] DEFAULT_IMPLEMENTATIONS = new Class[]{
            HashSet.class,
            TreeSet.class
    };

    @Override
    public Class[] getCollectionImplementations() {
        return DEFAULT_IMPLEMENTATIONS;
    }

    @Override
    public void read(Collection collect, Class<?> elementType, TProtocol ip) throws Exception {
        TSet set = ip.readSetBegin();
        EntityReader<TProtocol, Object> reader = getEntityReader(elementType);
        for (int i = 0; i < set.size; i++) {
            Object obj = reader.read(ip);
            collect.add(obj);
        }
        ip.readSetEnd();
    }

    @Override
    public void write(Collection collect, Class<?> elementClass, TProtocol op) throws Exception {
        byte type = getThriftType(elementClass);
        TSet set = new TSet(type, collect.size());
        op.writeSetBegin(set);
        Iterator it = collect.iterator();
        EntityWriter<TProtocol> writer = getEntityWriter(elementClass);
        while (it.hasNext()) {
            writer.write(it.next(), op);
        }
        op.writeSetEnd();
    }
}
