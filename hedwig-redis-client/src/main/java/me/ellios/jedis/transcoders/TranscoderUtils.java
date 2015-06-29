// Copyright (c) 2006  Dustin Sallings <dustin@spy.net>

package me.ellios.jedis.transcoders;

/**
 * Utility class for transcoding Java types.
 */
public final class TranscoderUtils {

    public static byte[] encodeNum(long l, int maxBytes) {
        byte[] rv = new byte[maxBytes];
        for (int i = 0; i < rv.length; i++) {
            int pos = rv.length - i - 1;
            rv[pos] = (byte) ((l >> (8 * i)) & 0xff);
        }
        return rv;
    }

    public static byte[] encodeLong(long l) {
        return encodeNum(l, 8);
    }

    public static long decodeLong(byte[] b) {
        assert b.length <= 8 : "Too long to be an long (" + b.length + ") bytes";
        long rv = 0;
        for (byte i : b) {
            rv = (rv << 8) | (i < 0 ? 256 + i : i);
        }
        return rv;
    }

    public static byte[] encodeInt(int in) {
        return encodeNum(in, 4);
    }

    public static int decodeInt(byte[] in) {
        assert in.length <= 4 : "Too long to be an int (" + in.length + ") bytes";
        return (int) decodeLong(in);
    }

    public static byte[] encodeByte(byte in) {
        return new byte[] { in };
    }

    public static byte decodeByte(byte[] in) {
        assert in.length <= 1 : "Too long for a byte";
        byte rv = 0;
        if (in.length == 1) {
            rv = in[0];
        }
        return rv;
    }

    public static byte[] encodeBoolean(boolean b) {
        byte[] rv = new byte[1];
        rv[0] = (byte) (b ? '1' : '0');
        return rv;
    }

    public static boolean decodeBoolean(byte[] in) {
        assert in.length == 1 : "Wrong length for a boolean";
        return in[0] == '1';
    }

}
