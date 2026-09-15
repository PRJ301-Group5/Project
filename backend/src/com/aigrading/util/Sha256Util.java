package com.aigrading.util;

import java.io.*;
import java.security.MessageDigest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Sha256Util {

    public static String calculateHash(InputStream is) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] buffer = new byte[8192];
        int read;
        while ((read = is.read(buffer)) != -1) {
            digest.update(buffer, 0, read);
        }
        return bytesToHex(digest.digest());
    }

    public static String calculateHash(byte[] bytes) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(bytes);
        return bytesToHex(hash);
    }

    public static String calculateHash(File file) throws Exception {
        try (FileInputStream fis = new FileInputStream(file)) {
            return calculateHash(fis);
        }
    }

    public static boolean verifyHash(File file, String expectedHash) {
        if (file == null || !file.exists() || expectedHash == null) return false;
        try {
            String actualHash = calculateHash(file);
            return actualHash.equalsIgnoreCase(expectedHash.trim());
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean verifyHash(byte[] bytes, String expectedHash) {
        if (bytes == null || expectedHash == null) return false;
        try {
            String actualHash = calculateHash(bytes);
            return actualHash.equalsIgnoreCase(expectedHash.trim());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Inspects a ZIP stream or file to detect Zip Slip path traversal vulnerabilities (e.g. ../../)
     */
    public static boolean isSafeZip(InputStream zipInputStream) {
        try (ZipInputStream zis = new ZipInputStream(zipInputStream)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String name = entry.getName();
                if (name.contains("..") || name.startsWith("/") || name.startsWith("\\")) {
                    return false; // Malicious path traversal detected
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b & 0xff));
        }
        return sb.toString();
    }
}
