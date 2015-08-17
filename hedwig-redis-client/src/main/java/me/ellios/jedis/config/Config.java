package me.ellios.jedis.config;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import me.ellios.jedis.Constants;
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
    private ServerMode serverMode;
    private List<RedisNode> nodes = Lists.newArrayList();
    private String sentinelName;
    private String password;
    private int db = 0;

    public String getSentinelName() {
        return sentinelName;
    }

    public void setSentinelName(String sentinelName) {
        this.sentinelName = sentinelName;
    }

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

    public ServerMode getServerMode() {
        return serverMode;
    }

    public void setServerMode(ServerMode serverMode) {
        this.serverMode = serverMode;
    }

    public List<RedisNode> getNodes() {
        return nodes;
    }

    public void setNodes(List<RedisNode> nodes) {
        this.nodes = nodes;
    }

    public void addNode(RedisNode node) {
        this.nodes.add(node);
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getDb() {
        return db;
    }

    public void setDb(int db) {
        this.db = db;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Config{");
        sb.append("sentinelName='").append(sentinelName).append('\'');
        sb.append(", schema=").append(schema);
        sb.append(", name='").append(name).append('\'');
        sb.append(", serverMode=").append(serverMode);
        sb.append(", nodes=").append(nodes);
        sb.append('}');
        return sb.toString();
    }

    /**
     * 解析格式化的连接字符串,格式为redis://127.0.0.1:6379,127.0.0.1:6380?mode=sentinel
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
        if(!StringUtils.equals(Schema.REDIS.name(), StringUtils.upperCase(uri.getScheme()))){
            LOG.warn("illegal schema : {}", uri.getScheme());
            return null;
        }

        Config config = new Config();
        config.setSchema(Schema.REDIS);
        config.setName(name);

        String query = uri.getQuery();
        Map<String, String> params = parseQuery(query);
        ServerMode serverMode = ServerMode.valueOfMode(params.get("mode"));
        if(serverMode == null){
            //默认是sentinel模式
            serverMode = ServerMode.SENTINEL;
        }
        config.setServerMode(serverMode);
        if(serverMode == ServerMode.SENTINEL){
            config.setSentinelName(MapUtils.getString(params, "sentinel", name));
        }
        config.setPassword(MapUtils.getString(params, "password"));
        config.setDb(MapUtils.getIntValue(params, "db", 0));

        String servers = uri.getAuthority();
        int index = 0;
        for(String server : servers.split(Constants.COMMA)){
            RedisNode node = RedisNode.parseNodeString(server);
            if(serverMode == ServerMode.MASTER_SLAVE && index == 0){
                node.setMaster(true);
            }
            index++;
            config.addNode(node);
        }

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


    public static class RedisNode {

        private String host;
        private int port;
        private int fail;
        private boolean master;

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public int getFail() {
            return fail;
        }

        public void setFail(int fail) {
            this.fail = fail;
        }

        public void incFail() {
            this.fail += 1;
        }

        public boolean isMaster() {
            return master;
        }

        public void setMaster(boolean master) {
            this.master = master;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof RedisNode)) return false;

            RedisNode redisNode = (RedisNode) o;

            if (port != redisNode.port) return false;
            if (!host.equals(redisNode.host)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = host.hashCode();
            result = 31 * result + port;
            return result;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("RedisNode{");
            sb.append("host='").append(host).append('\'');
            sb.append(", port=").append(port);
            sb.append(", fail=").append(fail);
            sb.append(", master=").append(master);
            sb.append('}');
            return sb.toString();
        }

        /**
         * 解析节点字符串，格式为127.0.0.1:6379
         *
         * @param nodeString
         * @return
         */
        public static RedisNode parseNodeString(String nodeString) {
            if (StringUtils.isEmpty(nodeString)) {
                return null;
            }
            String[] elems = StringUtils.split(nodeString, Constants.COLON);
            String host = elems[0];
            int port = 6379;
            int weight = 1;

            if (elems.length > 1) {
                port = NumberUtils.toInt(elems[1]);
            }
            RedisNode node = new RedisNode();
            node.setHost(host);
            node.setPort(port);
            return node;
        }
    }


    public static enum Schema {
        REDIS;
    }
}
