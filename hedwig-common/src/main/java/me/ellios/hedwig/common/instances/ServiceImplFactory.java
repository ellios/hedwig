package me.ellios.hedwig.common.instances;

/**
 * Author: ellios
 * Date: 12-11-14 Time: 上午11:09
 */
public class ServiceImplFactory {

    private static ServiceImplContainer container = new DefaultServiceImplContainer();

    public static ServiceImplContainer getContainer() {
        return container;
    }

    public static void setContainer(ServiceImplContainer container) {
        ServiceImplFactory.container = container;
    }

    public static <T> T getServiceImpl(Class<T> clazz){
        return container.getServiceImpl(clazz);
    }
}
