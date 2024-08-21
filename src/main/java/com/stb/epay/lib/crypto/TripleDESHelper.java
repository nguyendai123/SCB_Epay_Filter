package com.stb.epay.lib.crypto;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

/**
 * Bao gồm các hàm tiện ích dùng để thao tác mã hoá/giải mã với thuật toán
 * TripleDES (3DES)
 *
 * @author Administrator
 * @Created 6 Apr 2023
 */
public class TripleDESHelper {

    private static final String UTF8 = "utf-8";
    private static final String CIPHERDES = "DESede/ECB/PKCS5Padding";
    private static final String KEYSPECDES = "DESede";

    private TripleDESHelper() {
        throw new IllegalStateException("Utility class");
    }

    public static String encrypt(String message, String keyword) {
        if (message == null) {
            return "";
        }
        byte[] encrypted = encryptByte(message, keyword);
        return encrypted != null ? new String(Base64.getEncoder().encode(encrypted)) : "";
    }

    public static byte[] encryptByte(String message, String keyword) {
        try {
            final SecretKey key = getSecretKey(keyword);

            final Cipher cipher = Cipher.getInstance(CIPHERDES);
            cipher.init(Cipher.ENCRYPT_MODE, key);

            final byte[] planTextBytes = message.getBytes(UTF8);
            final byte[] cipherText = cipher.doFinal(planTextBytes, 0, planTextBytes.length);
            return cipherText;
        } catch (Exception ex) {
            return new byte[0];
        }
    }

    public static String decrypt(String message, String keyword) {
        if (message == null) {
            return "";
        }
        try {
            byte[] encryptedText = Base64.getDecoder().decode(message);
            return encryptedText != null ? decryptByte(encryptedText, keyword) : "";
        } catch (Exception ex) {
            return "";
        }
    }

    public static String decryptByte(byte[] encryptedText, String keyword) {
        try {
            final SecretKey key = getSecretKey(keyword);
            final Cipher decipher = Cipher.getInstance(CIPHERDES);
            decipher.init(Cipher.DECRYPT_MODE, key);

            final byte[] plainText = decipher.doFinal(encryptedText);
            return new String(plainText, UTF8);
        } catch (Exception ex) {
            return null;
        }
    }

    private static SecretKey getSecretKey(String keyword) throws UnsupportedEncodingException {
        final byte[] digestOfPassword = keyword.getBytes(UTF8);
        final byte[] keyBytes = Arrays.copyOf(digestOfPassword, 24);
        if (digestOfPassword.length < 24) {
            for (int j = 0, k = 16; j < 8; ) {
                keyBytes[k++] = keyBytes[j++];
            }
        }
        final SecretKey key = new SecretKeySpec(keyBytes, KEYSPECDES);
        return key;
    }

    public static class MD5Utils {
        public static String calculate(String input) {
            try {
                MessageDigest md = MessageDigest.getInstance("MD5");
                byte[] message = md.digest(input.getBytes(StandardCharsets.UTF_8));
                StringBuilder builder = new StringBuilder();
                for (byte b : message) {
                    builder.append(String.format("%02x", b));
                }
                return builder.toString().toUpperCase();
            } catch (Exception ex) {
                return "";
            }
        }
    }
}
