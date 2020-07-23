package com.utils;

public class EncodeUtil {

    public static byte[] parseArray(int... arr) {
        byte[] content = new byte[arr.length];
        int index = 0;
        for (int i : arr) {
            content[index++] = (byte) i;
        }
        content = decode(content);
        return content;
    }

    public static String parseArrayToText(int... arr) {
        byte[] content = new byte[arr.length];
        int index = 0;
        for (int i : arr) {
            content[index++] = (byte) i;
        }
        content = decode(content);
        return new String(content);
    }

    public static byte[] encode(byte[] bytes) {
        if (bytes == null || bytes.length < 2) {
            return bytes;
        }
        int length = bytes.length / 2;
        for (int i = 0; i < length; i++) {
            byte temp = bytes[i];
            bytes[i] = bytes[length+i];
            bytes[length+i] = temp;
        }
        return bytes;
    }

    public static byte[] decode(byte[] bytes) {
        if (bytes == null || bytes.length < 2) {
            return bytes;
        }
        int length = bytes.length / 2;
        for (int i = 0; i < length; i++) {
            byte temp = bytes[i];
            bytes[i] = bytes[length+i];
            bytes[length+i] = temp;
        }
        return bytes;
    }
}
