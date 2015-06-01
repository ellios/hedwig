package me.ellios.hedwig.common.utils;

import org.junit.Test;

import java.net.URI;
import java.net.URL;

/**
 * User: ellios
 * Time: 15-5-28 : 上午9:29
 */
public class URLUtils {

    @Test
    public void test() throws Exception{
        URI url = new URI("redis://127.0.0.1:6379:127.0.0.1:6379/sdfadsf?hello=true&gogo=hi");
        System.out.println(url.getScheme());
        System.out.println(url.getHost());
        System.out.println(url.getPort());
        System.out.println(url.getRawPath());
    }
}
