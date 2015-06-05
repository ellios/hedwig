package me.ellios.hedwig.rpc.thrift.protocol;

import com.google.common.base.Strings;
import org.apache.thrift.protocol.TMultiplexedProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

/**
 * Thrift message header.
 *
 * @author George Cao
 * @since 2014-01-13 16
 */
public class TMessageHeader {

    private static final Logger LOG = LoggerFactory.getLogger(TMessageHeader.class);
    private final Map<THeaderName, String> headers;
    private static final String SEP = TMultiplexedProtocol.SEPARATOR;

    public TMessageHeader() {
        this.headers = new TreeMap<>(new Comparator<THeaderName>() {

            @Override
            public int compare(THeaderName o1, THeaderName o2) {
                return o2.getIndex() - o1.getIndex();
            }
        });
    }

    public TMessageHeader add(THeaderName name, String value) {
        headers.put(name, Strings.nullToEmpty(value));
        return this;
    }

    public boolean contains(THeaderName name) {
        return headers.containsKey(name);
    }

    public String getValue(THeaderName name) {
        return Strings.nullToEmpty(headers.get(name));
    }

    public void clear() {
        headers.clear();
    }

    /**
     * The full name string in format user:service:api.
     *
     * @param fullName the full name
     * @return The message header.
     */
    public static TMessageHeader decode(String fullName) {
        TMessageHeader header = new TMessageHeader();
        if (Strings.isNullOrEmpty(fullName)) {
            return header;
        }
        String[] values = fullName.split(SEP);
        int index = 0;
        for (int i = values.length - 1; i >= 0; i--) {
            THeaderName name = THeaderName.findByIndex(index++);
            if (null != name) {
                header.add(name, Strings.nullToEmpty(values[i]));
            }
        }
        return header;
    }

    public String signature() {
        return getValue(THeaderName.SERVICE) + SEP + getValue(THeaderName.API);
    }

    /**
     * Encode user->service->API to user:service:API etc.
     *
     * @return the format string.
     */
    public String encode() {
        StringBuilder builder = new StringBuilder();
        for (String value : headers.values()) {
            if (builder.length() > 0) {
                builder.append(SEP);
            }
            builder.append(Strings.nullToEmpty(value));
        }
        return builder.toString();
    }
}
