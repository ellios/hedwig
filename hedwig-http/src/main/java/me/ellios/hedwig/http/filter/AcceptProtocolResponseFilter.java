package me.ellios.hedwig.http.filter;

import me.ellios.hedwig.http.provider.util.ThriftProviderUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.List;

import static me.ellios.hedwig.http.provider.util.ThriftProviderUtils.*;


/**
 * Say something?
 *
 * @author George Cao
 * @since 4/19/13 3:22 PM
 */
public class AcceptProtocolResponseFilter implements ContainerResponseFilter {
    private static final Logger LOG = LoggerFactory.getLogger(AcceptProtocolResponseFilter.class);

    private String getAcceptProtocol(ContainerRequestContext request) {
        String protocol = "";
        List<MediaType> acceptableMediaTypes = request.getAcceptableMediaTypes();
        for (MediaType mediaType : acceptableMediaTypes) {
            protocol = extractProtocolParameterValue(mediaType);
            if (isSupportedProtocol(protocol)) {
                break;
            }
        }
        return protocol;
    }

    @Override
    public void filter(ContainerRequestContext request, ContainerResponseContext response) throws IOException {
        MediaType mediaTye = response.getMediaType();
        if (null != mediaTye && !isSupportedProtocol(extractProtocolParameterValue(mediaTye))) {
            String protocol = getAcceptProtocol(request);
            LOG.info("Content-Type does not contain protocol parameter, append {} to it.", protocol);
            appendProtocolIfValid(mediaTye, protocol);
        }
    }
}
