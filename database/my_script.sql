-- ============================================================================
--   FILE VIẾT LỆNH DATABASE TRỰC TIẾP TRÊN IDE (VS CODE)
--   Không cần mở SQL Server Management Studio (SSMS).
--   Bạn chỉ cần gõ lệnh SQL vào đây, rồi chạy DatabasePlayground.java hoặc run_database_script.bat!
-- ============================================================================

-- 1. Xem danh sách các bảng trong hệ thống
-- SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES;

-- 2. Thêm một nhóm nghiên cứu mới (INSERT)
-- INSERT INTO Teams (team_code, team_name, track_name, project_title, repo_url)
-- VALUES ('G8', 'AI DevOps Engineers', 'Track 5: Git Analytics', 'Automated DevOps Pipeline Assessor', 'https://github.com/team-g8/devops-assessor');

-- 3. Cập nhật thông tin nhóm (UPDATE)
-- UPDATE Teams SET team_name = 'AI DevOps & Security Leaders' WHERE team_code = 'G8';

-- 4. Xóa nhóm (DELETE)
-- DELETE FROM Teams WHERE team_code = 'G8';

-- 5. Xem 5 bài tập gần nhất (SELECT)
SELECT TOP 5 assignment_id, title, deadline, created_at FROM Assignments ORDER BY assignment_id DESC;

-- 6. Xem tổng số thành viên của từng nhóm (GROUP BY)
SELECT T.team_code, T.team_name, COUNT(TM.user_id) AS total_members
FROM Teams T
LEFT JOIN TeamMembers TM ON T.team_id = TM.team_id
GROUP BY T.team_code, T.team_name
ORDER BY T.team_code;
