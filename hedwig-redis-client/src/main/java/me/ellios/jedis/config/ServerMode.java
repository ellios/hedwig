package me.ellios.jedis.config;

/**
 * User: ellios
 * Time: 15-5-28 : 上午11:10
 */
public enum ServerMode {
    MASTER_SLAVE("ms"),
    SENTINEL("sentinel"),
    CLUSTER("cluster"),;

    private String mode;

    ServerMode(String mode) {
        this.mode = mode;
    }

    public String getMode() {
        return mode;
    }

    public static ServerMode valueOfMode(String mode){
        for(ServerMode value : values()){
            if(value.getMode().equals(mode)){
                return value;
            }
        }
        return null;
    }
}
