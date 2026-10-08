package com.aigrading.db;

import java.sql.*;

public class TestConn {
    public static void main(String[] args) {
        String[] urls = {
            "jdbc:sqlserver://localhost:61370;databaseName=AIGradingDB;integratedSecurity=true;trustServerCertificate=true;",
            "jdbc:sqlserver://localhost;instanceName=MAYAO;databaseName=AIGradingDB;integratedSecurity=true;trustServerCertificate=true;"
        };

        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (Exception e) {
            System.err.println("Driver load failed: " + e.getMessage());
            return;
        }

        for (String url : urls) {
            System.out.println("Attempting connection to: " + url);
            try (Connection conn = DriverManager.getConnection(url)) {
                System.out.println("SUCCESS! Connected to SQL Server AIGradingDB!");
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM Users")) {
                    if (rs.next()) {
                        System.out.println("Current user count in SQL Server AIGradingDB: " + rs.getInt(1));
                    }
                }
                return;
            } catch (Exception e) {
                System.err.println("Failed with: " + e.getMessage());
            }
        }
    }
}
