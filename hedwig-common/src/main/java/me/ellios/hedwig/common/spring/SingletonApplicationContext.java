package me.ellios.hedwig.common.spring;

import com.google.common.base.Strings;
import me.ellios.hedwig.common.config.HedwigConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * In order to share the single on application context, we use this singleton.
 *
 * @author George Cao
 * @since 2014-01-10 12
 */
public class SingletonApplicationContext {
    private static final Logger LOG = LoggerFactory.getLogger(SingletonApplicationContext.class);

    private static volatile SingletonApplicationContext singleton;

    private SingletonApplicationContext() {
        loadApplicationContext();
    }

    public static final String SPRING_CONFIG = "hedwig.spring.config";

    public static final String DEFAULT_SPRING_CONFIG = "classpath*:META-INF/spring/*.xml";

    private ClassPathXmlApplicationContext applicationContext;

    public ClassPathXmlApplicationContext getApplicationContext() {
        return applicationContext;
    }

    private void loadApplicationContext() {
        String configPath = HedwigConfig.getInstance().getString(SPRING_CONFIG, "");
        if (Strings.isNullOrEmpty(configPath)) {
            configPath = DEFAULT_SPRING_CONFIG;
        }
        LOG.info("Load spring configuration file from {}", configPath);
        applicationContext = new ClassPathXmlApplicationContext(configPath.split("[,\\s]+"));
    }

    public static SingletonApplicationContext getSingleton() {
        if (null == singleton) {
            synchronized (SingletonApplicationContext.class) {
                if (null == singleton) {
                    singleton = new SingletonApplicationContext();
                }
            }
        }
        return singleton;
    }
}
