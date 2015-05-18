package me.ellios.hedwig.http.mediatype;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.Map;

/**
 * Extended Media Type for thrift and protobuf.
 *
 * @author George Cao
 * @since 4/15/13 5:39 PM
 */
public class ExtendedMediaType extends MediaType {
    private static final Logger LOG = LoggerFactory.getLogger(ExtendedMediaType.class);

    private static Map<String, String> parameters = new HashMap<>();

    static {
        parameters.put("protocol", "simple");
        parameters.put("charset", "utf-8");
    }

    /**
     * "application/x-thrift"
     * "application/x-thrift; protocol=json"
     */
    public final static MediaType APPLICATION_X_THRIFT_TYPE = new MediaType("application", "x-thrift", parameters);
    /**
     * "application/x-thrift"
     */
    public final static String APPLICATION_X_THRIFT = "application/x-thrift;protocol=simple";
}
