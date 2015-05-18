package me.ellios.hedwig.rpc.core;


import me.ellios.hedwig.common.Constants;
import me.ellios.hedwig.common.utils.ClassHelper;

/**
 * Author: ellios
 * Date: 12-11-16 Time: 下午1:38
 * TODO We need a new alg. for service name generation which will take {@link me.ellios.hedwig.rpc.core.ServiceSchema} into account.
 */
public class ServiceConfigHelper {

    /**
     * 根据类名获取默认的pb服务名
     *
     * @param serviceFace
     * @return
     */
    public static String buildDefaultPbServiceName(Class serviceFace) {
        return buildDefaultServiceName(Constants.DEFAULT_PB_SERVICE_PREFIX, serviceFace);
    }

    /**
     * Build the service based on {@link ServiceType} ans interface.
     *
     * @param type        service type.
     * @param serviceFace service interface.
     * @return service name.
     */
    public static String buildDefaultServiceName(ServiceType type, Class serviceFace) {
        switch (type) {
            case THRIFT:
                return buildDefaultThriftServiceName(serviceFace);
            case PROTOBUF:
                return buildDefaultPbServiceName(serviceFace);
            default:
                throw new IllegalArgumentException("Not supported type " + type);
        }
    }

    /**
     * 根据类名获取默认的thrift服务名
     *
     * @param serviceFace
     * @return
     */
    public static String buildDefaultThriftServiceName(Class serviceFace) {
        return buildDefaultServiceName(Constants.DEFAULT_THRIFT_SERVICE_PREFIX, serviceFace);
    }

    /**
     * Generate service name from interface and type prefix.
     *
     * @param prefix      type prefix
     * @param serviceFace service interface
     * @return service name.
     */
    private static String buildDefaultServiceName(String prefix, Class serviceFace) {
        return prefix + ClassHelper.getOuterClassName(serviceFace);
    }
}
