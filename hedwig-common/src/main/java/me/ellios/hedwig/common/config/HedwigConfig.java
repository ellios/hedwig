package me.ellios.hedwig.common.config;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.Properties;

/**
 * 获取系统属性，如果没有的话，从配置文件获取 Author: ellios Date: 12-10-29 Time: 下午2:10
 */
public class HedwigConfig implements RpcConfig {

    private static final Logger LOG = LoggerFactory.getLogger(HedwigConfig.class);
    private static final String CONFIG_FILE_NAME = "hedwig.properties";
    private static HedwigConfig instance = new HedwigConfig();
    private Properties config = new Properties();

    private HedwigConfig() {
        init();
    }

    public static HedwigConfig getInstance() {
        return instance;
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        String value = getString(key, null);
        return value == null ? defaultValue : Boolean.valueOf(value);
    }

    public int getInt(String key, int defaultValue) {
        String value = getString(key, null);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (Exception ignored) {
            }
        }
        return defaultValue;

    }

    public long getLong(String key, long defaultValue) {
        String value = getString(key, null);
        if (value != null) {
            try {
                return Long.valueOf(value);
            } catch (Exception ignored) {
            }
        }
        return defaultValue;

    }

    /**
     * We also support get config value from System properties.
     */
    public String getString(String key, String defaultValue) {
        String value = System.getProperty(key);
        if (value == null) {
            value = config.getProperty(key, defaultValue);
        }
        return value;
    }

    private void init() {
        try {
            Properties pro = loadOrStop(CONFIG_FILE_NAME);
            if (loadAggressive()) {
                pro.putAll(loadAllProperties(CONFIG_FILE_NAME));
            }
            config = pro;
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    /**
     * Load resource from the current directory.
     *
     * @param resourceName resource name, should be an absolute path to the
     *                     resource.
     * @return {@link java.util.Properties}
     * @throws java.io.IOException read the properties file error
     */
    private Properties loadOrStop(String resourceName) throws IOException {
        if (!resourceName.startsWith("/")) {
            resourceName = "/".concat(resourceName);
        }
        Properties properties = new Properties();
        URL url = getClass().getResource(resourceName);
        if (null == url) {
            return properties;
        }
        URLConnection conn = url.openConnection();
        conn.setUseCaches(false);
        try (InputStream in = conn.getInputStream()) {
            properties.load(in);
            return properties;
        }
    }

    /**
     * Load all the resources with the name supplied.
     *
     * @param resourceName resource name
     * @return {@link java.util.Properties}
     * @throws java.io.IOException read the properties file error
     */
    private Properties loadAllProperties(String resourceName) throws IOException {
        ClassLoader clToUse = HedwigConfig.class.getClassLoader();
        Properties properties = new Properties();
        Enumeration<URL> urls = clToUse.getResources(resourceName);
        while (urls.hasMoreElements()) {
            URL url = urls.nextElement();
            URLConnection con = url.openConnection();
            con.setUseCaches(false);
            try (InputStream is = con.getInputStream()) {
                properties.load(is);
            }
        }
        return properties;
    }

    @Override
    public int backlog() {
        return getInt(HEDWIG_RPC_BACKLOG, 1024000);
    }

    @Override
    public boolean tcpNoDelay() {
        return getBoolean(HEDWIG_RPC_TCP_NO_DELAY, true);
    }

    @Override
    public boolean reuseAddress() {
        return getBoolean(HEDWIG_RPC_REUSE_ADDRESS, true);
    }

    @Override
    public boolean keepAlive() {
        return getBoolean(HEDWIG_RPC_KEEP_ALIVE, true);
    }

    @Override
    public int sendBufferSize() {
        return getInt(HEDWIG_RPC_SEND_BUFFER_SIZE, 8192);
    }

    @Override
    public int receiveBufferSize() {
        return getInt(HEDWIG_RPC_RECEIVE_BUFFER_SIZE, 8192);
    }

    @Override
    public int maxNumberOfServices() {
        return getInt(HEDWIG_RPC_MAX_SERVICE_THRESHOLD, 3);
    }

    @Override
    public boolean loadAggressive() {
        return getBoolean(PROPERTY_LOAD_AGGRESSIVE, false);
    }

    @Override
    public int globalConnectionsThresholds() {
        return ensurePositive(getInt(HEDWIG_RPC_GLOBAL_CONNECTIONS_THRESHOLDS, -1));
    }

    private int ensurePositive(int n) {
        if (n <= 0) {
            return Integer.MAX_VALUE;
        }
        return n;
    }

    @Override
    public int connectionsThresholdsPerHost() {
        return ensurePositive(getInt(HEDWIG_RPC_HOST_CONNECTIONS_THRESHOLDS, -1));
    }

    @Override
    public boolean enableConnectionCheck() {
        return getBoolean(HEDWIG_RPC_CONNECTION_CHECK, false);
    }

    @Override
    public int connectTimeoutInMillis() {
        int seconds = connectTimeoutInSeconds();
        if (seconds <= 0) {
            // default connection time out
            seconds = 5;
        }
        return getInt(HEDWIG_RPC_CONNECT_TIMEOUT_IN_MILLIS, seconds * 1000);
    }

    public int connectTimeoutInSeconds() {
        // We need resolve the old config key.
        int n = getInt("hedwig.rpc.connect.timeout", -1);
        if (n > 0) {
            return n;
        }
        return getInt(HEDWIG_RPC_CONNECT_TIMEOUT_IN_SECONDS, -1);
    }

    @Override
    public int readTimeoutInMillis() {
        int seconds = readTimeoutInSeconds();
        if (seconds <= 0) {
            // default read time out
            seconds = 3;
        }
        return getInt(HEDWIG_RPC_READ_TIMEOUT_IN_MILLIS, seconds * 1000);
    }

    public int readTimeoutInSeconds() {
        int n = getInt("hedwig.rpc.read.timeout", -1);
        if (n > 0) {
            return n;
        }
        return getInt(HEDWIG_RPC_READ_TIMEOUT_IN_SECONDS, -1);
    }

    @Override
    public int writeTimeoutInMillis() {
        int seconds = getInt(HEDWIG_RPC_WRITE_TIMEOUT_IN_SECONDS, -1);
        if (-1 == seconds) {
            // default write time out
            seconds = 3;
        }
        return getInt(HEDWIG_RPC_WRITE_TIMEOUT_IN_MILLIS, seconds * 1000);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
