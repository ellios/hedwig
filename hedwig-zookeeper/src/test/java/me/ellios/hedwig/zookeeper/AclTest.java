package me.ellios.hedwig.zookeeper;

import org.apache.zookeeper.server.auth.DigestAuthenticationProvider;
import org.junit.Test;

import java.security.NoSuchAlgorithmException;

/**
 * User: ellios
 * Time: 15-8-17 : 下午4:28
 */
public class AclTest {

    @Test
    public void test(){
        try {
            System.out.println(DigestAuthenticationProvider.generateDigest("ellios:123456"));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
}
