//package me.ellios.hedwig.http.server;
//
//import com.google.common.base.Function;
//import com.google.common.collect.Lists;
//import com.qiyi.vrs.hedwig.http.service.TRestService;
//import com.qiyi.vrs.hedwig.http.service.model.TPayload;
//import me.ellios.hedwig.http.mediatype.ExtendedMediaType;
//import me.ellios.hedwig.http.provider.thrift.ThriftEntityProvider;
//import me.ellios.hedwig.http.provider.thrift.ThriftListProvider;
//import me.ellios.hedwig.http.provider.thrift.ThriftMapProvider;
//import me.ellios.hedwig.http.provider.thrift.ThriftSetProvider;
//import org.apache.thrift.TException;
//import org.glassfish.jersey.client.ClientConfig;
//import org.glassfish.jersey.client.ClientResponse;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.testng.annotations.AfterMethod;
//import org.testng.annotations.BeforeMethod;
//import org.testng.annotations.Test;
//
//import javax.annotation.Nullable;
//import javax.ws.rs.client.Client;
//import javax.ws.rs.core.*;
//import java.lang.reflect.ParameterizedType;
//import java.lang.reflect.Type;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//import java.util.concurrent.ThreadLocalRandom;
//
//import static org.testng.Assert.assertEquals;
//import static org.testng.Assert.assertNotNull;
//
//
///**
// * Thrift REST service test.
// *
// * @author George Cao
// * @since 4/18/13 7:24 PM
// */
//
//public class ThriftRestServiceTest implements TRestService.Iface {
//    private static final Logger LOG = LoggerFactory.getLogger(ThriftRestServiceTest.class);
//
//    @AfterMethod
//    public void tearDown() throws Exception {
//        server.stop();
//    }
//
//    @Test(enabled = false)
//    public void testPost() throws Exception {
//        TPayload pay = new TPayload(10, "George", "SMS");
//        long id = post(pay);
//        LOG.info("Id: {}", id);
//    }
//
//    @Override
//    public long post(TPayload payload) throws TException {
//        String id = web.accept(MediaType.TEXT_PLAIN)
//                .type(ExtendedMediaType.APPLICATION_X_THRIFT_TYPE)
//                .entity(payload)
//                .post(String.class);
//        return Long.valueOf(id);
//    }
//
//    @Test(enabled = false)
//    public void testMixin() throws Exception {
//        long id = genPositiveLong();
//        int uid = genPositiveInt();
//        long idx = mixin(id, genPayload(), uid);
//        assertEquals(idx, id + uid);
//    }
//
//    @Override
//    public long mixin(long p1, TPayload p2, int id) throws TException {
//        String idx = web.path("mixin").path(String.valueOf(p1)).path(String.valueOf(id))
//                .accept(MediaType.TEXT_PLAIN_TYPE)
//                .type(ExtendedMediaType.APPLICATION_X_THRIFT_TYPE)
//                .entity(p2)
//                .post(String.class);
//        return Long.valueOf(idx);
//    }
//
//    private int genPositiveInt() {
//        return ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE);
//    }
//
//    private long genPositiveLong() {
//        return ThreadLocalRandom.current().nextLong(Long.MAX_VALUE);
//    }
//
//    @Test(enabled = false)
//    public void testCreate() throws Exception {
//        long id = genPositiveLong();
//        long expected = create(id, genPayload());
//        assertEquals(expected, id);
//    }
//
//    @Override
//    public long create(long p1, TPayload p2) throws TException {
//        String id = web.path("create")
//                .path(String.valueOf(p1))
//                .accept(MediaType.TEXT_PLAIN_TYPE)
//                .type(ExtendedMediaType.APPLICATION_X_THRIFT_TYPE)
//                .entity(p2)
//                .post(String.class);
//        return Long.valueOf(id);
//    }
//
//    @Test(enabled = false)
//    public void testMultiPost() throws Exception {
//        long size = multiPost(genPayload(), genPayload());
//        assertEquals(size, -1);
//    }
//
//    @Override
//    public long multiPost(TPayload p1, TPayload p2) throws TException {
//        ClientResponse response = web.path("/batch/2")
//                .accept(MediaType.TEXT_PLAIN_TYPE)
//                .type(ExtendedMediaType.APPLICATION_X_THRIFT_TYPE)
//                .entity(p1)
//                .entity(p2)
//                .post(ClientResponse.class);
//        assertEquals(response.getStatus(), 400);
//        return -1;
//    }
//
//
//    Client client;
//    WebResource web;
//    static final String BASE_URI = "http://127.0.0.1:9080/rest";
//    HttpServer server = HttpServerStarter.createHttpServer();
//
//    @BeforeMethod
//    public void setUp() throws Exception {
//        server.start();
//        ClientConfig config = new ClientConfig(ThriftEntityProvider.class,
//                ThriftMapProvider.class,
//                ThriftListProvider.class,
//                ThriftSetProvider.class);
//        client = Client.create(config);
//        web = client.resource(BASE_URI);
//    }
//
//    TPayload genPayload() {
//        return new TPayload(ThreadLocalRandom.current().nextInt(200), "George", "Hi");
//    }
//
//    @Test(enabled = false)
//    public void testBatch() throws Exception {
//        int round = 100;
//        List<TPayload> list = new ArrayList<>(round);
//        for (int i = 0; i < round; i++) {
//            list.add(genPayload());
//        }
//        long size = batch(list);
//        assertEquals(size, round);
//    }
//
//
//    /**
//     * Returns the type from super class's type parameter.
//     */
//
//    private static Type getSuperclassTypeParameter(Class<?> subclass) {
//        Type superclass = subclass.getGenericSuperclass();
//        if (!(superclass instanceof ParameterizedType)) {
//            throw new RuntimeException("Missing type parameter.");
//        }
//        ParameterizedType parameterized = (ParameterizedType) superclass;
//        return parameterized.getActualTypeArguments()[0];
//    }
//
//
//    @Override
//    public long batch(final List<TPayload> payloads) throws TException {
//        GenericEntity<List<TPayload>> entity = new GenericEntity<List<TPayload>>(payloads) {
//        };
//        String size = web.path("/batch/list")
//                .type(ExtendedMediaType.APPLICATION_X_THRIFT)
//                .accept(MediaType.TEXT_PLAIN_TYPE)
//                .entity(entity)
//                .post(String.class);
//        return Long.valueOf(size);
//    }
//
//    @Test(enabled = false)
//    public void testFetch() throws Exception {
//        Long id = ThreadLocalRandom.current().nextLong(1000000);
//        TPayload payload = fetch(id);
//        assertNotNull(payload);
//        LOG.info("Entity: {}", payload);
//    }
//
//    @Override
//    public TPayload fetch(long id) throws TException {
//        return web.path(String.valueOf(id))
//                .accept(ExtendedMediaType.APPLICATION_X_THRIFT)
//                .get(TPayload.class);
//    }
//
//    @Test(enabled = false)
//    public void testMultiGet() throws Exception {
//        int round = ThreadLocalRandom.current().nextInt(50) + 1;
//        List<Long> idList = new ArrayList<>(round);
//        for (int i = 0; i < round; i++) {
//            idList.add(ThreadLocalRandom.current().nextLong(Long.MAX_VALUE));
//        }
//        List<TPayload> list = multiGet(idList);
//        assertNotNull(list);
//        assertEquals(list.size(), round);
//        LOG.info("{}", list);
//    }
//
//    @Override
//    public List<TPayload> multiGet(final List<Long> idList) throws TException {
//        List<String> list = Lists.transform(idList, new Function<Long, String>() {
//            @Nullable
//            @Override
//            public String apply(@Nullable Long input) {
//                return String.valueOf(input);
//            }
//        });
//        MultivaluedMap<String, String> query = new MultivaluedHashMap<>();
//        query.put("tid", list);
//        return web.path("multi")
//                .queryParams(query)
//                .accept(ExtendedMediaType.APPLICATION_X_THRIFT)
//                .get(new GenericType<List<TPayload>>() {
//                });
//
//    }
//
//    @Test(enabled = false)
//    public void testFetchMap() throws Exception {
//        Map<Long, TPayload> map = fetchMap(ThreadLocalRandom.current().nextLong(Long.MAX_VALUE));
//        assertNotNull(map);
//        assertEquals(map.size(), 1);
//    }
//
//    @Override
//    public Map<Long, TPayload> fetchMap(final long id) throws TException {
//        ///map/{id:\d+}
//        return web.path("map").path(String.valueOf(id))
//                .accept(ExtendedMediaType.APPLICATION_X_THRIFT_TYPE)
//                .get(new GenericType<Map<Long, TPayload>>() {
//                });
//    }
//
//    @Test(enabled = false)
//    public void testFetchSet() throws Exception {
//        Set<TPayload> set = fetchSet(ThreadLocalRandom.current().nextLong(Long.MAX_VALUE));
//        assertNotNull(set);
//        assertEquals(set.size(), 1);
//    }
//
//    @Override
//    public Set<TPayload> fetchSet(long id) throws TException {
//        ///map/{id:\d+}
//        return web.path("set").path(String.valueOf(id))
//                .accept(ExtendedMediaType.APPLICATION_X_THRIFT_TYPE)
//                .get(new GenericType<Set<TPayload>>() {
//                });
//    }
//}

