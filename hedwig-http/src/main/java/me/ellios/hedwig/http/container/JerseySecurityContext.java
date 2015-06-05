package me.ellios.hedwig.http.container;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.SecurityContext;
import java.security.Principal;

/**
 * Say something?
 *
 * @author George Cao
 * @since 2014-03-24 18:54
 */
public class JerseySecurityContext implements SecurityContext {
    private static final Logger LOG = LoggerFactory.getLogger(JerseySecurityContext.class);

    @Override
    public Principal getUserPrincipal() {
        return null;
    }

    @Override
    public boolean isUserInRole(String role) {
        return false;
    }

    @Override
    public boolean isSecure() {
        return false;
    }

    @Override
    public String getAuthenticationScheme() {
        return null;
    }
}
