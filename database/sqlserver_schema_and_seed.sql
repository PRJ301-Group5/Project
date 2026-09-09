-- ==========================================================
-- SQL SERVER DATABASE CREATION & SEED SCRIPT (PRJ301 & RBL TRACK 5)
-- Project: AI Grading Tool for Lecturers & Students
-- Track 5: Git Analytics Teamwork Assessor & École 42 System
-- ==========================================================

USE master;
GO

IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = 'AIGradingDB')
BEGIN
    CREATE DATABASE AIGradingDB;
    PRINT 'Database AIGradingDB created successfully!';
END
GO

USE AIGradingDB;
GO

-- Drop tables in reverse dependency order for clean schema initialization
IF OBJECT_ID('MoulinetteResults', 'U') IS NOT NULL DROP TABLE MoulinetteResults;
IF OBJECT_ID('MoulinetteTestCases', 'U') IS NOT NULL DROP TABLE MoulinetteTestCases;
IF OBJECT_ID('CorrectionPointTransactions', 'U') IS NOT NULL DROP TABLE CorrectionPointTransactions;
IF OBJECT_ID('CorrectionPointsWallet', 'U') IS NOT NULL DROP TABLE CorrectionPointsWallet;
IF OBJECT_ID('RotationalReviews', 'U') IS NOT NULL DROP TABLE RotationalReviews;
IF OBJECT_ID('GitMetrics', 'U') IS NOT NULL DROP TABLE GitMetrics;
IF OBJECT_ID('Appeals', 'U') IS NOT NULL DROP TABLE Appeals;
IF OBJECT_ID('Grades', 'U') IS NOT NULL DROP TABLE Grades;
IF OBJECT_ID('AIAnalysisResults', 'U') IS NOT NULL DROP TABLE AIAnalysisResults;
IF OBJECT_ID('Submissions', 'U') IS NOT NULL DROP TABLE Submissions;
IF OBJECT_ID('Assignments', 'U') IS NOT NULL DROP TABLE Assignments;
IF OBJECT_ID('TeamMembers', 'U') IS NOT NULL DROP TABLE TeamMembers;
IF OBJECT_ID('Teams', 'U') IS NOT NULL DROP TABLE Teams;
IF OBJECT_ID('Classes', 'U') IS NOT NULL DROP TABLE Classes;
IF OBJECT_ID('StudentSubjects', 'U') IS NOT NULL DROP TABLE StudentSubjects;
IF OBJECT_ID('Subjects', 'U') IS NOT NULL DROP TABLE Subjects;
IF OBJECT_ID('Students', 'U') IS NOT NULL DROP TABLE Students;
IF OBJECT_ID('Teachers', 'U') IS NOT NULL DROP TABLE Teachers;
IF OBJECT_ID('Users', 'U') IS NOT NULL DROP TABLE Users;
IF OBJECT_ID('OtpCodes', 'U') IS NOT NULL DROP TABLE OtpCodes;
GO

-- 1. Table Users
CREATE TABLE Users (
    user_id INT IDENTITY(1,1) PRIMARY KEY,
    full_name NVARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(64) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('teacher', 'student', 'admin')),
    created_at DATETIME DEFAULT GETDATE()
);
GO

-- 2. Table Teachers
CREATE TABLE Teachers (
    teacher_id INT IDENTITY(1,1) PRIMARY KEY,
    user_id INT NOT NULL UNIQUE FOREIGN KEY REFERENCES Users(user_id) ON DELETE CASCADE,
    employee_code VARCHAR(20) NOT NULL UNIQUE,
    department NVARCHAR(100)
);
GO

-- 3. Table Students
CREATE TABLE Students (
    student_id INT IDENTITY(1,1) PRIMARY KEY,
    user_id INT NOT NULL UNIQUE FOREIGN KEY REFERENCES Users(user_id) ON DELETE CASCADE,
    student_code VARCHAR(20) NOT NULL UNIQUE,
    class_name VARCHAR(50) NOT NULL
);
GO

-- 4. Table Subjects
CREATE TABLE Subjects (
    subject_id INT IDENTITY(1,1) PRIMARY KEY,
    subject_code VARCHAR(20) NOT NULL UNIQUE,
    subject_name NVARCHAR(150) NOT NULL,
    semester VARCHAR(50) NOT NULL
);
GO

-- 5. Table StudentSubjects
CREATE TABLE StudentSubjects (
    student_id INT NOT NULL FOREIGN KEY REFERENCES Students(student_id) ON DELETE CASCADE,
    subject_id INT NOT NULL FOREIGN KEY REFERENCES Subjects(subject_id) ON DELETE CASCADE,
    PRIMARY KEY (student_id, subject_id)
);
GO

-- 6. Table Classes
CREATE TABLE Classes (
    class_id INT IDENTITY(1,1) PRIMARY KEY,
    class_name VARCHAR(50) NOT NULL UNIQUE,
    department NVARCHAR(100) NOT NULL,
    teacher_id INT FOREIGN KEY REFERENCES Teachers(teacher_id) ON DELETE SET NULL,
    created_at DATETIME DEFAULT GETDATE()
);
GO

-- 7. Table Teams (G1 - G7)
CREATE TABLE Teams (
    team_id INT IDENTITY(1,1) PRIMARY KEY,
    team_code VARCHAR(10) NOT NULL UNIQUE,
    team_name NVARCHAR(100) NOT NULL,
    track_name NVARCHAR(100) NOT NULL,
    project_title NVARCHAR(255) NOT NULL,
    repo_url VARCHAR(255),
    created_at DATETIME DEFAULT GETDATE()
);
GO

-- 8. Table TeamMembers
CREATE TABLE TeamMembers (
    id INT IDENTITY(1,1) PRIMARY KEY,
    team_id INT NOT NULL FOREIGN KEY REFERENCES Teams(team_id) ON DELETE CASCADE,
    user_id INT NOT NULL FOREIGN KEY REFERENCES Users(user_id) ON DELETE CASCADE,
    role_in_team VARCHAR(20) DEFAULT 'member' CHECK(role_in_team IN ('leader', 'member')),
    CONSTRAINT UQ_TeamMember UNIQUE(team_id, user_id)
);
GO

-- 9. Table Assignments
CREATE TABLE Assignments (
    assignment_id INT IDENTITY(1,1) PRIMARY KEY,
    teacher_id INT NOT NULL FOREIGN KEY REFERENCES Teachers(teacher_id) ON DELETE CASCADE,
    subject_id INT FOREIGN KEY REFERENCES Subjects(subject_id) ON DELETE SET NULL,
    title NVARCHAR(200) NOT NULL,
    prototype_url VARCHAR(500),
    prompt_file_url VARCHAR(500),
    deadline DATETIME NOT NULL,
    rubric_json NVARCHAR(MAX),
    created_at DATETIME DEFAULT GETDATE()
);
GO

-- 10. Table Submissions
CREATE TABLE Submissions (
    submission_id INT IDENTITY(1,1) PRIMARY KEY,
    student_id INT FOREIGN KEY REFERENCES Students(student_id) ON DELETE NO ACTION,
    team_id INT FOREIGN KEY REFERENCES Teams(team_id) ON DELETE NO ACTION,
    assignment_id INT NOT NULL FOREIGN KEY REFERENCES Assignments(assignment_id) ON DELETE NO ACTION,
    screenshot_path VARCHAR(500),
    zip_file_path VARCHAR(500),
    sha256_hash VARCHAR(64),
    artifact_type VARCHAR(20) DEFAULT 'screenshot' CHECK(artifact_type IN ('screenshot', 'zip', 'git_repo')),
    queue_priority INT DEFAULT 1,
    retry_count INT DEFAULT 0,
    submitted_at DATETIME DEFAULT GETDATE(),
    status VARCHAR(20) DEFAULT 'submitted' CHECK (status IN ('submitted', 'queued', 'running', 'grading', 'graded', 'appealed', 'resolved', 'failed'))
);
GO

-- 11. Table AIAnalysisResults
CREATE TABLE AIAnalysisResults (
    result_id INT IDENTITY(1,1) PRIMARY KEY,
    submission_id INT NOT NULL UNIQUE FOREIGN KEY REFERENCES Submissions(submission_id) ON DELETE CASCADE,
    similarity_score FLOAT,
    detected_elements NVARCHAR(MAX),
    layout_report NVARCHAR(MAX),
    ai_feedback NVARCHAR(MAX),
    analyzed_at DATETIME DEFAULT GETDATE()
);
GO

-- 12. Table GitMetrics (Nhóm 5)
CREATE TABLE GitMetrics (
    metric_id INT IDENTITY(1,1) PRIMARY KEY,
    submission_id INT NOT NULL FOREIGN KEY REFERENCES Submissions(submission_id) ON DELETE CASCADE,
    user_id INT FOREIGN KEY REFERENCES Users(user_id) ON DELETE NO ACTION,
    author_name NVARCHAR(100) NOT NULL,
    author_email VARCHAR(100),
    total_commits INT DEFAULT 0,
    lines_added INT DEFAULT 0,
    lines_deleted INT DEFAULT 0,
    net_churn INT DEFAULT 0,
    commit_entropy FLOAT DEFAULT 0.0,
    semantic_score FLOAT DEFAULT 0.0,
    module_ownership FLOAT DEFAULT 0.0,
    contribution_score FLOAT DEFAULT 0.0,
    anomaly_flags NVARCHAR(MAX),
    ai_critique_summary NVARCHAR(MAX),
    analyzed_at DATETIME DEFAULT GETDATE()
);
GO

-- 13. Table RotationalReviews (École 42)
CREATE TABLE RotationalReviews (
    review_id INT IDENTITY(1,1) PRIMARY KEY,
    reviewer_team_id INT NOT NULL FOREIGN KEY REFERENCES Teams(team_id) ON DELETE NO ACTION,
    target_team_id INT NOT NULL FOREIGN KEY REFERENCES Teams(team_id) ON DELETE NO ACTION,
    submission_id INT FOREIGN KEY REFERENCES Submissions(submission_id) ON DELETE NO ACTION,
    sandbox_score FLOAT DEFAULT 0.0,
    mvc2_clean_code_score FLOAT DEFAULT 0.0,
    peer_defense_score FLOAT DEFAULT 0.0,
    final_score FLOAT DEFAULT 0.0,
    feedback NVARCHAR(MAX),
    status VARCHAR(20) DEFAULT 'pending' CHECK(status IN ('pending', 'in_progress', 'completed', 'disputed')),
    reviewed_at DATETIME,
    created_at DATETIME DEFAULT GETDATE()
);
GO

-- 14. Table CorrectionPointsWallet & Transactions
CREATE TABLE CorrectionPointsWallet (
    wallet_id INT IDENTITY(1,1) PRIMARY KEY,
    user_id INT FOREIGN KEY REFERENCES Users(user_id) ON DELETE NO ACTION,
    team_id INT FOREIGN KEY REFERENCES Teams(team_id) ON DELETE NO ACTION,
    points_balance INT NOT NULL DEFAULT 5,
    updated_at DATETIME DEFAULT GETDATE()
);
GO

CREATE TABLE CorrectionPointTransactions (
    trans_id INT IDENTITY(1,1) PRIMARY KEY,
    wallet_id INT NOT NULL FOREIGN KEY REFERENCES CorrectionPointsWallet(wallet_id) ON DELETE CASCADE,
    amount INT NOT NULL,
    action_type VARCHAR(50) NOT NULL CHECK(action_type IN ('initial_grant', 'review_earned', 'submission_spent', 'admin_adjustment')),
    description NVARCHAR(255),
    reference_review_id INT FOREIGN KEY REFERENCES RotationalReviews(review_id) ON DELETE NO ACTION,
    created_at DATETIME DEFAULT GETDATE()
);
GO

-- 15. Moulinette Test Cases & Results
CREATE TABLE MoulinetteTestCases (
    test_id INT IDENTITY(1,1) PRIMARY KEY,
    test_code VARCHAR(50) NOT NULL UNIQUE,
    title NVARCHAR(150) NOT NULL,
    description NVARCHAR(500),
    test_type VARCHAR(20) NOT NULL CHECK(test_type IN ('PUBLIC', 'HIDDEN')),
    expected_output NVARCHAR(MAX),
    weight FLOAT DEFAULT 1.0,
    is_active INT DEFAULT 1
);
GO

CREATE TABLE MoulinetteResults (
    result_id INT IDENTITY(1,1) PRIMARY KEY,
    submission_id INT NOT NULL FOREIGN KEY REFERENCES Submissions(submission_id) ON DELETE CASCADE,
    test_id INT NOT NULL FOREIGN KEY REFERENCES MoulinetteTestCases(test_id) ON DELETE CASCADE,
    is_passed INT NOT NULL DEFAULT 0,
    execution_time_ms INT DEFAULT 0,
    output_log NVARCHAR(MAX),
    executed_at DATETIME DEFAULT GETDATE()
);
GO

-- 16. Table Grades & Appeals
CREATE TABLE Grades (
    grade_id INT IDENTITY(1,1) PRIMARY KEY,
    submission_id INT NOT NULL UNIQUE FOREIGN KEY REFERENCES Submissions(submission_id) ON DELETE CASCADE,
    graded_by INT NOT NULL FOREIGN KEY REFERENCES Teachers(teacher_id) ON DELETE NO ACTION,
    score FLOAT NOT NULL,
    comment NVARCHAR(MAX),
    graded_at DATETIME DEFAULT GETDATE()
);
GO

CREATE TABLE Appeals (
    appeal_id INT IDENTITY(1,1) PRIMARY KEY,
    grade_id INT NOT NULL FOREIGN KEY REFERENCES Grades(grade_id) ON DELETE CASCADE,
    student_id INT NOT NULL FOREIGN KEY REFERENCES Students(student_id) ON DELETE NO ACTION,
    reason NVARCHAR(MAX) NOT NULL,
    status VARCHAR(20) DEFAULT 'pending' CHECK (status IN ('pending', 'approved', 'rejected')),
    created_at DATETIME DEFAULT GETDATE()
);
GO

-- 17. Table OtpCodes
CREATE TABLE OtpCodes (
    otp_id INT IDENTITY(1,1) PRIMARY KEY,
    email VARCHAR(100) NOT NULL,
    code VARCHAR(10) NOT NULL,
    type VARCHAR(20) NOT NULL,
    expires_at DATETIME NOT NULL,
    is_used INT DEFAULT 0,
    created_at DATETIME DEFAULT GETDATE()
);
GO

-- ==========================================================
-- INSERT SEED DATA FOR SQL SERVER
-- ==========================================================

SET IDENTITY_INSERT Users ON;
INSERT INTO Users (user_id, full_name, email, password_hash, role) VALUES
(1, N'Prof. Robert Davis', 'teacher@school.edu', 'd041c3d3ca4ed64c5b54c5d807bd9a0bd2d6ae3609ecd2d06ac383db449360e1', 'teacher'),
(2, N'Alex Nguyen', 'student@school.edu', 'b2a1f4fd0a460606b34c8913e2981dac8d2e283d778aba586c416ee2629bfa54', 'student'),
(3, N'Emma Watson', 'emma@school.edu', 'b2a1f4fd0a460606b34c8913e2981dac8d2e283d778aba586c416ee2629bfa54', 'student'),
(4, N'Admin Supervisor', 'admin@school.edu', 'e86f78a8a3caf0b60d8e74e5942aa6d86dc150cd3c03338aef25b7d2d7e3acc7', 'admin'),
(5, N'Võ Ngọc Phú (Lead G5)', 'phuvn@school.edu', 'b2a1f4fd0a460606b34c8913e2981dac8d2e283d778aba586c416ee2629bfa54', 'student'),
(6, N'Trần Hoàng Thịnh (Dev G5)', 'thinhth@school.edu', 'b2a1f4fd0a460606b34c8913e2981dac8d2e283d778aba586c416ee2629bfa54', 'student'),
(7, N'Đặng Xuân Thiện (Dev G5)', 'thiendx@school.edu', 'b2a1f4fd0a460606b34c8913e2981dac8d2e283d778aba586c416ee2629bfa54', 'student'),
(8, N'Lương Đức Duy (DB G5)', 'duyld@school.edu', 'b2a1f4fd0a460606b34c8913e2981dac8d2e283d778aba586c416ee2629bfa54', 'student'),
(9, N'Châu Minh Tiến (QA G5)', 'tiencm@school.edu', 'b2a1f4fd0a460606b34c8913e2981dac8d2e283d778aba586c416ee2629bfa54', 'student');
SET IDENTITY_INSERT Users OFF;
GO

SET IDENTITY_INSERT Teachers ON;
INSERT INTO Teachers (teacher_id, user_id, employee_code, department) VALUES
(1, 1, 'TCH-2026-01', N'Software Engineering');
SET IDENTITY_INSERT Teachers OFF;
GO

SET IDENTITY_INSERT Students ON;
INSERT INTO Students (student_id, user_id, student_code, class_name) VALUES
(1, 2, 'STU-10901', 'SE1701'),
(2, 3, 'STU-10902', 'SE1701'),
(3, 5, 'STU-10905', 'SE1701'),
(4, 6, 'STU-10906', 'SE1701'),
(5, 7, 'STU-10907', 'SE1701'),
(6, 8, 'STU-10908', 'SE1701'),
(7, 9, 'STU-10909', 'SE1701');
SET IDENTITY_INSERT Students OFF;
GO

SET IDENTITY_INSERT Subjects ON;
INSERT INTO Subjects (subject_id, subject_code, subject_name, semester) VALUES
(1, 'ANC111', N'Advanced Network & Cloud Computing', 'Semester 1'),
(2, 'ABC222', N'Applied Basic Cloud & Distributed Systems', 'Semester 1'),
(3, 'PRJ301', N'Java Web Application Development', 'Semester 2');
SET IDENTITY_INSERT Subjects OFF;
GO

INSERT INTO StudentSubjects (student_id, subject_id) VALUES
(1, 1), (1, 2), (1, 3),
(2, 1), (2, 2),
(3, 3), (4, 3), (5, 3), (6, 3), (7, 3);
GO

SET IDENTITY_INSERT Teams ON;
INSERT INTO Teams (team_id, team_code, team_name, track_name, project_title, repo_url) VALUES
(1, 'G1', N'Clean Code Masters', N'Code Style & Formatting', N'Automated Code Convention & Clean Architecture Validator', 'https://github.com/rbl-2026/prj301-g1-cleancode'),
(2, 'G2', N'Test Vanguard', N'Unit & Integration Testing', N'Test Case Generation & Dynamic Assertion Engine', 'https://github.com/rbl-2026/prj301-g2-testvanguard'),
(3, 'G3', N'SecGuard Lab', N'Security & Vulnerability', N'Static Security Scanner for SQLi, XSS and Zip Slip in Java Web', 'https://github.com/rbl-2026/prj301-g3-secguard'),
(4, 'G4', N'SimilCheck', N'Plagiarism & Similarity', N'AST-based Source Code Plagiarism & Moss Alternative', 'https://github.com/rbl-2026/prj301-g4-similcheck'),
(5, 'G5', N'GitPulse Assessor', N'Git Analytics Teamwork Assessor', N'Git Analytics Teamwork Assessor & Free-Riding Detection System', 'https://github.com/rbl-2026/prj301-g5-gitassessor'),
(6, 'G6', N'SandBoxer', N'Docker Isolation & Watchdog', N'Resource-Constrained Sandbox Runner & Process Watchdog', 'https://github.com/rbl-2026/prj301-g6-sandboxer'),
(7, 'G7', N'BenchMaster', N'Performance & Profiling', N'Database Query Optimizer & Latency Profiler for PRJ301', 'https://github.com/rbl-2026/prj301-g7-benchmaster');
SET IDENTITY_INSERT Teams OFF;
GO

SET IDENTITY_INSERT TeamMembers ON;
INSERT INTO TeamMembers (id, team_id, user_id, role_in_team) VALUES
(1, 5, 5, 'leader'),
(2, 5, 6, 'member'),
(3, 5, 7, 'member'),
(4, 5, 8, 'member'),
(5, 5, 9, 'member'),
(6, 1, 2, 'leader'),
(7, 2, 3, 'leader');
SET IDENTITY_INSERT TeamMembers OFF;
GO

SET IDENTITY_INSERT Assignments ON;
INSERT INTO Assignments (assignment_id, teacher_id, subject_id, title, prototype_url, prompt_file_url, deadline, rubric_json) VALUES
(1, 1, 1, N'Lab 1: Responsive Dashboard UI Implementation', 
 'https://raw.githubusercontent.com/ai-grading/mock-prototype/main/dashboard.png', 
 '/uploads/submissions/student1_lab1_screenshot.png', 
 DATEADD(day, 2, GETDATE()), 
 '{"criteria":[{"name":"Layout & Alignment","weight":30},{"name":"Color Scheme & Typography","weight":30},{"name":"Component Completeness","weight":40}]}'),
(2, 1, 3, N'Final Capstone Project: PRJ301 RBL Submissions', 
 'https://raw.githubusercontent.com/ai-grading/mock-prototype/main/mvc2-spec.png', 
 NULL, 
 DATEADD(day, 5, GETDATE()), 
 '{"criteria":[{"name":"Sandbox Compliance","weight":30},{"name":"MVC2 / Clean Code","weight":40},{"name":"Peer Defense","weight":30}]}');
SET IDENTITY_INSERT Assignments OFF;
GO

SET IDENTITY_INSERT Submissions ON;
INSERT INTO Submissions (submission_id, student_id, team_id, assignment_id, screenshot_path, zip_file_path, sha256_hash, artifact_type, queue_priority, status) VALUES
(1, 1, 1, 1, '/uploads/submissions/student1_lab1_screenshot.png', NULL, NULL, 'screenshot', 1, 'graded'),
(2, 3, 5, 2, NULL, '/uploads/submissions/g5_gitpulse_assessor.zip', 'a8f5b879c93e4d588f6230f81d115e45a57a167096d2fa48e9d50f83e20e8b9a', 'zip', 2, 'graded');
SET IDENTITY_INSERT Submissions OFF;
GO

SET IDENTITY_INSERT GitMetrics ON;
INSERT INTO GitMetrics (metric_id, submission_id, user_id, author_name, author_email, total_commits, lines_added, lines_deleted, net_churn, commit_entropy, semantic_score, module_ownership, contribution_score, anomaly_flags, ai_critique_summary) VALUES
(1, 2, 5, N'Huy Nguyen', 'huy.nguyen@school.edu', 38, 1420, 210, 1210, 0.89, 94.5, 0.58, 54.8, '["CORE_ARCHITECT"]', 
 N'Huy initiated repository, established MVC2 package architecture, and implemented Docker sandbox and SHA-256 filter.'),
(2, 2, 6, N'Linh Tran', 'linh.tran@school.edu', 24, 980, 160, 820, 0.78, 88.0, 0.35, 38.2, '["ACTIVE_CONTRIBUTOR"]', 
 N'Linh implemented Redis Job Queue and Peer-Review scoring module. High quality Conventional Commits.'),
(3, 2, 7, N'Minh Le', 'minh.le@school.edu', 2, 35, 12, 23, 0.12, 25.0, 0.07, 7.0, '["GHOST_COMMITTER", "11TH_HOUR_RUSHER"]', 
 N'CRITICAL ALERT: Minh contributed only 2 commits with minor README changes 1h before deadline. Negligible code churn.');
SET IDENTITY_INSERT GitMetrics OFF;
GO

SET IDENTITY_INSERT RotationalReviews ON;
INSERT INTO RotationalReviews (review_id, reviewer_team_id, target_team_id, submission_id, sandbox_score, mvc2_clean_code_score, peer_defense_score, final_score, feedback, status) VALUES
(1, 1, 2, NULL, 9.0, 8.5, 9.0, 8.8, N'G2 build and test assertions passed cleanly in Docker container.', 'completed'),
(2, 2, 3, NULL, 8.0, 8.0, 8.5, 8.15, N'SecGuard detected SQL injection well, but documentation could be expanded.', 'completed'),
(3, 3, 4, NULL, 9.5, 9.0, 9.0, 9.15, N'SimilCheck AST comparison is fast and accurate.', 'completed'),
(4, 4, 5, 2, 9.5, 9.5, 9.0, 9.35, N'G5 Git Analytics architecture is pristine! Clean MVC2, SHA-256 filter verified, Docker sandbox operates without network.', 'completed'),
(5, 5, 6, NULL, 9.0, 8.5, 8.5, 8.65, N'G6 SandBoxer resource isolation tests verified with 512MB memory boundary.', 'completed'),
(6, 6, 7, NULL, 8.5, 8.0, 8.0, 8.15, N'BenchMaster load testing showed steady query response under 50 req/s.', 'completed'),
(7, 7, 1, NULL, 9.0, 9.0, 9.5, 9.15, N'Clean Code validator detected all unformatted scriptlets.', 'completed');
SET IDENTITY_INSERT RotationalReviews OFF;
GO

SET IDENTITY_INSERT CorrectionPointsWallet ON;
INSERT INTO CorrectionPointsWallet (wallet_id, user_id, team_id, points_balance) VALUES
(1, 2, 1, 6),
(2, 3, 2, 5),
(3, 5, 5, 5),
(4, 6, 5, 5),
(5, 7, 5, 5);
SET IDENTITY_INSERT CorrectionPointsWallet OFF;
GO

SET IDENTITY_INSERT CorrectionPointTransactions ON;
INSERT INTO CorrectionPointTransactions (trans_id, wallet_id, amount, action_type, description, reference_review_id) VALUES
(1, 3, 5, 'initial_grant', N'Initial allocation of 5 École 42 Correction Points', NULL),
(2, 3, -1, 'submission_spent', N'Debited 1 point to request Peer Review for G5 Capstone Project', 4),
(3, 3, 1, 'review_earned', N'Earned 1 point for successfully completing Peer Review for G6 SandBoxer', 5);
SET IDENTITY_INSERT CorrectionPointTransactions OFF;
GO

SET IDENTITY_INSERT MoulinetteTestCases ON;
INSERT INTO MoulinetteTestCases (test_id, test_code, title, description, test_type, expected_output, weight) VALUES
(1, 'TC_PUB_01', N'SHA-256 Checksum Verification', N'Verify uploaded ZIP matches calculated SHA-256 hash', 'PUBLIC', 'STATUS_HASH_MATCH', 1.0),
(2, 'TC_PUB_02', N'MVC2 Package Structure Check', N'Inspect controller, service, dao, and model packages', 'PUBLIC', 'STATUS_PACKAGE_VALID', 1.0),
(3, 'TC_PUB_03', N'Docker Network Isolation', N'Ensure container runs with --network none and blocks outbound sockets', 'PUBLIC', 'STATUS_NETWORK_ISOLATED', 1.0),
(4, 'TC_HID_01', N'Zip Slip Path Traversal Resilience', N'Attempt to extract malicious zip with ../../ payload', 'HIDDEN', 'SECURITY_EXCEPTION_HANDLED', 2.0),
(5, 'TC_HID_02', N'Author Spoofing & Empty Commits Detection', N'Analyze git history containing empty commits and forged committer emails', 'HIDDEN', 'SPOOFING_FLAG_RAISED', 2.0),
(6, 'TC_HID_03', N'Process Watchdog & Infinite Loop Timeout', N'Execute script with while(true) to verify 45s hard cutoff', 'HIDDEN', 'TIMEOUT_TERMINATED_SAFE', 2.0);
SET IDENTITY_INSERT MoulinetteTestCases OFF;
GO

SET IDENTITY_INSERT MoulinetteResults ON;
INSERT INTO MoulinetteResults (result_id, submission_id, test_id, is_passed, execution_time_ms, output_log) VALUES
(1, 2, 1, 1, 120, N'SHA-256 verified successfully: a8f5b879c93e4d588f6230f81d115e45a57a167096d2fa48e9d50f83e20e8b9a'),
(2, 2, 2, 1, 340, N'Clean MVC2 structure confirmed. No scriptlets detected in views.'),
(3, 2, 3, 1, 850, N'Container execution in network-isolated sandbox passed without network leaks.'),
(4, 2, 4, 1, 410, N'Malicious Zip Slip ../../ rejected with SecurityException as expected.'),
(5, 2, 5, 1, 620, N'Successfully identified 2 anomalous commits and raised GHOST_COMMITTER flag.'),
(6, 2, 6, 1, 1500, N'Watchdog timer killed simulated loop container within safety boundary.');
SET IDENTITY_INSERT MoulinetteResults OFF;
GO

SET IDENTITY_INSERT Grades ON;
INSERT INTO Grades (grade_id, submission_id, graded_by, score, comment) VALUES
(1, 1, 1, 9.2, N'Great work! Approved AI assessment with a 9.2/10.');
SET IDENTITY_INSERT Grades OFF;
GO

SET IDENTITY_INSERT Appeals ON;
INSERT INTO Appeals (appeal_id, grade_id, student_id, reason, status) VALUES
(1, 1, 1, N'I updated the border-radius as suggested in AI feedback. Could the professor please review?', 'pending');
SET IDENTITY_INSERT Appeals OFF;
GO

-- ==========================================================
-- CASCADING DELETION PROCEDURES & TRIGGERS (DATA INTEGRITY)
-- ==========================================================

-- 1. Stored Procedure for cascading deletion of Assignments
CREATE OR ALTER PROCEDURE sp_DeleteAssignmentCascade
    @assignment_id INT
AS
BEGIN
    SET NOCOUNT ON;
    BEGIN TRANSACTION;
    BEGIN TRY
        -- A. Unlink RotationalReviews
        UPDATE RotationalReviews
        SET submission_id = NULL
        WHERE submission_id IN (
            SELECT s.submission_id 
            FROM Submissions s 
            WHERE s.assignment_id = @assignment_id
        );

        -- B. Delete GitMetrics
        DELETE FROM GitMetrics
        WHERE submission_id IN (
            SELECT s.submission_id 
            FROM Submissions s 
            WHERE s.assignment_id = @assignment_id
        );

        -- C. Delete AIAnalysisResults
        DELETE FROM AIAnalysisResults
        WHERE submission_id IN (
            SELECT s.submission_id 
            FROM Submissions s 
            WHERE s.assignment_id = @assignment_id
        );

        -- D. Delete MoulinetteResults
        DELETE FROM MoulinetteResults
        WHERE submission_id IN (
            SELECT s.submission_id 
            FROM Submissions s 
            WHERE s.assignment_id = @assignment_id
        );

        -- E. Delete Appeals
        DELETE FROM Appeals
        WHERE grade_id IN (
            SELECT g.grade_id
            FROM Grades g
            JOIN Submissions s ON g.submission_id = s.submission_id
            WHERE s.assignment_id = @assignment_id
        );

        -- F. Delete Grades
        DELETE FROM Grades
        WHERE submission_id IN (
            SELECT s.submission_id 
            FROM Submissions s 
            WHERE s.assignment_id = @assignment_id
        );

        -- G. Delete Submissions
        DELETE FROM Submissions
        WHERE assignment_id = @assignment_id;

        -- H. Finally delete the Assignment itself
        DELETE FROM Assignments
        WHERE assignment_id = @assignment_id;

        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        ROLLBACK TRANSACTION;
        THROW;
    END CATCH;
END;
GO

-- 2. Trigger for cascading deletion of Submissions
CREATE OR ALTER TRIGGER trg_DeleteSubmissionCascade
ON Submissions
INSTEAD OF DELETE
AS
BEGIN
    SET NOCOUNT ON;

    -- A. Unlink RotationalReviews
    UPDATE RotationalReviews
    SET submission_id = NULL
    WHERE submission_id IN (SELECT submission_id FROM deleted);

    -- B. Delete GitMetrics
    DELETE FROM GitMetrics
    WHERE submission_id IN (SELECT submission_id FROM deleted);

    -- C. Delete AIAnalysisResults
    DELETE FROM AIAnalysisResults
    WHERE submission_id IN (SELECT submission_id FROM deleted);

    -- D. Delete MoulinetteResults
    DELETE FROM MoulinetteResults
    WHERE submission_id IN (SELECT submission_id FROM deleted);

    -- E. Delete Appeals
    DELETE FROM Appeals
    WHERE grade_id IN (
        SELECT g.grade_id 
        FROM Grades g 
        JOIN deleted d ON g.submission_id = d.submission_id
    );

    -- F. Delete Grades
    DELETE FROM Grades
    WHERE submission_id IN (SELECT submission_id FROM deleted);

    -- G. Finally delete the Submissions themselves
    DELETE FROM Submissions
    WHERE submission_id IN (SELECT submission_id FROM deleted);
END;
GO

PRINT 'SQL Server AIGradingDB schema and seed data created and synchronized successfully!';
GO

