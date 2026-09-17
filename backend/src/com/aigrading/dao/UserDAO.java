package com.aigrading.dao;

import com.aigrading.db.DatabaseManager;
import com.aigrading.model.User;
import com.aigrading.util.Sha256Util;

import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for User entity.
 * Uses strictly parameterized queries (PreparedStatement) to prevent SQL Injection.
 */
public class UserDAO {

    public User authenticate(String email, String rawPassword) throws SQLException {
        if (email == null || rawPassword == null) return null;
        String passwordHash;
        try {
            passwordHash = Sha256Util.calculateHash(rawPassword.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new SQLException("Lỗi mã hóa mật khẩu: " + e.getMessage(), e);
        }
        String sql = DatabaseManager.isSqlServer() ?
            "SELECT user_id, full_name, email, password_hash, role, created_at FROM [Users] WHERE [email] = ? AND [password_hash] = ?" :
            "SELECT user_id, full_name, email, password_hash, role, created_at FROM Users WHERE email = ? AND password_hash = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email.trim().toLowerCase());
            ps.setString(2, passwordHash);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public User findByEmail(String email) throws SQLException {
        if (email == null) return null;
        String sql = DatabaseManager.isSqlServer() ?
            "SELECT user_id, full_name, email, password_hash, role, created_at FROM [Users] WHERE [email] = ?" :
            "SELECT user_id, full_name, email, password_hash, role, created_at FROM Users WHERE email = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email.trim().toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public User findById(int userId) throws SQLException {
        String sql = DatabaseManager.isSqlServer() ?
            "SELECT user_id, full_name, email, password_hash, role, created_at FROM [Users] WHERE [user_id] = ?" :
            "SELECT user_id, full_name, email, password_hash, role, created_at FROM Users WHERE user_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public List<User> getAllUsers() throws SQLException {
        List<User> list = new ArrayList<>();
        String sql = DatabaseManager.isSqlServer() ?
            "SELECT user_id, full_name, email, password_hash, role, created_at FROM [Users] ORDER BY [user_id] ASC" :
            "SELECT user_id, full_name, email, password_hash, role, created_at FROM Users ORDER BY user_id ASC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public int createUser(User user, String rawPassword) throws SQLException {
        String passwordHash;
        try {
            passwordHash = Sha256Util.calculateHash(rawPassword.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new SQLException("Lỗi mã hóa mật khẩu: " + e.getMessage(), e);
        }
        String sql = DatabaseManager.isSqlServer() ?
            "INSERT INTO [Users] ([full_name], [email], [password_hash], [role]) VALUES (?, ?, ?, ?)" :
            "INSERT INTO Users (full_name, email, password_hash, role) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getFullName());
            ps.setString(2, user.getEmail().trim().toLowerCase());
            ps.setString(3, passwordHash);
            ps.setString(4, user.getRole() != null ? user.getRole().toLowerCase() : "student");
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int generatedId = rs.getInt(1);
                    user.setUserId(generatedId);
                    return generatedId;
                }
            }
        }
        return -1;
    }

    public boolean updateUser(User user) throws SQLException {
        String sql = DatabaseManager.isSqlServer() ?
            "UPDATE [Users] SET [full_name] = ?, [email] = ?, [role] = ? WHERE [user_id] = ?" :
            "UPDATE Users SET full_name = ?, email = ?, role = ? WHERE user_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getFullName());
            ps.setString(2, user.getEmail().trim().toLowerCase());
            ps.setString(3, user.getRole());
            ps.setInt(4, user.getUserId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteUser(int userId) throws SQLException {
        String sql = DatabaseManager.isSqlServer() ?
            "DELETE FROM [Users] WHERE [user_id] = ?" :
            "DELETE FROM Users WHERE user_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        }
    }

    private User mapRow(ResultSet rs) throws SQLException {
        User u = new User();
        u.setUserId(rs.getInt("user_id"));
        u.setFullName(rs.getString("full_name"));
        u.setEmail(rs.getString("email"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setRole(rs.getString("role"));
        u.setCreatedAt(rs.getTimestamp("created_at"));
        return u;
    }
}
