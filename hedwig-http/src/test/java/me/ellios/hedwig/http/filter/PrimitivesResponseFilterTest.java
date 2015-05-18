package me.ellios.hedwig.http.filter;

import org.glassfish.jersey.server.ContainerResponse;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.ws.rs.container.ContainerResponseFilter;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Say something?
 *
 * @author George Cao
 * @since 4/19/13 1:57 PM
 */
public class PrimitivesResponseFilterTest {
    ContainerResponseFilter filter;

    @BeforeMethod
    public void setUp() throws Exception {
        filter = new PrimitivesResponseFilter();
    }

    private void testWrapper(Object v) throws Exception {
        ContainerResponse r = new ContainerResponse(null, null);
        r.setEntity(v);
        filter.filter(null, r);
        assertTrue(r.getEntity() instanceof String);
        assertEquals(String.valueOf(v), r.getEntity());
    }

    @Test
    public void testFilter() throws Exception {
        int v = 212;
        testWrapper(v);
        short s = 212;
        testWrapper(s);
        byte b = 21;
        testWrapper(b);
        long l = 212;
        testWrapper(l);
        double d = 212;
        testWrapper(d);
    }
}
