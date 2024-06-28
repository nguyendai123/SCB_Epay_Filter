package com.ewallet.lib.crypto;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public final class AESUtils {
    private static final String CIPHERDES = "AES/CBC/PKCS5Padding";

    private AESUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static String encrypt(String message, String keyword, String salt) {
        if (message == null) {
            return "";
        }
        byte[] encrypted = encryptByte(message, keyword, salt);
        return encrypted != null ? new String(Base64.getEncoder().encode(encrypted)) : "";
    }

    public static byte[] encryptByte(String message, String keyword, String salt) {
        try {
            final SecretKey key = getSecretKey(keyword);
            final Cipher cipher = Cipher.getInstance(CIPHERDES);
            cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(getBytesMD5(salt)));

            final byte[] planTextBytes = message.getBytes(StandardCharsets.UTF_8);
            return cipher.doFinal(planTextBytes, 0, planTextBytes.length);
        } catch (Exception ex) {
            return new byte[0];
        }
    }

    public static String decrypt(String message, String keyword, String salt) {
        if (message == null) {
            return "";
        }
        try {
            byte[] encryptedText = Base64.getDecoder().decode(message);
            return encryptedText != null ? decryptByte(encryptedText, keyword, salt) : "";
        } catch (Exception ex) {
            return "";
        }
    }

    public static String decryptByte(byte[] encryptedText, String keyword, String salt) {
        try {
            final SecretKey key = getSecretKey(keyword);
            final Cipher decipher = Cipher.getInstance(CIPHERDES);
            decipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(getBytesMD5(salt)));

            final byte[] plainText = decipher.doFinal(encryptedText);
            return new String(plainText, StandardCharsets.UTF_8);
        } catch (Exception ex) {
            return null;
        }
    }

    private static SecretKey getSecretKey(String keyword) throws Exception {
        byte[] keyBytes = getBytesMD5(keyword);
        return new SecretKeySpec(keyBytes, 0, keyBytes.length, "AES");
    }

    private static byte[] getBytesMD5(String str) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(str.getBytes());
        return md.digest();
    }

    public static String getKeyCache(String str) throws NoSuchAlgorithmException {
        byte[] base64Encoded = Base64.getEncoder().encode(getBytesMD5(str));
        return new String(base64Encoded);
    }

    public static class AESCrypto {
        private static final String CIPHER_MODE = "AES/ECB/PKCS5Padding";

        public static String encrypt(String key, String plainText) {
            try {
                //to convert String to Byte[]
                byte[] input = plainText.getBytes(StandardCharsets.UTF_8);
                //instance cipher_mode
                final Cipher cipher = Cipher.getInstance(CIPHER_MODE);
                var keyEncrypt = getKey(key);
                cipher.init(Cipher.ENCRYPT_MODE, keyEncrypt);
                var response = cipher.doFinal(input, 0, input.length);
                return new String(Base64.getEncoder().encode(response));
            } catch (Exception ex) {
                return "";
            }
        }

        private static SecretKey getKey(String keyword) {
            byte[] keyBytes = keyword.getBytes(StandardCharsets.UTF_8);
            return new SecretKeySpec(keyBytes, 0, keyBytes.length, "AES");
        }

        public static String decrypt(String key, String cipherText) {
            try {
                final Cipher cipher = Cipher.getInstance(CIPHER_MODE);
                var keyEncrypt = getKey(key);
                cipher.init(Cipher.DECRYPT_MODE, keyEncrypt);
                var input = Base64.getDecoder().decode(cipherText);
                return new String(cipher.doFinal(input, 0, input.length), StandardCharsets.UTF_8);
            } catch (Exception ex) {
                return "";
            }
        }
    }
}
