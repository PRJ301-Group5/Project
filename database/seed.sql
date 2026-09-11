-- Seed Data for AI Grading System & Git Analytics Teamwork Assessor

-- 1. Users (Teacher, Students, Admin, Team Members)
INSERT OR IGNORE INTO Users (user_id, full_name, email, password_hash, role, created_at) VALUES
(1, 'Prof. Robert Davis', 'teacher@school.edu', 'd041c3d3ca4ed64c5b54c5d807bd9a0bd2d6ae3609ecd2d06ac383db449360e1', 'teacher', '2026-09-01 08:00:00'),
(2, 'Alex Nguyen', 'student@school.edu', 'b2a1f4fd0a460606b34c8913e2981dac8d2e283d778aba586c416ee2629bfa54', 'student', '2026-09-01 08:30:00'),
(3, 'Emma Watson', 'emma@school.edu', 'b2a1f4fd0a460606b34c8913e2981dac8d2e283d778aba586c416ee2629bfa54', 'student', '2026-09-01 08:45:00'),
(4, 'Admin Supervisor', 'admin@school.edu', 'e86f78a8a3caf0b60d8e74e5942aa6d86dc150cd3c03338aef25b7d2d7e3acc7', 'admin', '2026-09-01 07:00:00'),
(5, 'Võ Ngọc Phú (Lead G5)', 'phuvn@school.edu', 'b2a1f4fd0a460606b34c8913e2981dac8d2e283d778aba586c416ee2629bfa54', 'student', '2026-09-01 09:00:00'),
(6, 'Trần Hoàng Thịnh (Dev G5)', 'thinhth@school.edu', 'b2a1f4fd0a460606b34c8913e2981dac8d2e283d778aba586c416ee2629bfa54', 'student', '2026-09-01 09:10:00'),
(7, 'Đặng Xuân Thiện (Dev G5)', 'thiendx@school.edu', 'b2a1f4fd0a460606b34c8913e2981dac8d2e283d778aba586c416ee2629bfa54', 'student', '2026-09-01 09:20:00'),
(8, 'Lương Đức Duy (DB G5)', 'duyld@school.edu', 'b2a1f4fd0a460606b34c8913e2981dac8d2e283d778aba586c416ee2629bfa54', 'student', '2026-09-01 09:30:00'),
(9, 'Châu Minh Tiến (QA G5)', 'tiencm@school.edu', 'b2a1f4fd0a460606b34c8913e2981dac8d2e283d778aba586c416ee2629bfa54', 'student', '2026-09-01 09:40:00');

-- 2. Teachers & Students extensions
INSERT OR IGNORE INTO Teachers (teacher_id, user_id, employee_code, department) VALUES
(1, 1, 'TCH-2026-01', 'Computer Science & Software Engineering');

INSERT OR IGNORE INTO Students (student_id, user_id, student_code, class_name) VALUES
(1, 2, 'STU-10901', 'SE1701'),
(2, 3, 'STU-10902', 'SE1701'),
(3, 5, 'STU-10905', 'SE1701'),
(4, 6, 'STU-10906', 'SE1701'),
(5, 7, 'STU-10907', 'SE1701'),
(6, 8, 'STU-10908', 'SE1701'),
(7, 9, 'STU-10909', 'SE1701');

-- 3. Subjects
INSERT OR IGNORE INTO Subjects (subject_id, subject_code, subject_name, semester) VALUES
(1, 'ANC111', 'Advanced Network & Cloud Computing', 'Semester 1'),
(2, 'ABC222', 'Applied Basic Cloud & Distributed Systems', 'Semester 1'),
(3, 'PRJ301', 'Java Web Application Development', 'Semester 2');

-- 4. Enrollments
INSERT OR IGNORE INTO StudentSubjects (student_id, subject_id) VALUES
(1, 1), (1, 2), (1, 3),
(2, 1), (2, 2),
(3, 3), (4, 3), (5, 3), (6, 3), (7, 3);

-- 5. RBL Tracks & Teams (G1 đến G7)
INSERT OR IGNORE INTO Teams (team_id, team_code, team_name, track_name, project_title, repo_url) VALUES
(1, 'G1', 'Clean Code Masters', 'Code Style & Formatting', 'Automated Code Convention & Clean Architecture Validator', 'https://github.com/rbl-2026/prj301-g1-cleancode'),
(2, 'G2', 'Test Vanguard', 'Unit & Integration Testing', 'Test Case Generation & Dynamic Assertion Engine', 'https://github.com/rbl-2026/prj301-g2-testvanguard'),
(3, 'G3', 'SecGuard Lab', 'Security & Vulnerability', 'Static Security Scanner for SQLi, XSS and Zip Slip in Java Web', 'https://github.com/rbl-2026/prj301-g3-secguard'),
(4, 'G4', 'SimilCheck', 'Plagiarism & Similarity', 'AST-based Source Code Plagiarism & Moss Alternative', 'https://github.com/rbl-2026/prj301-g4-similcheck'),
(5, 'G5', 'GitPulse Assessor', 'Git Analytics Teamwork Assessor', 'Git Analytics Teamwork Assessor & Free-Riding Detection System', 'https://github.com/rbl-2026/prj301-g5-gitassessor'),
(6, 'G6', 'SandBoxer', 'Docker Isolation & Watchdog', 'Resource-Constrained Sandbox Runner & Process Watchdog', 'https://github.com/rbl-2026/prj301-g6-sandboxer'),
(7, 'G7', 'BenchMaster', 'Performance & Profiling', 'Database Query Optimizer & Latency Profiler for PRJ301', 'https://github.com/rbl-2026/prj301-g7-benchmaster');

-- Team Members for G5
INSERT OR IGNORE INTO TeamMembers (id, team_id, user_id, role_in_team) VALUES
(1, 5, 5, 'leader'), -- Ngô Đình Phú
(2, 5, 6, 'member'), -- Nguyễn Phúc Thịnh
(3, 5, 7, 'member'), -- Trần Xuân Thiện
(4, 5, 8, 'member'), -- Lê Đức Duy
(5, 5, 9, 'member'), -- Phạm Minh Tiến
(6, 1, 2, 'leader'), -- Alex Nguyen in G1
(7, 2, 3, 'leader'); -- Emma Watson in G2

-- 6. Assignments
INSERT OR IGNORE INTO Assignments (assignment_id, teacher_id, subject_id, title, prototype_url, deadline, rubric_json, created_at) VALUES
(1, 1, 1, 'Lab 1: Responsive Dashboard UI Implementation', 'https://raw.githubusercontent.com/ai-grading/mock-prototype/main/dashboard.png', datetime('now', '+1 days'), 
 '{"criteria":[{"name":"Layout & Alignment","weight":30},{"name":"Color Scheme & Typography","weight":30},{"name":"Component Completeness","weight":40}]}', '2026-09-15 10:00:00'),
(2, 1, 3, 'Final Capstone Project: PRJ301 RBL Submissions', 'https://raw.githubusercontent.com/ai-grading/mock-prototype/main/mvc2-spec.png', datetime('now', '+5 days'),
 '{"criteria":[{"name":"Sandbox Compliance","weight":30},{"name":"MVC2 / Clean Code","weight":40},{"name":"Peer Defense","weight":30}]}', '2026-09-18 14:00:00');

-- 7. Submissions (Including Team G5 ZIP submission with SHA-256 Hash)
INSERT OR IGNORE INTO Submissions (submission_id, student_id, team_id, assignment_id, screenshot_path, zip_file_path, sha256_hash, artifact_type, queue_priority, status) VALUES
(1, 1, 1, 1, '/uploads/submissions/student1_lab1_screenshot.png', NULL, NULL, 'screenshot', 1, 'graded'),
(2, 3, 5, 2, NULL, '/uploads/submissions/g5_gitpulse_assessor.zip', 'a8f5b879c93e4d588f6230f81d115e45a57a167096d2fa48e9d50f83e20e8b9a', 'zip', 2, 'graded');

-- 8. GitMetrics (Team 5 - Demonstrating Contribution Scores & Free-rider Anomaly)
INSERT OR IGNORE INTO GitMetrics (metric_id, submission_id, user_id, author_name, author_email, total_commits, lines_added, lines_deleted, net_churn, commit_entropy, semantic_score, module_ownership, contribution_score, anomaly_flags, ai_critique_summary) VALUES
(1, 2, 5, 'Huy Nguyen', 'huy.nguyen@school.edu', 38, 1420, 210, 1210, 0.89, 94.5, 0.58, 54.8, '["CORE_ARCHITECT"]', 
 'Huy initiated the repository, established MVC2 package structure, and implemented the core Docker sandbox and SHA-256 filter. Commits span evenly across all 4 weeks.'),
(2, 2, 6, 'Linh Tran', 'linh.tran@school.edu', 24, 980, 160, 820, 0.78, 88.0, 0.35, 38.2, '["ACTIVE_CONTRIBUTOR"]', 
 'Linh implemented the Redis Job Queue and Peer-Review scoring module. High quality commit messages conforming strictly to Conventional Commits.'),
(3, 2, 7, 'Minh Le', 'minh.le@school.edu', 2, 35, 12, 23, 0.12, 25.0, 0.07, 7.0, '["GHOST_COMMITTER", "11TH_HOUR_RUSHER"]', 
 'CRITICAL ALERT: Minh contributed only 2 commits containing minor README formatting changes and license tweaks 1 hour before submission deadline. Negligible code churn and zero backend logic contributions.');

-- 9. École 42: Rotational Reviews (Vòng tròn G1 -> G2 -> ... -> G7 -> G1)
INSERT OR IGNORE INTO RotationalReviews (review_id, reviewer_team_id, target_team_id, submission_id, sandbox_score, mvc2_clean_code_score, peer_defense_score, final_score, feedback, status) VALUES
(1, 1, 2, NULL, 9.0, 8.5, 9.0, 8.8, 'G2 build and test assertions passed cleanly in Docker container.', 'completed'),
(2, 2, 3, NULL, 8.0, 8.0, 8.5, 8.15, 'SecGuard detected SQL injection well, but documentation could be expanded.', 'completed'),
(3, 3, 4, NULL, 9.5, 9.0, 9.0, 9.15, 'SimilCheck AST comparison is fast and accurate.', 'completed'),
(4, 4, 5, 2, 9.5, 9.5, 9.0, 9.35, 'G5 Git Analytics architecture is pristine! Clean MVC2, SHA-256 filter verified, Docker sandbox operates without network.', 'completed'),
(5, 5, 6, NULL, 9.0, 8.5, 8.5, 8.65, 'G6 SandBoxer resource isolation tests verified with 512MB memory boundary.', 'completed'),
(6, 6, 7, NULL, 8.5, 8.0, 8.0, 8.15, 'BenchMaster load testing showed steady query response under 50 req/s.', 'completed'),
(7, 7, 1, NULL, 9.0, 9.0, 9.5, 9.15, 'Clean Code validator detected all unformatted scriptlets.', 'completed');

-- 10. École 42: Correction Points Wallet (5 initial points per user/team)
INSERT OR IGNORE INTO CorrectionPointsWallet (wallet_id, user_id, team_id, points_balance) VALUES
(1, 2, 1, 6), -- G1 earned 1 from reviewing G2 (5 - 0 + 1 = 6)
(2, 3, 2, 5), -- G2
(3, 5, 5, 5), -- G5: 5 initial - 1 spent for submission + 1 earned reviewing G6 = 5
(4, 6, 5, 5),
(5, 7, 5, 5);

INSERT OR IGNORE INTO CorrectionPointTransactions (trans_id, wallet_id, amount, action_type, description, reference_review_id) VALUES
(1, 3, 5, 'initial_grant', 'Initial allocation of 5 École 42 Correction Points', NULL),
(2, 3, -1, 'submission_spent', 'Debited 1 point to request Peer Review for G5 Capstone Project', 4),
(3, 3, 1, 'review_earned', 'Earned 1 point for successfully completing Peer Review for G6 SandBoxer', 5);

-- 11. Moulinette Test Cases (Public & Hidden)
INSERT OR IGNORE INTO MoulinetteTestCases (test_id, test_code, title, description, test_type, expected_output, weight) VALUES
(1, 'TC_PUB_01', 'SHA-256 Checksum Verification', 'Verify uploaded ZIP matches calculated SHA-256 hash', 'PUBLIC', 'STATUS_HASH_MATCH', 1.0),
(2, 'TC_PUB_02', 'MVC2 Package Structure Check', 'Inspect controller, service, dao, and model packages', 'PUBLIC', 'STATUS_PACKAGE_VALID', 1.0),
(3, 'TC_PUB_03', 'Docker Network Isolation', 'Ensure container runs with --network none and blocks outbound sockets', 'PUBLIC', 'STATUS_NETWORK_ISOLATED', 1.0),
(4, 'TC_HID_01', 'Zip Slip Path Traversal Resilience', 'Attempt to extract malicious zip with ../../ payload', 'HIDDEN', 'SECURITY_EXCEPTION_HANDLED', 2.0),
(5, 'TC_HID_02', 'Author Spoofing & Empty Commits Detection', 'Analyze git history containing empty commits and forged committer emails', 'HIDDEN', 'SPOOFING_FLAG_RAISED', 2.0),
(6, 'TC_HID_03', 'Process Watchdog & Infinite Loop Timeout', 'Execute script with while(true) to verify 45s hard cutoff', 'HIDDEN', 'TIMEOUT_TERMINATED_SAFE', 2.0);

-- Moulinette Sample Results for G5 Submission (submission_id = 2)
INSERT OR IGNORE INTO MoulinetteResults (result_id, submission_id, test_id, is_passed, execution_time_ms, output_log) VALUES
(1, 2, 1, 1, 120, 'SHA-256 verified successfully: a8f5b879c93e4d588f6230f81d115e45a57a167096d2fa48e9d50f83e20e8b9a'),
(2, 2, 2, 1, 340, 'Clean MVC2 structure confirmed. No scriptlets detected in views.'),
(3, 2, 3, 1, 850, 'Container execution in network-isolated sandbox passed without network leaks.'),
(4, 2, 4, 1, 410, 'Malicious Zip Slip ../../ rejected with SecurityException as expected.'),
(5, 2, 5, 1, 620, 'Successfully identified 2 anomalous commits and raised GHOST_COMMITTER flag.'),
(6, 2, 6, 1, 1500, 'Watchdog timer killed simulated loop container within safety boundary.');
