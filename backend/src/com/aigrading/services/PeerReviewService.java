package com.aigrading.services;

import com.aigrading.db.DatabaseManager;
import com.aigrading.model.PeerReview;

import java.sql.SQLException;
import java.util.*;

/**
 * École 42 Peer-Review Engine:
 * Implements closed rotational review loop (G1 -> G2 -> ... -> G7 -> G1),
 * Correction Points economy (initial 5, -1 to request review, +1 to complete a review),
 * and standard 3-part Rubric (Sandbox 30%, MVC2/Clean Code 40%, Peer Defense 30%).
 */
public class PeerReviewService {

    /**
     * Generate or synchronize the closed rotational review cycle:
     * G1 -> G2 -> G3 -> G4 -> G5 -> G6 -> G7 -> G1
     */
    public static void initializeRotationalCycle() throws SQLException {
        List<Map<String, Object>> teams = DatabaseManager.query("SELECT team_id, team_code FROM Teams ORDER BY team_id ASC");
        if (teams.size() < 2) return;

        int n = teams.size();
        for (int i = 0; i < n; i++) {
            int reviewerTeamId = ((Number) teams.get(i).get("team_id")).intValue();
            int targetTeamId = ((Number) teams.get((i + 1) % n).get("team_id")).intValue();

            // Check if rotation already exists
            Map<String, Object> existing = DatabaseManager.queryOne(
                "SELECT review_id FROM RotationalReviews WHERE reviewer_team_id = ? AND target_team_id = ?",
                reviewerTeamId, targetTeamId
            );

            if (existing == null) {
                DatabaseManager.insert(
                    "INSERT INTO RotationalReviews (reviewer_team_id, target_team_id, status) VALUES (?, ?, 'pending')",
                    reviewerTeamId, targetTeamId
                );
            }
        }
    }

    /**
     * Get assigned target review for a specific team.
     */
    public static Map<String, Object> getAssignedTargetTeam(int reviewerTeamId) throws SQLException {
        return DatabaseManager.queryOne(
            "SELECT r.*, t.team_code AS target_code, t.team_name AS target_name, t.project_title, t.repo_url, " +
            "s.submission_id, s.zip_file_path, s.sha256_hash " +
            "FROM RotationalReviews r " +
            "JOIN Teams t ON r.target_team_id = t.team_id " +
            "LEFT JOIN Submissions s ON s.team_id = t.team_id " +
            "WHERE r.reviewer_team_id = ? LIMIT 1",
            reviewerTeamId
        );
    }

    /**
     * Submit Rubric evaluation for peer review.
     * Computes final score (30% Sandbox, 40% MVC2/Clean Code, 30% Peer Defense)
     * and grants +1 Correction Point to the reviewing team.
     */
    public static Map<String, Object> submitPeerEvaluation(int reviewId, int reviewerTeamId, double sandboxScore,
                                                           double mvc2Score, double defenseScore, String feedback) throws SQLException {
        // Enforce score range [0.0, 10.0]
        sandboxScore = Math.max(0.0, Math.min(10.0, sandboxScore));
        mvc2Score = Math.max(0.0, Math.min(10.0, mvc2Score));
        defenseScore = Math.max(0.0, Math.min(10.0, defenseScore));

        double finalScore = PeerReview.calculateFinalScore(sandboxScore, mvc2Score, defenseScore);

        DatabaseManager.update(
            "UPDATE RotationalReviews SET sandbox_score = ?, mvc2_clean_code_score = ?, " +
            "peer_defense_score = ?, final_score = ?, feedback = ?, status = 'completed', " +
            "reviewed_at = datetime('now') WHERE review_id = ? AND reviewer_team_id = ?",
            sandboxScore, mvc2Score, defenseScore, finalScore, feedback, reviewId, reviewerTeamId
        );

        // Award +1 Correction Point to reviewing team
        earnCorrectionPoint(reviewerTeamId, reviewId, "Earned +1 Point for completing Peer Review evaluation");

        Map<String, Object> res = new HashMap<>();
        res.put("review_id", reviewId);
        res.put("final_score", finalScore);
        res.put("status", "completed");
        return res;
    }

    /**
     * Get Correction Points balance for a team or user.
     */
    public static int getCorrectionPointBalance(int teamId) throws SQLException {
        Map<String, Object> wallet = DatabaseManager.queryOne(
            "SELECT points_balance FROM CorrectionPointsWallet WHERE team_id = ? LIMIT 1", teamId
        );
        if (wallet == null) {
            // Auto initialize with default 5 points
            long wId = DatabaseManager.insert(
                "INSERT INTO CorrectionPointsWallet (team_id, points_balance) VALUES (?, 5)", teamId
            );
            DatabaseManager.insert(
                "INSERT INTO CorrectionPointTransactions (wallet_id, amount, action_type, description) " +
                "VALUES (?, 5, 'initial_grant', 'Initial 5 École 42 Correction Points allocated')", wId
            );
            return 5;
        }
        return ((Number) wallet.get("points_balance")).intValue();
    }

    /**
     * Debits 1 Correction Point when submitting a project for review.
     */
    public static void debitCorrectionPointForSubmission(int teamId, int submissionId) throws SQLException {
        int balance = getCorrectionPointBalance(teamId);
        if (balance < 1) {
            throw new IllegalStateException("Hết điểm sửa lỗi (Correction Points = 0)! Bạn cần chấm bài cho nhóm bạn để nhận thêm điểm trước khi nộp bài.");
        }

        Map<String, Object> wallet = DatabaseManager.queryOne("SELECT wallet_id FROM CorrectionPointsWallet WHERE team_id = ? LIMIT 1", teamId);
        int walletId = ((Number) wallet.get("wallet_id")).intValue();

        DatabaseManager.update(
            "UPDATE CorrectionPointsWallet SET points_balance = points_balance - 1, updated_at = datetime('now') WHERE wallet_id = ?",
            walletId
        );
        DatabaseManager.insert(
            "INSERT INTO CorrectionPointTransactions (wallet_id, amount, action_type, description) " +
            "VALUES (?, -1, 'submission_spent', ?)",
            walletId, "Debited 1 point to request Peer Review for Submission #" + submissionId
        );
    }

    /**
     * Awards 1 Correction Point when completing a peer review.
     */
    public static void earnCorrectionPoint(int reviewerTeamId, int reviewId, String description) throws SQLException {
        int balance = getCorrectionPointBalance(reviewerTeamId);
        Map<String, Object> wallet = DatabaseManager.queryOne("SELECT wallet_id FROM CorrectionPointsWallet WHERE team_id = ? LIMIT 1", reviewerTeamId);
        int walletId = ((Number) wallet.get("wallet_id")).intValue();

        DatabaseManager.update(
            "UPDATE CorrectionPointsWallet SET points_balance = points_balance + 1, updated_at = datetime('now') WHERE wallet_id = ?",
            walletId
        );
        DatabaseManager.insert(
            "INSERT INTO CorrectionPointTransactions (wallet_id, amount, action_type, description, reference_review_id) " +
            "VALUES (?, 1, 'review_earned', ?, ?)",
            walletId, description, reviewId
        );
    }
}
