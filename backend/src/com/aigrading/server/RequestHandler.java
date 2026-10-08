package com.aigrading.server;

import com.aigrading.auth.AuthService;
import com.aigrading.db.DatabaseManager;
import com.aigrading.filter.Sha256IntegrityFilter;
import com.aigrading.filter.JwtAuthFilter;
import com.aigrading.model.GitMetric;
import com.aigrading.services.*;
import com.aigrading.util.JsonHelper;
import com.aigrading.util.Sha256Util;
import com.aigrading.util.JwtUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.*;

public class RequestHandler implements HttpHandler {
    private final String frontendDir;
    private final String uploadsDir;

    public RequestHandler(String frontendDir, String uploadsDir) {
        this.frontendDir = frontendDir;
        this.uploadsDir = uploadsDir;
        new File(uploadsDir).mkdirs();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Set CORS headers for all requests
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");

        String method = exchange.getRequestMethod().toUpperCase();
        if ("OPTIONS".equals(method)) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        String path = exchange.getRequestURI().getPath();
        try {
            if (path.startsWith("/api/")) {
                handleApi(exchange, method, path);
            } else if (path.startsWith("/uploads/")) {
                handleStaticFile(exchange, uploadsDir, path.substring("/uploads/".length()));
            } else {
                // Serve frontend static assets
                String relPath = path.equals("/") ? "index.html" : path.substring(1);
                handleStaticFile(exchange, frontendDir, relPath);
            }
        } catch (Exception e) {
            sendError(exchange, 500, "Internal Server Error: " + sanitize(e.getMessage()));
        }
    }

    private void handleApi(HttpExchange exchange, String method, String path) throws Exception {
        AuthService.SessionUser authUser = authenticate(exchange);
        Map<String, String> queryParams = parseQueryParams(exchange.getRequestURI().getQuery());
        String body = readRequestBody(exchange);
        Map<String, Object> bodyMap = (body != null && !body.trim().isEmpty()) ? JsonHelper.parseObject(body) : Collections.emptyMap();

        // 1. Auth Endpoints
        if (path.equals("/api/auth/send-otp") && "POST".equals(method)) {
            String email = (String) bodyMap.get("email");
            String type = (String) bodyMap.getOrDefault("type", "register");
            if (email == null || email.trim().isEmpty()) {
                sendError(exchange, 400, "Email is required");
                return;
            }
            try {
                String code = AuthService.generateOtp(email, type);
                Map<String, Object> data = new LinkedHashMap<>();
                data.put("message", "OTP sent successfully to " + email);
                data.put("dev_code", code); // Included for local convenience
                sendJson(exchange, 200, data);
            } catch (IllegalArgumentException e) {
                sendError(exchange, 400, e.getMessage());
            }
            return;
        }

        if (path.equals("/api/auth/register") && "POST".equals(method)) {
            try {
                String fullName = (String) bodyMap.get("full_name");
                String email = (String) bodyMap.get("email");
                String password = (String) bodyMap.get("password");
                String role = (String) bodyMap.get("role");
                String codeNumber = (String) bodyMap.get("code_number");
                String extraField = (String) bodyMap.get("extra_field");
                String otpCode = (String) bodyMap.get("otp_code");

                Map<String, Object> result = AuthService.register(fullName, email, password, role, codeNumber, extraField, otpCode);
                sendJson(exchange, 201, result);
            } catch (IllegalArgumentException e) {
                sendError(exchange, 400, e.getMessage());
            }
            return;
        }

        if (path.equals("/api/auth/login") && "POST".equals(method)) {
            try {
                String email = (String) bodyMap.get("email");
                String password = (String) bodyMap.get("password");
                Map<String, Object> result = AuthService.login(email, password);
                sendJson(exchange, 200, result);
            } catch (IllegalArgumentException e) {
                sendError(exchange, 401, e.getMessage());
            }
            return;
        }

        if (path.equals("/api/auth/forgot-password") && "POST".equals(method)) {
            try {
                String email = (String) bodyMap.get("email");
                String otpCode = (String) bodyMap.get("otp_code");
                String newPassword = (String) bodyMap.get("new_password");
                AuthService.resetPassword(email, otpCode, newPassword);
                Map<String, Object> resp = new LinkedHashMap<>();
                resp.put("message", "Password reset successfully. You can now login.");
                sendJson(exchange, 200, resp);
            } catch (IllegalArgumentException e) {
                sendError(exchange, 400, e.getMessage());
            }
            return;
        }

        if (path.equals("/api/auth/me") && "GET".equals(method)) {
            if (authUser == null) {
                sendError(exchange, 401, "Unauthorized: Please log in");
                return;
            }
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("user_id", authUser.userId);
            data.put("email", authUser.email);
            data.put("full_name", authUser.fullName);
            data.put("role", authUser.role);
            data.put("specific_id", authUser.specificId);
            sendJson(exchange, 200, data);
            return;
        }

        if (path.equals("/api/auth/logout") && "POST".equals(method)) {
            String token = getAuthToken(exchange);
            AuthService.logout(token);
            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("message", "Logged out successfully");
            sendJson(exchange, 200, resp);
            return;
        }

        // 2. Classes & Subjects
        if (path.equals("/api/classes") && "POST".equals(method)) {
            if (authUser == null || (!"teacher".equalsIgnoreCase(authUser.role) && !"admin".equalsIgnoreCase(authUser.role))) {
                sendError(exchange, 403, "Only lecturers can create classes");
                return;
            }
            String className = (String) bodyMap.get("class_name");
            String department = (String) bodyMap.get("department");
            if (className == null || className.trim().isEmpty()) {
                sendError(exchange, 400, "Class name is required");
                return;
            }
            long teacherId = authUser.specificId != null ? authUser.specificId : 1L;
            long newClassId = DatabaseManager.insert(
                "INSERT INTO Classes (class_name, department, teacher_id) VALUES (?, ?, ?)",
                className.trim(), department != null ? department.trim() : "Software Engineering", teacherId
            );
            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("class_id", newClassId);
            resp.put("message", "Class created successfully");
            sendJson(exchange, 201, resp);
            return;
        }

        if (path.equals("/api/classes") && "GET".equals(method)) {
            List<Map<String, Object>> classes = DatabaseManager.query("SELECT * FROM Classes ORDER BY class_name ASC");
            sendJson(exchange, 200, classes);
            return;
        }

        if (path.equals("/api/subjects") && "GET".equals(method)) {
            List<Map<String, Object>> subjects = DatabaseManager.query("SELECT * FROM Subjects ORDER BY subject_id ASC");
            sendJson(exchange, 200, subjects);
            return;
        }

        if (path.equals("/api/students/me/subjects") && "GET".equals(method)) {
            if (authUser == null || !"student".equalsIgnoreCase(authUser.role)) {
                sendError(exchange, 403, "Access restricted to students");
                return;
            }
            String sql = "SELECT s.subject_id, s.subject_code, s.subject_name, s.semester " +
                         "FROM Subjects s " +
                         "JOIN StudentSubjects ss ON s.subject_id = ss.subject_id " +
                         "WHERE ss.student_id = ? " +
                         "ORDER BY s.semester, s.subject_code";
            List<Map<String, Object>> subjects = DatabaseManager.query(sql, authUser.specificId);
            sendJson(exchange, 200, subjects);
            return;
        }

        // 3. Assignments
        if (path.equals("/api/assignments") && "GET".equals(method)) {
            handleGetAssignments(exchange, queryParams);
            return;
        }

        if (path.equals("/api/assignments/imminent") && "GET".equals(method)) {
            if (authUser == null || !"student".equalsIgnoreCase(authUser.role)) {
                sendError(exchange, 403, "Access restricted to students");
                return;
            }
            List<Map<String, Object>> list = DeadlineNotificationService.getPendingNotificationsForStudent(authUser.specificId);
            sendJson(exchange, 200, list);
            return;
        }

        if (path.matches("^/api/assignments/\\d+$") && "GET".equals(method)) {
            long id = Long.parseLong(path.substring("/api/assignments/".length()));
            String sql = "SELECT a.*, s.subject_code, s.subject_name, u.full_name as teacher_name " +
                         "FROM Assignments a " +
                         "LEFT JOIN Subjects s ON a.subject_id = s.subject_id " +
                         "JOIN Teachers t ON a.teacher_id = t.teacher_id " +
                         "JOIN Users u ON t.user_id = u.user_id " +
                         "WHERE a.assignment_id = ?";
            Map<String, Object> item = DatabaseManager.queryOne(sql, id);
            if (item == null) {
                sendError(exchange, 404, "Assignment not found");
            } else {
                sendJson(exchange, 200, item);
            }
            return;
        }

        if (path.equals("/api/assignments") && "POST".equals(method)) {
            if (authUser == null || (!"teacher".equalsIgnoreCase(authUser.role) && !"admin".equalsIgnoreCase(authUser.role))) {
                sendError(exchange, 403, "Only lecturers can create assignments");
                return;
            }
            String title = (String) bodyMap.get("title");
            if (title == null || title.trim().isEmpty()) {
                sendError(exchange, 400, "Assignment title is required");
                return;
            }
            Object subIdObj = bodyMap.get("subject_id");
            Long subjectId = subIdObj != null ? ((Number) subIdObj).longValue() : 1L;
            String prototypeUrl = (String) bodyMap.get("prototype_url");
            String promptFileUrl = (String) bodyMap.get("prompt_file_url");
            String promptFileData = (String) bodyMap.get("prompt_file_data");
            String promptFileName = (String) bodyMap.get("prompt_file_name");

            if (promptFileData != null && promptFileData.contains(",")) {
                String b64 = promptFileData.substring(promptFileData.indexOf(",") + 1);
                byte[] b = Base64.getDecoder().decode(b64);
                String safeName = (promptFileName != null && !promptFileName.isEmpty()) ? promptFileName.replaceAll("[^a-zA-Z0-9._-]", "_") : "prompt_material.pdf";
                String filename = "mat_" + System.currentTimeMillis() + "_" + safeName;
                File dest = new File(uploadsDir, filename);
                Files.write(dest.toPath(), b);
                promptFileUrl = "/uploads/" + filename;
            }

            String deadline = (String) bodyMap.get("deadline");
            if (deadline == null || deadline.trim().isEmpty()) {
                sendError(exchange, 400, "Assignment deadline is required");
                return;
            }
            Object rubric = bodyMap.get("rubric_json");
            String rubricJson = (rubric instanceof String) ? (String) rubric : JsonHelper.toJson(rubric);

            long teacherId = authUser.specificId != null ? authUser.specificId : 1L;
            long newId = DatabaseManager.insert(
                "INSERT INTO Assignments (teacher_id, subject_id, title, prototype_url, prompt_file_url, deadline, rubric_json) VALUES (?, ?, ?, ?, ?, ?, ?)",
                teacherId, subjectId, title.trim(), prototypeUrl, promptFileUrl, deadline.trim(), rubricJson
            );

            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("assignment_id", newId);
            resp.put("prompt_file_url", promptFileUrl);
            resp.put("message", "Assignment created successfully");
            sendJson(exchange, 201, resp);
            return;
        }

        if (path.matches("^/api/assignments/\\d+$") && "PUT".equals(method)) {
            if (authUser == null || (!"teacher".equalsIgnoreCase(authUser.role) && !"admin".equalsIgnoreCase(authUser.role))) {
                sendError(exchange, 403, "Only lecturers can update assignments");
                return;
            }
            long id = Long.parseLong(path.substring("/api/assignments/".length()));
            String title = (String) bodyMap.get("title");
            String prototypeUrl = (String) bodyMap.get("prototype_url");
            String deadline = (String) bodyMap.get("deadline");
            Object rubric = bodyMap.get("rubric_json");
            String rubricJson = rubric != null ? ((rubric instanceof String) ? (String) rubric : JsonHelper.toJson(rubric)) : null;

            int affected = DatabaseManager.update(
                "UPDATE Assignments SET title = COALESCE(?, title), prototype_url = COALESCE(?, prototype_url), " +
                "deadline = COALESCE(?, deadline), rubric_json = COALESCE(?, rubric_json) WHERE assignment_id = ?",
                title, prototypeUrl, deadline, rubricJson, id
            );
            if (affected == 0) {
                sendError(exchange, 404, "Assignment not found");
            } else {
                Map<String, Object> resp = new LinkedHashMap<>();
                resp.put("message", "Assignment updated successfully");
                sendJson(exchange, 200, resp);
            }
            return;
        }

        if (path.matches("^/api/assignments/\\d+$") && "DELETE".equals(method)) {
            if (authUser == null || (!"teacher".equalsIgnoreCase(authUser.role) && !"admin".equalsIgnoreCase(authUser.role))) {
                sendError(exchange, 403, "Only lecturers can delete assignments");
                return;
            }
            long id = Long.parseLong(path.substring("/api/assignments/".length()));
            boolean deleted = DatabaseManager.deleteAssignmentCascade(id);
            if (!deleted) {
                sendError(exchange, 404, "Assignment not found");
            } else {
                Map<String, Object> resp = new LinkedHashMap<>();
                resp.put("message", "Assignment and all related student submissions deleted successfully");
                sendJson(exchange, 200, resp);
            }
            return;
        }

        // 4. Submissions
        if (path.equals("/api/submissions") && "POST".equals(method)) {
            if (authUser == null || !"student".equalsIgnoreCase(authUser.role)) {
                sendError(exchange, 403, "Only students can submit assignments");
                return;
            }
            Object assignIdObj = bodyMap.get("assignment_id");
            if (assignIdObj == null) {
                sendError(exchange, 400, "assignment_id is required");
                return;
            }
            long assignmentId = ((Number) assignIdObj).longValue();
            String screenshotData = (String) bodyMap.get("screenshot_data");
            String screenshotPath = (String) bodyMap.get("screenshot_path");

            // Handle base64 image upload if provided
            if (screenshotData != null && screenshotData.contains(",")) {
                String base64 = screenshotData.substring(screenshotData.indexOf(",") + 1);
                byte[] imgBytes = Base64.getDecoder().decode(base64);
                String filename = "sub_" + authUser.specificId + "_" + assignmentId + "_" + System.currentTimeMillis() + ".png";
                File dest = new File(uploadsDir, filename);
                Files.write(dest.toPath(), imgBytes);
                screenshotPath = "/uploads/" + filename;
            } else if (screenshotPath == null || screenshotPath.trim().isEmpty()) {
                screenshotPath = "/uploads/mock_submission.png";
            }

            long submissionId = DatabaseManager.insert(
                "INSERT INTO Submissions (student_id, assignment_id, screenshot_path, status) VALUES (?, ?, ?, 'submitted')",
                authUser.specificId, assignmentId, screenshotPath
            );

            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("submission_id", submissionId);
            resp.put("status", "submitted");
            resp.put("message", "Assignment submitted successfully");
            sendJson(exchange, 201, resp);
            return;
        }

        if (path.equals("/api/submissions") && "GET".equals(method)) {
            handleGetSubmissions(exchange, queryParams, authUser);
            return;
        }

        if (path.matches("^/api/submissions/\\d+$") && "GET".equals(method)) {
            long id = Long.parseLong(path.substring("/api/submissions/".length()));
            String sql = "SELECT s.*, a.title as assignment_title, a.prototype_url, a.rubric_json, " +
                         "u.full_name as student_name, st.student_code, st.class_name, " +
                         "ai.similarity_score, ai.detected_elements, ai.layout_report, ai.ai_feedback, " +
                         "g.score as grade_score, g.comment as grade_comment, g.graded_at, " +
                         "ap.appeal_id, ap.reason as appeal_reason, ap.status as appeal_status " +
                         "FROM Submissions s " +
                         "JOIN Assignments a ON s.assignment_id = a.assignment_id " +
                         "JOIN Students st ON s.student_id = st.student_id " +
                         "JOIN Users u ON st.user_id = u.user_id " +
                         "LEFT JOIN AIAnalysisResults ai ON s.submission_id = ai.submission_id " +
                         "LEFT JOIN Grades g ON s.submission_id = g.submission_id " +
                         "LEFT JOIN Appeals ap ON g.grade_id = ap.grade_id " +
                         "WHERE s.submission_id = ?";
            Map<String, Object> sub = DatabaseManager.queryOne(sql, id);
            if (sub == null) {
                sendError(exchange, 404, "Submission not found");
            } else {
                sendJson(exchange, 200, sub);
            }
            return;
        }

        if (path.matches("^/api/submissions/\\d+$") && "DELETE".equals(method)) {
            if (authUser == null) {
                sendError(exchange, 401, "Authentication required");
                return;
            }
            long id = Long.parseLong(path.substring("/api/submissions/".length()));
            if ("student".equalsIgnoreCase(authUser.role)) {
                Map<String, Object> sub = DatabaseManager.queryOne("SELECT student_id FROM Submissions WHERE submission_id = ?", id);
                if (sub == null) {
                    sendError(exchange, 404, "Submission not found");
                    return;
                }
                long subStudentId = ((Number) sub.get("student_id")).longValue();
                if (subStudentId != authUser.specificId) {
                    sendError(exchange, 403, "Cannot delete another student's submission");
                    return;
                }
            }
            boolean deleted = DatabaseManager.deleteSubmissionCascade(id);
            if (!deleted) {
                sendError(exchange, 404, "Submission not found");
            } else {
                Map<String, Object> resp = new LinkedHashMap<>();
                resp.put("message", "Submission deleted successfully");
                sendJson(exchange, 200, resp);
            }
            return;
        }

        // 5. AI Grading Trigger
        if (path.equals("/api/ai/grade") && "POST".equals(method)) {
            if (authUser == null || (!"teacher".equalsIgnoreCase(authUser.role) && !"admin".equalsIgnoreCase(authUser.role))) {
                sendError(exchange, 403, "Only lecturers can trigger AI grading");
                return;
            }
            Object subIdObj = bodyMap.get("submission_id");
            if (subIdObj == null) {
                sendError(exchange, 400, "submission_id is required");
                return;
            }
            long submissionId = ((Number) subIdObj).longValue();
            String apiKey = (String) bodyMap.get("api_key");

            Map<String, Object> result = AIGradingService.gradeSubmission(submissionId, apiKey);
            sendJson(exchange, 200, result);
            return;
        }

        // 6. Grades & Appeals
        if (path.equals("/api/grades") && "POST".equals(method)) {
            if (authUser == null || (!"teacher".equalsIgnoreCase(authUser.role) && !"admin".equalsIgnoreCase(authUser.role))) {
                sendError(exchange, 403, "Only lecturers can grade assignments");
                return;
            }
            Object subIdObj = bodyMap.get("submission_id");
            Object scoreObj = bodyMap.get("score");
            String comment = (String) bodyMap.get("comment");
            if (subIdObj == null || scoreObj == null) {
                sendError(exchange, 400, "submission_id and score are required");
                return;
            }
            long submissionId = ((Number) subIdObj).longValue();
            double score = ((Number) scoreObj).doubleValue();
            if (score < 0 || score > 10) {
                sendError(exchange, 400, "Score must be between 0 and 10");
                return;
            }

            long teacherId = authUser.specificId != null ? authUser.specificId : 1L;
            int updatedGrade = DatabaseManager.update(
                "UPDATE Grades SET score = ?, comment = ?, graded_at = datetime('now') WHERE submission_id = ?",
                score, comment, submissionId
            );
            if (updatedGrade == 0) {
                DatabaseManager.insert(
                    "INSERT INTO Grades (submission_id, graded_by, score, comment, graded_at) VALUES (?, ?, ?, ?, datetime('now'))",
                    submissionId, teacherId, score, comment
                );
            }
            DatabaseManager.update("UPDATE Submissions SET status = 'graded' WHERE submission_id = ?", submissionId);

            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("message", "Grade saved successfully");
            resp.put("score", score);
            sendJson(exchange, 200, resp);
            return;
        }

        if (path.equals("/api/appeals") && "POST".equals(method)) {
            if (authUser == null || !"student".equalsIgnoreCase(authUser.role)) {
                sendError(exchange, 403, "Only students can submit appeals");
                return;
            }
            Object gradeIdObj = bodyMap.get("grade_id");
            String reason = (String) bodyMap.get("reason");
            if (gradeIdObj == null || reason == null || reason.trim().isEmpty()) {
                sendError(exchange, 400, "grade_id and reason are required");
                return;
            }
            long gradeId = ((Number) gradeIdObj).longValue();
            long appealId = DatabaseManager.insert(
                "INSERT INTO Appeals (grade_id, student_id, reason, status) VALUES (?, ?, ?, 'pending')",
                gradeId, authUser.specificId, reason.trim()
            );
            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("appeal_id", appealId);
            resp.put("status", "pending");
            resp.put("message", "Appeal submitted to lecturer");
            sendJson(exchange, 201, resp);
            return;
        }

        if (path.matches("^/api/appeals/\\d+$") && "PUT".equals(method)) {
            if (authUser == null || (!"teacher".equalsIgnoreCase(authUser.role) && !"admin".equalsIgnoreCase(authUser.role))) {
                sendError(exchange, 403, "Only lecturers can review appeals");
                return;
            }
            long id = Long.parseLong(path.substring("/api/appeals/".length()));
            String status = (String) bodyMap.get("status");
            if (status == null || (!status.equals("approved") && !status.equals("rejected"))) {
                sendError(exchange, 400, "Status must be 'approved' or 'rejected'");
                return;
            }

            DatabaseManager.update("UPDATE Appeals SET status = ? WHERE appeal_id = ?", status, id);

            if ("approved".equals(status) && bodyMap.containsKey("new_score")) {
                double newScore = ((Number) bodyMap.get("new_score")).doubleValue();
                String comment = (String) bodyMap.get("comment");
                Map<String, Object> appeal = DatabaseManager.queryOne("SELECT grade_id FROM Appeals WHERE appeal_id = ?", id);
                if (appeal != null) {
                    long gradeId = ((Number) appeal.get("grade_id")).longValue();
                    DatabaseManager.update("UPDATE Grades SET score = ?, comment = COALESCE(?, comment) WHERE grade_id = ?", newScore, comment, gradeId);
                }
            }

            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("message", "Appeal status updated to " + status);
            sendJson(exchange, 200, resp);
            return;
        }

        if (path.equals("/api/appeals") && "GET".equals(method)) {
            String sql;
            List<Map<String, Object>> appeals;
            if (authUser != null && "student".equalsIgnoreCase(authUser.role)) {
                sql = "SELECT ap.*, g.score, g.comment as teacher_comment, a.title as assignment_title, u.full_name as teacher_name " +
                      "FROM Appeals ap " +
                      "JOIN Grades g ON ap.grade_id = g.grade_id " +
                      "JOIN Submissions s ON g.submission_id = s.submission_id " +
                      "JOIN Assignments a ON s.assignment_id = a.assignment_id " +
                      "JOIN Teachers t ON a.teacher_id = t.teacher_id " +
                      "JOIN Users u ON t.user_id = u.user_id " +
                      "WHERE ap.student_id = ? " +
                      "ORDER BY ap.created_at DESC";
                appeals = DatabaseManager.query(sql, authUser.specificId);
            } else {
                sql = "SELECT ap.*, g.score, a.title as assignment_title, u.full_name as student_name, st.student_code " +
                      "FROM Appeals ap " +
                      "JOIN Grades g ON ap.grade_id = g.grade_id " +
                      "JOIN Submissions s ON g.submission_id = s.submission_id " +
                      "JOIN Assignments a ON s.assignment_id = a.assignment_id " +
                      "JOIN Students st ON ap.student_id = st.student_id " +
                      "JOIN Users u ON st.user_id = u.user_id " +
                      "ORDER BY ap.created_at DESC";
                appeals = DatabaseManager.query(sql);
            }
            sendJson(exchange, 200, appeals);
            return;
        }

        // 7. Notifications Log
        if (path.equals("/api/notifications") && "GET".equals(method)) {
            sendJson(exchange, 200, DeadlineNotificationService.getAllNotificationLogs());
            return;
        }

        // 8. RBL Tracks & Teams (G1 - G7)
        if (path.equals("/api/teams") && "GET".equals(method)) {
            List<Map<String, Object>> teams = DatabaseManager.query("SELECT * FROM Teams ORDER BY team_id ASC");
            for (Map<String, Object> t : teams) {
                int tId = ((Number) t.get("team_id")).intValue();
                List<Map<String, Object>> members = DatabaseManager.query(
                    "SELECT tm.role_in_team, u.user_id, u.full_name, u.email, st.student_code " +
                    "FROM TeamMembers tm " +
                    "JOIN Users u ON tm.user_id = u.user_id " +
                    "LEFT JOIN Students st ON st.user_id = u.user_id " +
                    "WHERE tm.team_id = ?", tId
                );
                t.put("members", members);
            }
            sendJson(exchange, 200, teams);
            return;
        }

        // 9. Cổng Nộp Bài (Submission Portal) with SHA-256 Filter & École 42 Point Deduct
        if (path.equals("/api/submissions/upload-zip") && "POST".equals(method)) {
            try {
                Object assignIdObj = bodyMap.get("assignment_id");
                Object teamIdObj = bodyMap.get("team_id");
                String zipBase64 = (String) bodyMap.get("zip_base64");
                String clientSha256 = (String) bodyMap.get("sha256_client");
                String filename = (String) bodyMap.getOrDefault("filename", "submission_" + System.currentTimeMillis() + ".zip");

                if (assignIdObj == null || zipBase64 == null || zipBase64.trim().isEmpty()) {
                    sendError(exchange, 400, "assignment_id and zip_base64 payload are required");
                    return;
                }

                long assignmentId = ((Number) assignIdObj).longValue();
                Integer teamId = teamIdObj != null ? ((Number) teamIdObj).intValue() : 5; // Default to Team G5

                // Strip data url prefix if present
                if (zipBase64.contains(",")) {
                    zipBase64 = zipBase64.substring(zipBase64.indexOf(",") + 1);
                }
                byte[] rawZipBytes = Base64.getDecoder().decode(zipBase64.trim());

                // Run Servlet Gateway Filter: SHA-256 Checksum & Zip Slip Verification
                Sha256IntegrityFilter.FilterResult filterRes = Sha256IntegrityFilter.inspectArtifact(rawZipBytes, clientSha256);
                if (!filterRes.isAccepted) {
                    sendError(exchange, 400, filterRes.errorMessage);
                    return;
                }

                // Check and debit 1 Correction Point (École 42 Rule)
                try {
                    PeerReviewService.debitCorrectionPointForSubmission(teamId, 0);
                } catch (IllegalStateException ise) {
                    sendError(exchange, 400, ise.getMessage());
                    return;
                }

                // Save ZIP to disk safely
                File destZip = new File(uploadsDir + "/submissions", filename);
                destZip.getParentFile().mkdirs();
                try (FileOutputStream fos = new FileOutputStream(destZip)) {
                    fos.write(filterRes.validatedBytes);
                }

                String savedPath = "/uploads/submissions/" + filename;
                long subId = DatabaseManager.insert(
                    "INSERT INTO Submissions (student_id, team_id, assignment_id, zip_file_path, sha256_hash, artifact_type, queue_priority, status) " +
                    "VALUES (?, ?, ?, ?, ?, 'zip', 2, 'queued')",
                    authUser != null ? authUser.specificId : 1, teamId, assignmentId, savedPath, filterRes.calculatedSha256
                );

                // Asynchronously dispatch into Redis Job Queue
                RedisJobQueueService.enqueueJob((int) subId, 2, "FULL_ASSESSMENT");

                Map<String, Object> resp = new LinkedHashMap<>();
                resp.put("submission_id", subId);
                resp.put("sha256_hash", filterRes.calculatedSha256);
                resp.put("zip_file_path", savedPath);
                resp.put("status", "queued");
                resp.put("queue_message", "Artifact passed SHA-256 Filter and is queued for Docker Sandbox & AI evaluation.");
                sendJson(exchange, 201, resp);
            } catch (Exception e) {
                sendError(exchange, 500, "Artifact submission failed: " + sanitize(e.getMessage()));
            }
            return;
        }

        // 10. Nhóm 5: Git Analytics Teamwork Assessor
        if (path.equals("/api/git/analyze") && "POST".equals(method)) {
            try {
                Object subIdObj = bodyMap.get("submission_id");
                if (subIdObj == null) {
                    sendError(exchange, 400, "submission_id is required");
                    return;
                }
                int subId = ((Number) subIdObj).intValue();
                String rawGitLog = (String) bodyMap.get("raw_git_log");
                String apiKey = (String) bodyMap.get("api_key");

                // If no raw git log provided, generate standard team commit pattern for G5
                if (rawGitLog == null || rawGitLog.trim().isEmpty()) {
                    rawGitLog = "---COMMIT---\n" +
                                "commit: c1a2b3\nauthor: Huy Nguyen\nemail: huy.nguyen@school.edu\n" +
                                "date: " + (System.currentTimeMillis() - 14 * 86400000L) + "\n" +
                                "message: feat: initialize MVC2 package architecture and DatabaseManager\nadded: 420\ndeleted: 20\nfile: src/com/aigrading/server/RequestHandler.java\n" +
                                "---COMMIT---\n" +
                                "commit: c2b3c4\nauthor: Huy Nguyen\nemail: huy.nguyen@school.edu\n" +
                                "date: " + (System.currentTimeMillis() - 7 * 86400000L) + "\n" +
                                "message: feat: implement Sha256IntegrityFilter for artifact upload\nadded: 380\ndeleted: 40\nfile: src/com/aigrading/filter/Sha256IntegrityFilter.java\n" +
                                "---COMMIT---\n" +
                                "commit: c3c4d5\nauthor: Linh Tran\nemail: linh.tran@school.edu\n" +
                                "date: " + (System.currentTimeMillis() - 5 * 86400000L) + "\n" +
                                "message: feat: integrate RedisJobQueueService with priority and retry\nadded: 510\ndeleted: 60\nfile: src/com/aigrading/services/RedisJobQueueService.java\n" +
                                "---COMMIT---\n" +
                                "commit: c4d5e6\nauthor: Linh Tran\nemail: linh.tran@school.edu\n" +
                                "date: " + (System.currentTimeMillis() - 2 * 86400000L) + "\n" +
                                "message: feat: create PeerReviewService and rotational scoring loop\nadded: 340\ndeleted: 30\nfile: src/com/aigrading/services/PeerReviewService.java\n" +
                                "---COMMIT---\n" +
                                "commit: c5e6f7\nauthor: Minh Le\nemail: minh.le@school.edu\n" +
                                "date: " + (System.currentTimeMillis() - 3600000L) + "\n" +
                                "message: update readme and fix typo\nadded: 12\ndeleted: 3\nfile: README.md\n";
                }

                List<GitAnalyticsService.RawCommit> parsedCommits = GitAnalyticsService.parseGitLog(rawGitLog);
                List<GitMetric> metrics = GitAnalyticsService.analyzeTeamCommits(subId, parsedCommits, System.currentTimeMillis() + 86400000L);
                String critique = GitAnalyticsService.generateAiCritique(metrics, apiKey);
                for (GitMetric m : metrics) {
                    m.setAiCritiqueSummary(critique);
                }
                GitAnalyticsService.saveMetrics(subId, metrics);

                Map<String, Object> resp = new LinkedHashMap<>();
                resp.put("submission_id", subId);
                resp.put("metrics", metrics);
                resp.put("ai_critique", critique);
                sendJson(exchange, 200, resp);
            } catch (Exception e) {
                sendError(exchange, 500, "Git analytics execution failed: " + sanitize(e.getMessage()));
            }
            return;
        }

        if (path.equals("/api/git/metrics") && "GET".equals(method)) {
            String subIdStr = queryParams.get("submission_id");
            if (subIdStr == null) {
                sendError(exchange, 400, "submission_id query param is required");
                return;
            }
            List<Map<String, Object>> metrics = DatabaseManager.query(
                "SELECT * FROM GitMetrics WHERE submission_id = ? ORDER BY contribution_score DESC",
                Integer.parseInt(subIdStr)
            );
            sendJson(exchange, 200, metrics);
            return;
        }

        // 11. École 42: Rotational Peer-Review & Correction Points Wallet
        if (path.equals("/api/peer-review/assignment") && "GET".equals(method)) {
            String reviewerTeamIdStr = queryParams.getOrDefault("reviewer_team_id", "5");
            int reviewerTeamId = Integer.parseInt(reviewerTeamIdStr);
            Map<String, Object> assignment = PeerReviewService.getAssignedTargetTeam(reviewerTeamId);
            sendJson(exchange, 200, assignment != null ? assignment : Collections.emptyMap());
            return;
        }

        if (path.equals("/api/peer-review/all") && "GET".equals(method)) {
            List<Map<String, Object>> reviews = DatabaseManager.query(
                "SELECT r.*, t1.team_code as reviewer_code, t1.team_name as reviewer_name, " +
                "t2.team_code as target_code, t2.team_name as target_name " +
                "FROM RotationalReviews r " +
                "JOIN Teams t1 ON r.reviewer_team_id = t1.team_id " +
                "JOIN Teams t2 ON r.target_team_id = t2.team_id " +
                "ORDER BY r.review_id ASC"
            );
            sendJson(exchange, 200, reviews);
            return;
        }

        if (path.equals("/api/peer-review/submit") && "POST".equals(method)) {
            try {
                int reviewId = ((Number) bodyMap.get("review_id")).intValue();
                int reviewerTeamId = ((Number) bodyMap.get("reviewer_team_id")).intValue();
                double sandboxScore = ((Number) bodyMap.get("sandbox_score")).doubleValue();
                double mvc2Score = ((Number) bodyMap.get("mvc2_score")).doubleValue();
                double defenseScore = ((Number) bodyMap.get("defense_score")).doubleValue();
                String feedback = (String) bodyMap.get("feedback");

                Map<String, Object> res = PeerReviewService.submitPeerEvaluation(
                    reviewId, reviewerTeamId, sandboxScore, mvc2Score, defenseScore, feedback
                );
                int newBalance = PeerReviewService.getCorrectionPointBalance(reviewerTeamId);
                res.put("updated_correction_points", newBalance);
                res.put("reward_message", "+1 Correction Point granted to your team for completing peer evaluation!");
                sendJson(exchange, 200, res);
            } catch (Exception e) {
                sendError(exchange, 400, "Peer review submission failed: " + sanitize(e.getMessage()));
            }
            return;
        }

        if (path.equals("/api/correction-points") && "GET".equals(method)) {
            String teamIdStr = queryParams.getOrDefault("team_id", "5");
            int teamId = Integer.parseInt(teamIdStr);
            int balance = PeerReviewService.getCorrectionPointBalance(teamId);
            Map<String, Object> wallet = DatabaseManager.queryOne("SELECT * FROM CorrectionPointsWallet WHERE team_id = ? LIMIT 1", teamId);
            List<Map<String, Object>> transactions = Collections.emptyList();
            if (wallet != null) {
                int walletId = ((Number) wallet.get("wallet_id")).intValue();
                transactions = DatabaseManager.query("SELECT * FROM CorrectionPointTransactions WHERE wallet_id = ? ORDER BY created_at DESC", walletId);
            }

            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("team_id", teamId);
            resp.put("balance", balance);
            resp.put("transactions", transactions);
            sendJson(exchange, 200, resp);
            return;
        }

        // 12. Moulinette Automated Testing Engine (Public & Hidden Tests)
        if (path.equals("/api/moulinette/run") && "POST".equals(method)) {
            try {
                Object subIdObj = bodyMap.get("submission_id");
                int subId = subIdObj != null ? ((Number) subIdObj).intValue() : 2;
                List<MoulinetteGradingService.TestExecutionOutput> results = MoulinetteGradingService.runAllTestsForSubmission(subId);
                sendJson(exchange, 200, results);
            } catch (Exception e) {
                sendError(exchange, 500, "Moulinette test execution failed: " + sanitize(e.getMessage()));
            }
            return;
        }

        if (path.equals("/api/moulinette/results") && "GET".equals(method)) {
            String subIdStr = queryParams.getOrDefault("submission_id", "2");
            int subId = Integer.parseInt(subIdStr);
            List<Map<String, Object>> results = MoulinetteGradingService.getResultsForSubmission(subId);
            sendJson(exchange, 200, results);
            return;
        }

        // 13. Docker Sandbox Execution Runner
        if (path.equals("/api/sandbox/execute") && "POST".equals(method)) {
            String command = (String) bodyMap.getOrDefault("command", "echo Sandbox container running in isolation && javac -version");
            int timeout = bodyMap.containsKey("timeout_seconds") ? ((Number) bodyMap.get("timeout_seconds")).intValue() : 15;
            DockerSandboxService.SandboxExecutionResult res = DockerSandboxService.runInSandbox(new File("."), command, timeout);
            sendJson(exchange, 200, res);
            return;
        }

        // 14. Database Dynamic CRUD & Schema Sync Endpoints (Pure Java Management)
        if (path.equals("/api/db/sync") && "POST".equals(method)) {
            Map<String, Object> syncResult = DatabaseCrudService.synchronizeDatabase();
            sendJson(exchange, 200, syncResult);
            return;
        }

        if (path.equals("/api/db/tables") && "GET".equals(method)) {
            List<Map<String, Object>> tables = DatabaseManager.listAllTablesWithCounts();
            sendJson(exchange, 200, tables);
            return;
        }

        if (path.matches("^/api/db/tables/[a-zA-Z0-9_]+$") && "GET".equals(method)) {
            String tableName = path.substring("/api/db/tables/".length());
            List<Map<String, Object>> columns = DatabaseManager.getTableColumnsMetadata(tableName);
            List<Map<String, Object>> rows = DatabaseManager.queryAll(tableName, null);
            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("table_name", tableName);
            resp.put("columns", columns);
            resp.put("rows", rows);
            sendJson(exchange, 200, resp);
            return;
        }

        if (path.matches("^/api/db/tables/[a-zA-Z0-9_]+$") && "POST".equals(method)) {
            String tableName = path.substring("/api/db/tables/".length());
            long newId = DatabaseManager.insertRecord(tableName, bodyMap);
            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("message", "Record inserted successfully into " + tableName);
            resp.put("id", newId);
            sendJson(exchange, 201, resp);
            return;
        }

        if (path.matches("^/api/db/tables/[a-zA-Z0-9_]+/[^/]+$") && "PUT".equals(method)) {
            String sub = path.substring("/api/db/tables/".length());
            int slash = sub.indexOf('/');
            String tableName = sub.substring(0, slash);
            String idValStr = sub.substring(slash + 1);
            String idCol = (String) bodyMap.remove("_id_col");
            if (idCol == null) {
                if (tableName.equalsIgnoreCase("Users")) idCol = "user_id";
                else if (tableName.equalsIgnoreCase("Teams")) idCol = "team_id";
                else if (tableName.equalsIgnoreCase("Assignments")) idCol = "assignment_id";
                else if (tableName.equalsIgnoreCase("Submissions")) idCol = "submission_id";
                else if (tableName.equalsIgnoreCase("Grades")) idCol = "grade_id";
                else if (tableName.equalsIgnoreCase("MoulinetteTestCases")) idCol = "test_id";
                else idCol = tableName.toLowerCase() + "_id";
            }
            int updated = DatabaseManager.updateRecord(tableName, bodyMap, idCol, idValStr);
            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("message", "Record updated successfully in " + tableName);
            resp.put("affected", updated);
            sendJson(exchange, 200, resp);
            return;
        }

        if (path.matches("^/api/db/tables/[a-zA-Z0-9_]+/[^/]+$") && "DELETE".equals(method)) {
            String sub = path.substring("/api/db/tables/".length());
            int slash = sub.indexOf('/');
            String tableName = sub.substring(0, slash);
            String idValStr = sub.substring(slash + 1);
            String idCol = queryParams.getOrDefault("id_col", null);
            if (idCol == null) {
                if (tableName.equalsIgnoreCase("Users")) idCol = "user_id";
                else if (tableName.equalsIgnoreCase("Teams")) idCol = "team_id";
                else if (tableName.equalsIgnoreCase("Assignments")) idCol = "assignment_id";
                else if (tableName.equalsIgnoreCase("Submissions")) idCol = "submission_id";
                else if (tableName.equalsIgnoreCase("Grades")) idCol = "grade_id";
                else if (tableName.equalsIgnoreCase("MoulinetteTestCases")) idCol = "test_id";
                else idCol = tableName.toLowerCase() + "_id";
            }
            int deleted = DatabaseManager.deleteRecord(tableName, idCol, idValStr, true);
            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("message", "Record deleted successfully from " + tableName);
            resp.put("affected", deleted);
            sendJson(exchange, 200, resp);
            return;
        }

        if (path.equals("/api/db/columns") && "POST".equals(method)) {
            String tableName = (String) bodyMap.get("table_name");
            String colName = (String) bodyMap.get("column_name");
            String colType = (String) bodyMap.get("column_type");
            if (tableName == null || colName == null || colType == null) {
                sendError(exchange, 400, "table_name, column_name, column_type are required");
                return;
            }
            DatabaseManager.addColumnIfNotExists(tableName, colName, colType);
            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("message", "Column " + colName + " ensured on table " + tableName);
            sendJson(exchange, 200, resp);
            return;
        }

        sendError(exchange, 404, "Endpoint not found: " + method + " " + path);
    }

    private void handleGetAssignments(HttpExchange exchange, Map<String, String> queryParams) throws SQLException, IOException {
        String search = queryParams.get("search");
        String subjectId = queryParams.get("subject_id");
        String sortBy = queryParams.getOrDefault("sort_by", "created_at");
        String order = "asc".equalsIgnoreCase(queryParams.get("order")) ? "ASC" : "DESC";

        int page = 1;
        int limit = 10;
        try {
            if (queryParams.containsKey("page")) page = Math.max(1, Integer.parseInt(queryParams.get("page")));
            if (queryParams.containsKey("limit")) limit = Math.max(1, Math.min(100, Integer.parseInt(queryParams.get("limit"))));
        } catch (NumberFormatException ignored) {}

        List<String> whereClauses = new ArrayList<>();
        List<Object> params = new ArrayList<>();

        if (search != null && !search.trim().isEmpty()) {
            whereClauses.add("(a.title LIKE ? OR s.subject_name LIKE ?)");
            params.add("%" + search.trim() + "%");
            params.add("%" + search.trim() + "%");
        }
        if (subjectId != null && !subjectId.trim().isEmpty()) {
            whereClauses.add("a.subject_id = ?");
            params.add(Long.parseLong(subjectId));
        }

        String whereSql = whereClauses.isEmpty() ? "" : "WHERE " + String.join(" AND ", whereClauses);

        // Allowed sort columns
        String sortCol = "a.created_at";
        if ("deadline".equalsIgnoreCase(sortBy)) sortCol = "a.deadline";
        else if ("title".equalsIgnoreCase(sortBy)) sortCol = "a.title";

        String countSql = "SELECT COUNT(*) as total FROM Assignments a LEFT JOIN Subjects s ON a.subject_id = s.subject_id " + whereSql;
        Map<String, Object> countRow = DatabaseManager.queryOne(countSql, params.toArray());
        long total = countRow != null ? ((Number) countRow.get("total")).longValue() : 0;

        int offset = (page - 1) * limit;
        List<Object> queryParamList = new ArrayList<>(params);
        String querySql;
        if (DatabaseManager.isSqlServer()) {
            querySql = "SELECT a.*, s.subject_code, s.subject_name, u.full_name as teacher_name, " +
                       "(SELECT COUNT(*) FROM Submissions sub WHERE sub.assignment_id = a.assignment_id) as submission_count " +
                       "FROM Assignments a " +
                       "LEFT JOIN Subjects s ON a.subject_id = s.subject_id " +
                       "JOIN Teachers t ON a.teacher_id = t.teacher_id " +
                       "JOIN Users u ON t.user_id = u.user_id " +
                       whereSql + " ORDER BY " + sortCol + " " + order + " OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
            queryParamList.add(offset);
            queryParamList.add(limit);
        } else {
            querySql = "SELECT a.*, s.subject_code, s.subject_name, u.full_name as teacher_name, " +
                       "(SELECT COUNT(*) FROM Submissions sub WHERE sub.assignment_id = a.assignment_id) as submission_count " +
                       "FROM Assignments a " +
                       "LEFT JOIN Subjects s ON a.subject_id = s.subject_id " +
                       "JOIN Teachers t ON a.teacher_id = t.teacher_id " +
                       "JOIN Users u ON t.user_id = u.user_id " +
                       whereSql + " ORDER BY " + sortCol + " " + order + " LIMIT ? OFFSET ?";
            queryParamList.add(limit);
            queryParamList.add(offset);
        }

        List<Map<String, Object>> items = DatabaseManager.query(querySql, queryParamList.toArray());

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("items", items);
        Map<String, Object> pagination = new LinkedHashMap<>();
        pagination.put("page", page);
        pagination.put("limit", limit);
        pagination.put("total", total);
        pagination.put("pages", (int) Math.ceil((double) total / limit));
        resp.put("pagination", pagination);

        sendJson(exchange, 200, resp);
    }

    private void handleGetSubmissions(HttpExchange exchange, Map<String, String> queryParams, AuthService.SessionUser authUser) throws SQLException, IOException {
        String assignmentId = queryParams.get("assignment_id");
        String studentId = queryParams.get("student_id");
        String status = queryParams.get("status");

        List<String> whereClauses = new ArrayList<>();
        List<Object> params = new ArrayList<>();

        if (assignmentId != null && !assignmentId.trim().isEmpty()) {
            whereClauses.add("s.assignment_id = ?");
            params.add(Long.parseLong(assignmentId));
        }

        // If authenticated user is student, restrict to their own submissions
        if (authUser != null && "student".equalsIgnoreCase(authUser.role)) {
            whereClauses.add("s.student_id = ?");
            params.add(authUser.specificId);
        } else if (studentId != null && !studentId.trim().isEmpty()) {
            whereClauses.add("s.student_id = ?");
            params.add(Long.parseLong(studentId));
        }

        if (status != null && !status.trim().isEmpty()) {
            whereClauses.add("s.status = ?");
            params.add(status.trim());
        }

        String whereSql = whereClauses.isEmpty() ? "" : "WHERE " + String.join(" AND ", whereClauses);

        String sql = "SELECT s.*, a.title as assignment_title, a.prototype_url, a.deadline, " +
                     "u.full_name as student_name, st.student_code, st.class_name, " +
                     "ai.similarity_score, g.score as grade_score, g.comment as grade_comment " +
                     "FROM Submissions s " +
                     "JOIN Assignments a ON s.assignment_id = a.assignment_id " +
                     "JOIN Students st ON s.student_id = st.student_id " +
                     "JOIN Users u ON st.user_id = u.user_id " +
                     "LEFT JOIN AIAnalysisResults ai ON s.submission_id = ai.submission_id " +
                     "LEFT JOIN Grades g ON s.submission_id = g.submission_id " +
                     whereSql + " ORDER BY s.submitted_at DESC";

        List<Map<String, Object>> items = DatabaseManager.query(sql, params.toArray());
        sendJson(exchange, 200, items);
    }

    private void handleStaticFile(HttpExchange exchange, String baseDir, String relPath) throws IOException {
        // Prevent path traversal
        File file = new File(baseDir, relPath).getCanonicalFile();
        File base = new File(baseDir).getCanonicalFile();
        if (!file.getPath().startsWith(base.getPath()) || !file.exists() || file.isDirectory()) {
            // If requesting a frontend route, fallback to index.html for SPA
            File indexFile = new File(frontendDir, "index.html");
            if (indexFile.exists() && baseDir.equals(frontendDir)) {
                serveFile(exchange, indexFile, "text/html; charset=UTF-8");
                return;
            }
            sendError(exchange, 404, "File not found");
            return;
        }

        String contentType = getMimeType(file.getName());
        serveFile(exchange, file, contentType);
    }

    private void serveFile(HttpExchange exchange, File file, String contentType) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(200, file.length());
        try (OutputStream os = exchange.getResponseBody(); FileInputStream fis = new FileInputStream(file)) {
            byte[] buf = new byte[8192];
            int n;
            while ((n = fis.read(buf)) > 0) {
                os.write(buf, 0, n);
            }
        }
    }

    private String getMimeType(String filename) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".html")) return "text/html; charset=UTF-8";
        if (lower.endsWith(".css")) return "text/css; charset=UTF-8";
        if (lower.endsWith(".js")) return "application/javascript; charset=UTF-8";
        if (lower.endsWith(".json")) return "application/json; charset=UTF-8";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".svg")) return "image/svg+xml";
        if (lower.endsWith(".ico")) return "image/x-icon";
        return "application/octet-stream";
    }

    private AuthService.SessionUser authenticate(HttpExchange exchange) {
        String token = getAuthToken(exchange);
        return AuthService.validateToken(token);
    }

    private String getAuthToken(HttpExchange exchange) {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7).trim();
        }
        return null;
    }

    private String readRequestBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buf = new byte[4096];
            int n;
            while ((n = is.read(buf)) > 0) {
                baos.write(buf, 0, n);
            }
            return new String(baos.toByteArray(), StandardCharsets.UTF_8);
        }
    }

    private Map<String, String> parseQueryParams(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null || query.isEmpty()) return map;
        for (String pair : query.split("&")) {
            int idx = pair.indexOf("=");
            try {
                if (idx > 0) {
                    map.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
                } else if (!pair.isEmpty()) {
                    map.put(URLDecoder.decode(pair, "UTF-8"), "");
                }
            } catch (UnsupportedEncodingException ignored) {}
        }
        return map;
    }

    private void sendJson(HttpExchange exchange, int statusCode, Object data) throws IOException {
        Map<String, Object> envelope = new LinkedHashMap<>();
        envelope.put("success", statusCode >= 200 && statusCode < 300);
        envelope.put("data", data);
        byte[] bytes = JsonHelper.toJson(envelope).getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private void sendError(HttpExchange exchange, int statusCode, String message) throws IOException {
        Map<String, Object> envelope = new LinkedHashMap<>();
        envelope.put("success", false);
        envelope.put("error", sanitize(message));
        byte[] bytes = JsonHelper.toJson(envelope).getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private String sanitize(String s) {
        if (s == null) return "Unknown error";
        return s.replace("<", "&lt;").replace(">", "&gt;");
    }
}
