package tests;

import com.aigrading.auth.AuthService;
import com.aigrading.db.DatabaseManager;
import com.aigrading.filter.JwtAuthFilter;
import com.aigrading.filter.Sha256IntegrityFilter;
import com.aigrading.model.GitMetric;
import com.aigrading.model.PeerReview;
import com.aigrading.services.*;
import com.aigrading.util.JwtUtil;
import com.aigrading.util.Sha256Util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class BackendTests {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("      RUNNING BACKEND AUTOMATED TEST SUITE        ");
        System.out.println("   (PRJ301 MVC2 + Track 5 Git Assessor + Tests)   ");
        System.out.println("==================================================");

        try {
            // Setup fresh test database
            new File("database/test_aigrading.db").delete();
            DatabaseManager.init("database/test_aigrading.db");

            testDatabaseInitialization();
            testPasswordHashing();
            testEmailAndInputValidation();
            testOtpWorkflow();
            testUserRegistrationAndLogin();
            testRbacAuthorization();
            testAssignmentCrudAndPagination();
            testAiGradingEngine();
            testGradeAndAppealLifecycle();
            testSqlInjectionResilience();
            testEdgeCases();

            // Track 5 & RBL Advanced Architecture Tests
            testSha256FilterAndZipSlipResilience();
            testJwtAuthenticationAndClaims();
            testGitAnalyticsAndFreeRiderDetection();
            testEcole42PeerReviewAndCorrectionPoints();
            testMoulinetteAutomatedTestSuite();
            testDockerSandboxRunner();

        } catch (Exception e) {
            System.err.println("Fatal test exception: " + e.getMessage());
            e.printStackTrace();
            failed++;
        }

        System.out.println("==================================================");
        System.out.println(String.format("TEST RESULTS: %d PASSED, %d FAILED", passed, failed));
        System.out.println("==================================================");

        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void assertTrue(String testName, boolean condition, String message) {
        if (condition) {
            System.out.println("[PASS] " + testName);
            passed++;
        } else {
            System.err.println("[FAIL] " + testName + " -> " + message);
            failed++;
        }
    }

    private static void testDatabaseInitialization() throws Exception {
        String[] requiredTables = {
            "Users", "Teachers", "Students", "Subjects", "StudentSubjects",
            "Teams", "TeamMembers", "Assignments", "Submissions", "AIAnalysisResults",
            "GitMetrics", "RotationalReviews", "CorrectionPointsWallet", "CorrectionPointTransactions",
            "MoulinetteTestCases", "MoulinetteResults", "Grades", "Appeals", "OtpCodes"
        };
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {
            for (String tbl : requiredTables) {
                try (ResultSet rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='" + tbl + "'")) {
                    assertTrue("Table " + tbl + " exists", rs.next(), "Table " + tbl + " was not created");
                }
            }
        }
    }

    private static void testPasswordHashing() {
        String hash1 = AuthService.hashPassword("Teacher@123");
        String hash2 = AuthService.hashPassword("Teacher@123");
        String hash3 = AuthService.hashPassword("Student@123");

        assertTrue("Password hashing is deterministic", hash1.equals(hash2), "Hashes should match for same password");
        assertTrue("Different passwords produce different hashes", !hash1.equals(hash3), "Hashes should differ");
        assertTrue("Hash is not plaintext", !hash1.contains("Teacher"), "Hash must obscure password");
    }

    private static void testEmailAndInputValidation() {
        assertTrue("Valid email accepted", AuthService.isValidEmail("test@school.edu"), "Should accept valid email");
        assertTrue("Invalid email without @ rejected", !AuthService.isValidEmail("invalid-email"), "Should reject missing @");
        assertTrue("Invalid email without domain rejected", !AuthService.isValidEmail("user@"), "Should reject missing domain");
        assertTrue("Password min 6 chars", AuthService.isValidPassword("secret123"), "Should accept 6+ chars");
        assertTrue("Short password rejected", !AuthService.isValidPassword("123"), "Should reject < 6 chars");
    }

    private static void testOtpWorkflow() throws Exception {
        String code = AuthService.generateOtp("verify@school.edu", "register");
        assertTrue("OTP is 6 digits", code != null && code.length() == 6, "OTP should be 6 digits");

        boolean verified = AuthService.verifyOtp("verify@school.edu", code, "register");
        assertTrue("OTP verifies successfully", verified, "Valid OTP should verify");

        boolean secondUse = AuthService.verifyOtp("verify@school.edu", code, "register");
        assertTrue("OTP cannot be reused", !secondUse, "OTP should be single-use");
    }

    private static void testUserRegistrationAndLogin() throws Exception {
        Map<String, Object> reg = AuthService.register(
            "Dr. Alan Turing", "alan.turing@school.edu", "Secret@2026", "teacher", "TCH-999", "Computer Science", "123456"
        );
        assertTrue("Registration succeeds", reg != null && reg.containsKey("token"), "Registration should return auth token");

        Map<String, Object> login = AuthService.login("alan.turing@school.edu", "Secret@2026");
        assertTrue("Login succeeds with correct credentials", login != null && "teacher".equals(login.get("role")), "Login should return teacher session");

        boolean failedLogin = false;
        try {
            AuthService.login("alan.turing@school.edu", "WrongPassword");
        } catch (IllegalArgumentException e) {
            failedLogin = true;
        }
        assertTrue("Login fails with incorrect password", failedLogin, "Should reject incorrect password");
    }

    private static void testRbacAuthorization() {
        String token = AuthService.createSession(100, "stud@school.edu", "Student User", "student", 10L);
        AuthService.SessionUser session = AuthService.validateToken(token);
        assertTrue("Session user validated", session != null && "student".equals(session.role), "Token should resolve to student role");

        AuthService.logout(token);
        AuthService.SessionUser expired = AuthService.validateToken(token);
        assertTrue("Session destroyed on logout", expired == null, "Logged out token should be null");
    }

    private static void testAssignmentCrudAndPagination() throws Exception {
        long assignId = DatabaseManager.insert(
            "INSERT INTO Assignments (teacher_id, subject_id, title, prototype_url, deadline, rubric_json) " +
            "VALUES (?, ?, ?, ?, datetime('now', '+3 days'), ?)",
            1, 1, "Automated Test Assignment", "https://example.com/proto", "{}"
        );
        assertTrue("Assignment created with generated ID", assignId > 0, "Insert should return generated PK");

        Map<String, Object> read = DatabaseManager.queryOne("SELECT * FROM Assignments WHERE assignment_id = ?", assignId);
        assertTrue("Assignment read successfully", read != null && "Automated Test Assignment".equals(read.get("title")), "Should fetch created assignment");

        int updated = DatabaseManager.update("UPDATE Assignments SET title = ? WHERE assignment_id = ?", "Updated Test Assignment", assignId);
        assertTrue("Assignment updated", updated == 1, "Update should affect 1 row");

        List<Map<String, Object>> search = DatabaseManager.query(
            "SELECT * FROM Assignments WHERE title LIKE ?", "%Updated Test%"
        );
        assertTrue("Assignment searchable", !search.isEmpty(), "Should find assignment by title search");

        int deleted = DatabaseManager.update("DELETE FROM Assignments WHERE assignment_id = ?", assignId);
        assertTrue("Assignment deleted", deleted == 1, "Delete should remove row");
    }

    private static void testAiGradingEngine() throws Exception {
        long assignId = DatabaseManager.insert(
            "INSERT INTO Assignments (teacher_id, subject_id, title, prototype_url, deadline, rubric_json) " +
            "VALUES (?, ?, ?, ?, datetime('now', '+5 days'), ?)",
            1, 1, "AI Vision Evaluation Test", "https://example.com/ui-spec.png", "{\"criteria\":[]}"
        );

        long subId = DatabaseManager.insert(
            "INSERT INTO Submissions (student_id, assignment_id, screenshot_path, status) " +
            "VALUES (?, ?, ?, 'submitted')",
            1, assignId, "/uploads/test_sub.png"
        );

        Map<String, Object> aiResult = AIGradingService.gradeSubmission(subId, null);
        assertTrue("AI Grading produces similarity score", aiResult.containsKey("similarity_score"), "Should compute similarity score");
        assertTrue("AI Grading produces detected elements", aiResult.containsKey("detected_elements"), "Should detect UI elements");
        assertTrue("AI Grading produces layout report", aiResult.containsKey("layout_report"), "Should generate layout report");
        assertTrue("AI Grading produces feedback", aiResult.containsKey("ai_feedback"), "Should generate feedback");

        Map<String, Object> saved = DatabaseManager.queryOne("SELECT * FROM AIAnalysisResults WHERE submission_id = ?", subId);
        assertTrue("AI Result persisted to database", saved != null, "AI analysis result should be stored in AIAnalysisResults table");
    }

    private static void testGradeAndAppealLifecycle() throws Exception {
        long subId = 1L;
        DatabaseManager.update(
            "INSERT INTO Grades (submission_id, graded_by, score, comment) VALUES (?, ?, ?, ?) " +
            "ON CONFLICT(submission_id) DO UPDATE SET score = excluded.score",
            subId, 1, 8.5, "Good work with solid structure."
        );
        Map<String, Object> grade = DatabaseManager.queryOne("SELECT grade_id, score FROM Grades WHERE submission_id = ?", subId);
        assertTrue("Grade saved", grade != null && Math.abs(((Number) grade.get("score")).doubleValue() - 8.5) < 0.01, "Grade score should be 8.5");

        long gradeId = ((Number) grade.get("grade_id")).longValue();

        long appealId = DatabaseManager.insert(
            "INSERT INTO Appeals (grade_id, student_id, reason, status) VALUES (?, ?, ?, 'pending')",
            gradeId, 1, "Requesting review of typography criteria"
        );
        assertTrue("Appeal filed", appealId > 0, "Appeal record should be created");

        DatabaseManager.update("UPDATE Appeals SET status = 'approved' WHERE appeal_id = ?", appealId);
        DatabaseManager.update("UPDATE Grades SET score = 9.0 WHERE grade_id = ?", gradeId);

        Map<String, Object> updatedGrade = DatabaseManager.queryOne("SELECT score FROM Grades WHERE grade_id = ?", gradeId);
        assertTrue("Appeal resolved with updated score", Math.abs(((Number) updatedGrade.get("score")).doubleValue() - 9.0) < 0.01, "Grade should be updated to 9.0");
    }

    private static void testSqlInjectionResilience() throws Exception {
        String maliciousInput = "admin' OR '1'='1";
        Map<String, Object> user = DatabaseManager.queryOne("SELECT * FROM Users WHERE email = ?", maliciousInput);
        assertTrue("SQL Injection safely prevented by prepared statements", user == null, "Malicious payload must not return records");
    }

    private static void testEdgeCases() {
        try {
            AuthService.register("", "", "", "student", "", "", "");
            assertTrue("Rejects empty register payload", false, "Should throw exception");
        } catch (Exception e) {
            assertTrue("Rejects empty register payload", true, "Properly caught validation error");
        }

        try {
            AuthService.register("Tester", "t@t.com", "Password123", "superadmin", "", "", "");
            assertTrue("Rejects unauthorized role", false, "Should throw exception");
        } catch (Exception e) {
            assertTrue("Rejects unauthorized role", true, "Properly caught role error");
        }
    }

    // =========================================================================
    // NEW TRACK 5 & ADVANCED ARCHITECTURE TESTS
    // =========================================================================

    private static void testSha256FilterAndZipSlipResilience() throws Exception {
        // 1. Valid ZIP creation in memory
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            ZipEntry entry = new ZipEntry("src/com/aigrading/Main.java");
            zos.putNextEntry(entry);
            zos.write("package com.aigrading; public class Main {}".getBytes());
            zos.closeEntry();
        }
        byte[] validZip = baos.toByteArray();
        String validHash = Sha256Util.calculateHash(validZip);

        assertTrue("Valid ZIP has 64-char SHA-256", validHash != null && validHash.length() == 64, "SHA-256 must be 64 hex characters");

        // 2. Gateway Filter accepts valid artifact
        Sha256IntegrityFilter.FilterResult resOk = Sha256IntegrityFilter.inspectArtifact(validZip, validHash);
        assertTrue("Filter accepts valid artifact", resOk.isAccepted, "Should pass valid ZIP artifact");
        assertTrue("Calculated SHA-256 matches", validHash.equalsIgnoreCase(resOk.calculatedSha256), "Hash should match");

        // 3. Gateway Filter rejects tampered checksum
        Sha256IntegrityFilter.FilterResult resTampered = Sha256IntegrityFilter.inspectArtifact(validZip, "0000000000000000000000000000000000000000000000000000000000000000");
        assertTrue("Filter rejects tampered checksum", !resTampered.isAccepted, "Should detect mismatched hash");

        // 4. Gateway Filter rejects non-ZIP
        byte[] nonZip = "This is not a zip file".getBytes();
        Sha256IntegrityFilter.FilterResult resNonZip = Sha256IntegrityFilter.inspectArtifact(nonZip, null);
        assertTrue("Filter rejects non-ZIP format", !resNonZip.isAccepted, "Should reject invalid magic bytes");

        // 5. Security: Zip Slip Path Traversal Attack Test
        ByteArrayOutputStream evilBaos = new ByteArrayOutputStream();
        try (ZipOutputStream evilZos = new ZipOutputStream(evilBaos)) {
            ZipEntry evilEntry = new ZipEntry("../../Windows/System32/evil.bat");
            evilZos.putNextEntry(evilEntry);
            evilZos.write("malicious payload".getBytes());
            evilZos.closeEntry();
        }
        byte[] evilZip = evilBaos.toByteArray();
        boolean safeCheck = Sha256Util.isSafeZip(new ByteArrayInputStream(evilZip));
        assertTrue("Sha256Util detects Zip Slip path traversal", !safeCheck, "Must detect ../ path traversal");

        Sha256IntegrityFilter.FilterResult resEvil = Sha256IntegrityFilter.inspectArtifact(evilZip, null);
        assertTrue("Filter blocks Zip Slip upload", !resEvil.isAccepted && resEvil.errorMessage.contains("Zip Slip"), "Must reject Zip Slip archive");
    }

    private static void testJwtAuthenticationAndClaims() {
        // 1. Generate token
        String token = JwtUtil.generateToken(5, "huy.nguyen@school.edu", "student", 5);
        assertTrue("JWT generated with 3 segments", token != null && token.split("\\.").length == 3, "JWT must contain header.payload.signature");

        // 2. Parse and verify
        JwtUtil.JwtClaims claims = JwtUtil.parseAndVerify(token);
        assertTrue("JWT verified successfully", claims != null, "Valid JWT must parse cleanly");
        assertTrue("Claims contain correct userId", claims.userId == 5, "userId should be 5");
        assertTrue("Claims contain correct role", "student".equals(claims.role), "Role should be student");
        assertTrue("Claims contain correct teamId", claims.teamId != null && claims.teamId == 5, "TeamId should be 5");

        // 3. Tampered token rejected
        String tampered = token.substring(0, token.lastIndexOf('.')) + ".InvalidSignature12345";
        JwtUtil.JwtClaims tamperedClaims = JwtUtil.parseAndVerify(tampered);
        assertTrue("Tampered JWT rejected", tamperedClaims == null, "Forged signature must fail verification");

        // 4. Role Authorization check
        JwtAuthFilter.AuthContext authCtx = JwtAuthFilter.authenticate("Bearer " + token);
        assertTrue("AuthContext authenticated", authCtx.isAuthenticated, "Bearer token should authenticate");
        assertTrue("Role check student allowed", JwtAuthFilter.checkRole(authCtx, "student", "teacher"), "Student role should match");
        assertTrue("Role check admin denied", !JwtAuthFilter.checkRole(authCtx, "admin"), "Student should not have admin role");
    }

    private static void testGitAnalyticsAndFreeRiderDetection() throws Exception {
        String testGitLog = 
            "---COMMIT---\n" +
            "commit: hash1\nauthor: Alice Wonder\nemail: alice@school.edu\ndate: " + (System.currentTimeMillis() - 10000000L) + "\n" +
            "message: feat: implement MVC2 controllers and services\nadded: 800\ndeleted: 50\nfile: src/com/controller/MainServlet.java\n" +
            "---COMMIT---\n" +
            "commit: hash2\nauthor: Alice Wonder\nemail: alice@school.edu\ndate: " + (System.currentTimeMillis() - 5000000L) + "\n" +
            "message: feat: add DAO and database connection pooling\nadded: 450\ndeleted: 20\nfile: src/com/dao/UserDAO.java\n" +
            "---COMMIT---\n" +
            "commit: hash3\nauthor: Bob Lazy\nemail: bob@school.edu\ndate: " + (System.currentTimeMillis() - 1800000L) + "\n" +
            "message: fix typo in readme\nadded: 10\ndeleted: 2\nfile: README.md\n";

        List<GitAnalyticsService.RawCommit> commits = GitAnalyticsService.parseGitLog(testGitLog);
        assertTrue("Parsed 3 git commits", commits.size() == 3, "Should parse all 3 commits");

        // Analyze team contribution
        List<GitMetric> metrics = GitAnalyticsService.analyzeTeamCommits(2, commits, System.currentTimeMillis() + 3600000L);
        assertTrue("Metrics computed for 2 authors", metrics.size() == 2, "Should group by 2 authors");

        GitMetric alice = metrics.get(0).getAuthorName().equals("Alice Wonder") ? metrics.get(0) : metrics.get(1);
        GitMetric bob = metrics.get(0).getAuthorName().equals("Bob Lazy") ? metrics.get(0) : metrics.get(1);

        assertTrue("Alice has major contribution", alice.getContributionScore() > bob.getContributionScore(), "Alice should have higher ICS");
        assertTrue("Alice contribution > 70%", alice.getContributionScore() >= 70.0, "Alice did overwhelming majority of work");

        // Free-rider anomaly flag check
        assertTrue("Bob flagged as GHOST_COMMITTER", bob.getAnomalyFlags().contains("GHOST_COMMITTER"), "Bob must be flagged as free-rider");

        // AI Critique generated
        String critique = GitAnalyticsService.generateAiCritique(metrics, null);
        assertTrue("Critique mentions free-rider warning", critique != null && (critique.contains("FREE-RIDER") || critique.contains("Bob")), "Pedagogical feedback must address contribution gap");
    }

    private static void testEcole42PeerReviewAndCorrectionPoints() throws Exception {
        // 1. Check Initial 5 Correction Points
        int balanceG5 = PeerReviewService.getCorrectionPointBalance(5);
        assertTrue("Initial Correction Points >= 5", balanceG5 >= 5, "Teams should start with at least 5 points");

        // 2. Check Rotational Circle
        Map<String, Object> target = PeerReviewService.getAssignedTargetTeam(5);
        assertTrue("G5 assigned target group in circle", target != null && target.containsKey("target_code"), "G5 should have assigned group");
        assertTrue("G5 reviews G6 in rotational order", "G6".equals(target.get("target_code")), "G5 must be assigned G6 in closed circle");

        // 3. Rubric Score calculation
        double finalScore = PeerReview.calculateFinalScore(10.0, 8.0, 9.0);
        // (10*0.3) + (8*0.4) + (9*0.3) = 3.0 + 3.2 + 2.7 = 8.9
        assertTrue("Rubric weights (30/40/30) calculate correctly", Math.abs(finalScore - 8.9) < 0.01, "Final score should be 8.9");

        // 4. Submit Peer Review and earn +1 Correction Point
        int beforeBalance = PeerReviewService.getCorrectionPointBalance(5);
        Map<String, Object> eval = PeerReviewService.submitPeerEvaluation(5, 5, 9.0, 9.0, 9.0, "Excellent sandbox compliance and clean code");
        assertTrue("Peer review submission completed", "completed".equals(eval.get("status")), "Review should be completed");

        int afterBalance = PeerReviewService.getCorrectionPointBalance(5);
        assertTrue("Awarded +1 Correction Point for review", afterBalance == beforeBalance + 1, "Reviewer must receive +1 point");
    }

    private static void testMoulinetteAutomatedTestSuite() throws Exception {
        // Run Moulinette on G5 submission (submission_id = 2)
        List<MoulinetteGradingService.TestExecutionOutput> results = MoulinetteGradingService.runAllTestsForSubmission(2);
        assertTrue("Moulinette executed all 6 test cases", results.size() == 6, "Must run all 6 public/hidden tests");

        int passedCount = 0;
        for (MoulinetteGradingService.TestExecutionOutput out : results) {
            if (out.passed) passedCount++;
        }
        assertTrue("All Moulinette test cases passed", passedCount == 6, "All tests should pass on G5 submission");

        // Results saved in database
        List<Map<String, Object>> dbResults = MoulinetteGradingService.getResultsForSubmission(2);
        assertTrue("Moulinette results persisted in database", dbResults.size() == 6, "Should fetch 6 result records");
    }

    private static void testDockerSandboxRunner() {
        // Run safe sandbox command (echo)
        DockerSandboxService.SandboxExecutionResult res = DockerSandboxService.runInSandbox(new File("."), "echo Sandbox execution verified", 5);
        assertTrue("Sandbox execution completed successfully", res.isSuccess, "Command should exit with 0");
        assertTrue("Sandbox stdout captured", res.stdout.contains("Sandbox execution verified"), "Stdout must be captured");
        assertTrue("Sandbox is network isolated", res.isNetworkIsolated, "Network isolation flag must be true");
    }
}
