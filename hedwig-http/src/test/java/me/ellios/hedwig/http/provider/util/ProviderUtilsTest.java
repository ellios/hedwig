package me.ellios.hedwig.http.provider.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import static me.ellios.hedwig.http.provider.util.ProviderUtils.createMap;

/**
 * Say something?
 *
 * @author George Cao
 * @since 4/20/13 7:41 PM
 */
public class ProviderUtilsTest {
    private static final Logger LOG = LoggerFactory.getLogger(ProviderUtilsTest.class);

    @Test
    public void testCreateMap() throws Exception {
        HashMap<String, Long> map = new HashMap<>();
        Map entityMap = createMap(map.getClass());
        Class<?> clazz = map.getClass();
        Type[] type = clazz.getGenericInterfaces();
        LOG.info("clazz: {}, type:{}", clazz, type);
    }
}
