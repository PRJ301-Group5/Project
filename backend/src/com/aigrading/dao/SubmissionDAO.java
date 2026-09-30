package com.aigrading.dao;

import com.aigrading.db.DatabaseManager;
import com.aigrading.model.Submission;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Submission entity.
 * Uses parameterized queries with PreparedStatement.
 */
public class SubmissionDAO {

    public List<Submission> getAllSubmissions() throws SQLException {
        List<Submission> list = new ArrayList<>();
        String sql = DatabaseManager.isSqlServer() ?
            "SELECT s.submission_id, s.student_id, u.full_name AS student_name, s.team_id, t.team_code, " +
            "       s.assignment_id, a.title AS assignment_title, s.screenshot_path, s.zip_file_path, " +
            "       s.sha256_hash, s.artifact_type, s.queue_priority, s.retry_count, s.submitted_at, s.status " +
            "FROM [Submissions] s " +
            "LEFT JOIN [Students] st ON s.student_id = st.student_id " +
            "LEFT JOIN [Users] u ON st.user_id = u.user_id " +
            "LEFT JOIN [Teams] t ON s.team_id = t.team_id " +
            "JOIN [Assignments] a ON s.assignment_id = a.assignment_id " +
            "ORDER BY s.submission_id DESC" :
            "SELECT s.submission_id, s.student_id, u.full_name AS student_name, s.team_id, t.team_code, " +
            "       s.assignment_id, a.title AS assignment_title, s.screenshot_path, s.zip_file_path, " +
            "       s.sha256_hash, s.artifact_type, s.queue_priority, s.retry_count, s.submitted_at, s.status " +
            "FROM Submissions s " +
            "LEFT JOIN Students st ON s.student_id = st.student_id " +
            "LEFT JOIN Users u ON st.user_id = u.user_id " +
            "LEFT JOIN Teams t ON s.team_id = t.team_id " +
            "JOIN Assignments a ON s.assignment_id = a.assignment_id " +
            "ORDER BY s.submission_id DESC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public Submission getSubmissionById(int id) throws SQLException {
        String sql = DatabaseManager.isSqlServer() ?
            "SELECT s.submission_id, s.student_id, u.full_name AS student_name, s.team_id, t.team_code, " +
            "       s.assignment_id, a.title AS assignment_title, s.screenshot_path, s.zip_file_path, " +
            "       s.sha256_hash, s.artifact_type, s.queue_priority, s.retry_count, s.submitted_at, s.status " +
            "FROM [Submissions] s " +
            "LEFT JOIN [Students] st ON s.student_id = st.student_id " +
            "LEFT JOIN [Users] u ON st.user_id = u.user_id " +
            "LEFT JOIN [Teams] t ON s.team_id = t.team_id " +
            "JOIN [Assignments] a ON s.assignment_id = a.assignment_id " +
            "WHERE s.submission_id = ?" :
            "SELECT s.submission_id, s.student_id, u.full_name AS student_name, s.team_id, t.team_code, " +
            "       s.assignment_id, a.title AS assignment_title, s.screenshot_path, s.zip_file_path, " +
            "       s.sha256_hash, s.artifact_type, s.queue_priority, s.retry_count, s.submitted_at, s.status " +
            "FROM Submissions s " +
            "LEFT JOIN Students st ON s.student_id = st.student_id " +
            "LEFT JOIN Users u ON st.user_id = u.user_id " +
            "LEFT JOIN Teams t ON s.team_id = t.team_id " +
            "JOIN Assignments a ON s.assignment_id = a.assignment_id " +
            "WHERE s.submission_id = ?";

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

    public List<Submission> getSubmissionsByAssignment(int assignmentId) throws SQLException {
        List<Submission> list = new ArrayList<>();
        String sql = DatabaseManager.isSqlServer() ?
            "SELECT s.submission_id, s.student_id, u.full_name AS student_name, s.team_id, t.team_code, " +
            "       s.assignment_id, a.title AS assignment_title, s.screenshot_path, s.zip_file_path, " +
            "       s.sha256_hash, s.artifact_type, s.queue_priority, s.retry_count, s.submitted_at, s.status " +
            "FROM [Submissions] s " +
            "LEFT JOIN [Students] st ON s.student_id = st.student_id " +
            "LEFT JOIN [Users] u ON st.user_id = u.user_id " +
            "LEFT JOIN [Teams] t ON s.team_id = t.team_id " +
            "JOIN [Assignments] a ON s.assignment_id = a.assignment_id " +
            "WHERE s.assignment_id = ? ORDER BY s.submission_id DESC" :
            "SELECT s.submission_id, s.student_id, u.full_name AS student_name, s.team_id, t.team_code, " +
            "       s.assignment_id, a.title AS assignment_title, s.screenshot_path, s.zip_file_path, " +
            "       s.sha256_hash, s.artifact_type, s.queue_priority, s.retry_count, s.submitted_at, s.status " +
            "FROM Submissions s " +
            "LEFT JOIN Students st ON s.student_id = st.student_id " +
            "LEFT JOIN Users u ON st.user_id = u.user_id " +
            "LEFT JOIN Teams t ON s.team_id = t.team_id " +
            "JOIN Assignments a ON s.assignment_id = a.assignment_id " +
            "WHERE s.assignment_id = ? ORDER BY s.submission_id DESC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, assignmentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public int createSubmission(Submission s) throws SQLException {
        String sql = DatabaseManager.isSqlServer() ?
            "INSERT INTO [Submissions] ([student_id], [team_id], [assignment_id], [screenshot_path], [zip_file_path], [sha256_hash], [artifact_type], [status]) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)" :
            "INSERT INTO Submissions (student_id, team_id, assignment_id, screenshot_path, zip_file_path, sha256_hash, artifact_type, status) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (s.getStudentId() != null) ps.setInt(1, s.getStudentId());
            else ps.setNull(1, Types.INTEGER);

            if (s.getTeamId() != null) ps.setInt(2, s.getTeamId());
            else ps.setNull(2, Types.INTEGER);

            ps.setInt(3, s.getAssignmentId());
            ps.setString(4, s.getScreenshotPath());
            ps.setString(5, s.getZipFilePath());
            ps.setString(6, s.getSha256Hash());
            ps.setString(7, s.getArtifactType() != null ? s.getArtifactType() : "screenshot");
            ps.setString(8, s.getStatus() != null ? s.getStatus() : "submitted");
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    s.setSubmissionId(id);
                    return id;
                }
            }
        }
        return -1;
    }

    public boolean updateStatus(int submissionId, String status) throws SQLException {
        String sql = DatabaseManager.isSqlServer() ?
            "UPDATE [Submissions] SET [status] = ? WHERE [submission_id] = ?" :
            "UPDATE Submissions SET status = ? WHERE submission_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, submissionId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteSubmission(int submissionId) throws SQLException {
        return DatabaseManager.deleteSubmissionCascade(submissionId);
    }

    private Submission mapRow(ResultSet rs) throws SQLException {
        Submission s = new Submission();
        s.setSubmissionId(rs.getInt("submission_id"));
        int stId = rs.getInt("student_id");
        if (!rs.wasNull()) s.setStudentId(stId);
        try { s.setStudentName(rs.getString("student_name")); } catch (Exception ignored) {}

        int tmId = rs.getInt("team_id");
        if (!rs.wasNull()) s.setTeamId(tmId);
        try { s.setTeamCode(rs.getString("team_code")); } catch (Exception ignored) {}

        s.setAssignmentId(rs.getInt("assignment_id"));
        try { s.setAssignmentTitle(rs.getString("assignment_title")); } catch (Exception ignored) {}

        s.setScreenshotPath(rs.getString("screenshot_path"));
        s.setZipFilePath(rs.getString("zip_file_path"));
        s.setSha256Hash(rs.getString("sha256_hash"));
        s.setArtifactType(rs.getString("artifact_type"));
        s.setQueuePriority(rs.getInt("queue_priority"));
        s.setRetryCount(rs.getInt("retry_count"));
        s.setSubmittedAt(rs.getTimestamp("submitted_at"));
        s.setStatus(rs.getString("status"));
        return s;
    }
}
