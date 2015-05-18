package me.ellios.hedwig.http.server;

import me.ellios.hedwig.http.service.TRestService;
import me.ellios.hedwig.http.service.impl.TRestServiceImpl;
import me.ellios.hedwig.rpc.core.ServiceConfig;
import me.ellios.hedwig.rpc.core.ServiceSchema;
import me.ellios.hedwig.rpc.core.ServiceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Say something?
 *
 * @author George Cao
 * @since 4/18/13 11:06 AM
 */
public class HttpServerStarter {
    private static final Logger LOG = LoggerFactory.getLogger(HttpServerStarter.class);
    private static volatile HttpServer server;

    private HttpServerStarter() {
    }

    public static HttpServer createHttpServer() {
        HttpServer newServer = server;
        if (null == newServer) {
            synchronized (HttpServerStarter.class) {
                newServer = server;
                if (null == newServer) {
                    newServer = new HttpServer();
                    ServiceConfig config = ServiceConfig.newBuilder()
                            .serviceFace(TRestService.Iface.class)
                            .serviceImpl(TRestServiceImpl.class)
                            .port(9080)
                            .schema(ServiceSchema.HTTP)
                            .type(ServiceType.THRIFT)
                            .build();
                    newServer.registerService(config);

                    server = newServer;
                }
            }
        }
        return newServer;
    }
}
