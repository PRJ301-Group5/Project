package com.aigrading.services;

import com.aigrading.db.DatabaseManager;
import com.aigrading.util.Sha256Util;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.*;

/**
 * Service quản lý Thêm, Xóa, Sửa (CRUD) và Đồng bộ CSDL toàn diện trực tiếp từ Java.
 * Cho phép lập trình viên và người dùng điều khiển CSDL (SQL Server & SQLite)
 * mà không cần phải can thiệp thủ công trong SQL Server Management Studio (SSMS).
 */
public class DatabaseCrudService {

    // ==========================================================
    // 1. TỰ ĐỘNG ĐỒNG BỘ CSDL (SCHEMA, PROCEDURES, TRIGGERS)
    // ==========================================================
    
    /**
     * Đồng bộ hóa toàn bộ cấu trúc CSDL từ mã nguồn Java.
     * Tự động kiểm tra bảng, tạo bảng thiếu, bổ sung cột mới và cài đặt Stored Procedures / Triggers.
     */
    public static Map<String, Object> synchronizeDatabase() {
        Map<String, Object> result = new LinkedHashMap<>();
        try {
            DatabaseManager.syncAllDatabaseStructures();
            result.put("success", true);
            result.put("message", "Cơ sở dữ liệu đã được đồng bộ hóa 100% từ Java!");
            result.put("engine", DatabaseManager.isSqlServer() ? "Microsoft SQL Server (localhost\\MAYAO)" : "SQLite (Embedded)");
            result.put("tables", DatabaseManager.listAllTablesWithCounts());
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    // ==========================================================
    // 2. QUẢN LÝ NHÓM (TEAMS & MEMBERS) TRỰC TIẾP TỪ JAVA
    // ==========================================================

    public static long createTeam(String teamCode, String teamName, String trackName, String projectTitle, String repoUrl) throws SQLException {
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("team_code", teamCode);
        fields.put("team_name", teamName);
        fields.put("track_name", trackName);
        fields.put("project_title", projectTitle);
        fields.put("repo_url", repoUrl);
        return DatabaseManager.insertRecord("Teams", fields);
    }

    public static boolean updateTeam(int teamId, Map<String, Object> updateFields) throws SQLException {
        return DatabaseManager.updateRecord("Teams", updateFields, "team_id", teamId) > 0;
    }

    public static boolean deleteTeam(int teamId) throws SQLException {
        // Cascade delete members first
        DatabaseManager.deleteRecord("TeamMembers", "team_id", teamId, false);
        return DatabaseManager.deleteRecord("Teams", "team_id", teamId, false) > 0;
    }

    public static long addTeamMember(int teamId, int userId, String roleInTeam) throws SQLException {
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("team_id", teamId);
        fields.put("user_id", userId);
        fields.put("role_in_team", roleInTeam != null ? roleInTeam : "member");
        return DatabaseManager.insertRecord("TeamMembers", fields);
    }

    public static boolean removeTeamMember(int teamId, int userId) throws SQLException {
        String sql = DatabaseManager.isSqlServer() ?
            "DELETE FROM [TeamMembers] WHERE [team_id] = ? AND [user_id] = ?" :
            "DELETE FROM TeamMembers WHERE team_id = ? AND user_id = ?";
        return DatabaseManager.update(sql, teamId, userId) > 0;
    }

    // ==========================================================
    // 3. QUẢN LÝ NGƯỜI DÙNG (USERS) TRỰC TIẾP TỪ JAVA
    // ==========================================================

    public static long createUser(String fullName, String email, String plainPassword, String role) throws Exception {
        String hash = Sha256Util.calculateHash(plainPassword.getBytes(StandardCharsets.UTF_8));
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("full_name", fullName);
        fields.put("email", email);
        fields.put("password_hash", hash);
        fields.put("role", role);
        long userId = DatabaseManager.insertRecord("Users", fields);

        // Auto create Student or Teacher record
        if ("student".equalsIgnoreCase(role)) {
            String studentCode = "HE" + (160000 + userId);
            Map<String, Object> sFields = new LinkedHashMap<>();
            sFields.put("user_id", userId);
            sFields.put("student_code", studentCode);
            sFields.put("major", "Software Engineering");
            DatabaseManager.insertRecord("Students", sFields);
        } else if ("teacher".equalsIgnoreCase(role)) {
            String empCode = "GV" + (100 + userId);
            Map<String, Object> tFields = new LinkedHashMap<>();
            tFields.put("user_id", userId);
            tFields.put("employee_code", empCode);
            tFields.put("department", "Information Technology");
            DatabaseManager.insertRecord("Teachers", tFields);
        }
        return userId;
    }

    public static boolean updateUser(int userId, Map<String, Object> updateFields) throws Exception {
        if (updateFields.containsKey("password")) {
            String plain = (String) updateFields.remove("password");
            String hash = Sha256Util.calculateHash(plain.getBytes(StandardCharsets.UTF_8));
            updateFields.put("password_hash", hash);
        }
        return DatabaseManager.updateRecord("Users", updateFields, "user_id", userId) > 0;
    }

    public static boolean deleteUser(int userId) throws SQLException {
        return DatabaseManager.deleteRecord("Users", "user_id", userId, false) > 0;
    }

    // ==========================================================
    // 4. QUẢN LÝ BÀI TẬP (ASSIGNMENTS) VỚI CASCADE TỰ ĐỘNG
    // ==========================================================

    public static long createAssignment(int teacherId, Integer subjectId, String title, String deadline, String rubricJson) throws SQLException {
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("teacher_id", teacherId);
        if (subjectId != null) fields.put("subject_id", subjectId);
        fields.put("title", title);
        fields.put("deadline", deadline);
        fields.put("rubric_json", rubricJson != null ? rubricJson : "{}");
        return DatabaseManager.insertRecord("Assignments", fields);
    }

    public static boolean updateAssignment(int assignmentId, Map<String, Object> fields) throws SQLException {
        return DatabaseManager.updateRecord("Assignments", fields, "assignment_id", assignmentId) > 0;
    }

    public static boolean deleteAssignment(long assignmentId) throws SQLException {
        return DatabaseManager.deleteAssignmentCascade(assignmentId);
    }

    // ==========================================================
    // 5. QUẢN LÝ TEST CASES MOULINETTE
    // ==========================================================

    public static long createTestCase(String testCode, String title, String description, String testType, String expectedOutput, double weight) throws SQLException {
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("test_code", testCode);
        fields.put("title", title);
        fields.put("description", description);
        fields.put("test_type", testType);
        fields.put("expected_output", expectedOutput);
        fields.put("weight", weight);
        return DatabaseManager.insertRecord("MoulinetteTestCases", fields);
    }

    public static boolean updateTestCase(int testId, Map<String, Object> fields) throws SQLException {
        return DatabaseManager.updateRecord("MoulinetteTestCases", fields, "test_id", testId) > 0;
    }

    public static boolean deleteTestCase(int testId) throws SQLException {
        DatabaseManager.deleteRecord("MoulinetteResults", "test_id", testId, false);
        return DatabaseManager.deleteRecord("MoulinetteTestCases", "test_id", testId, false) > 0;
    }

    // ==========================================================
    // 6. THAO TÁC CƠ SỞ DỮ LIỆU ĐỘNG TỔNG QUÁT (GENERIC CRUD)
    // ==========================================================

    public static List<Map<String, Object>> getTableRows(String tableName, String orderBy) throws SQLException {
        return DatabaseManager.queryAll(tableName, orderBy);
    }

    public static long insertDynamicRow(String tableName, Map<String, Object> fields) throws SQLException {
        return DatabaseManager.insertRecord(tableName, fields);
    }

    public static boolean updateDynamicRow(String tableName, Map<String, Object> fields, String idCol, Object idVal) throws SQLException {
        return DatabaseManager.updateRecord(tableName, fields, idCol, idVal) > 0;
    }

    public static boolean deleteDynamicRow(String tableName, String idCol, Object idVal, boolean cascade) throws SQLException {
        return DatabaseManager.deleteRecord(tableName, idCol, idVal, cascade) > 0;
    }

    public static void addNewColumnToTable(String tableName, String columnName, String columnType) {
        DatabaseManager.addColumnIfNotExists(tableName, columnName, columnType);
    }
}
