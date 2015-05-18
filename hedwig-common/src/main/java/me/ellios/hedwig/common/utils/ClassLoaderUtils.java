/*
 * This is  a part of the Video Resource System(VRS).
 * Copyright (C) 2010-2012 iqiyi.com Corporation
 * All rights reserved.
 *
 * Licensed under the iqiyi.com private License.
 */
package me.ellios.hedwig.common.utils;

import me.ellios.hedwig.common.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * @author wangwp
 * @version $Id: ClassLoaderUtils.java 8525 2009-11-25 04:10:10Z wangweiping $
 */
public abstract class ClassLoaderUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClassLoaderUtils.class);
    private static final Properties EMPTY_PROPERTIES = new Properties();

    private static ClassLoader classLoader;

    /**
     * 获得资源真实文件路径
     *
     * @param resource 资源
     * @return
     */
    public static String getPath(String resource) {
        URL url = getURL(resource);
        if (null != url) {
            return url.getPath();
        }
        return "";
    }

    public static URL getURL(String resource) {
        return getClassLoader().getResource(resource);
    }

    /**
     * 创建指定类的实例
     *
     * @param clazzName 类名
     * @return
     */
    public static Object getInstance(String clazzName) {
        try {
            return loadClass(clazzName).newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            LOGGER.error("Create new instance of {} error.", clazzName, e);
        }
        return null;
    }

    /**
     * 根据指定的类名加载类
     *
     * @param name 类名
     * @return
     */
    public static Class<?> loadClass(String name) {
        try {
            return getClassLoader().loadClass(name);
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static ClassLoader getClassLoader() {
        if (classLoader != null) {
            return classLoader;
        }
        classLoader = Thread.currentThread().getContextClassLoader();
        // Context class loader may be null.
        if (null == classLoader) {
            classLoader = ClassLoaderUtils.class.getClassLoader();
        }
        return classLoader;

    }

    /**
     * 将资源文件加载到输入流中
     *
     * @param resource 资源文件
     * @return
     */
    public static InputStream getStream(String resource) {
        return getClassLoader().getResourceAsStream(resource);
    }

    public static InputStream getStreamNoJvmCache(String resource) {
        URL url = getURL(resource);
        if (null != url) {
            try {
                return url.openStream();
            } catch (IOException e) {
                LOGGER.error("Open resource stream {} error", resource, e);
            }
        }
        return null;
    }

    /**
     * 将资源文件转化为Properties对象
     *
     * @param resource 资源文件
     * @return
     */
    public static Properties getProperties(String resource) {
        try (InputStream is = getStream(resource)) {
            if (null != is) {
                Properties properties = new Properties();
                properties.load(is);
                return properties;
            }
        } catch (IOException ex) {
            LOGGER.warn("Read property resource {} error.", resource, ex);
        }
        return EMPTY_PROPERTIES;
    }

    /**
     * 将资源文件转化为Reader
     *
     * @param resource 资源文件
     * @return
     */
    public static Reader getReader(String resource) {
        InputStream is = getStream(resource);
        if (is == null) {
            return null;
        }
        return new BufferedReader(new InputStreamReader(is, Constants.DEFAULT_CHARSET));
    }

    public static BufferedReader getBufferedReader(String resource) {
        InputStream is = getStream(resource);
        if (is == null) {
            return null;
        }
        return new BufferedReader(new InputStreamReader(is, Constants.DEFAULT_CHARSET));
    }

    /**
     * 将资源文件的内容转化为List实例
     *
     * @param resource 资源文件
     * @return
     */
    public static List<String> getList(String resource) {
        try (BufferedReader reader = getBufferedReader(resource)) {
            if (null == reader) {
                return Collections.emptyList();
            }
            List<String> result = new ArrayList<>();
            for (; ; ) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }
                result.add(line);
            }
            return result;
        } catch (IOException e) {
            LOGGER.error("将资源文件 {} 转化为list出现异常", resource, e);
        }
        return Collections.emptyList();
    }
}
