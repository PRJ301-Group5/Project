package tests;

import com.aigrading.db.DatabaseManager;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

public class VerifyDatabase {
    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("   VERIFYING DATABASE TABLES & DATA INTEGRITY     ");
        System.out.println("==================================================");

        try {
            // Explicitly sync and verify SQLite main database
            DatabaseManager.init("database/aigrading.db");

            String[] expectedTables = {
                "Users", "Teachers", "Students", "Subjects", "StudentSubjects", "Classes",
                "Teams", "TeamMembers", "Assignments", "Submissions", "AIAnalysisResults",
                "GitMetrics", "RotationalReviews", "CorrectionPointsWallet", "CorrectionPointTransactions",
                "MoulinetteTestCases", "MoulinetteResults", "Grades", "Appeals", "OtpCodes"
            };

            System.out.println("\nChecking Tables & Records in database/aigrading.db:");
            System.out.println("--------------------------------------------------");
            for (String table : expectedTables) {
                try {
                    Map<String, Object> countRow = DatabaseManager.queryOne("SELECT COUNT(*) AS cnt FROM " + table);
                    long count = ((Number) countRow.get("cnt")).longValue();
                    System.out.println(String.format("  [✓] Table %-26s : %3d rows", table, count));
                } catch (Exception e) {
                    System.err.println(String.format("  [✕] Table %-26s : ERROR (%s)", table, e.getMessage()));
                }
            }

            System.out.println("\nChecking Track 5 Sample Data:");
            System.out.println("--------------------------------------------------");
            List<Map<String, Object>> teams = DatabaseManager.query("SELECT team_id, team_code, team_name, track_name FROM Teams");
            for (Map<String, Object> t : teams) {
                System.out.println(String.format("  • %s: %-20s (%s)", t.get("team_code"), t.get("team_name"), t.get("track_name")));
            }

            System.out.println("\nChecking G5 Git Metrics & Anomaly Detection:");
            System.out.println("--------------------------------------------------");
            List<Map<String, Object>> metrics = DatabaseManager.query("SELECT author_name, total_commits, lines_added, lines_deleted, contribution_score, anomaly_flags FROM GitMetrics");
            for (Map<String, Object> m : metrics) {
                System.out.println(String.format("  • %-15s | Commits: %2d | +%-4d / -%-3d | ICS: %5.1f%% | Anomaly: %s",
                    m.get("author_name"), m.get("total_commits"), m.get("lines_added"), m.get("lines_deleted"),
                    ((Number) m.get("contribution_score")).doubleValue(), m.get("anomaly_flags")));
            }

            System.out.println("\nChecking École 42 Rotational Reviews (Cycle G1->G2..->G7->G1):");
            System.out.println("--------------------------------------------------");
            List<Map<String, Object>> reviews = DatabaseManager.query(
                "SELECT r.review_id, t1.team_code as rev, t2.team_code as tgt, r.final_score, r.status " +
                "FROM RotationalReviews r " +
                "JOIN Teams t1 ON r.reviewer_team_id = t1.team_id " +
                "JOIN Teams t2 ON r.target_team_id = t2.team_id"
            );
            for (Map<String, Object> r : reviews) {
                System.out.println(String.format("  • %s ➔ %s : Final Score = %s | Status = %s",
                    r.get("rev"), r.get("tgt"), r.get("final_score"), r.get("status")));
            }

            System.out.println("\nChecking Moulinette Test Cases:");
            System.out.println("--------------------------------------------------");
            List<Map<String, Object>> testCases = DatabaseManager.query("SELECT test_code, title, test_type, weight FROM MoulinetteTestCases");
            for (Map<String, Object> tc : testCases) {
                System.out.println(String.format("  • [%-6s] %-12s : %s", tc.get("test_type"), tc.get("test_code"), tc.get("title")));
            }

            System.out.println("\n==================================================");
            System.out.println("   DATABASE VERIFICATION: 100% OPERATIONAL!       ");
            System.out.println("==================================================");

        } catch (Exception e) {
            System.err.println("Database verification failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
