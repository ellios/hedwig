package me.ellios.hedwig.common.utils;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.*;
import java.util.Enumeration;

/**
 * Author: ellios
 * Date: 12-11-1 Time: 上午10:11
 */
public class NetworkUtils {

    private static Logger logger = LoggerFactory.getLogger(NetworkUtils.class);

    public static String getLocalHost() {
        try {
            InetAddress addr = InetAddress.getLocalHost();
            return addr.getHostName();
        } catch (UnknownHostException e) {
            logger.error(e.getMessage(), e);
            return "localhost";
        }

    }

    public static String getLocalAddress() {
        try {
            InetAddress addr = InetAddress.getLocalHost();
            return addr.getHostAddress();
        } catch (UnknownHostException e) {
            logger.error(e.getMessage(), e);
            return "127.0.0.1";
        }

    }

    /**
     * 获取内网的ip地址，取第一块网卡的地址
     * @return
     */
    public static String getEth0Address() {
        try {
            Enumeration netInterfaces = NetworkInterface.getNetworkInterfaces();
            while (netInterfaces.hasMoreElements()) {
                NetworkInterface netInterface = (NetworkInterface) netInterfaces.nextElement();
                if (StringUtils.equals(netInterface.getName(), "eth0")) {
                    Enumeration addresses = netInterface.getInetAddresses();
                    InetAddress address = null;
                    while (addresses.hasMoreElements()) {
                        address = (InetAddress) addresses.nextElement();
                        if (address != null && address instanceof Inet4Address) {
                            return address.getHostAddress();
                        }
                    }
                }
            }
        } catch (SocketException e) {
            logger.error("getEth0Address fail." + e.getMessage(), e);

        }
        //找不到的话取本机的地址
        return getLocalAddress();
    }
}
