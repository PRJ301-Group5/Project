package com.aigrading;

import com.aigrading.db.DatabaseManager;
import com.aigrading.services.DatabaseCrudService;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.*;

/**
 * ====================================================================================
 *    TRÌNH THỰC THI DATABASE TRỰC TIẾP TRÊN IDE (VS CODE / INTELLIJ / ECLIPSE)
 * ====================================================================================
 * 
 * 💡 HƯỚNG DẪN DÀNH CHO BẠN:
 * 1. Bạn có thể tự do viết bất kỳ lệnh SQL nào (INSERT, UPDATE, DELETE, SELECT, CREATE, ALTER...)
 *    ngay trong hàm main() bên dưới hoặc trong file "database/my_script.sql".
 * 2. Để chạy: Nhấn nút [Run] ngay trên hàm main() trong VS Code, hoặc bấm F5 
 *    (chọn "▶ Tự Chạy Lệnh Database (DatabasePlayground)"), hoặc chạy file "run_database_script.bat".
 * 3. Tất cả lệnh sẽ tự động đồng bộ trực tiếp vào Microsoft SQL Server (AIGradingDB)
 *    mà bạn KHÔNG CẦN phải mở hay chỉnh sửa gì trong SQL Server Management Studio (SSMS)!
 */
public class DatabasePlayground {

    public static void main(String[] args) {
        // Tự động kết nối tới SQL Server (hoặc SQLite fallback) và đồng bộ
        DatabaseManager.init(null);

        System.out.println("================================================================================");
        System.out.println("       TRÌNH THỰC THI DATABASE TRỰC TIẾP TRÊN IDE (VS CODE / JAVA)");
        System.out.println("   Đang kết nối: " + (DatabaseManager.isSqlServer() ? "Microsoft SQL Server (localhost\\MAYAO - AIGradingDB)" : "SQLite (Embedded)"));
        System.out.println("================================================================================\n");

        try {
            // =========================================================================
            // 👇 BẠN HÃY TỰ VIẾT HOẶC MỞ COMMENT CÁC LỆNH BẠN MUỐN THỰC THI Ở ĐÂY 👇
            // =========================================================================

            // --- VÍ DỤ 1: TỰ VIẾT LỆNH THÊM (INSERT) DỮ LIỆU ---
            // execute("INSERT INTO Teams (team_code, team_name, track_name, project_title, repo_url) " +
            //         "VALUES ('G8', 'Cloud DevOps Team', 'DevOps Track', 'CI/CD Pipelines', 'https://github.com/team-g8/devops')");

            // --- VÍ DỤ 2: TỰ VIẾT LỆNH SỬA (UPDATE) DỮ LIỆU ---
            // execute("UPDATE Teams SET team_name = 'Cloud DevOps & Security Masters' WHERE team_code = 'G8'");

            // --- VÍ DỤ 3: TỰ VIẾT LỆNH XÓA (DELETE) DỮ LIỆU ---
            // execute("DELETE FROM Teams WHERE team_code = 'G8'");

            // --- VÍ DỤ 4: TỰ VIẾT LỆNH TẠO BẢNG HOẶC THÊM CỘT (DDL) ---
            // execute("ALTER TABLE Teams ADD notes NVARCHAR(255) NULL");

            // --- VÍ DỤ 5: TRUY VẤN VÀ IN DỮ LIỆU DẠNG BẢNG ĐẸP MẮT ---
            System.out.println("📊 DANH SÁCH CÁC NHÓM TRONG HỆ THỐNG:");
            queryAndPrint("SELECT team_id, team_code, team_name, track_name, repo_url FROM Teams");

            System.out.println("\n👥 DANH SÁCH NGƯỜI DÙNG (USERS):");
            queryAndPrint("SELECT user_id, full_name, email, role, created_at FROM Users");

            // --- VÍ DỤ 6: CHẠY TOÀN BỘ FILE SQL BÊN NGOÀI (database/my_script.sql) ---
            // Bạn có thể mở file database/my_script.sql, gõ lệnh SQL thoải mái rồi mở dòng dưới:
            executeFile("database/my_script.sql");

            // =========================================================================
            // 👆 HẾT PHẦN LỆNH CỦA BẠN 👆
            // =========================================================================

            System.out.println("\n✅ Tất cả lệnh của bạn đã được thực thi và đồng bộ thành công vào SQL Server!");
        } catch (Exception e) {
            System.err.println("\n❌ LỖI KHI THỰC THI LỆNH:");
            e.printStackTrace();
        }
    }

    // ====================================================================================
    // CÁC HÀM TIỆN ÍCH HỖ TRỢ BẠN THỰC THI DATABASE TRÊN IDE NHANH CHÓNG
    // ====================================================================================

    /**
     * Thực thi bất kỳ câu lệnh SQL thay đổi dữ liệu nào: INSERT, UPDATE, DELETE, CREATE, ALTER...
     * Tự động in kết quả số dòng bị tác động ra màn hình IDE.
     */
    public static void execute(String sql) throws SQLException {
        System.out.println("👉 [EXECUTE SQL]: " + sql);
        int affected = DatabaseManager.executeUpdate(sql);
        System.out.println("   └─ Thành công! Số dòng bị tác động: " + affected + "\n");
    }

    /**
     * Thực thi câu lệnh SELECT và in kết quả ra dưới dạng bảng (ASCII table) chuyên nghiệp.
     */
    public static void queryAndPrint(String sql) throws SQLException {
        List<Map<String, Object>> rows = DatabaseManager.query(sql);
        if (rows.isEmpty()) {
            System.out.println("   (Không có bản ghi nào phù hợp)\n");
            return;
        }

        // Lấy danh sách cột
        List<String> headers = new ArrayList<>(rows.get(0).keySet());
        Map<String, Integer> colWidths = new LinkedHashMap<>();

        for (String h : headers) {
            colWidths.put(h, h.length());
        }

        for (Map<String, Object> row : rows) {
            for (String h : headers) {
                Object val = row.get(h);
                String str = (val == null) ? "NULL" : val.toString();
                if (str.length() > 40) str = str.substring(0, 37) + "...";
                if (str.length() > colWidths.get(h)) {
                    colWidths.put(h, str.length());
                }
            }
        }

        // Vẽ đường phân cách
        StringBuilder separator = new StringBuilder("+");
        for (String h : headers) {
            int w = colWidths.get(h);
            for (int i = 0; i < w + 2; i++) separator.append("-");
            separator.append("+");
        }

        // In tiêu đề
        System.out.println(separator);
        System.out.print("|");
        for (String h : headers) {
            int w = colWidths.get(h);
            System.out.printf(" %-" + w + "s |", h);
        }
        System.out.println();
        System.out.println(separator);

        // In từng dòng dữ liệu
        for (Map<String, Object> row : rows) {
            System.out.print("|");
            for (String h : headers) {
                int w = colWidths.get(h);
                Object val = row.get(h);
                String str = (val == null) ? "NULL" : val.toString();
                if (str.length() > 40) str = str.substring(0, 37) + "...";
                System.out.printf(" %-" + w + "s |", str);
            }
            System.out.println();
        }
        System.out.println(separator);
        System.out.println("   Tổng cộng: " + rows.size() + " dòng.\n");
    }

    /**
     * Đọc và thực thi toàn bộ câu lệnh trong một file .sql (ví dụ: database/my_script.sql)
     */
    public static void executeFile(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            return;
        }
        System.out.println("📂 Đang nạp và thực thi script SQL từ file: " + filePath + "...");
        try {
            byte[] bytes = Files.readAllBytes(file.toPath());
            String content = new String(bytes, StandardCharsets.UTF_8).trim();
            if (content.isEmpty()) {
                System.out.println("   └─ (File trống, bỏ qua)\n");
                return;
            }

            // Lọc bỏ các dòng chú thích (-- ...)
            StringBuilder cleanSql = new StringBuilder();
            for (String line : content.split("\\r?\\n")) {
                String lineTrim = line.trim();
                if (lineTrim.startsWith("--") || lineTrim.startsWith("/*")) {
                    continue;
                }
                cleanSql.append(line).append("\n");
            }

            // Tách theo GO hoặc dấu chấm phẩy ;
            String[] commands = cleanSql.toString().split("(?i)(?:;\\s*(?:\\r?\\n|$)|\\r?\\nGO\\s*(?:\\r?\\n|$))");
            int count = 0;
            for (String cmd : commands) {
                String trimmed = cmd.trim();
                if (trimmed.isEmpty()) continue;
                if (trimmed.equalsIgnoreCase("USE master") || trimmed.equalsIgnoreCase("USE AIGradingDB")) continue;
                try {
                    if (trimmed.toUpperCase().startsWith("SELECT ")) {
                        System.out.println("👉 [SQL FILE QUERY]: " + trimmed);
                        queryAndPrint(trimmed);
                    } else {
                        execute(trimmed);
                    }
                    count++;
                } catch (Exception ex) {
                    System.err.println("   ⚠️ Lỗi ở lệnh [" + trimmed + "]: " + ex.getMessage());
                }
            }
            System.out.println("   └─ Hoàn thành thực thi " + count + " lệnh trong file: " + filePath + "!\n");
        } catch (Exception e) {
            System.err.println("   ❌ Lỗi khi đọc file SQL: " + e.getMessage());
        }
    }
}
