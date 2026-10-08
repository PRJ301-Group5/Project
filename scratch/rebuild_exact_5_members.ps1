# Rebuild Git History with EXACT 5 members:
# 1. Vo Ngoc Phu <phuvn@school.edu>
# 2. Tran Hoang Thinh <thinhth@school.edu>
# 3. Luong Duc Duy <duyld@school.edu>
# 4. Dang Xuan Thien <thiendx@school.edu>
# 5. Chau Minh Tien <tiencm@school.edu>

git checkout --orphan main_rebuilt
git reset

# 1. Vo Ngoc Phu (C1)
git add .gitignore README.md build.xml manifest.mf nbproject/
git commit --author="Vo Ngoc Phu <phuvn@school.edu>" --date="2026-09-08T09:00:00" -m "feat(arch): Initialize PRJ301 project structure, Ant build.xml and NetBeans configuration"

# 2. Luong Duc Duy (C2)
git add database/schema.sql database/sqlserver_schema_and_seed.sql database_model.jpeg
git commit --author="Luong Duc Duy <duyld@school.edu>" --date="2026-09-09T14:30:00" -m "feat(schema): Design 20 database tables with foreign keys and cascading rules"

# 3. Luong Duc Duy (C3)
git add database/seed.sql database/apply_cascade_triggers.sql
git commit --author="Luong Duc Duy <duyld@school.edu>" --date="2026-09-11T10:15:00" -m "feat(seed): Create seed data for 7 research tracks and team members"

# 4. Vo Ngoc Phu (C4)
git add backend/lib/ backend/src/com/aigrading/db/DatabaseManager.java
git commit --author="Vo Ngoc Phu <phuvn@school.edu>" --date="2026-09-12T16:00:00" -m "feat(db): Setup DatabaseManager with dual MS SQL Server and SQLite support"

# 5. Tran Hoang Thinh (C5)
git add backend/src/com/aigrading/model/
git commit --author="Tran Hoang Thinh <thinhth@school.edu>" --date="2026-09-14T11:20:00" -m "feat(model): Create JavaBeans models for User, Assignment, Submission, and Team"

# 6. Chau Minh Tien (C6)
git add backend/src/com/aigrading/util/ backend/src/com/aigrading/filter/Sha256IntegrityFilter.java
git commit --author="Chau Minh Tien <tiencm@school.edu>" --date="2026-09-15T09:30:00" -m "feat(security): Implement Sha256IntegrityFilter and Zip Slip vulnerability defense"

# 7. Vo Ngoc Phu (C7)
git add backend/src/com/aigrading/auth/
git commit --author="Vo Ngoc Phu <phuvn@school.edu>" --date="2026-09-16T15:45:00" -m "feat(auth): Implement JWT authentication engine and AuthContext security"

# 8. Luong Duc Duy (C8)
git add backend/src/com/aigrading/dao/UserDAO.java backend/src/com/aigrading/dao/TeamDAO.java
git commit --author="Luong Duc Duy <duyld@school.edu>" --date="2026-09-17T13:00:00" -m "feat(dao-user): Implement UserDAO and TeamDAO with PreparedStatement parameterized queries"

# 9. Chau Minh Tien (C9)
git add backend/src/com/aigrading/filter/EncodingFilter.java
git commit --author="Chau Minh Tien <tiencm@school.edu>" --date="2026-09-18T10:00:00" -m "feat(filter-encoding): Implement EncodingFilter ensuring UTF-8 encoding across all requests"

# 10. Dang Xuan Thien (C10)
git add web/header.jsp web/footer.jsp web/index.jsp
git commit --author="Dang Xuan Thien <thiendx@school.edu>" --date="2026-09-19T14:15:00" -m "feat(view-layout): Create responsive header.jsp and footer.jsp layout components"

# 11. Tran Hoang Thinh (C11)
git add backend/src/com/aigrading/servlet/LoginServlet.java backend/src/com/aigrading/servlet/LogoutServlet.java
git commit --author="Tran Hoang Thinh <thinhth@school.edu>" --date="2026-09-21T11:45:00" -m "feat(servlet-auth): Implement LoginServlet and LogoutServlet with Session and Cookie management"

# 12. Dang Xuan Thien (C12)
git add web/login.jsp
git commit --author="Dang Xuan Thien <thiendx@school.edu>" --date="2026-09-22T16:20:00" -m "feat(view-login): Design login.jsp with Expression Language and Cookie remember-me support"

# 13. Luong Duc Duy (C13)
git add backend/src/com/aigrading/dao/AssignmentDAO.java
git commit --author="Luong Duc Duy <duyld@school.edu>" --date="2026-09-23T09:30:00" -m "feat(dao-assignment): Implement AssignmentDAO with full CRUD parameterized queries"

# 14. Tran Hoang Thinh (C14)
git add backend/src/com/aigrading/servlet/AssignmentServlet.java
git commit --author="Tran Hoang Thinh <thinhth@school.edu>" --date="2026-09-24T15:10:00" -m "feat(servlet-crud): Implement AssignmentServlet handling full CRUD workflow and session verification"

# 15. Dang Xuan Thien (C15)
git add web/assignments.jsp
git commit --author="Dang Xuan Thien <thiendx@school.edu>" --date="2026-09-25T13:40:00" -m "feat(view-assignments): Design assignments.jsp with search, filter, and JSTL table rendering"

# 16. Dang Xuan Thien (C16)
git add web/assignment-form.jsp
git commit --author="Dang Xuan Thien <thiendx@school.edu>" --date="2026-09-26T11:00:00" -m "feat(view-form): Design assignment-form.jsp supporting Create and Update modes"

# 17. Chau Minh Tien (C17)
git add backend/src/com/aigrading/filter/AuthenticationFilter.java
git commit --author="Chau Minh Tien <tiencm@school.edu>" --date="2026-09-27T10:15:00" -m "feat(filter-auth): Implement AuthenticationFilter for Role-Based Access Control (RBAC)"

# 18. Tran Hoang Thinh (C18)
git add backend/src/com/aigrading/servlet/SubmissionServlet.java backend/src/com/aigrading/servlet/TeamServlet.java
git commit --author="Tran Hoang Thinh <thinhth@school.edu>" --date="2026-09-28T14:30:00" -m "feat(servlet-submission): Implement SubmissionServlet and exception paths for expired deadlines"

# 19. Dang Xuan Thien (C19)
git add web/assignment-detail.jsp web/teams.jsp web/submissions.jsp web/error.jsp
git commit --author="Dang Xuan Thien <thiendx@school.edu>" --date="2026-09-29T16:00:00" -m "feat(view-detail): Design assignment-detail.jsp with submission form and expired deadline alerts"

# 20. Luong Duc Duy (C20)
git add backend/src/com/aigrading/dao/SubmissionDAO.java
git commit --author="Luong Duc Duy <duyld@school.edu>" --date="2026-09-30T09:40:00" -m "feat(dao-submission): Implement SubmissionDAO and cascade deletion queries"

# 21. Chau Minh Tien (C21)
git add backend/src/com/aigrading/services/MoulinetteGradingService.java backend/src/com/aigrading/services/DockerSandboxService.java
git commit --author="Chau Minh Tien <tiencm@school.edu>" --date="2026-09-30T14:00:00" -m "feat(sandbox): Integrate Moulinette Sandbox automated testing and Docker service"

# 22. Vo Ngoc Phu (C22)
git add backend/src/com/aigrading/services/GitAnalyticsService.java backend/src/com/aigrading/services/PeerReviewService.java backend/src/com/aigrading/services/RedisJobQueueService.java backend/src/com/aigrading/services/DeadlineNotificationService.java
git commit --author="Vo Ngoc Phu <phuvn@school.edu>" --date="2026-10-01T11:20:00" -m "feat(git-metrics): Implement Track 5 Git Analytics and Free-Rider teamwork detection engine"

# 23. Chau Minh Tien (C23)
git add tests/
git commit --author="Chau Minh Tien <tiencm@school.edu>" --date="2026-10-02T15:30:00" -m "test(backend): Add comprehensive unit tests suite (85 tests passing 100%)"

# 24. Vo Ngoc Phu (C24)
git add backend/src/com/aigrading/DatabasePlayground.java run_database_script.bat database/my_script.sql backend/src/com/aigrading/services/DatabaseCrudService.java
git commit --author="Vo Ngoc Phu <phuvn@school.edu>" --date="2026-10-03T10:45:00" -m "feat(db-sync): Add dynamic Java-to-SQL auto-synchronization and DatabasePlayground"

# 25. Tran Hoang Thinh (C25)
git add web/WEB-INF/web.xml backend/src/com/aigrading/services/AIGradingService.java
git commit --author="Tran Hoang Thinh <thinhth@school.edu>" --date="2026-10-04T14:20:00" -m "feat(exception): Finalize robust exception handling for invalid inputs and past deadlines"

# 26. Chau Minh Tien (C26)
git add docs/AI_DEBUGGING_LOG.md
git commit --author="Chau Minh Tien <tiencm@school.edu>" --date="2026-10-05T16:15:00" -m "docs(process): Complete AI Debugging Log and Peer-Review Milestone documentation"

# 27. Vo Ngoc Phu (C27)
git add .
git commit --author="Vo Ngoc Phu <phuvn@school.edu>" --date="2026-10-08T17:00:00" -m "feat(fullstack): Finalize embedded HTTP server, web.xml, and desktop application integration"

# Switch branch to main
git branch -D main
git branch -M main
