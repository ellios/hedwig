package me.ellios.hedwig.memcached.example;

import me.ellios.hedwig.http.mediatype.ExtendedMediaType;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

/**
 * parser service
 *
 * @author gaofeng
 * @since: 14-3-17
 */
@Path("/user")
@Produces(ExtendedMediaType.APPLICATION_X_THRIFT)
public class ParserService {

    @GET
    @Path("/memcached/hello/p/string")
    public String helloString(@PathParam("p") String p) {
        return p;
    }

    @GET
    @Path("/memcached/hello/p/b")
    public String helloBoolean(@PathParam("p") boolean p) {
        return String.valueOf(p);
    }

    @GET
    @Path("/memcached/hello/p/byte")
    public String helloByte(@PathParam("p") byte p) {
        return String.valueOf(p);
    }

    @GET
    @Path("/memcached/hello/p/char")
    public String helloChar(@PathParam("p") char p) {
        return String.valueOf(p);
    }

    @GET
    @Path("/memcached/hello/p/double")
    public String helloDouble(@PathParam("p") double p) {
        return String.valueOf(p);
    }

    @GET
    @Path("/memcached/hello/p/float")
    public String helloFloat(@PathParam("p") float p) {
        return String.valueOf(p);
    }

    @GET
    @Path("/memcached/hello/p/int")
    public String helloInt(@PathParam("p") int p) {
        return String.valueOf(p);
    }

    @GET
    @Path("/memcached/hello/p/short")
    public String helloShort(@PathParam("p") short p) {
        return String.valueOf(p);
    }
}
