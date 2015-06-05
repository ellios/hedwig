package me.ellios.hedwig.rpc.thrift.protocol;

import com.google.common.base.Strings;
import org.apache.thrift.protocol.TMultiplexedProtocol;

/**
 * Multiplex service helper.
 *
 * @author George Cao
 */
public class MultiplexHelper {

    /**
     * Generate the multiplexed name by joining the service and api name with {@link org.apache.thrift.protocol.TMultiplexedProtocol#SEPARATOR}.
     *
     * @param service service name.
     * @param api     api name.
     * @return the full name.
     */
    public static String join(String service, String api) {
        return Strings.nullToEmpty(service) + TMultiplexedProtocol.SEPARATOR + Strings.nullToEmpty(api);
    }

    /**
     * Split message name.
     *
     * @param name the original message name.
     * @return service and api names. The first element of this array may be empty.
     */
    public static String[] split(String name) {
        String[] names = new String[2];
        int index = name.indexOf(TMultiplexedProtocol.SEPARATOR);
        if (index >= 0) {
            names[0] = name.substring(0, index);
            names[1] = name.substring(index + TMultiplexedProtocol.SEPARATOR.length());
        } else {
            names[1] = name;
        }
        return names;
    }
}
