package me.ellios.hedwig.common.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Get the spring application context in the current application.
 * This class need to be initialized in the spring configuration file.
 *
 * @author George Cao
 * @see me.ellios.hedwig.common.spring.SingletonApplicationContext
 * @since 2014-01-09 21
 */
public class ApplicationContextProvider implements ApplicationContextAware {
    private static final Logger LOG = LoggerFactory.getLogger(ApplicationContextProvider.class);


    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        LOG.info("Application context injected {}", applicationContext);
        ApplicationContextProvider.applicationContext = applicationContext;
    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }
}
