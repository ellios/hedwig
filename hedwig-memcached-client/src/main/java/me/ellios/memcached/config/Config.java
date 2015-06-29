package me.ellios.memcached.config;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sun.org.apache.xpath.internal.operations.Bool;
import me.ellios.hedwig.common.Constants;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * User: ellios
 * Time: 13-7-30 : 上午10:45
 */
public class Config {

    private static final Logger LOG = LoggerFactory.getLogger(Config.class);

    private Schema schema;
    private String name;
    private String address;
    private Boolean textmode;
    private Boolean failover;
    private Boolean noop;


    public Schema getSchema() {
        return schema;
    }

    public void setSchema(Schema schema) {
        this.schema = schema;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Boolean getTextmode() {
        return textmode;
    }

    public void setTextmode(Boolean textmode) {
        this.textmode = textmode;
    }

    public Boolean getFailover() {
        return failover;
    }

    public void setFailover(Boolean failover) {
        this.failover = failover;
    }

    public Boolean getNoop() {
        return noop;
    }

    public void setNoop(Boolean noop) {
        this.noop = noop;
    }

    /**
     * 解析格式化的连接字符串,格式为memcached://127.0.0.1:6379,127.0.0.1:6380?failover=false&textmode=false
     * mode 包括 ms, cluster, sentinel
     * 当mode为ms时,表示主从,第一个server是master,其余的都是slave
     * 当mode为cluster时,表示集群表示,所有的server组成一个集群
     * 当mode为sentinel时,表示sentinel
     * @param conn
     * @return
     */
    public static Config parseConnString(String name, String conn) {
        if (StringUtils.isBlank(conn) || StringUtils.isBlank(name)) {
            return null;
        }

        URI uri = URI.create(conn);
        if(!StringUtils.equals(Schema.MEMCACHED.name(), StringUtils.upperCase(uri.getScheme()))){
            LOG.warn("illegal schema : {}", uri.getScheme());
            return null;
        }

        Config config = new Config();
        config.setSchema(Schema.MEMCACHED);
        config.setName(name);

        String query = uri.getQuery();
        Map<String, String> params = parseQuery(query);
        config.setFailover("true".equals(String.valueOf(params.get("failover"))));
        config.setTextmode("true".equals(String.valueOf(params.get("textmode"))));
        config.setNoop("true".equals(String.valueOf(params.get("noop"))));
        config.setAddress(uri.getAuthority());

        return config;

    }

    public static Map<String, String> parseQuery(String query){
        if(StringUtils.isEmpty(query)){
            return Maps.newHashMap();
        }
        Map<String, String> params = Maps.newHashMap();
        for(String param : query.split(Constants.AMPERSAND)){
            String[] tuple = param.split(Constants.EQUALITY);
            params.put(tuple[0], tuple[1]);
        }
        return params;
    }


    public static enum Schema {
        MEMCACHED;
    }
}
