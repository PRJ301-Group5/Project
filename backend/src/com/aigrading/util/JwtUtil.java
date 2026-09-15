package com.aigrading.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Enterprise-grade JWT (JSON Web Token) implementation in pure Java (HMAC-SHA256).
 */
public class JwtUtil {
    private static final String SECRET = System.getenv("JWT_SECRET") != null ? 
            System.getenv("JWT_SECRET") : "PRJ301_RBL_GROUP5_GIT_ASSESSOR_SUPER_SECRET_KEY_2026";
    private static final long DEFAULT_EXPIRY_MS = 24 * 60 * 60 * 1000L; // 24 hours

    public static class JwtClaims {
        public int userId;
        public String email;
        public String role;
        public Integer teamId;
        public long exp;

        public JwtClaims(int userId, String email, String role, Integer teamId, long exp) {
            this.userId = userId;
            this.email = email;
            this.role = role;
            this.teamId = teamId;
            this.exp = exp;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > exp;
        }
    }

    public static String generateToken(int userId, String email, String role, Integer teamId) {
        try {
            long exp = System.currentTimeMillis() + DEFAULT_EXPIRY_MS;
            String header = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
            String payload = String.format(
                "{\"userId\":%d,\"email\":\"%s\",\"role\":\"%s\",\"teamId\":%s,\"exp\":%d}",
                userId, escape(email), escape(role), (teamId != null ? teamId.toString() : "null"), exp
            );

            String encodedHeader = base64UrlEncode(header.getBytes(StandardCharsets.UTF_8));
            String encodedPayload = base64UrlEncode(payload.getBytes(StandardCharsets.UTF_8));
            String content = encodedHeader + "." + encodedPayload;

            String signature = sign(content, SECRET);
            return content + "." + signature;
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate JWT token", e);
        }
    }

    public static JwtClaims parseAndVerify(String token) {
        if (token == null || token.trim().isEmpty()) return null;
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) return null;

            String encodedHeader = parts[0];
            String encodedPayload = parts[1];
            String signature = parts[2];

            String content = encodedHeader + "." + encodedPayload;
            String expectedSig = sign(content, SECRET);
            if (!expectedSig.equals(signature)) {
                return null; // Invalid signature
            }

            String payloadJson = new String(base64UrlDecode(encodedPayload), StandardCharsets.UTF_8);
            Map<String, Object> map = JsonHelper.parseObject(payloadJson);
            if (map == null) return null;

            int userId = ((Number) map.get("userId")).intValue();
            String email = (String) map.get("email");
            String role = (String) map.get("role");
            Integer teamId = map.get("teamId") != null && !"null".equals(map.get("teamId").toString()) ? 
                    ((Number) map.get("teamId")).intValue() : null;
            long exp = ((Number) map.get("exp")).longValue();

            JwtClaims claims = new JwtClaims(userId, email, role, teamId, exp);
            if (claims.isExpired()) {
                return null; // Token expired
            }
            return claims;
        } catch (Exception e) {
            return null;
        }
    }

    private static String sign(String data, String secret) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(keySpec);
        byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return base64UrlEncode(rawHmac);
    }

    private static String base64UrlEncode(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static byte[] base64UrlDecode(String str) {
        return Base64.getUrlDecoder().decode(str);
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\"", "\\\"");
    }
}
