package me.ellios.hedwig.http.service.impl;

import me.ellios.hedwig.http.service.TRestService;
import me.ellios.hedwig.http.service.model.TPayload;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static me.ellios.hedwig.http.mediatype.ExtendedMediaType.APPLICATION_X_THRIFT;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;

/**
 * Say something?
 *
 * @author George Cao
 * @since 4/18/13 7:12 PM
 */
@Path("/rest")
@Produces({APPLICATION_X_THRIFT})
public class TRestServiceImpl implements TRestService.Iface {
    private static final Logger LOG = LoggerFactory.getLogger(TRestServiceImpl.class);

    @POST
    @Path("/batch/2")
    @Consumes({APPLICATION_X_THRIFT})
    @Produces({TEXT_PLAIN})
    @Override
    public long multiPost(@Context TPayload p1, TPayload p2) throws TException {
        throw new UnsupportedOperationException("cannot read this kind of data from underlying stream.");
    }

    @POST
    @Path("/mixin/{p1:\\d+}/{id:\\d+}")
    @Consumes({APPLICATION_X_THRIFT})
    @Produces({TEXT_PLAIN})
    @Override
    public long mixin(@PathParam("p1") long p1, TPayload p2, @PathParam("id") int id) throws TException {
        LOG.info("{}-{}-{}", p1, id, p2);
        return p1 + id;
    }

    @POST
    @Path("/create/{pid:\\d+}")
    @Consumes({APPLICATION_X_THRIFT})
    @Produces({TEXT_PLAIN})
    @Override
    public long create(@PathParam("pid") long p1, TPayload p2) throws TException {
        LOG.info("{}-{}", p1, p2);
        if (p2 == null) {
            return 0;
        }

        return p1;
    }

    @POST
    @Consumes({APPLICATION_X_THRIFT})
    @Produces({TEXT_PLAIN})
    @Override
    public long post(TPayload payload) throws TException {
        long id = ThreadLocalRandom.current().nextLong(Long.MAX_VALUE);
        LOG.info("Created payload with id: {}, {}, ", id, payload);
        return id;
    }

    @POST
    @Path("/batch/list")
    @Consumes({APPLICATION_X_THRIFT})
    @Produces({TEXT_PLAIN})
    @Override
    public long batch(List<TPayload> payloads) throws TException {
        return payloads.size();
    }

    @GET
    @Path("/{id:\\d+}")
    @Override
    public TPayload fetch(@PathParam("id") long id) throws TException {
        TPayload payload = new TPayload();
        int userId = ThreadLocalRandom.current().nextInt(6555);
        LOG.info("Id: {},UserId: {}", id, userId);
        payload.setUserId(userId);
        payload.setUserName("George");
        payload.setText(UUID.randomUUID().toString());
        return payload;
    }

    @GET
    @Path("/multi/")
    @Override
    public List<TPayload> multiGet(@QueryParam("tid") List<Long> idList) throws TException {
        List<TPayload> payloads = new ArrayList<>(idList.size());
        for (Long id : idList) {
            payloads.add(fetch(id));
        }
        return payloads;
    }

    @Path("/map/{id:\\d+}")
    @GET
    @Override
    public Map<Long, TPayload> fetchMap(@PathParam("id") long id) throws TException {
        Map<Long, TPayload> map = new HashMap<>();
        map.put(id, new TPayload(12, "fetch", "map"));
        return map;
    }

    @GET
    @Path("/set/{id:\\d+}")
    @Override
    public Set<TPayload> fetchSet(@PathParam("id") long id) throws TException {
        Set<TPayload> set = new HashSet<>();
        set.add(fetch(id));
        return set;
    }
}
