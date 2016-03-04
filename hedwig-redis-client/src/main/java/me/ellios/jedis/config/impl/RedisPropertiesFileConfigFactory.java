package me.ellios.jedis.config.impl;

import me.ellios.hedwig.common.utils.ClassLoaderUtils;
import me.ellios.jedis.config.ConfigListener;
import me.ellios.jedis.config.support.BaseConfigFactory;
import me.ellios.jedis.util.RedisConfigFileParsers;

import java.io.IOException;
import java.nio.file.*;

/**
 * User: ellios
 * Time: 13-9-24 : 下午4:33
 */
public class RedisPropertiesFileConfigFactory extends BaseConfigFactory {

    public RedisPropertiesFileConfigFactory(String configName) {
        super(configName);
    }

    @Override
    protected String doGetServers(String configName) {
        return RedisConfigFileParsers.getRedisServers(configName);
    }

    @Override
    protected void doAttachChangeListener(ConfigListener listener) {
        Thread thread = new Thread(new RedisPropertyFileChangeTask(listener));
        thread.start();
    }

    class RedisPropertyFileChangeTask implements Runnable {

        private ConfigListener listener;

        RedisPropertyFileChangeTask(ConfigListener listener) {
            this.listener = listener;
        }

        @Override
        public void run() {
            Path configFilePath = Paths.get(RedisConfigFileParsers.getConfigFileAbsolutePath());
            Path watchDir = configFilePath.getParent();
            try {
                try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
                    watchDir.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
                    while (true) {
                        final WatchKey watchKey = watchService.take();
                        for (WatchEvent<?> watchEvent : watchKey.pollEvents()) {
                            final WatchEvent.Kind<?> kind = watchEvent.kind();
                            LOG.info("file : {} has changed. event : {}", watchEvent.context(), watchEvent);

                            //handle OVERFLOW event
                            if (kind == StandardWatchEventKinds.OVERFLOW) {
                                continue;
                            }

                            Path changedFilePath = (Path) watchEvent.context();
                            if (changedFilePath.getFileName().toString().equals(configFilePath.getFileName().toString())
                                    && kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                                refreshServers(getConfigName());
                                listener.onChange(getConfig());
                                LOG.info("finish refreshing config. configFile : {}, configName : {}",
                                        watchEvent.context(), getConfigName());
                            }
                        }
                        watchKey.reset();
                        if (!watchKey.isValid()) {
                            break;
                        }
                    }
                }
            } catch (IOException e) {
                LOG.error("fail to addChangeListener, configName : {}", getConfigName(), e);
            } catch (InterruptedException e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }
}
