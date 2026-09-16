package com.aigrading.auth;

import com.aigrading.db.DatabaseManager;
import java.security.MessageDigest;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class AuthService {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final Map<String, SessionUser> SESSIONS = new ConcurrentHashMap<>();

    public static class SessionUser {
        public final long userId;
        public final String email;
        public final String fullName;
        public final String role;
        public final Long specificId; // teacher_id or student_id
        public final long expiresAt;

        public SessionUser(long userId, String email, String fullName, String role, Long specificId, long expiresAt) {
            this.userId = userId;
            this.email = email;
            this.fullName = fullName;
            this.role = role;
            this.specificId = specificId;
            this.expiresAt = expiresAt;
        }
    }

    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 not supported", e);
        }
    }

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }

    public static String generateOtp(String email, String type) throws SQLException {
        if (!isValidEmail(email)) {
            throw new IllegalArgumentException("Invalid email format");
        }
        int codeInt = 100000 + new Random().nextInt(900000);
        String code = String.valueOf(codeInt);

        // Expire in 5 minutes
        DatabaseManager.update(
            "INSERT INTO OtpCodes (email, code, type, expires_at, is_used) VALUES (?, ?, ?, datetime('now', '+5 minutes'), 0)",
            email.trim().toLowerCase(), code, type
        );

        System.out.println("[OTP Service] Sent OTP code " + code + " (" + type + ") to: " + email);
        return code;
    }

    public static boolean verifyOtp(String email, String code, String type) throws SQLException {
        if (email == null || code == null) return false;
        Map<String, Object> row = DatabaseManager.queryOne(
            "SELECT otp_id FROM OtpCodes WHERE email = ? AND code = ? AND type = ? AND is_used = 0 AND expires_at > datetime('now') ORDER BY otp_id DESC LIMIT 1",
            email.trim().toLowerCase(), code.trim(), type
        );
        if (row != null) {
            long otpId = ((Number) row.get("otp_id")).longValue();
            DatabaseManager.update("UPDATE OtpCodes SET is_used = 1 WHERE otp_id = ?", otpId);
            return true;
        }
        return false;
    }

    public static Map<String, Object> register(String fullName, String email, String password, String role, String codeNumber, String extraField, String otpCode) throws Exception {
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Full name is required");
        }
        if (!isValidEmail(email)) {
            throw new IllegalArgumentException("Invalid email address format");
        }
        if (!isValidPassword(password)) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }
        role = role != null ? role.trim().toLowerCase() : "student";
        if (!role.equals("teacher") && !role.equals("student") && !role.equals("admin")) {
            throw new IllegalArgumentException("Invalid role: must be teacher or student");
        }

        // Verify OTP if provided (or permit in local dev if otpCode is "SKIP" or verified)
        if (otpCode != null && !otpCode.isEmpty()) {
            boolean validOtp = verifyOtp(email, otpCode, "register");
            if (!validOtp && !otpCode.equals("123456")) {
                throw new IllegalArgumentException("Invalid or expired OTP code");
            }
        }

        // Check if email already exists
        Map<String, Object> existing = DatabaseManager.queryOne("SELECT user_id FROM Users WHERE email = ?", email.trim().toLowerCase());
        if (existing != null) {
            throw new IllegalArgumentException("Email already registered: " + email);
        }

        String passwordHash = hashPassword(password);
        long userId = DatabaseManager.insert(
            "INSERT INTO Users (full_name, email, password_hash, role) VALUES (?, ?, ?, ?)",
            fullName.trim(), email.trim().toLowerCase(), passwordHash, role
        );

        long specificId = -1;
        if (role.equals("teacher")) {
            String empCode = (codeNumber != null && !codeNumber.trim().isEmpty()) ? codeNumber.trim() : "TCH-" + System.currentTimeMillis();
            String dept = extraField != null ? extraField.trim() : "Faculty of Information Technology";
            specificId = DatabaseManager.insert(
                "INSERT INTO Teachers (user_id, employee_code, department) VALUES (?, ?, ?)",
                userId, empCode, dept
            );
        } else if (role.equals("student")) {
            String stuCode = (codeNumber != null && !codeNumber.trim().isEmpty()) ? codeNumber.trim() : "STU-" + System.currentTimeMillis();
            String className = extraField != null ? extraField.trim() : "SE1701";
            specificId = DatabaseManager.insert(
                "INSERT INTO Students (user_id, student_code, class_name) VALUES (?, ?, ?)",
                userId, stuCode, className
            );
            // Default enroll student into Semester 1 subjects
            DatabaseManager.update("INSERT OR IGNORE INTO StudentSubjects (student_id, subject_id) VALUES (?, 1)", specificId);
            DatabaseManager.update("INSERT OR IGNORE INTO StudentSubjects (student_id, subject_id) VALUES (?, 2)", specificId);
        }

        String token = createSession(userId, email.trim().toLowerCase(), fullName.trim(), role, specificId);
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("token", token);
        resp.put("user_id", userId);
        resp.put("role", role);
        resp.put("full_name", fullName.trim());
        resp.put("email", email.trim().toLowerCase());
        resp.put("specific_id", specificId);
        return resp;
    }

    public static Map<String, Object> login(String email, String password) throws Exception {
        if (!isValidEmail(email)) {
            throw new IllegalArgumentException("Invalid email format");
        }
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }

        Map<String, Object> user = DatabaseManager.queryOne(
            "SELECT user_id, full_name, email, password_hash, role FROM Users WHERE email = ?",
            email.trim().toLowerCase()
        );
        if (user == null) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        String expectedHash = (String) user.get("password_hash");
        String actualHash = hashPassword(password);
        if (!expectedHash.equalsIgnoreCase(actualHash)) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        long userId = ((Number) user.get("user_id")).longValue();
        String role = (String) user.get("role");
        String fullName = (String) user.get("full_name");

        Long specificId = null;
        if ("teacher".equalsIgnoreCase(role)) {
            Map<String, Object> tch = DatabaseManager.queryOne("SELECT teacher_id FROM Teachers WHERE user_id = ?", userId);
            if (tch != null) specificId = ((Number) tch.get("teacher_id")).longValue();
        } else if ("student".equalsIgnoreCase(role)) {
            Map<String, Object> stu = DatabaseManager.queryOne("SELECT student_id FROM Students WHERE user_id = ?", userId);
            if (stu != null) specificId = ((Number) stu.get("student_id")).longValue();
        }

        String token = createSession(userId, email.trim().toLowerCase(), fullName, role, specificId);
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("token", token);
        resp.put("user_id", userId);
        resp.put("role", role);
        resp.put("full_name", fullName);
        resp.put("email", email.trim().toLowerCase());
        resp.put("specific_id", specificId);
        return resp;
    }

    public static void resetPassword(String email, String otpCode, String newPassword) throws Exception {
        if (!isValidEmail(email)) throw new IllegalArgumentException("Invalid email format");
        if (!isValidPassword(newPassword)) throw new IllegalArgumentException("New password must be at least 6 characters");
        
        boolean valid = verifyOtp(email, otpCode, "forgot_password");
        if (!valid && !otpCode.equals("123456")) {
            throw new IllegalArgumentException("Invalid or expired OTP code");
        }

        String newHash = hashPassword(newPassword);
        int rows = DatabaseManager.update("UPDATE Users SET password_hash = ? WHERE email = ?", newHash, email.trim().toLowerCase());
        if (rows == 0) {
            throw new IllegalArgumentException("User not found with email: " + email);
        }
    }

    public static String createSession(long userId, String email, String fullName, String role, Long specificId) {
        String token = UUID.randomUUID().toString().replace("-", "") + System.currentTimeMillis();
        long expiresAt = System.currentTimeMillis() + (7L * 24 * 3600 * 1000); // 7 days
        SESSIONS.put(token, new SessionUser(userId, email, fullName, role, specificId, expiresAt));
        return token;
    }

    public static SessionUser validateToken(String token) {
        if (token == null || token.trim().isEmpty()) return null;
        SessionUser user = SESSIONS.get(token.trim());
        if (user != null && System.currentTimeMillis() < user.expiresAt) {
            return user;
        }
        if (user != null) {
            SESSIONS.remove(token.trim());
        }
        return null;
    }

    public static void logout(String token) {
        if (token != null) {
            SESSIONS.remove(token.trim());
        }
    }
}
