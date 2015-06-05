package me.ellios.hedwig.memcached.resource.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * url rule object.
 *
 * @author gaofeng
 * @since: 14-3-19
 */
public class UriRule {
    private static final Logger LOG = LoggerFactory.getLogger(UriRule.class);
    private static final Pattern AND_PATTERN = Pattern.compile("&");
    private static final Pattern EQ_PATTERN = Pattern.compile("=");
    private static final Pattern JSON_PATTERN = Pattern.compile("\\{.*?:.*?}");

    private String uri;

    private final Map<String, String> parameters = new HashMap<>();


    public UriRule(String url) {
        try {
            url = URLDecoder.decode(url, "utf-8");
        } catch (UnsupportedEncodingException e) {
            LOG.error("url cann't decode.url={}", url, e);
            url = "";
        }
        uri = url;
        String queryString = getQueryString(url);
        initParameters(queryString);
    }

    public String getUri() {
        return uri;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public String getParameter(String name) {
        return parameters.get(name);
    }

    public boolean isSimpleMatchUri() {
        return JSON_PATTERN.matcher(uri).matches();
    }

    public static boolean isSimpleMatchUri(String uri) {
        return JSON_PATTERN.matcher(uri).matches();
    }


    private String getQueryString(String url) {
        String queryString = null;
        int queryIndex = url.indexOf('?');
        if (queryIndex != -1) {
            uri = url.substring(0, queryIndex);
            queryString = url.substring(queryIndex + 1);
        }
        return queryString;
    }

    private void initParameters(String queryString) {
        if (queryString != null) {
            String[] piars = AND_PATTERN.split(queryString);
            for (String piar : piars) {
                String[] keyValue = EQ_PATTERN.split(piar);
                if (keyValue.length > 1) {
                    parameters.put(keyValue[0], keyValue[1]);
                }
            }
        }
    }
}
