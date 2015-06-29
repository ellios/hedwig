package me.ellios.memcached.config.support;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import me.ellios.memcached.config.Config;
import me.ellios.memcached.config.ConfigFactory;
import me.ellios.memcached.config.ConfigListener;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User: ellios
 * Time: 13-9-24 : 下午3:38
 */
public abstract class BaseConfigFactory implements ConfigFactory {

    protected static final Logger LOG = LoggerFactory.getLogger(BaseConfigFactory.class);
    private final String configName;
    private volatile String servers;
    private volatile ConfigListener listener;

    protected BaseConfigFactory(String configName) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(configName));
        this.configName = configName;
        refreshServers(configName);
    }

    public String getConfigName() {
        return configName;
    }

    public String getServers() {
        return servers;
    }

    @Override
    public Config getConfig() {
        return build(configName, servers);
    }

    @Override
    public void attachChangeListener(ConfigListener listener) {
        if(listener == null){
            LOG.warn("config listener is null. will not attachChangeListener.");
            return;
        }
        if(this.listener != null){
            LOG.warn("config listener has been attached. will not attachChangeListener");
            return;
        }
        this.listener = listener;
        doAttachChangeListener(listener);
    }

    protected Config build(String name, String conn) {
        if (StringUtils.isEmpty(name) || StringUtils.isBlank(conn)) {
            return null;
        }
        return Config.parseConnString(name, conn);
    }

    protected void refreshServers(String configName){
        String servers = doGetServers(configName);
        if (StringUtils.isEmpty(servers)) {
            LOG.warn("fail to init redis servers. servers is empty. configName : {}", getConfigName());
            return;
        }
        this.servers = servers;
    }

    protected abstract String doGetServers(String configName);

    protected abstract void doAttachChangeListener(ConfigListener listener);
}
