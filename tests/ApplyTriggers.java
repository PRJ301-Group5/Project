package tests;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class ApplyTriggers {
    public static void main(String[] args) {
        String url = "jdbc:sqlserver://localhost:61370;databaseName=AIGradingDB;integratedSecurity=true;trustServerCertificate=true;";
        System.out.println("Connecting to SQL Server AIGradingDB to apply triggers...");
        try (Connection conn = DriverManager.getConnection(url)) {
            File sqlFile = new File("database/apply_cascade_triggers.sql");
            String content = new String(Files.readAllBytes(sqlFile.toPath()), StandardCharsets.UTF_8);
            String[] batches = content.split("(?i)\\r?\\nGO\\r?\\n");
            try (Statement stmt = conn.createStatement()) {
                for (String batch : batches) {
                    String sql = batch.trim();
                    if (!sql.isEmpty() && !sql.equalsIgnoreCase("USE AIGradingDB;")) {
                        try {
                            stmt.execute(sql);
                            System.out.println("[SUCCESS] Executed SQL batch successfully.");
                        } catch (Exception e) {
                            System.err.println("[ERROR] Failed batch: " + e.getMessage());
                        }
                    }
                }
            }
            System.out.println(">>> All cascading triggers applied to SQL Server AIGradingDB!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
