package com.example.dp4coruna.network;

import android.os.Build;

import androidx.annotation.RequiresApi;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.KeyGenerator;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import android.util.Base64;
import java.nio.ByteBuffer;

public class AES {

    //public static SecretKeySpec secretKey;
    //private static byte[] key;

    // The devices need to generate randomized strings as passwords and then share them with each other.
    // Then, they can use the following method to derive the AES secret key from that password.


    public static SecretKey setKey(String myKey){
        MessageDigest sha = null;
        try{
            byte[] key = myKey.getBytes("UTF-8");
            sha = MessageDigest.getInstance("SHA-1");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16);
            SecretKey secretKey = new SecretKeySpec(key, "AES");
            return secretKey;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }




    public static String encrypt(String encryptStr, String secret){
        try{
            SecretKey secretKey = setKey(secret);
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return Base64.encodeToString(cipher.doFinal(encryptStr.getBytes("UTF-8")), Base64.DEFAULT);
        } catch (Exception e) {
            System.out.println("Error while attempting to encrypt: " + e.toString());
        }
        return null;
    }


    public static String decrypt(String decryptStr, String secret){
        try{
            SecretKey secretKey = setKey(secret);
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return new String(cipher.doFinal(Base64.decode(decryptStr, Base64.DEFAULT)));
        } catch (Exception e){
            System.out.println("Error while attempting to decrypt: " + e.toString());
        }
        return null;
    }
}

