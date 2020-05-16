package com.example.epassapp.utilities;

import com.example.epassapp.BuildConfig;
import com.yakivmospan.scytale.Crypto;
import com.yakivmospan.scytale.Options;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class Cryptography {

    public static String encrypt(String text) {
        SecretKey key = new SecretKeySpec(BuildConfig.KEY.getBytes(), "AES");
        Crypto crypto = new Crypto(Options.TRANSFORMATION_SYMMETRIC);
        return crypto.encrypt(text, key);
    }

    public static String decrypt(String encryptedText) {
        SecretKey key = new SecretKeySpec(BuildConfig.KEY.getBytes(), "AES");
        Crypto crypto = new Crypto(Options.TRANSFORMATION_SYMMETRIC);
        return crypto.decrypt(encryptedText, key);
    }
}
