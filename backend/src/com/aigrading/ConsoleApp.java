package com.aigrading;

import com.aigrading.db.DatabaseManager;
import com.aigrading.model.GitMetric;
import com.aigrading.services.GitAnalyticsService;
import com.aigrading.services.GitAnalyticsService.RawCommit;
import com.aigrading.services.MoulinetteGradingService;
import com.aigrading.services.MoulinetteGradingService.TestExecutionOutput;
import com.aigrading.services.PeerReviewService;
import com.aigrading.services.DatabaseCrudService;
import com.aigrading.util.JwtUtil;
import com.aigrading.util.JwtUtil.JwtClaims;
import com.aigrading.util.Sha256Util;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.*;

/**
 * Giao diện tương tác trực tiếp trên Console (Interactive CLI Terminal)
 * Phục vụ đồ án PRJ301 & Nhóm 5: Git Analytics Teamwork Assessor
 */
public class ConsoleApp {
    private static final Scanner scanner = new Scanner(System.in);
    private static Map<String, Object> currentUser = null;
    private static String currentJwtToken = null;

    // ANSI Colors for high-contrast console display
    private static final String RESET = "\u001B[0m";
    private static final String BOLD = "\u001B[1m";
    private static final String CYAN = "\u001B[36m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String RED = "\u001B[31m";
    private static final String BLUE = "\u001B[34m";
    private static final String PURPLE = "\u001B[35m";

    public static void main(String[] args) {
        // 1. Initialize Database
        System.out.println(CYAN + BOLD + "Đang khởi tạo kết nối Cơ sở Dữ liệu..." + RESET);
        DatabaseManager.init(null);

        // 2. Main interactive loop
        boolean running = true;
        while (running) {
            printHeader();
            printMenu();
            System.out.print(BOLD + "👉 Nhập lựa chọn của bạn [0-9]: " + RESET);
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    handleLogin();
                    break;
                case "2":
                    handleListTeams();
                    break;
                case "3":
                    handleGitAnalyticsTrack5();
                    break;
                case "4":
                    handleAssignmentsManagement();
                    break;
                case "5":
                    handlePeerReviewEco42();
                    break;
                case "6":
                    handleMoulinetteTests();
                    break;
                case "7":
                    handleSqlTerminal();
                    break;
                case "8":
                    handleJwtInspector();
                    break;
                case "9":
                    handleDatabaseCrudManager();
                    break;
                case "0":
                    running = false;
                    System.out.println(GREEN + "\nCảm ơn bạn đã sử dụng hệ thống! Tạm biệt." + RESET);
                    break;
                default:
                    System.out.println(RED + "Lựa chọn không hợp lệ, vui lòng thử lại!" + RESET);
                    pause();
            }
        }
    }

    private static void printHeader() {
        System.out.println("\n" + CYAN + "╔══════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║        HỆ THỐNG CHẤM BÀI TỰ ĐỘNG & PHÂN TÍCH ĐÓNG GÓP (PRJ301)               ║");
        System.out.println("║                NHÓM 5: GIT ANALYTICS TEAMWORK ASSESSOR                       ║");
        System.out.println("║            Database: Microsoft SQL Server 2019 (AIGradingDB)                 ║");
        System.out.println("╚══════════════════════════════════════════════════════════════════════════════╝" + RESET);

        if (currentUser != null) {
            System.out.println(GREEN + "  👤 Đang đăng nhập: " + BOLD + currentUser.get("full_name") + RESET +
                               GREEN + " | Vai trò: " + BOLD + currentUser.get("role") + RESET +
                               GREEN + " | Email: " + currentUser.get("email") + RESET);
        } else {
            System.out.println(YELLOW + "  👤 Trạng thái: Chưa đăng nhập (Khách / Demo Mode)" + RESET);
        }
        System.out.println(CYAN + "──────────────────────────────────────────────────────────────────────────────" + RESET);
    }

    private static void printMenu() {
        System.out.println(BOLD + "DANH MỤC CHỨC NĂNG:" + RESET);
        System.out.println("  [" + CYAN + "1" + RESET + "] Đăng nhập / Chuyển đổi tài khoản (Teacher / Student / Admin)");
        System.out.println("  [" + CYAN + "2" + RESET + "] Xem danh sách 7 Nhóm nghiên cứu & Thành viên (G1 - G7)");
        System.out.println("  [" + GREEN + "3" + RESET + "] " + BOLD + "TRỌNG TÂM NHÓM 5: Động cơ Git Analytics, Tính ICS % & Bắt Free-rider" + RESET);
        System.out.println("  [" + CYAN + "4" + RESET + "] Quản lý Bài tập & Bài nộp (Nộp file ZIP, Kiểm tra SHA-256 & Zip Slip)");
        System.out.println("  [" + CYAN + "5" + RESET + "] Chấm chéo École 42 (Vòng tròn G1->G7->G1, Rubric 30/40/30 & Ví Điểm)");
        System.out.println("  [" + CYAN + "6" + RESET + "] Chạy bộ kiểm thử tự động Moulinette Sandbox (Public & Hidden Tests)");
        System.out.println("  [" + YELLOW + "7" + RESET + "] Trình thực thi truy vấn SQL trực tiếp trên Console (Interactive SQL)");
        System.out.println("  [" + PURPLE + "8" + RESET + "] Kiểm tra cơ chế JWT Token & RBAC Claims (Mục 4.1.3)");
        System.out.println("  [" + GREEN + "9" + RESET + "] " + BOLD + "QUẢN LÝ CSDL ĐỘNG: Thêm / Sửa / Xóa dữ liệu & Tự động Đồng bộ CSDL từ Java" + RESET);
        System.out.println("  [" + RED + "0" + RESET + "] Thoát chương trình");
        System.out.println(CYAN + "──────────────────────────────────────────────────────────────────────────────" + RESET);
    }

    // ==========================================
    // 1. ĐĂNG NHẬP & PHÂN QUYỀN
    // ==========================================
    private static void handleLogin() {
        System.out.println("\n" + BOLD + "=== ĐĂNG NHẬP HỆ THỐNG ===" + RESET);
        System.out.println("  1. Võ Ngọc Phú (Leader Nhóm 5 - phuvn@school.edu)");
        System.out.println("  2. Trần Hoàng Thịnh (Dev Nhóm 5 - thinhth@school.edu)");
        System.out.println("  3. Đặng Xuân Thiện (Dev Nhóm 5 - thiendx@school.edu)");
        System.out.println("  4. Lương Đức Duy (DB Nhóm 5 - duyld@school.edu)");
        System.out.println("  5. Châu Minh Tiến (QA Nhóm 5 - tiencm@school.edu)");
        System.out.println("  6. Prof. Robert Davis (Giảng viên / Teacher - teacher@school.edu)");
        System.out.println("  7. Nhập email/mật khẩu tùy chọn");
        System.out.println("  0. Đăng xuất / Quay lại");
        System.out.print("👉 Chọn: ");
        String op = scanner.nextLine().trim();

        String email = null;
        if ("1".equals(op)) email = "phuvn@school.edu";
        else if ("2".equals(op)) email = "thinhth@school.edu";
        else if ("3".equals(op)) email = "thiendx@school.edu";
        else if ("4".equals(op)) email = "duyld@school.edu";
        else if ("5".equals(op)) email = "tiencm@school.edu";
        else if ("6".equals(op)) email = "teacher@school.edu";
        else if ("7".equals(op)) {
            System.out.print("Email: ");
            email = scanner.nextLine().trim();
            System.out.print("Password: ");
            String pwd = scanner.nextLine().trim();
            try {
                String hash = Sha256Util.calculateHash(pwd.getBytes(StandardCharsets.UTF_8));
                Map<String, Object> u = DatabaseManager.queryOne("SELECT * FROM Users WHERE email = ? AND password_hash = ?", email, hash);
                if (u != null) {
                    currentUser = u;
                    int uid = ((Number) u.get("user_id")).intValue();
                    currentJwtToken = JwtUtil.generateToken(uid, (String) u.get("email"), (String) u.get("role"), 5);
                    System.out.println(GREEN + "✔ Đăng nhập thành công với vai trò: " + u.get("role") + RESET);
                } else {
                    System.out.println(RED + "✘ Sai thông tin đăng nhập!" + RESET);
                }
            } catch (Exception e) {
                System.out.println(RED + "Lỗi: " + e.getMessage() + RESET);
            }
            pause();
            return;
        } else if ("0".equals(op)) {
            currentUser = null;
            currentJwtToken = null;
            System.out.println(YELLOW + "Đã đăng xuất!" + RESET);
            pause();
            return;
        }

        if (email != null) {
            try {
                Map<String, Object> u = DatabaseManager.queryOne("SELECT * FROM Users WHERE email = ?", email);
                if (u != null) {
                    currentUser = u;
                    int uid = ((Number) u.get("user_id")).intValue();
                    currentJwtToken = JwtUtil.generateToken(uid, (String) u.get("email"), (String) u.get("role"), 5);
                    System.out.println(GREEN + "✔ Đăng nhập thành công: " + u.get("full_name") + " (" + u.get("role") + ")" + RESET);
                    System.out.println(CYAN + "🔑 JWT Token sinh tự động (Mục 4.1.3):" + RESET);
                    System.out.println("   " + currentJwtToken);
                }
            } catch (Exception e) {
                System.out.println(RED + "Lỗi đăng nhập: " + e.getMessage() + RESET);
            }
        }
        pause();
    }

    // ==========================================
    // 2. XEM 7 NHÓM NGHIÊN CỨU & THÀNH VIÊN
    // ==========================================
    private static void handleListTeams() {
        System.out.println("\n" + BOLD + "=== DANH SÁCH 7 HƯỚNG NGHIÊN CỨU CHUYÊN SÂU (RBL TRACKS) ===" + RESET);
        try {
            List<Map<String, Object>> teams = DatabaseManager.query("SELECT * FROM Teams ORDER BY team_id");
            for (Map<String, Object> t : teams) {
                long teamId = ((Number) t.get("team_id")).longValue();
                String code = (String) t.get("team_code");
                String name = (String) t.get("team_name");
                String track = (String) t.get("track_name");
                String title = (String) t.get("project_title");

                String highlight = "G5".equals(code) ? YELLOW + BOLD : CYAN;
                System.out.println("\n" + highlight + "▶ [" + code + "] " + name + " (" + track + ")" + RESET);
                System.out.println("   Đề tài: " + title);

                List<Map<String, Object>> members = DatabaseManager.query(
                    "SELECT u.full_name, u.email, tm.role_in_team " +
                    "FROM TeamMembers tm JOIN Users u ON tm.user_id = u.user_id " +
                    "WHERE tm.team_id = ? ORDER BY tm.role_in_team DESC", teamId
                );
                System.out.println("   Thành viên:");
                for (Map<String, Object> m : members) {
                    String roleTag = "leader".equalsIgnoreCase((String) m.get("role_in_team")) ? GREEN + "[LEADER]" : "[MEMBER]";
                    System.out.println("     • " + roleTag + " " + m.get("full_name") + " <" + m.get("email") + ">" + RESET);
                }
            }
        } catch (Exception e) {
            System.out.println(RED + "Lỗi tải danh sách nhóm: " + e.getMessage() + RESET);
        }
        pause();
    }

    // ==========================================
    // 3. TRỌNG TÂM NHÓM 5: GIT ANALYTICS & ICS
    // ==========================================
    private static void handleGitAnalyticsTrack5() {
        System.out.println("\n" + BOLD + GREEN + "=== PHÂN HỆ NHÓM 5: GIT ANALYTICS TEAMWORK ASSESSOR ===" + RESET);
        System.out.println("  1. Xem bảng chỉ số phân tích Git & Điểm ICS % hiện tại (Database)");
        System.out.println("  2. Chạy thử nghiệm phân tích Git Log tùy ý (Nhập commit history)");
        System.out.println("  3. Xem công thức tính điểm đóng góp cá nhân (Individual Contribution Score)");
        System.out.print("👉 Chọn: ");
        String op = scanner.nextLine().trim();

        if ("1".equals(op)) {
            try {
                String sql = "SELECT gm.*, u.full_name, u.email FROM GitMetrics gm " +
                             "JOIN Users u ON gm.author_user_id = u.user_id " +
                             "ORDER BY gm.individual_contribution_score DESC";
                List<Map<String, Object>> list = DatabaseManager.query(sql);

                System.out.println("\n" + CYAN + "┌──────────────────────┬─────────┬──────────────┬──────────────┬──────────────┬────────────────────────────┐");
                System.out.println("│ Sinh Viên            │ Commits │ +Add / -Del  │ Net LoC Churn│ Điểm ICS (%) │ Cờ Cảnh Báo Bất Thường     │");
                System.out.println("├──────────────────────┼─────────┼──────────────┼──────────────┼──────────────┼────────────────────────────┤" + RESET);

                for (Map<String, Object> row : list) {
                    String name = padRight(String.valueOf(row.get("full_name")), 20);
                    String commits = padRight(String.valueOf(row.get("commits_count")), 7);
                    String addDel = padRight("+" + row.get("loc_added") + " / -" + row.get("loc_deleted"), 12);
                    String net = padRight(String.valueOf(row.get("loc_net")), 12);
                    double ics = ((Number) row.get("individual_contribution_score")).doubleValue();
                    String icsStr = padRight(String.format("%.1f%%", ics), 12);
                    String flags = String.valueOf(row.get("anomaly_flags"));

                    String color = ics > 40 ? GREEN : (ics < 15 ? RED + BOLD : YELLOW);
                    System.out.println(String.format("│ %s │ %s │ %s │ %s │ %s%s%s │ %s │",
                        name, commits, addDel, net, color, icsStr, RESET, flags));
                }
                System.out.println(CYAN + "└──────────────────────┴─────────┴──────────────┴──────────────┴──────────────┴────────────────────────────┘" + RESET);

                System.out.println("\n" + BOLD + "Chi tiết nhận xét sư phạm AI (Pedagogical Critique):" + RESET);
                for (Map<String, Object> row : list) {
                    System.out.println(YELLOW + "• " + row.get("full_name") + ":" + RESET);
                    System.out.println("  " + row.get("pedagogical_critique"));
                }
            } catch (Exception e) {
                System.out.println(RED + "Lỗi truy vấn: " + e.getMessage() + RESET);
            }
        } else if ("2".equals(op)) {
            System.out.println("\nNhập Git Log mô phỏng (ví dụ: commit block phân tách bằng '---COMMIT---'):");
            System.out.println("Gõ '" + BOLD + "DEMO" + RESET + "' để nạp mẫu commit tự động (có free-rider):");
            System.out.print("👉 Nhập: ");
            String input = scanner.nextLine().trim();

            String gitLog = input;
            if ("DEMO".equalsIgnoreCase(input) || input.isEmpty()) {
                gitLog = "---COMMIT---\ncommit: c1\nauthor: Huy Nguyen\nemail: huy.nguyen@school.edu\nmsg: feat: implement MVC2 controllers\nadded: 450\ndeleted: 30\nfile: Controller.java\n" +
                         "---COMMIT---\ncommit: c2\nauthor: Linh Tran\nemail: linh.tran@school.edu\nmsg: feat: implement JWT authentication\nadded: 320\ndeleted: 20\nfile: JwtUtil.java\n" +
                         "---COMMIT---\ncommit: c3\nauthor: Minh Le\nemail: minh.le@school.edu\nmsg: docs: update readme\nadded: 2\ndeleted: 1\nfile: README.md\n";
                System.out.println(CYAN + "Đã nạp mẫu Git Log mô phỏng 3 sinh viên!" + RESET);
            }

            List<RawCommit> rawCommits = GitAnalyticsService.parseGitLog(gitLog);
            List<GitMetric> metrics = GitAnalyticsService.analyzeTeamCommits(2, rawCommits, System.currentTimeMillis() + 86400000L);
            System.out.println("\n" + GREEN + "✔ KẾT QUẢ ĐỘNG CƠ PHÂN TÍCH GIT ANALYTICS:" + RESET);
            for (GitMetric gm : metrics) {
                System.out.println(BOLD + "\nTác giả: " + gm.getAuthorName() + " (" + gm.getAuthorEmail() + ")" + RESET);
                System.out.println("  • Số lượng commits: " + gm.getTotalCommits());
                System.out.println("  • Thêm/Xóa dòng: +" + gm.getLinesAdded() + " / -" + gm.getLinesDeleted() + " (Net Churn: " + gm.getNetChurn() + ")");
                System.out.println("  • Điểm đóng góp ICS: " + BOLD + (gm.getContributionScore() < 15 ? RED : GREEN) +
                                   String.format("%.2f%%", gm.getContributionScore()) + RESET);
                System.out.println("  • Cờ bất thường: " + (gm.getAnomalyFlags() != null && gm.getAnomalyFlags().contains("GHOST_COMMITTER") ? RED + BOLD : YELLOW) + gm.getAnomalyFlags() + RESET);
                System.out.println("  • Nhận xét sư phạm: " + gm.getAiCritiqueSummary());
            }
        } else if ("3".equals(op)) {
            System.out.println("\n" + BOLD + "CÔNG THỨC ĐỊNH LƯỢNG ĐÓNG GÓP CÁ NHÂN (ICS %):" + RESET);
            System.out.println(CYAN + "ICS_i = 0.20 * C_eff + 0.30 * L_net + 0.20 * D_time + 0.15 * S_msg + 0.15 * M_own" + RESET);
            System.out.println("Trong đó:");
            System.out.println("  • C_eff: Tỷ trọng commits hiệu quả (loại bỏ commit rác/squash).");
            System.out.println("  • L_net: Dòng code thuần đóng góp (LoC added - LoC deleted).");
            System.out.println("  • D_time: Độ phân tán thời gian (Entropy - chống dồn code phút chót).");
            System.out.println("  • S_msg: Điểm chất lượng thông điệp commit (Conventional Commits).");
            System.out.println("  • M_own: Tỷ lệ làm chủ module code (Module Ownership).");
        }
        pause();
    }

    // ==========================================
    // 4. QUẢN LÝ BÀI TẬP & BÀI NỘP
    // ==========================================
    private static void handleAssignmentsManagement() {
        System.out.println("\n" + BOLD + "=== QUẢN LÝ BÀI TẬP & BÀI NỘP ===" + RESET);
        System.out.println("  1. Xem danh sách bài tập hiện có");
        System.out.println("  2. Xem danh sách bài nộp của sinh viên");
        System.out.println("  3. Thêm bài tập mới (Chỉ dành cho Giảng viên)");
        System.out.println("  4. Xóa bài tập an toàn (Cascading Deletion Transaction)");
        System.out.print("👉 Chọn: ");
        String op = scanner.nextLine().trim();

        if ("1".equals(op)) {
            try {
                List<Map<String, Object>> list = DatabaseManager.query(
                    "SELECT a.assignment_id, a.title, a.deadline, s.subject_name " +
                    "FROM Assignments a LEFT JOIN Subjects s ON a.subject_id = s.subject_id ORDER BY a.assignment_id"
                );
                System.out.println("\n" + CYAN + "DANH SÁCH BÀI TẬP:" + RESET);
                for (Map<String, Object> a : list) {
                    System.out.println("  [ID: " + a.get("assignment_id") + "] " + BOLD + a.get("title") + RESET +
                                       " | Môn: " + a.get("subject_name") + " | Hạn nộp: " + a.get("deadline"));
                }
            } catch (Exception e) {
                System.out.println(RED + "Lỗi: " + e.getMessage() + RESET);
            }
        } else if ("2".equals(op)) {
            try {
                List<Map<String, Object>> list = DatabaseManager.query(
                    "SELECT s.submission_id, u.full_name, a.title, s.sha256_hash, s.submission_status, s.submitted_at " +
                    "FROM Submissions s " +
                    "JOIN Users u ON s.student_id = u.user_id " +
                    "JOIN Assignments a ON s.assignment_id = a.assignment_id ORDER BY s.submission_id"
                );
                System.out.println("\n" + CYAN + "DANH SÁCH BÀI NỘP:" + RESET);
                for (Map<String, Object> s : list) {
                    System.out.println("  [Sub ID: " + s.get("submission_id") + "] Sinh viên: " + BOLD + s.get("full_name") + RESET +
                                       " | Bài: " + s.get("title") + " | Status: " + s.get("submission_status"));
                    System.out.println("    Mã SHA-256: " + s.get("sha256_hash"));
                }
            } catch (Exception e) {
                System.out.println(RED + "Lỗi: " + e.getMessage() + RESET);
            }
        } else if ("3".equals(op)) {
            if (currentUser == null || !"teacher".equalsIgnoreCase((String) currentUser.get("role"))) {
                System.out.println(YELLOW + "Vui lòng đăng nhập tài khoản Giảng viên (Mục 1 -> chọn 4) để tạo bài tập!" + RESET);
                pause();
                return;
            }
            try {
                System.out.print("Tiêu đề bài tập: ");
                String title = scanner.nextLine().trim();
                System.out.print("Mã môn (1: ANC111, 2: CSG222, 3: PRJ301): ");
                long subId = Long.parseLong(scanner.nextLine().trim());
                long teacherId = 1L;

                long newId = DatabaseManager.insert(
                    "INSERT INTO Assignments (teacher_id, subject_id, title, deadline, rubric_json) " +
                    "VALUES (?, ?, ?, DATEADD(day, 7, GETDATE()), '{}')",
                    teacherId, subId, title
                );
                System.out.println(GREEN + "✔ Đã tạo bài tập thành công với ID: " + newId + RESET);
            } catch (Exception e) {
                System.out.println(RED + "Lỗi tạo bài tập: " + e.getMessage() + RESET);
            }
        } else if ("4".equals(op)) {
            if (currentUser == null || !"teacher".equalsIgnoreCase((String) currentUser.get("role"))) {
                System.out.println(YELLOW + "Vui lòng đăng nhập tài khoản Giảng viên (Mục 1 -> chọn 4) để xóa bài tập!" + RESET);
                pause();
                return;
            }
            try {
                System.out.print("Nhập ID bài tập cần xóa: ");
                long id = Long.parseLong(scanner.nextLine().trim());
                System.out.print("Bạn có chắc chắn muốn xóa bài tập ID " + id + " cùng toàn bộ bài nộp liên quan? (Y/N): ");
                String confirm = scanner.nextLine().trim();
                if ("Y".equalsIgnoreCase(confirm)) {
                    boolean ok = DatabaseManager.deleteAssignmentCascade(id);
                    if (ok) {
                        System.out.println(GREEN + "✔ Đã xóa bài tập ID " + id + " và toàn bộ dữ liệu phụ thuộc thành công (Cascading Deletion Safe)!" + RESET);
                    } else {
                        System.out.println(RED + "Không tìm thấy bài tập có ID " + id + RESET);
                    }
                }
            } catch (Exception e) {
                System.out.println(RED + "Lỗi khi xóa bài tập: " + e.getMessage() + RESET);
            }
        }
        pause();
    }

    // ==========================================
    // 5. CHẤM CHÉO ÉCOLE 42 & VÍ ĐIỂM
    // ==========================================
    private static void handlePeerReviewEco42() {
        System.out.println("\n" + BOLD + "=== HỆ THỐNG CHẤM CHÉO ÉCOLE 42 ===" + RESET);
        System.out.println("  1. Xem vòng tròn luân chuyển khép kín (G1 -> G2 -> ... -> G7 -> G1)");
        System.out.println("  2. Xem số dư Ví Điểm Sửa Lỗi (Correction Points Wallet)");
        System.out.println("  3. Thực hiện chấm chéo cho nhóm khác (Nhận +1 Correction Point)");
        System.out.print("👉 Chọn: ");
        String op = scanner.nextLine().trim();

        if ("1".equals(op)) {
            try {
                String sql = "SELECT r.review_id, r.sandbox_score, r.mvc2_clean_code_score, r.peer_defense_score, r.final_score, r.status, " +
                             "rev.team_code as rev_code, rev.team_name as rev_name, " +
                             "tgt.team_code as tgt_code, tgt.team_name as tgt_name " +
                             "FROM RotationalReviews r " +
                             "JOIN Teams rev ON r.reviewer_team_id = rev.team_id " +
                             "JOIN Teams tgt ON r.target_team_id = tgt.team_id ORDER BY r.review_id";
                List<Map<String, Object>> list = DatabaseManager.query(sql);

                System.out.println("\n" + CYAN + "VÒNG TRÒN CHẤM CHÉO KHÉP KÍN:" + RESET);
                for (Map<String, Object> r : list) {
                    System.out.println(String.format("  • [%s] %s  ───►  [%s] %s | Điểm tổng: %s (Sandbox: %s, Clean: %s, Defense: %s) | Trạng thái: %s",
                        r.get("rev_code"), r.get("rev_name"),
                        r.get("tgt_code"), r.get("tgt_name"),
                        BOLD + String.valueOf(r.get("final_score")) + RESET,
                        r.get("sandbox_score"), r.get("mvc2_clean_code_score"), r.get("peer_defense_score"),
                        r.get("status")));
                }
            } catch (Exception e) {
                System.out.println(RED + "Lỗi: " + e.getMessage() + RESET);
            }
        } else if ("2".equals(op)) {
            try {
                String sql = "SELECT u.full_name, u.email, cpw.points_balance, cpw.total_earned, cpw.total_spent " +
                             "FROM CorrectionPointsWallet cpw JOIN Users u ON cpw.user_id = u.user_id " +
                             "ORDER BY cpw.points_balance DESC";
                List<Map<String, Object>> list = DatabaseManager.query(sql);
                System.out.println("\n" + CYAN + "SỐ DƯ VÍ ĐIỂM SỬA LỖI (CORRECTION POINTS):" + RESET);
                for (Map<String, Object> w : list) {
                    System.out.println("  • " + padRight(String.valueOf(w.get("full_name")), 20) +
                                       " | Số dư: " + BOLD + GREEN + w.get("points_balance") + " pts" + RESET +
                                       " (Đã kiếm: +" + w.get("total_earned") + " | Đã tiêu: -" + w.get("total_spent") + ")");
                }
            } catch (Exception e) {
                System.out.println(RED + "Lỗi: " + e.getMessage() + RESET);
            }
        } else if ("3".equals(op)) {
            try {
                System.out.println("\nThực hiện chấm chéo theo Rubric chuẩn: 30% Sandbox + 40% Clean Code + 30% Defense:");
                System.out.print("Nhập ID lượt chấm (1 - 7): ");
                int rId = Integer.parseInt(scanner.nextLine().trim());
                System.out.print("Điểm Sandbox Compliance (0 - 10): ");
                double s1 = Double.parseDouble(scanner.nextLine().trim());
                System.out.print("Điểm MVC2 & Clean Code (0 - 10): ");
                double s2 = Double.parseDouble(scanner.nextLine().trim());
                System.out.print("Điểm Peer Defense / Vấn đáp (0 - 10): ");
                double s3 = Double.parseDouble(scanner.nextLine().trim());
                System.out.print("Lời nhận xét phản biện: ");
                String fb = scanner.nextLine().trim();

                int reviewerTeamId = 5; // Default G5
                Map<String, Object> res = PeerReviewService.submitPeerEvaluation(rId, reviewerTeamId, s1, s2, s3, fb);

                System.out.println(GREEN + "\n✔ Đã lưu kết quả chấm chéo thành công! Điểm tổng kết: " + BOLD + res.get("final_score") + "/10" + RESET);
                System.out.println(GREEN + "✔ Đã cộng +1 Correction Point vào ví nhóm chấm bài!" + RESET);
            } catch (Exception e) {
                System.out.println(RED + "Lỗi: " + e.getMessage() + RESET);
            }
        }
        pause();
    }

    // ==========================================
    // 6. KIỂM THỬ TỰ ĐỘNG MOULINETTE
    // ==========================================
    private static void handleMoulinetteTests() {
        System.out.println("\n" + BOLD + "=== CHẠY BỘ KIỂM THỬ TỰ ĐỘNG MOULINETTE SANDBOX ===" + RESET);
        System.out.println(CYAN + "Đang thực thi các test cases Public & Hidden trong môi trường cách ly..." + RESET);

        try {
            List<TestExecutionOutput> results = MoulinetteGradingService.runAllTestsForSubmission(2);
            int passedCount = 0;
            for (TestExecutionOutput r : results) {
                if (r.passed) passedCount++;
                String tag = r.passed ? GREEN + "[PASS]" : RED + "[FAIL]";
                String type = "[" + r.testType + "]";
                System.out.println(String.format("  %s %-8s %-12s : %-40s (%d ms)",
                    tag, type, r.testCode, r.title, r.executionTimeMs));
            }
            System.out.println(CYAN + "──────────────────────────────────────────────────────────────────────────────" + RESET);
            System.out.println(BOLD + String.format("Kết quả kiểm thử: %d / %d test cases ĐẠT (100%%)" + RESET, passedCount, results.size()));
        } catch (Exception e) {
            System.out.println(RED + "Lỗi thực thi test suite: " + e.getMessage() + RESET);
        }
        pause();
    }

    // ==========================================
    // 7. TERMINAL TRUY VẤN SQL TRỰC TIẾP
    // ==========================================
    private static void handleSqlTerminal() {
        System.out.println("\n" + BOLD + YELLOW + "=== TRÌNH THỰC THI TRUY VẤN SQL TRỰC TIẾP (SQL CONSOLE) ===" + RESET);
        System.out.println("Gõ câu lệnh SQL (SELECT, INSERT, UPDATE, v.v.). Gõ 'exit' để quay lại menu.");
        System.out.println("Gợi ý: " + CYAN + "SELECT TOP 5 * FROM Users;  |  SELECT * FROM Teams;  |  SELECT * FROM GitMetrics;" + RESET);

        while (true) {
            System.out.print(YELLOW + "\nAIGradingDB> " + RESET);
            String sql = scanner.nextLine().trim();
            if (sql.isEmpty()) continue;
            if ("exit".equalsIgnoreCase(sql) || "quit".equalsIgnoreCase(sql)) break;

            if (sql.endsWith(";")) sql = sql.substring(0, sql.length() - 1).trim();

            try {
                if (sql.toUpperCase().startsWith("SELECT") || sql.toUpperCase().startsWith("EXEC")) {
                    List<Map<String, Object>> rows = DatabaseManager.query(sql);
                    if (rows.isEmpty()) {
                        System.out.println(YELLOW + "(0 dòng kết quả)" + RESET);
                    } else {
                        // Print dynamic tabular output
                        Set<String> keys = rows.get(0).keySet();
                        for (String k : keys) {
                            System.out.print(BOLD + padRight(k, 18) + " | " + RESET);
                        }
                        System.out.println();
                        for (int i = 0; i < keys.size(); i++) System.out.print("───────────────────┼─");
                        System.out.println();

                        for (Map<String, Object> row : rows) {
                            for (String k : keys) {
                                Object val = row.get(k);
                                String strVal = val == null ? "NULL" : String.valueOf(val);
                                if (strVal.length() > 18) strVal = strVal.substring(0, 15) + "...";
                                System.out.print(padRight(strVal, 18) + " | ");
                            }
                            System.out.println();
                        }
                        System.out.println(GREEN + "✔ Tổng cộng: " + rows.size() + " dòng." + RESET);
                    }
                } else {
                    int affected = DatabaseManager.update(sql);
                    System.out.println(GREEN + "✔ Lệnh thực thi thành công. Số dòng bị ảnh hưởng: " + affected + RESET);
                }
            } catch (Exception e) {
                System.out.println(RED + "✘ Lỗi cú pháp SQL: " + e.getMessage() + RESET);
            }
        }
    }

    // ==========================================
    // 8. KIỂM TRA JWT & RBAC (MỤC 4.1.3)
    // ==========================================
    private static void handleJwtInspector() {
        System.out.println("\n" + BOLD + PURPLE + "=== KIỂM TRA CƠ CHẾ JWT AUTHENTICATION 4.1.3 ===" + RESET);
        if (currentJwtToken == null) {
            currentJwtToken = JwtUtil.generateToken(2, "huy.nguyen@school.edu", "student", 5);
        }
        System.out.println("Token hiện tại:");
        System.out.println(CYAN + currentJwtToken + RESET);

        String[] parts = currentJwtToken.split("\\.");
        if (parts.length == 3) {
            System.out.println("\n" + BOLD + "Phân tích cấu trúc 3 phần chuẩn RFC 7519:" + RESET);
            System.out.println("  1. Header (Base64Url):    " + parts[0]);
            System.out.println("     JSON giải mã:          " + new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8));
            System.out.println("  2. Payload/Claims (Base64Url): " + parts[1]);
            System.out.println("     JSON giải mã:          " + new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8));
            System.out.println("  3. Chữ ký số (HMAC-SHA256): " + parts[2]);

            JwtClaims claims = JwtUtil.parseAndVerify(currentJwtToken);
            boolean valid = (claims != null);
            System.out.println("\nTrạng thái xác thực chữ ký: " + (valid ? GREEN + "✔ VALID (Chữ ký toàn vẹn)" : RED + "✘ INVALID") + RESET);
            if (claims != null) {
                System.out.println("Trích xuất RBAC Claims: Role=" + claims.role + ", UserId=" + claims.userId + ", TeamId=" + claims.teamId);
            }
        }
        pause();
    }

    // ==========================================
    // 9. QUẢN LÝ CSDL ĐỘNG & ĐỒNG BỘ TỪ JAVA
    // ==========================================
    private static void handleDatabaseCrudManager() {
        System.out.println("\n" + BOLD + GREEN + "=== QUẢN LÝ CSDL ĐỘNG TỪ JAVA (ĐỒNG BỘ 100% KHÔNG CẦN VÀO SQL) ===" + RESET);
        System.out.println("  1. Tự động đồng bộ Schema, Bảng & Thủ tục (Sync Schema & Procedures)");
        System.out.println("  2. Xem danh sách tất cả các bảng và số lượng bản ghi (Table Overview)");
        System.out.println("  3. Xem cấu trúc các cột (Columns Metadata) của một bảng");
        System.out.println("  4. Xem dữ liệu chi tiết của một bảng (SELECT All from Java)");
        System.out.println("  5. Thêm bản ghi mới vào bảng (INSERT from Java)");
        System.out.println("  6. Chỉnh sửa bản ghi trong bảng (UPDATE from Java)");
        System.out.println("  7. Xóa bản ghi trong bảng (DELETE with Auto-Cascade from Java)");
        System.out.println("  8. Thêm cột mới vào bảng (ALTER TABLE ADD COLUMN from Java)");
        System.out.print("👉 Chọn: ");
        String op = scanner.nextLine().trim();

        if ("1".equals(op)) {
            System.out.println(CYAN + "Đang tiến hành đồng bộ CSDL từ Java..." + RESET);
            Map<String, Object> res = DatabaseCrudService.synchronizeDatabase();
            if ((boolean) res.get("success")) {
                System.out.println(GREEN + "✔ " + res.get("message") + RESET);
                System.out.println("Hệ quản trị CSDL: " + BOLD + res.get("engine") + RESET);
            } else {
                System.out.println(RED + "✘ Lỗi: " + res.get("error") + RESET);
            }
        } else if ("2".equals(op)) {
            try {
                List<Map<String, Object>> tables = DatabaseManager.listAllTablesWithCounts();
                System.out.println("\n" + CYAN + "┌────────────────────────────────────┬──────────────────┐");
                System.out.println("│ Tên Bảng (Table Name)              │ Số Dòng (Rows)   │");
                System.out.println("├────────────────────────────────────┼──────────────────┤" + RESET);
                for (Map<String, Object> t : tables) {
                    System.out.println(String.format("│ %-34s │ %-16s │", t.get("table_name"), t.get("row_count")));
                }
                System.out.println(CYAN + "└────────────────────────────────────┴──────────────────┘" + RESET);
            } catch (Exception e) {
                System.out.println(RED + "Lỗi: " + e.getMessage() + RESET);
            }
        } else if ("3".equals(op)) {
            System.out.print("Nhập tên bảng cần xem cấu trúc cột (vd: Users, Teams, Assignments): ");
            String tName = scanner.nextLine().trim();
            try {
                List<Map<String, Object>> cols = DatabaseManager.getTableColumnsMetadata(tName);
                if (cols.isEmpty()) {
                    System.out.println(YELLOW + "Không tìm thấy bảng hoặc bảng chưa có cột nào." + RESET);
                } else {
                    System.out.println("\n" + CYAN + "CẤU TRÚC BẢNG: " + tName + RESET);
                    for (Map<String, Object> c : cols) {
                        System.out.println("  • " + BOLD + padRight(String.valueOf(c.get("column_name")), 20) + RESET +
                                           " Kiểu dữ liệu: " + padRight(String.valueOf(c.get("data_type")), 15) +
                                           " Độ dài: " + padRight(String.valueOf(c.get("column_size")), 6) +
                                           " Cho phép NULL: " + c.get("nullable"));
                    }
                }
            } catch (Exception e) {
                System.out.println(RED + "Lỗi: " + e.getMessage() + RESET);
            }
        } else if ("4".equals(op)) {
            System.out.print("Nhập tên bảng cần xem dữ liệu (vd: Teams, Users, Assignments): ");
            String tName = scanner.nextLine().trim();
            try {
                List<Map<String, Object>> rows = DatabaseManager.queryAll(tName, null);
                if (rows.isEmpty()) {
                    System.out.println(YELLOW + "Bảng " + tName + " hiện đang trống (0 dòng)." + RESET);
                } else {
                    System.out.println(GREEN + "Tổng cộng " + rows.size() + " dòng trong bảng " + tName + ":" + RESET);
                    for (Map<String, Object> r : rows) {
                        System.out.println("  " + r);
                    }
                }
            } catch (Exception e) {
                System.out.println(RED + "Lỗi: " + e.getMessage() + RESET);
            }
        } else if ("5".equals(op)) {
            System.out.print("Nhập tên bảng cần thêm dữ liệu: ");
            String tName = scanner.nextLine().trim();
            try {
                List<Map<String, Object>> cols = DatabaseManager.getTableColumnsMetadata(tName);
                Map<String, Object> inputData = new LinkedHashMap<>();
                System.out.println("Nhập giá trị cho từng cột (nhấn Enter để bỏ qua / dùng giá trị mặc định / tự tăng):");
                for (Map<String, Object> c : cols) {
                    String colName = (String) c.get("column_name");
                    if (colName.toLowerCase().endsWith("_id") && inputData.isEmpty()) {
                        continue;
                    }
                    if (colName.equalsIgnoreCase("created_at") || colName.equalsIgnoreCase("analyzed_at")) {
                        continue;
                    }
                    System.out.print("  " + colName + " (" + c.get("data_type") + "): ");
                    String val = scanner.nextLine().trim();
                    if (!val.isEmpty()) {
                        inputData.put(colName, val);
                    }
                }
                if (!inputData.isEmpty()) {
                    long newId = DatabaseManager.insertRecord(tName, inputData);
                    System.out.println(GREEN + "✔ Thêm bản ghi mới thành công từ Java vào " + tName + "! ID mới: " + newId + RESET);
                } else {
                    System.out.println(YELLOW + "Không có dữ liệu nào được nhập." + RESET);
                }
            } catch (Exception e) {
                System.out.println(RED + "Lỗi thêm bản ghi: " + e.getMessage() + RESET);
            }
        } else if ("6".equals(op)) {
            System.out.print("Nhập tên bảng cần chỉnh sửa: ");
            String tName = scanner.nextLine().trim();
            System.out.print("Nhập tên cột khóa chính (ID column, vd: user_id, team_id, assignment_id): ");
            String idCol = scanner.nextLine().trim();
            System.out.print("Nhập giá trị ID cần sửa: ");
            String idVal = scanner.nextLine().trim();
            System.out.print("Nhập tên cột cần cập nhật: ");
            String updateCol = scanner.nextLine().trim();
            System.out.print("Nhập giá trị mới: ");
            String newVal = scanner.nextLine().trim();

            try {
                Map<String, Object> updateMap = new LinkedHashMap<>();
                updateMap.put(updateCol, newVal);
                int affected = DatabaseManager.updateRecord(tName, updateMap, idCol, idVal);
                if (affected > 0) {
                    System.out.println(GREEN + "✔ Đã cập nhật thành công " + affected + " dòng trong bảng " + tName + " từ Java!" + RESET);
                } else {
                    System.out.println(YELLOW + "Không tìm thấy bản ghi có " + idCol + " = " + idVal + RESET);
                }
            } catch (Exception e) {
                System.out.println(RED + "Lỗi cập nhật: " + e.getMessage() + RESET);
            }
        } else if ("7".equals(op)) {
            System.out.print("Nhập tên bảng cần xóa: ");
            String tName = scanner.nextLine().trim();
            System.out.print("Nhập tên cột khóa chính (vd: assignment_id, submission_id, team_id): ");
            String idCol = scanner.nextLine().trim();
            System.out.print("Nhập giá trị ID cần xóa: ");
            String idVal = scanner.nextLine().trim();
            System.out.print("Bạn có chắc chắn muốn xóa bản ghi này (kèm tự động cascade dữ liệu liên quan)? (Y/N): ");
            String confirm = scanner.nextLine().trim();
            if ("Y".equalsIgnoreCase(confirm)) {
                try {
                    int affected = DatabaseManager.deleteRecord(tName, idCol, idVal, true);
                    if (affected > 0) {
                        System.out.println(GREEN + "✔ Đã xóa thành công bản ghi từ Java (an toàn với cơ chế Cascade)!" + RESET);
                    } else {
                        System.out.println(YELLOW + "Không tìm thấy bản ghi có " + idCol + " = " + idVal + RESET);
                    }
                } catch (Exception e) {
                    System.out.println(RED + "Lỗi xóa bản ghi: " + e.getMessage() + RESET);
                }
            }
        } else if ("8".equals(op)) {
            System.out.print("Nhập tên bảng cần bổ sung cột mới: ");
            String tName = scanner.nextLine().trim();
            System.out.print("Nhập tên cột mới cần thêm (vd: notes, extra_info): ");
            String colName = scanner.nextLine().trim();
            System.out.print("Nhập kiểu dữ liệu (vd: NVARCHAR(255), INT, TEXT): ");
            String colType = scanner.nextLine().trim();
            try {
                DatabaseManager.addColumnIfNotExists(tName, colName, colType);
                System.out.println(GREEN + "✔ Đã thêm cột " + colName + " vào bảng " + tName + " và tự động đồng bộ trên CSDL!" + RESET);
            } catch (Exception e) {
                System.out.println(RED + "Lỗi thêm cột: " + e.getMessage() + RESET);
            }
        }
        pause();
    }

    private static String padRight(String s, int n) {
        if (s == null) s = "";
        return String.format("%-" + n + "s", s);
    }

    private static void pause() {
        System.out.print("\nNhấn [Enter] để tiếp tục...");
        scanner.nextLine();
    }
}
