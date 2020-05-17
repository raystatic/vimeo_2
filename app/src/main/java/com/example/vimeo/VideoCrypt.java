package com.example.vimeo;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class VideoCrypt {

    public static final int REVERSE_BYTE_COUNT = 1024;

    private String encryptedFileName = "Enc_File.txt";
    private static String algorithm = "AES";
    static SecretKey yourKey = null;

    public static SecretKey generateKey(char[] passphraseOrPin, byte[] salt)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        // Number of PBKDF2 hardening rounds to use. Larger values increase
        // computation time. You should select a value that causes computation
        // to take >100ms.
        final int iterations = 1000;

        // Generate a 256-bit key
        final int outputKeyLength = 256;

        SecretKeyFactory secretKeyFactory = SecretKeyFactory
                .getInstance("PBKDF2WithHmacSHA1");
        KeySpec keySpec = new PBEKeySpec(passphraseOrPin, salt, iterations,
                outputKeyLength);
        yourKey = secretKeyFactory.generateSecret(keySpec);
        return yourKey;
    }

    public static SecretKey generateSalt() throws NoSuchAlgorithmException {
        // Generate a 256-bit key
        final int outputKeyLength = 256;

        SecureRandom secureRandom = new SecureRandom();
        // Do *not* seed secureRandom! Automatically seeded from system entropy.
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(outputKeyLength, secureRandom);
        SecretKey key = keyGenerator.generateKey();
        return key;
    }

    public static byte[] encodeFile(SecretKey yourKey, byte[] fileData)
            throws Exception {
        byte[] data = yourKey.getEncoded();
        SecretKeySpec skeySpec = new SecretKeySpec(data, 0, data.length,
                algorithm);
        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);

        byte[] encrypted = cipher.doFinal(fileData);

        return encrypted;
    }

    public static byte[] decodeFile(SecretKey yourKey, byte[] fileData)
            throws Exception {
        byte[] data = yourKey.getEncoded();
        SecretKeySpec skeySpec = new SecretKeySpec(data, 0, data.length,
                algorithm);
        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.DECRYPT_MODE, skeySpec);

        byte[] decrypted = cipher.doFinal(fileData);

        return decrypted;
    }

    public static void encryptAndSaveFile(String path) {
        try {
            File file = new File(path);
            BufferedOutputStream bos = new BufferedOutputStream(
                    new FileOutputStream(file));
            char[] p = { 'p', 'a', 's', 's' };
            byte[] data = new byte[(int) file.length()];
            SecretKey yourKey = generateKey(p, generateSalt().toString()
                    .getBytes());
            byte[] filesBytes = encodeFile(yourKey, data);
            bos.write(filesBytes);
            bos.flush();
            bos.close();
            Log.d("videopath", "doWork: encrypted "+file.getAbsolutePath());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void decodeFile(String path) {
        try {
            byte[] decodedData = decodeFile(yourKey, readFile(path));
//            String str = new String(decodedData);
//            System.out.println("DECODED FILE CONTENTS : " + str);
            File file = new File(path);
            OutputStream os = new FileOutputStream(file);
            os.write(decodedData);
            os.close();
            Log.d("videopath", "doWork: decrypted "+file.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static byte[] readFile(String path) {
        byte[] contents = null;

        File file = new File(path);
        int size = (int) file.length();
        contents = new byte[size];
        try {
            BufferedInputStream buf = new BufferedInputStream(
                    new FileInputStream(file));
            try {
                buf.read(contents);
                buf.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return contents;
    }

    public static boolean decrypt(String path) {
        try {
            if (path == null) return false;
            File source = new File(path);
            int byteToReverse = source.length() < REVERSE_BYTE_COUNT ? ((int) source.length()) : REVERSE_BYTE_COUNT;
            RandomAccessFile f = new RandomAccessFile(source, "rw");
            f.seek(0);
            byte b[] = new byte[byteToReverse];
            f.read(b);
            f.seek(0);
            reverseBytes(b);
            f.write(b);
            f.seek(0);
            b = new byte[byteToReverse];
            f.read(b);
            f.close();
            Log.d("videopath", "doWork: decrypted "+source.getAbsolutePath());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean encrypt(String path) {
        try {
            if (path == null) return false;
            File source = new File(path);
            RandomAccessFile f = new RandomAccessFile(source, "rw");
            f.seek(0);
            int byteToReverse = source.length() < REVERSE_BYTE_COUNT ? ((int) source.length()) : REVERSE_BYTE_COUNT;
            byte b[] = new byte[byteToReverse];
            f.read(b);
            f.seek(0);
            reverseBytes(b);
            f.write(b);
            f.seek(0);
            b = new byte[byteToReverse];
            f.read(b);
            f.close();
            Log.d("videopath", "doWork: encrypted "+source.getAbsolutePath());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static void reverseBytes(byte[] array) {
        if (array == null) return;
        int i = 0;
        int j = array.length - 1;
        byte tmp;
        while (j > i) {
            tmp = array[j];
            array[j] = array[i];
            array[i] = tmp;
            j--;
            i++;
        }
    }


}
