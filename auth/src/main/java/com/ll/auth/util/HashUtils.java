package com.ll.auth.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

public final class HashUtils {

    private HashUtils() {}

    public static String sha256Hex(String input) {
        return sha256Hex(input, null);
    }

    public static String sha256Hex(String input, String salt) {
        Objects.requireNonNull(input, "input must not be null");

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(input.getBytes(StandardCharsets.UTF_8));
            if (salt != null && !salt.isEmpty()) {
                digest.update(salt.getBytes(StandardCharsets.UTF_8));
            }
            byte[] hash = digest.digest();
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
