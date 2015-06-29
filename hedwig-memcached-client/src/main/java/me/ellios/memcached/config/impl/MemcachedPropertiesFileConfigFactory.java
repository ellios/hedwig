package me.ellios.memcached.config.impl;

import me.ellios.memcached.config.ConfigListener;
import me.ellios.memcached.config.support.BaseConfigFactory;
import me.ellios.memcached.utils.MemcachedConfigFileParsers;

import java.io.IOException;
import java.nio.file.*;

/**
 * User: ellios
 * Time: 13-9-24 : 下午4:33
 */
public class MemcachedPropertiesFileConfigFactory extends BaseConfigFactory {

    public MemcachedPropertiesFileConfigFactory(String configName) {
        super(configName);
    }

    @Override
    protected String doGetServers(String configName) {
        return MemcachedConfigFileParsers.getMemcachedServers(configName);
    }

    @Override
    protected void doAttachChangeListener(ConfigListener listener) {
        Thread thread = new Thread(new MemcachedPropertyFileChangeTask(listener));
        thread.start();
    }

    class MemcachedPropertyFileChangeTask implements Runnable {

        private ConfigListener listener;

        MemcachedPropertyFileChangeTask(ConfigListener listener) {
            this.listener = listener;
        }

        @Override
        public void run() {
            Path configFilePath = Paths.get(MemcachedConfigFileParsers.getConfigFileAbsolutePath());
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
