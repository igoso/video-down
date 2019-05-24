package com.igoso.me.xdf.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * created by igoso at 2018/10/21
 **/
public class CryptUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(CryptUtil.class);

    /**
     * 解密方法参数为String
     * @param bytes
     * @param key
     * @param iv
     * @return
     */
    public static byte[] decrypt(byte[] bytes, String key, String iv) {
        try {
            final byte[] secretKey = hexStrToByte(key);
            final byte[] initVector = hexStrToByte(iv);
            final Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE,new SecretKeySpec(secretKey,"AES"),new IvParameterSpec(initVector));
            return cipher.doFinal(bytes);
        } catch (Exception e) {
            System.out.println("decrypt fails");
            LOGGER.error("decrypt fails , key:{},iv:{}",key,iv,e);

        }

        return null;
    }

    /**
     * convert hex string to bytes
     * @param hex
     * @return
     */
    private static byte[] hexStrToByte(String hex) {
        return javax.xml.bind.DatatypeConverter.parseHexBinary(hex);
    }


    /**
     * string stream to bytes[]
     * @param in
     * @return
     * @throws IOException
     */
    public static byte[] toByteArray(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024 * 4];
        int n;
        while ((n = in.read(buffer)) != -1) {
            out.write(buffer, 0, n);
        }
        return out.toByteArray();
    }

    /**
     * bytes to hex
     * @param bytes
     * @return
     */
    public static String toHexString(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        StringBuilder result = new StringBuilder();
        String hex;
        for (byte b : bytes) {
            hex = Integer.toHexString(b & 0xFF);
            if (hex.length() < 2) {
                hex = "0" + hex;
            }
            result.append(hex);
        }
        return result.toString();
    }
}
