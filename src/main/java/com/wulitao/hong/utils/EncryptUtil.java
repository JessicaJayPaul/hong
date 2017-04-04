package com.wulitao.hong.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class EncryptUtil {

    /**
     * MD5加密字符串
     */
    public static String encryptByMd5(String key) {
        String result;
        try {
            final MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(key.getBytes());
            result = bytesToHexString(mDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            result = String.valueOf(key.hashCode());
        }
        return result;
    }

    private static String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes){
            String hex = Integer.toHexString(0xFF & b);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }
}
