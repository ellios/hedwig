package me.ellios.hedwig.rpc.tracer;

import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;

import static com.alibaba.fastjson.serializer.SerializerFeature.*;

/**
 * Service API invocation info.
 *
 * @author George Cao
 * @since 13-10-11 下午6:23
 */
public class TracerInfo {
    private static final Logger LOG = LoggerFactory.getLogger(TracerInfo.class);
    String service;
    String api;
    byte type;
    int seqId;
    SocketAddress remoteAddress;
    SocketAddress localAddress;
    long time;
    TimeUnit unit;
    String user;

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getApi() {
        return api;
    }

    public void setApi(String api) {
        this.api = api;
    }

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public int getSeqId() {
        return seqId;
    }

    public void setSeqId(int seqId) {
        this.seqId = seqId;
    }

    public SocketAddress getRemoteAddress() {
        return remoteAddress;
    }

    public void setRemoteAddress(SocketAddress remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    public SocketAddress getLocalAddress() {
        return localAddress;
    }

    public void setLocalAddress(SocketAddress localAddress) {
        this.localAddress = localAddress;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public TimeUnit getUnit() {
        return unit;
    }

    public void setUnit(TimeUnit unit) {
        this.unit = unit;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this,
                SkipTransientField,
                WriteNullStringAsEmpty,
                WriteNullBooleanAsFalse,
                WriteNullListAsEmpty,
                WriteNullNumberAsZero,
                WriteMapNullValue,
                WriteTabAsSpecial,
                DisableCircularReferenceDetect
        );
    }
}
