package tests;

import com.aigrading.db.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class Sync5MembersToSQL {

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("   ĐỒNG BỘ CSDL VỚI 5 HỌ TÊN CHÍNH THỨC CỦA NHÓM 5 (PRJ301):");
        System.out.println("   1. Võ Ngọc Phú     (Team Leader - Architecture & JDBC)");
        System.out.println("   2. Trần Hoàng Thịnh (Backend Developer - Servlets & Exception Paths)");
        System.out.println("   3. Đặng Xuân Thiện (Frontend Developer - JSP, JSTL & UI)");
        System.out.println("   4. Lương Đức Duy   (Database Engineer - SQL Server & DAO Layer)");
        System.out.println("   5. Châu Minh Tiến  (QA & Security Engineer - Filters & Tests)");
        System.out.println("================================================================================\n");

        // 1. Sync to Microsoft SQL Server directly
        System.out.println("👉 [BƯỚC 1]: Đang kết nối và đồng bộ Microsoft SQL Server (localhost\\MAYAO - AIGradingDB)...");
        DatabaseManager.init(null);
        syncCurrentDatabase();

        // 2. Sync to embedded SQLite
        System.out.println("\n👉 [BƯỚC 2]: Đang đồng bộ cơ sở dữ liệu SQLite cục bộ (database/aigrading.db)...");
        DatabaseManager.init("database/aigrading.db");
        syncCurrentDatabase();

        System.out.println("\n================================================================================");
        System.out.println("   ✅ HOÀN TẤT ĐỒNG BỘ 100% CẢ MICROSOFT SQL SERVER VÀ SQLITE!");
        System.out.println("================================================================================");
    }

    private static void syncCurrentDatabase() {
        boolean isSqlServer = DatabaseManager.isSqlServer();
        String passHash = "b2a1f4fd0a460606b34c8913e2981dac8d2e283d778aba586c416ee2629bfa54";

        try (Connection conn = DatabaseManager.getConnection()) {
            // 1. Upsert 5 Members into Users with EXACT FULL NAMES
            upsertUser(conn, isSqlServer, 5, "Võ Ngọc Phú (Lead G5)", "phuvn@school.edu", passHash, "student");
            upsertUser(conn, isSqlServer, 6, "Trần Hoàng Thịnh (Dev G5)", "thinhth@school.edu", passHash, "student");
            upsertUser(conn, isSqlServer, 7, "Đặng Xuân Thiện (Dev G5)", "thiendx@school.edu", passHash, "student");
            upsertUser(conn, isSqlServer, 8, "Lương Đức Duy (DB G5)", "duyld@school.edu", passHash, "student");
            upsertUser(conn, isSqlServer, 9, "Châu Minh Tiến (QA G5)", "tiencm@school.edu", passHash, "student");

            // 2. Upsert Students
            upsertStudent(conn, isSqlServer, 5, "STU-10905", "SE1701");
            upsertStudent(conn, isSqlServer, 6, "STU-10906", "SE1701");
            upsertStudent(conn, isSqlServer, 7, "STU-10907", "SE1701");
            upsertStudent(conn, isSqlServer, 8, "STU-10908", "SE1701");
            upsertStudent(conn, isSqlServer, 9, "STU-10909", "SE1701");

            // 3. Clear and re-populate TeamMembers for G5 (team_id = 5)
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(isSqlServer ? "DELETE FROM [TeamMembers] WHERE [team_id] = 5" : "DELETE FROM TeamMembers WHERE team_id = 5");
            }

            insertTeamMember(conn, isSqlServer, 5, 5, "leader");
            insertTeamMember(conn, isSqlServer, 5, 6, "member");
            insertTeamMember(conn, isSqlServer, 5, 7, "member");
            insertTeamMember(conn, isSqlServer, 5, 8, "member");
            insertTeamMember(conn, isSqlServer, 5, 9, "member");

            // 4. Verify & Print
            System.out.println("   └─ [XÁC MINH CSDL] Danh sách 5 thành viên Nhóm 5 trong bảng Users:");
            String querySql = isSqlServer ?
                "SELECT user_id, full_name, email, role FROM [Users] WHERE [user_id] IN (5,6,7,8,9) ORDER BY [user_id]" :
                "SELECT user_id, full_name, email, role FROM Users WHERE user_id IN (5,6,7,8,9) ORDER BY user_id";

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(querySql)) {
                while (rs.next()) {
                    System.out.printf("      • User #%d: %-30s | %-25s | Role: %s%n",
                            rs.getInt("user_id"),
                            rs.getString("full_name"),
                            rs.getString("email"),
                            rs.getString("role"));
                }
            }

            // Verify TeamMembers count
            String countSql = isSqlServer ?
                "SELECT COUNT(*) FROM [TeamMembers] WHERE [team_id] = 5" :
                "SELECT COUNT(*) FROM TeamMembers WHERE team_id = 5";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(countSql)) {
                if (rs.next()) {
                    System.out.println("   └─ [XÁC MINH CSDL] Số lượng thành viên Team G5 (team_id=5): " + rs.getInt(1) + " thành viên.");
                }
            }

        } catch (Exception e) {
            System.err.println("❌ Lỗi đồng bộ: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void upsertUser(Connection conn, boolean isSqlServer, int userId, String name, String email, String hash, String role) throws Exception {
        boolean exists = false;
        String checkSql = isSqlServer ? "SELECT 1 FROM [Users] WHERE [user_id] = ?" : "SELECT 1 FROM Users WHERE user_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) exists = true;
            }
        }

        if (exists) {
            String updateSql = isSqlServer ?
                "UPDATE [Users] SET [full_name] = ?, [email] = ? WHERE [user_id] = ?" :
                "UPDATE Users SET full_name = ?, email = ? WHERE user_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                ps.setString(1, name);
                ps.setString(2, email);
                ps.setInt(3, userId);
                ps.executeUpdate();
            }
        } else {
            if (isSqlServer) {
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("SET IDENTITY_INSERT [Users] ON");
                }
                String insertSql = "INSERT INTO [Users] ([user_id], [full_name], [email], [password_hash], [role]) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                    ps.setInt(1, userId);
                    ps.setString(2, name);
                    ps.setString(3, email);
                    ps.setString(4, hash);
                    ps.setString(5, role);
                    ps.executeUpdate();
                }
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("SET IDENTITY_INSERT [Users] OFF");
                }
            } else {
                String insertSql = "INSERT INTO Users (user_id, full_name, email, password_hash, role) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                    ps.setInt(1, userId);
                    ps.setString(2, name);
                    ps.setString(3, email);
                    ps.setString(4, hash);
                    ps.setString(5, role);
                    ps.executeUpdate();
                }
            }
        }
    }

    private static void upsertStudent(Connection conn, boolean isSqlServer, int userId, String studentCode, String className) throws Exception {
        boolean exists = false;
        String checkSql = isSqlServer ? "SELECT 1 FROM [Students] WHERE [user_id] = ?" : "SELECT 1 FROM Students WHERE user_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) exists = true;
            }
        }

        if (!exists) {
            String insertSql = isSqlServer ?
                "INSERT INTO [Students] ([user_id], [student_code], [class_name]) VALUES (?, ?, ?)" :
                "INSERT INTO Students (user_id, student_code, class_name) VALUES (?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                ps.setInt(1, userId);
                ps.setString(2, studentCode);
                ps.setString(3, className);
                ps.executeUpdate();
            }
        }
    }

    private static void insertTeamMember(Connection conn, boolean isSqlServer, int teamId, int userId, String role) throws Exception {
        String insertSql = isSqlServer ?
            "INSERT INTO [TeamMembers] ([team_id], [user_id], [role_in_team]) VALUES (?, ?, ?)" :
            "INSERT INTO TeamMembers (team_id, user_id, role_in_team) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
            ps.setInt(1, teamId);
            ps.setInt(2, userId);
            ps.setString(3, role);
            ps.executeUpdate();
        }
    }
}
