package me.ellios.hedwig.http.container;

import me.ellios.hedwig.common.spring.SpringContainer;
import me.ellios.hedwig.http.filter.AcceptProtocolResponseFilter;
import me.ellios.hedwig.http.filter.PrimitivesResponseFilter;
import org.glassfish.jersey.server.ContainerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseFilter;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Say something?
 *
 * @author George Cao
 * @since 4/26/13 1:53 PM
 */
public class ContainerBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(ContainerBuilder.class);

    public static ContainerBuilder newBuilder() {
        return new ContainerBuilder();
    }

    private Set<String> packages = new LinkedHashSet<>();

    public ContainerBuilder packages(String... packages) {
        for (String pkg : packages) {
            this.packages.add(pkg);
        }
        return this;
    }

    public ContainerBuilder packages(Set<String> packages) {
        this.packages.addAll(packages);
        return this;
    }


    private Set<Class<?>> resources = new LinkedHashSet<>();

    public ContainerBuilder resources(Class<?>... resources) {
        for (Class<?> klass : resources) {
            this.resources.add(klass);
        }
        return this;
    }

    public ContainerBuilder resources(Set<Class<?>> resources) {
        this.resources.addAll(resources);
        return this;
    }

    private List<ContainerRequestFilter> requestFilters = new LinkedList<>();
    private List<ContainerRequestFilter> responseFilters = new LinkedList<>();

    public ContainerBuilder filter(Object filter) {
        if (filter instanceof ContainerResponseFilter) {
            responseFilters.add((ContainerRequestFilter) filter);
        } else if (filter instanceof ContainerRequestFilter) {
            requestFilters.add((ContainerRequestFilter) filter);
        }
        return this;
    }

    protected ConfigurableApplicationContext getSpringContext() {
        return SpringContainer.getContext();
    }

    private ConfigurableApplicationContext applicationContext;

    public ContainerBuilder applicationContext(ConfigurableApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        return this;
    }

    /**
     * Get the application context.
     *
     * @param contextConfigLocation the location of the application context.
     * @return the child application context.
     */
    protected ConfigurableApplicationContext getSpringContext(String contextConfigLocation) {
        final ConfigurableApplicationContext ctx = new ClassPathXmlApplicationContext(contextConfigLocation);
        ctx.refresh();
        return ctx;
    }

    public <C> C build(Class<C> clazz) {
        String[] pkg = new String[packages.size()];
        ResourceConfig config = new ResourceConfig(resources);
        config.packages(packages.toArray(pkg));
        config.register(AcceptProtocolResponseFilter.class);
        config.register(PrimitivesResponseFilter.class);
        if (null == applicationContext) {
            LOG.warn("You did NOT configure a spring application context, we will try to find one.");
            applicationContext = getSpringContext();
        }
        config.property("contextConfig", applicationContext);
        return ContainerFactory.createContainer(clazz, config);
    }
}
