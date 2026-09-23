package com.aigrading.dao;

import com.aigrading.db.DatabaseManager;
import com.aigrading.model.Assignment;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Assignment entity.
 * Full CRUD operations with parameterized PreparedStatement (Safe against SQL Injection).
 */
public class AssignmentDAO {

    public List<Assignment> getAllAssignments() throws SQLException {
        List<Assignment> list = new ArrayList<>();
        String sql = DatabaseManager.isSqlServer() ?
            "SELECT a.assignment_id, a.teacher_id, u.full_name AS teacher_name, a.subject_id, s.subject_code, " +
            "       a.title, a.prototype_url, a.prompt_file_url, a.deadline, a.rubric_json, a.created_at " +
            "FROM [Assignments] a " +
            "JOIN [Teachers] t ON a.teacher_id = t.teacher_id " +
            "JOIN [Users] u ON t.user_id = u.user_id " +
            "LEFT JOIN [Subjects] s ON a.subject_id = s.subject_id " +
            "ORDER BY a.assignment_id DESC" :
            "SELECT a.assignment_id, a.teacher_id, u.full_name AS teacher_name, a.subject_id, s.subject_code, " +
            "       a.title, a.prototype_url, a.prompt_file_url, a.deadline, a.rubric_json, a.created_at " +
            "FROM Assignments a " +
            "JOIN Teachers t ON a.teacher_id = t.teacher_id " +
            "JOIN Users u ON t.user_id = u.user_id " +
            "LEFT JOIN Subjects s ON a.subject_id = s.subject_id " +
            "ORDER BY a.assignment_id DESC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public Assignment getAssignmentById(int id) throws SQLException {
        String sql = DatabaseManager.isSqlServer() ?
            "SELECT a.assignment_id, a.teacher_id, u.full_name AS teacher_name, a.subject_id, s.subject_code, " +
            "       a.title, a.prototype_url, a.prompt_file_url, a.deadline, a.rubric_json, a.created_at " +
            "FROM [Assignments] a " +
            "JOIN [Teachers] t ON a.teacher_id = t.teacher_id " +
            "JOIN [Users] u ON t.user_id = u.user_id " +
            "LEFT JOIN [Subjects] s ON a.subject_id = s.subject_id " +
            "WHERE a.assignment_id = ?" :
            "SELECT a.assignment_id, a.teacher_id, u.full_name AS teacher_name, a.subject_id, s.subject_code, " +
            "       a.title, a.prototype_url, a.prompt_file_url, a.deadline, a.rubric_json, a.created_at " +
            "FROM Assignments a " +
            "JOIN Teachers t ON a.teacher_id = t.teacher_id " +
            "JOIN Users u ON t.user_id = u.user_id " +
            "LEFT JOIN Subjects s ON a.subject_id = s.subject_id " +
            "WHERE a.assignment_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public List<Assignment> searchAssignments(String keyword) throws SQLException {
        List<Assignment> list = new ArrayList<>();
        String sql = DatabaseManager.isSqlServer() ?
            "SELECT a.assignment_id, a.teacher_id, u.full_name AS teacher_name, a.subject_id, s.subject_code, " +
            "       a.title, a.prototype_url, a.prompt_file_url, a.deadline, a.rubric_json, a.created_at " +
            "FROM [Assignments] a " +
            "JOIN [Teachers] t ON a.teacher_id = t.teacher_id " +
            "JOIN [Users] u ON t.user_id = u.user_id " +
            "LEFT JOIN [Subjects] s ON a.subject_id = s.subject_id " +
            "WHERE a.title LIKE ? OR u.full_name LIKE ? " +
            "ORDER BY a.assignment_id DESC" :
            "SELECT a.assignment_id, a.teacher_id, u.full_name AS teacher_name, a.subject_id, s.subject_code, " +
            "       a.title, a.prototype_url, a.prompt_file_url, a.deadline, a.rubric_json, a.created_at " +
            "FROM Assignments a " +
            "JOIN Teachers t ON a.teacher_id = t.teacher_id " +
            "JOIN Users u ON t.user_id = u.user_id " +
            "LEFT JOIN Subjects s ON a.subject_id = s.subject_id " +
            "WHERE a.title LIKE ? OR u.full_name LIKE ? " +
            "ORDER BY a.assignment_id DESC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String param = "%" + (keyword != null ? keyword.trim() : "") + "%";
            ps.setString(1, param);
            ps.setString(2, param);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public int createAssignment(Assignment a) throws SQLException {
        String sql = DatabaseManager.isSqlServer() ?
            "INSERT INTO [Assignments] ([teacher_id], [subject_id], [title], [prototype_url], [prompt_file_url], [deadline], [rubric_json]) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?)" :
            "INSERT INTO Assignments (teacher_id, subject_id, title, prototype_url, prompt_file_url, deadline, rubric_json) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, a.getTeacherId());
            if (a.getSubjectId() != null) ps.setInt(2, a.getSubjectId());
            else ps.setNull(2, Types.INTEGER);
            ps.setString(3, a.getTitle());
            ps.setString(4, a.getPrototypeUrl());
            ps.setString(5, a.getPromptFileUrl());
            ps.setTimestamp(6, a.getDeadline());
            ps.setString(7, a.getRubricJson());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int genId = rs.getInt(1);
                    a.setAssignmentId(genId);
                    return genId;
                }
            }
        }
        return -1;
    }

    public boolean updateAssignment(Assignment a) throws SQLException {
        String sql = DatabaseManager.isSqlServer() ?
            "UPDATE [Assignments] SET [title] = ?, [prototype_url] = ?, [prompt_file_url] = ?, [deadline] = ?, [rubric_json] = ? " +
            "WHERE [assignment_id] = ?" :
            "UPDATE Assignments SET title = ?, prototype_url = ?, prompt_file_url = ?, deadline = ?, rubric_json = ? " +
            "WHERE assignment_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, a.getTitle());
            ps.setString(2, a.getPrototypeUrl());
            ps.setString(3, a.getPromptFileUrl());
            ps.setTimestamp(4, a.getDeadline());
            ps.setString(5, a.getRubricJson());
            ps.setInt(6, a.getAssignmentId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteAssignment(int assignmentId) throws SQLException {
        // Safe cascade deletion
        return DatabaseManager.deleteAssignmentCascade(assignmentId);
    }

    public boolean isDeadlineExpired(int assignmentId) throws SQLException {
        Assignment a = getAssignmentById(assignmentId);
        return a != null && a.isExpired();
    }

    private Assignment mapRow(ResultSet rs) throws SQLException {
        Assignment a = new Assignment();
        a.setAssignmentId(rs.getInt("assignment_id"));
        a.setTeacherId(rs.getInt("teacher_id"));
        try { a.setTeacherName(rs.getString("teacher_name")); } catch (Exception ignored) {}
        int subId = rs.getInt("subject_id");
        if (!rs.wasNull()) a.setSubjectId(subId);
        try { a.setSubjectCode(rs.getString("subject_code")); } catch (Exception ignored) {}
        a.setTitle(rs.getString("title"));
        a.setPrototypeUrl(rs.getString("prototype_url"));
        a.setPromptFileUrl(rs.getString("prompt_file_url"));
        a.setDeadline(rs.getTimestamp("deadline"));
        a.setRubricJson(rs.getString("rubric_json"));
        a.setCreatedAt(rs.getTimestamp("created_at"));
        return a;
    }
}
