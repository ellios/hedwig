package me.ellios.hedwig.memcached.example;


import me.ellios.hedwig.http.mediatype.ExtendedMediaType;

import javax.ws.rs.*;

/**
 * hello service
 *
 * @author gaofeng@qiyi.com
 * @since: 14-3-17
 */
@Path("/")
@Produces(ExtendedMediaType.APPLICATION_X_THRIFT)
public class HelloService {
    @GET
    @Path("memcached/hello")
    public String hello() {
        return "hello";
    }

    @GET
    @Path("/memcached/hello/p")
    public String helloParameters(@PathParam("name") String name, @PathParam("age") Integer age) {
        return "name=" + name + ",age=" + age;
    }

    @GET
    @Path("/memcached/hello/user/{uid:\\d+}")
    public String rest(@PathParam("uid") long uid) {
        return "uid=" + uid;
    }

    @GET
    @Path("/memcached/hello/user/{name:\\w+}/info")
    public String restWithParameters(@PathParam("name") String name, @PathParam("age") @DefaultValue("16") Integer age) {
        return "name=" + name + ",age=" + age;
    }
}
