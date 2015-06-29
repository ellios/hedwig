// Copyright (c) 2006  Dustin Sallings <dustin@spy.net>

package me.ellios.jedis.transcoders;

import java.util.Arrays;

/**
 * Cached data with its attributes.
 */
public final class CachedData {

    private final int flag;

    private final byte[] data;

    private byte[] fullData;

    /**
     * Get a CachedData instance for the given flags and byte array.
     * 
     * @param f the flags
     * @param d the data
     */
    public CachedData(int f, byte[] d) {
        this.flag = f;
        this.data = d;
        final int intLen = 4;
        fullData = new byte[intLen + data.length];
        byte[] flagBytes = TranscoderUtils.encodeInt(flag);
        System.arraycopy(flagBytes, 0, fullData, 0, intLen);
        System.arraycopy(data, 0, fullData, intLen, data.length);
    }

    public CachedData(byte[] fullData) {
        this.fullData = fullData;
        this.flag = TranscoderUtils.decodeInt(Arrays.copyOf(fullData, 4));
        this.data = Arrays.copyOfRange(fullData, 4, fullData.length);
    }

    public final byte[] getFullData() {
        return fullData;
    }

    /**
     * Get the stored data.
     */
    public final byte[] getData() {
        return this.data;
    }

    /**
     * Get the flags stored along with this value.
     */
    public final int getFlag() {
        return this.flag;
    }

    @Override
    public String toString() {
        return "{CachedData flags=" + this.flag + " data=" + Arrays.toString(this.data) + "}";
    }

}
