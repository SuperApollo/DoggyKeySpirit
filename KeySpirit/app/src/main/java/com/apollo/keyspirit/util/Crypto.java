package com.apollo.keyspirit.util;

import android.util.Base64;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * 加密类
 */
public class Crypto {
    private Cipher ecipher;
    private SecretKeySpec skey;

    public Crypto(String key) {
        try {
            skey = new SecretKeySpec(generateKey(key), "AES");
        } catch (Exception e) {
            //XLog.e("Crypto", e);
        }
    }

    private void setupEnCrypto() {
        byte[] iv = new byte[]{0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f};
        AlgorithmParameterSpec paramSpec = new IvParameterSpec(iv);
        try {
            ecipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            ecipher.init(Cipher.ENCRYPT_MODE, skey, paramSpec);
        } catch (Exception e) {
            ecipher = null;
            //XLog.e("setupCrypto", e);
        }
    }

    private void setupDeCrypto() {
        byte[] iv = new byte[]{0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f};
        AlgorithmParameterSpec paramSpec = new IvParameterSpec(iv);
        try {
            ecipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            ecipher.init(Cipher.DECRYPT_MODE, skey, paramSpec);
        } catch (Exception e) {
            ecipher = null;
            //XLog.e("setupCrypto", e);
        }
    }

    public String encrypt(String plaintext) {
        setupEnCrypto();
        if (ecipher == null)
            return "";

        try {
            byte[] ciphertext = ecipher.doFinal(plaintext.getBytes("UTF-8"));
            return Base64.encodeToString(ciphertext, Base64.NO_WRAP);
        } catch (Exception e) {
            //XLog.e("encryp", e);
            return "";
        }
    }

    public String decrypt(String plaintext) {
        setupDeCrypto();
        if (ecipher == null)
            return "";

        try {
            byte[] ciphertext = ecipher.doFinal(Base64.decode(plaintext, Base64.NO_WRAP));
            return new String(ciphertext, "UTF-8");
        } catch (Exception e) {
            //XLog.e("encryp", e);
            return "";
        }
    }

    public static String md5(String plain) {
        try {
            MessageDigest m = MessageDigest.getInstance("MD5");
            m.update(plain.getBytes());
            byte[] digest = m.digest();
            BigInteger bigInt = new BigInteger(1, digest);
            String hashtext = bigInt.toString(16);
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            return hashtext;
        } catch (Exception e) {
            return "";
        }
    }

    private static byte[] generateKey(String input) {
        try {
            byte[] bytesOfMessage = input.getBytes("UTF-8");
            MessageDigest md = MessageDigest.getInstance("SHA256");
            return md.digest(bytesOfMessage);
        } catch (Exception e) {
            //XLog.e("generateKey", e);
            return null;
        }
    }

    private PublicKey readKeyFromStream(InputStream keyStream) throws IOException {
        ObjectInputStream oin = new ObjectInputStream(new BufferedInputStream(keyStream));
        try {
            PublicKey pubKey = (PublicKey) oin.readObject();
            return pubKey;
        } catch (Exception e) {
            //XLog.e("readKeyFromStream", e);
            return null;
        } finally {
            oin.close();
        }
    }

    public String rsaEncrypt(InputStream keyStream, String data) {
        try {
            return rsaEncrypt(keyStream, data.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            return "";
        }
    }

    public String rsaEncrypt(InputStream keyStream, byte[] data) {
        try {
            PublicKey pubKey = readKeyFromStream(keyStream);
            Cipher cipher = Cipher.getInstance("RSA/ECB/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, pubKey);
            byte[] cipherData = cipher.doFinal(data);
            return Base64.encodeToString(cipherData, Base64.NO_WRAP);
        } catch (Exception e) {
            //XLog.e("rsaEncrypt", e);
            return "";
        }
    }

}