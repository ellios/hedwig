package me.ellios.hedwig.common.spring;

import me.ellios.hedwig.common.exceptions.HedwigException;
import me.ellios.hedwig.common.instances.ServiceImplContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * In the same jvm, we should initialize only one instance of {@link org.springframework.context.ApplicationContext}.
 */
public class SpringContainer implements ServiceImplContainer {

    private static final Logger LOG = LoggerFactory.getLogger(SpringContainer.class);

    /**
     * You can use {@link SingletonApplicationContext#getApplicationContext()} directly.
     *
     * @return the application context.
     */
    public static ClassPathXmlApplicationContext getContext() {
        return SingletonApplicationContext.getSingleton().getApplicationContext();
    }

    @Override
    public <T> T getServiceImpl(Class<T> clazz) {
        LOG.info("Instance class: {} from spring container", clazz);
        try {
            return getContext().getBean(clazz);
        } catch (BeansException e) {
            throw new HedwigException(e);
        }
    }
}
