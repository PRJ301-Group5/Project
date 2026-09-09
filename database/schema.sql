-- Relational Database Schema for AI Grading System & Git Analytics Teamwork Assessor
-- PRJ301 MVC2 + École 42 Rotational Peer-Review + Moulinette Auto-Grading

PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS Users (
    user_id INTEGER PRIMARY KEY AUTOINCREMENT,
    full_name TEXT NOT NULL,
    email TEXT NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    role TEXT NOT NULL CHECK(role IN ('teacher', 'student', 'admin')),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS Teachers (
    teacher_id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL UNIQUE,
    employee_code TEXT NOT NULL UNIQUE,
    department TEXT,
    FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS Students (
    student_id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL UNIQUE,
    student_code TEXT NOT NULL UNIQUE,
    class_name TEXT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS Subjects (
    subject_id INTEGER PRIMARY KEY AUTOINCREMENT,
    subject_code TEXT NOT NULL UNIQUE,
    subject_name TEXT NOT NULL,
    semester TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS StudentSubjects (
    student_id INTEGER NOT NULL,
    subject_id INTEGER NOT NULL,
    PRIMARY KEY (student_id, subject_id),
    FOREIGN KEY (student_id) REFERENCES Students(student_id) ON DELETE CASCADE,
    FOREIGN KEY (subject_id) REFERENCES Subjects(subject_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS Classes (
    class_id INTEGER PRIMARY KEY AUTOINCREMENT,
    class_name TEXT NOT NULL UNIQUE,
    department TEXT NOT NULL,
    teacher_id INTEGER REFERENCES Teachers(teacher_id) ON DELETE SET NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- ==========================================================
-- RBL TRACKS & TEAMS (Nhóm 1 đến Nhóm 7, Nhóm 5: Git Analytics)
-- ==========================================================
CREATE TABLE IF NOT EXISTS Teams (
    team_id INTEGER PRIMARY KEY AUTOINCREMENT,
    team_code TEXT NOT NULL UNIQUE, -- 'G1', 'G2', 'G3', 'G4', 'G5', 'G6', 'G7'
    team_name TEXT NOT NULL,
    track_name TEXT NOT NULL,
    project_title TEXT NOT NULL,
    repo_url TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS TeamMembers (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    team_id INTEGER NOT NULL,
    user_id INTEGER NOT NULL,
    role_in_team TEXT DEFAULT 'member' CHECK(role_in_team IN ('leader', 'member')),
    FOREIGN KEY (team_id) REFERENCES Teams(team_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE,
    UNIQUE(team_id, user_id)
);

CREATE TABLE IF NOT EXISTS Assignments (
    assignment_id INTEGER PRIMARY KEY AUTOINCREMENT,
    teacher_id INTEGER NOT NULL,
    subject_id INTEGER,
    title TEXT NOT NULL,
    prototype_url TEXT,
    prompt_file_url TEXT,
    deadline DATETIME NOT NULL,
    rubric_json TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (teacher_id) REFERENCES Teachers(teacher_id) ON DELETE CASCADE,
    FOREIGN KEY (subject_id) REFERENCES Subjects(subject_id) ON DELETE SET NULL
);

-- ==========================================================
-- SUBMISSION PORTAL & ARTIFACT (Supports ZIP & SHA-256 Hash)
-- ==========================================================
CREATE TABLE IF NOT EXISTS Submissions (
    submission_id INTEGER PRIMARY KEY AUTOINCREMENT,
    student_id INTEGER,
    team_id INTEGER,
    assignment_id INTEGER NOT NULL,
    screenshot_path TEXT,
    zip_file_path TEXT,
    sha256_hash TEXT,
    artifact_type TEXT DEFAULT 'screenshot' CHECK(artifact_type IN ('screenshot', 'zip', 'git_repo')),
    queue_priority INTEGER DEFAULT 1,
    retry_count INTEGER DEFAULT 0,
    submitted_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    status TEXT NOT NULL DEFAULT 'submitted' CHECK(status IN ('submitted', 'queued', 'running', 'grading', 'graded', 'appealed', 'resolved', 'failed')),
    FOREIGN KEY (student_id) REFERENCES Students(student_id) ON DELETE CASCADE,
    FOREIGN KEY (team_id) REFERENCES Teams(team_id) ON DELETE SET NULL,
    FOREIGN KEY (assignment_id) REFERENCES Assignments(assignment_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS AIAnalysisResults (
    result_id INTEGER PRIMARY KEY AUTOINCREMENT,
    submission_id INTEGER NOT NULL UNIQUE,
    similarity_score REAL,
    detected_elements TEXT,
    layout_report TEXT,
    ai_feedback TEXT,
    analyzed_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (submission_id) REFERENCES Submissions(submission_id) ON DELETE CASCADE
);

-- ==========================================================
-- NHÓM 5: GIT ANALYTICS & INDIVIDUAL CONTRIBUTION METRICS
-- ==========================================================
CREATE TABLE IF NOT EXISTS GitMetrics (
    metric_id INTEGER PRIMARY KEY AUTOINCREMENT,
    submission_id INTEGER NOT NULL,
    user_id INTEGER,
    author_name TEXT NOT NULL,
    author_email TEXT,
    total_commits INTEGER DEFAULT 0,
    lines_added INTEGER DEFAULT 0,
    lines_deleted INTEGER DEFAULT 0,
    net_churn INTEGER DEFAULT 0,
    commit_entropy REAL DEFAULT 0.0,
    semantic_score REAL DEFAULT 0.0,
    module_ownership REAL DEFAULT 0.0,
    contribution_score REAL DEFAULT 0.0, -- ICS (Individual Contribution Score %)
    anomaly_flags TEXT, -- JSON string or comma-separated red flags (e.g. GHOST_COMMITTER, 11TH_HOUR_RUSHER)
    ai_critique_summary TEXT,
    analyzed_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (submission_id) REFERENCES Submissions(submission_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE SET NULL
);

-- ==========================================================
-- ÉCOLE 42: ROTATIONAL PEER-REVIEW (G1 -> G2 -> ... -> G7 -> G1)
-- ==========================================================
CREATE TABLE IF NOT EXISTS RotationalReviews (
    review_id INTEGER PRIMARY KEY AUTOINCREMENT,
    reviewer_team_id INTEGER NOT NULL,
    target_team_id INTEGER NOT NULL,
    submission_id INTEGER,
    sandbox_score REAL DEFAULT 0.0,      -- 30% Rubric weight
    mvc2_clean_code_score REAL DEFAULT 0.0, -- 40% Rubric weight
    peer_defense_score REAL DEFAULT 0.0, -- 30% Rubric weight
    final_score REAL DEFAULT 0.0,
    feedback TEXT,
    status TEXT DEFAULT 'pending' CHECK(status IN ('pending', 'in_progress', 'completed', 'disputed')),
    reviewed_at DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (reviewer_team_id) REFERENCES Teams(team_id) ON DELETE CASCADE,
    FOREIGN KEY (target_team_id) REFERENCES Teams(team_id) ON DELETE CASCADE,
    FOREIGN KEY (submission_id) REFERENCES Submissions(submission_id) ON DELETE SET NULL
);

-- ==========================================================
-- ÉCOLE 42: CORRECTION POINTS WALLET & TRANSACTIONS
-- ==========================================================
CREATE TABLE IF NOT EXISTS CorrectionPointsWallet (
    wallet_id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER,
    team_id INTEGER,
    points_balance INTEGER NOT NULL DEFAULT 5, -- Bắt đầu với 5 điểm sửa lỗi
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (team_id) REFERENCES Teams(team_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS CorrectionPointTransactions (
    trans_id INTEGER PRIMARY KEY AUTOINCREMENT,
    wallet_id INTEGER NOT NULL,
    amount INTEGER NOT NULL, -- Dương (+) nếu được cộng (đi chấm bài bạn), Âm (-) nếu tiêu (được người khác chấm)
    action_type TEXT NOT NULL CHECK(action_type IN ('initial_grant', 'review_earned', 'submission_spent', 'admin_adjustment')),
    description TEXT,
    reference_review_id INTEGER,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (wallet_id) REFERENCES CorrectionPointsWallet(wallet_id) ON DELETE CASCADE,
    FOREIGN KEY (reference_review_id) REFERENCES RotationalReviews(review_id) ON DELETE SET NULL
);

-- ==========================================================
-- MOULINETTE-STYLE AUTOMATED TEST CASES & RESULTS
-- ==========================================================
CREATE TABLE IF NOT EXISTS MoulinetteTestCases (
    test_id INTEGER PRIMARY KEY AUTOINCREMENT,
    test_code TEXT NOT NULL UNIQUE,
    title TEXT NOT NULL,
    description TEXT,
    test_type TEXT NOT NULL CHECK(test_type IN ('PUBLIC', 'HIDDEN')),
    expected_output TEXT,
    weight REAL DEFAULT 1.0,
    is_active INTEGER DEFAULT 1
);

CREATE TABLE IF NOT EXISTS MoulinetteResults (
    result_id INTEGER PRIMARY KEY AUTOINCREMENT,
    submission_id INTEGER NOT NULL,
    test_id INTEGER NOT NULL,
    is_passed INTEGER NOT NULL DEFAULT 0,
    execution_time_ms INTEGER DEFAULT 0,
    output_log TEXT,
    executed_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (submission_id) REFERENCES Submissions(submission_id) ON DELETE CASCADE,
    FOREIGN KEY (test_id) REFERENCES MoulinetteTestCases(test_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS Grades (
    grade_id INTEGER PRIMARY KEY AUTOINCREMENT,
    submission_id INTEGER NOT NULL UNIQUE,
    graded_by INTEGER NOT NULL,
    score REAL NOT NULL,
    comment TEXT,
    graded_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (submission_id) REFERENCES Submissions(submission_id) ON DELETE CASCADE,
    FOREIGN KEY (graded_by) REFERENCES Teachers(teacher_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS Appeals (
    appeal_id INTEGER PRIMARY KEY AUTOINCREMENT,
    grade_id INTEGER NOT NULL,
    student_id INTEGER NOT NULL,
    reason TEXT NOT NULL,
    status TEXT NOT NULL DEFAULT 'pending' CHECK(status IN ('pending', 'approved', 'rejected')),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (grade_id) REFERENCES Grades(grade_id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES Students(student_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS OtpCodes (
    otp_id INTEGER PRIMARY KEY AUTOINCREMENT,
    email TEXT NOT NULL,
    code TEXT NOT NULL,
    type TEXT NOT NULL CHECK(type IN ('register', 'forgot_password')),
    expires_at DATETIME NOT NULL,
    is_used INTEGER NOT NULL DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for optimal query performance
CREATE INDEX IF NOT EXISTS idx_users_email ON Users(email);
CREATE INDEX IF NOT EXISTS idx_assignments_teacher ON Assignments(teacher_id);
CREATE INDEX IF NOT EXISTS idx_assignments_subject ON Assignments(subject_id);
CREATE INDEX IF NOT EXISTS idx_submissions_student ON Submissions(student_id);
CREATE INDEX IF NOT EXISTS idx_submissions_team ON Submissions(team_id);
CREATE INDEX IF NOT EXISTS idx_submissions_assignment ON Submissions(assignment_id);
CREATE INDEX IF NOT EXISTS idx_gitmetrics_submission ON GitMetrics(submission_id);
CREATE INDEX IF NOT EXISTS idx_rotational_reviews_reviewer ON RotationalReviews(reviewer_team_id);
CREATE INDEX IF NOT EXISTS idx_rotational_reviews_target ON RotationalReviews(target_team_id);
CREATE INDEX IF NOT EXISTS idx_grades_submission ON Grades(submission_id);
CREATE INDEX IF NOT EXISTS idx_appeals_grade ON Appeals(grade_id);
