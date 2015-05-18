/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.ellios.hedwig.rpc.thrift.protocol;


import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Thrift message header Test.
 *
 * @author George Cao(caozhangzhi@qiyi.com)
 * @since 2014-01-13 16
 */
public class TMessageHeaderTest {

    public TMessageHeaderTest() {
    }


    /**
     * Test of add method, of class TMessageHeader.
     */
    @Test
    public void testAdd() {
        System.out.println("add");
        THeaderName name = THeaderName.API;
        String value = "";
        TMessageHeader instance = new TMessageHeader();
        TMessageHeader result = instance.add(name, value);
        assertTrue(result.contains(name));
    }

    /**
     * Test of contains method, of class TMessageHeader.
     */
    @Test
    public void testContains() {
        System.out.println("contains");
        THeaderName name = THeaderName.API;
        TMessageHeader instance = new TMessageHeader();
        boolean expResult = false;
        boolean result = instance.contains(name);
        assertEquals(expResult, result);
        instance.add(name, null);
        result = instance.contains(name);
        assertTrue(result);
        assertEquals("", instance.getValue(name));
                
    }

    /**
     * Test of getValue method, of class TMessageHeader.
     */
    @Test
    public void testGetValue(){
        System.out.println("getValue");
        THeaderName name = THeaderName.API;
        TMessageHeader instance = new TMessageHeader();
        String expResult = "";
        String result = instance.getValue(name);
        assertEquals(expResult, result);
        instance.add(name, "api");
        expResult= "api";
        assertEquals(expResult, instance.getValue(name));
    }

    /**
     * Test of clear method, of class TMessageHeader.
     */
    @Test
    public void testClear() {
        System.out.println("clear");
        TMessageHeader instance = new TMessageHeader();
        instance.clear();
        assertTrue(!instance.contains(THeaderName.API));
    }

    /**
     * Test of decode method, of class TMessageHeader.
     */
    @Test
    public void testDecode() {
        System.out.println("decode");
        String fullName = "user:service:api";
        TMessageHeader result = TMessageHeader.decode(fullName);
        assertEquals("api", result.getValue(THeaderName.API));
        assertEquals("service", result.getValue(THeaderName.SERVICE));
        assertEquals("user", result.getValue(THeaderName.USER));
    }

    /**
     * Test of signature method, of class TMessageHeader.
     */
    @Test
    public void testSignature() {
        System.out.println("signature");
        TMessageHeader instance = new TMessageHeader()
                .add(THeaderName.API, "api")
                .add(THeaderName.SERVICE, "service")
                .add(THeaderName.USER, "user");
        String expResult = "service:api";
        String result = instance.signature();
        assertEquals(expResult, result);
    }

    /**
     * Test of encode method, of class TMessageHeader.
     */
    @Test
    public void testEncode() {
        System.out.println("encode");
        TMessageHeader instance = new TMessageHeader();
        instance.add(THeaderName.API, "api");
        String expResult = "api";
        String result = instance.encode();
        assertEquals(expResult, result);
        instance.add(THeaderName.SERVICE, "service");
        expResult = "service:api";
        result = instance.encode();
        assertEquals(expResult, result);
    }

}
