package me.ellios.hedwig.http.container;

import org.glassfish.jersey.internal.PropertiesDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * Say something?
 *
 * @author George Cao(caozhangzhi@qiyi.com)
 * @since 2014-03-24 18:55
 */
public class JerseyPropertiesDelegate implements PropertiesDelegate {
    private static final Logger LOG = LoggerFactory.getLogger(JerseyPropertiesDelegate.class);

    @Override
    public Object getProperty(String s) {
        return null;
    }

    @Override
    public Collection<String> getPropertyNames() {
        return null;
    }

    @Override
    public void setProperty(String s, Object o) {

    }

    @Override
    public void removeProperty(String s) {

    }
}
