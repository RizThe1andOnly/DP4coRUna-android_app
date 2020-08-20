package com.example.dp4coruna.network;

import android.os.Build;

import androidx.annotation.RequiresApi;

import javax.crypto.Cipher;
import java.security.*;
import android.util.Base64;

import static java.nio.charset.StandardCharsets.UTF_8;

public class RSA {

    public static KeyPair generateKeyPair(int size) {

        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(size, new SecureRandom());
            KeyPair pair = generator.generateKeyPair();

            return pair;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static String encrypt(String plainText, PublicKey publicKey){
        try {
            Cipher encryptCipher = Cipher.getInstance("RSA");
            encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey);

            byte[] cipherText = encryptCipher.doFinal(plainText.getBytes(UTF_8));

            return Base64.encodeToString(cipherText, Base64.DEFAULT);
        } catch (Exception e){
            System.out.println("Error while attempting to encrypt: " + e.toString());
        }
        return null;
    }


    public static String decrypt(String cipherText, PrivateKey privateKey){
        try {
            byte[] bytes = Base64.decode(cipherText, Base64.DEFAULT);

            Cipher decryptCipher = Cipher.getInstance("RSA");
            decryptCipher.init(Cipher.DECRYPT_MODE, privateKey);

            return new String(decryptCipher.doFinal(bytes), UTF_8);
        } catch (Exception e){
            System.out.println("Error while attempting to decrypt: " + e.toString());
        }
        return null;
    }
}

