package me.ellios.hedwig.rpc.core;

/**
 * Service Schema refactored out from {@link me.ellios.hedwig.rpc.core.ServiceConfig}.
 *
 * @author George Cao
 * @since 4/26/13 11:43 AM
 */
public enum ServiceSchema {
    HTTP, TCP, SPDY, MEMCACHED;

    /**
     * 判断协议是否支持
     *
     * @param schemaName
     * @return
     */
    public static boolean isSupport(String schemaName) {
        for (ServiceSchema schema : values()) {
            if (schema.name().equalsIgnoreCase(schemaName)) {
                return true;
            }
        }
        return false;
    }
}
