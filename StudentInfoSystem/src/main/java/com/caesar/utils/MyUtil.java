package main.java.com.caesar.utils;

import java.nio.charset.StandardCharsets;

public class MyUtil {

    //将字节数组转成long
    public static long bytesToLong(byte[] bytes, int pos){
        long FF = 0xff;
        long res = 0L;
        for (int i = 0; i < 8; i++) {
            res += (bytes[pos ++] & FF) << (56 - 8 * i);
        }
        return res;
    }

    //返回值为long最后一个字节的下一个位置
    public static int longToBytes(byte[] bytes, int pos, long value){
        for (int i = 0; i < 8; i++) {
            bytes[pos ++] = (byte) (value >> (56 - i * 8));
        }
        return pos;
    }

    //此方法会将字符串拷贝到字节数组中，并将其长度储存为第一个字节,返回结果为字符串最后一个字节的下一个位置
    public static int stringToBytes(byte[] bytes, int pos, String value){
        int length = value.length();
        bytes[pos ++] = (byte) length;
        System.arraycopy(value.getBytes(StandardCharsets.UTF_8), 0, bytes, pos, length);
        return pos + length;
    }

    //将字节数组转成String
    public static String bytesToString(byte[] bytes, int pos){
        int length = bytes[pos ++];
        byte[] stringBytes = new byte[length];
        System.arraycopy(bytes, pos, stringBytes, 0, length);
        return new String(stringBytes);
    }
}
