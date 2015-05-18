package me.ellios.hedwig.http.provider.util;

import com.google.common.base.Strings;
import org.apache.thrift.protocol.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Say something?
 *
 * @author George Cao
 * @since 4/15/13 7:38 PM
 */
public class ThriftProviderUtils {
    private static final Logger LOG = LoggerFactory.getLogger(ThriftProviderUtils.class);

    private ThriftProviderUtils() {
    }

    /**
     * See {@link <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.17">HTTP/1.1 documentation</a>}.
     * {@code "Content-Type"}'s protocol parameter name;
     */
    public static final String PROTOCOL_PARAMETER = "protocol";
    private static final String DEFAULT_PROTOCOL_KEY = "default";
    private static final String COMPACT_PROTOCOL = "compact";
    private static final String BINARY_PROTOCOL = "binary";
    private static final String JSON_PROTOCOL = "json";
    private static final String SIMPLE_JSON_PROTOCOL = "simple";
    private static final String TUPLE_PROTOCOL = "tuple";
    private static Map<String, TProtocolFactory> supportedProtocol;

    static {
        HashMap<String, TProtocolFactory> map = new HashMap<>();
        map.put(COMPACT_PROTOCOL, new TCompactProtocol.Factory());
        map.put(BINARY_PROTOCOL, new TBinaryProtocol.Factory());
        map.put(JSON_PROTOCOL, new TJSONProtocol.Factory());
        map.put(SIMPLE_JSON_PROTOCOL, new TSimpleJSONProtocol.Factory());
        map.put(TUPLE_PROTOCOL, new TTupleProtocol.Factory());
        map.put(DEFAULT_PROTOCOL_KEY, map.get(SIMPLE_JSON_PROTOCOL));
        supportedProtocol = Collections.unmodifiableMap(map);
    }

    public static boolean isSupportedProtocol(String protocol) {
        if (Strings.isNullOrEmpty(protocol)) {
            return false;
        }
        return supportedProtocol.containsKey(protocol);
    }

    public static void appendProtocolIfValid(MediaType mediaType, String protocol) {
        if (null != mediaType && isSupportedProtocol(protocol)) {
            mediaType.getParameters().put(PROTOCOL_PARAMETER, protocol);
        }
    }

    /**
     * Extract protocol parameter from the specified media type.
     *
     * @param mediaType It's your responsibility to make sure this is not null.
     * @return Empty string or the real protocol parameter
     */
    public static String extractProtocolParameterValue(MediaType mediaType) {
        return Strings.nullToEmpty(mediaType.getParameters().get(PROTOCOL_PARAMETER)).toLowerCase();
    }

    public static TProtocolFactory getDefaultProtocolFactory() {
        return supportedProtocol.get(DEFAULT_PROTOCOL_KEY);
    }

    public static TProtocolFactory getProtocolFactory(MediaType mediaType) {
        return getProtocolFactory(extractProtocolParameterValue(mediaType));
    }

    public static TProtocolFactory getProtocolFactory(String protocol) {
        if (isSupportedProtocol(protocol)) {
            return supportedProtocol.get(protocol);
        } else {
            return getDefaultProtocolFactory();
        }
    }
}
