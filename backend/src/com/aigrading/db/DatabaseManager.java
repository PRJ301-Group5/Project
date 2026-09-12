package com.aigrading.db;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.*;
import java.util.*;

public class DatabaseManager {
    private static final String DEFAULT_SQLSERVER_URL = "jdbc:sqlserver://127.0.0.1:61370;databaseName=AIGradingDB;integratedSecurity=true;trustServerCertificate=true;encrypt=false;loginTimeout=5;";
    private static final String DEFAULT_SQLITE_PATH = "database/aigrading.db";
    private static String dbUrl;
    private static boolean isSqlServer = false;

    static {
        try {
            java.util.logging.Logger.getLogger("com.microsoft.sqlserver.jdbc").setLevel(java.util.logging.Level.OFF);
        } catch (Throwable ignored) {}
    }

    public static synchronized void init(String customDbPath) {
        String envDb = System.getenv("DB_URL");
        if (envDb != null && !envDb.isEmpty()) {
            dbUrl = envDb;
        }

        // Check if SQLite is explicitly requested or path ends with .db
        boolean isExplicitSqlite = customDbPath != null && (customDbPath.endsWith(".db") || customDbPath.startsWith("jdbc:sqlite"));
        String forceSqlite = System.getenv("FORCE_SQLITE");
        if (!isExplicitSqlite && (forceSqlite == null || !"true".equalsIgnoreCase(forceSqlite))) {
            try {
                Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                String sqlServerUrl = (customDbPath != null && customDbPath.startsWith("jdbc:sqlserver")) ? customDbPath : DEFAULT_SQLSERVER_URL;
                try (Connection conn = DriverManager.getConnection(sqlServerUrl)) {
                    dbUrl = sqlServerUrl;
                    isSqlServer = true;
                    System.out.println("[DatabaseManager] SUCCESS: Connected directly to Microsoft SQL Server AIGradingDB (localhost\\MAYAO)!");
                    syncSqlServerSchema(conn);
                    return;
                }
            } catch (Throwable e) {
                System.out.println("[DatabaseManager] SQL Server connection attempt info: " + e.getMessage() + ". Defaulting to SQLite.");
            }
        }

        // Fallback or explicit SQLite mode
        try {
            Class.forName("org.sqlite.JDBC");
            String path = (customDbPath != null && !customDbPath.isEmpty()) ? customDbPath : DEFAULT_SQLITE_PATH;
            File dbFile = new File(path);
            if (dbFile.getParentFile() != null) {
                dbFile.getParentFile().mkdirs();
            }
            boolean isNew = !dbFile.exists() || dbFile.length() == 0;
            dbUrl = "jdbc:sqlite:" + path.replace('\\', '/');
            isSqlServer = false;

            try (Connection conn = getConnection()) {
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("PRAGMA foreign_keys = ON;");
                    stmt.execute("PRAGMA journal_mode = WAL;");
                }
            }

            // 1. Run Auto-migrations for SQLite BEFORE schema scripts
            try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
                try {
                    stmt.execute("ALTER TABLE Assignments ADD COLUMN prompt_file_url TEXT;");
                } catch (SQLException ignored) {}
                try {
                    stmt.execute("ALTER TABLE Submissions ADD COLUMN zip_file_path TEXT;");
                } catch (SQLException ignored) {}
                try {
                    stmt.execute("ALTER TABLE Submissions ADD COLUMN sha256_hash TEXT;");
                } catch (SQLException ignored) {}
                try {
                    stmt.execute("ALTER TABLE Submissions ADD COLUMN artifact_type TEXT DEFAULT 'screenshot';");
                } catch (SQLException ignored) {}
                try {
                    stmt.execute("ALTER TABLE Submissions ADD COLUMN team_id INTEGER REFERENCES Teams(team_id);");
                } catch (SQLException ignored) {}
                try {
                    stmt.execute("ALTER TABLE Submissions ADD COLUMN queue_priority INTEGER DEFAULT 1;");
                } catch (SQLException ignored) {}
                try {
                    stmt.execute("ALTER TABLE Submissions ADD COLUMN retry_count INTEGER DEFAULT 0;");
                } catch (SQLException ignored) {}
                try {
                    stmt.execute("CREATE TABLE IF NOT EXISTS Classes (class_id INTEGER PRIMARY KEY AUTOINCREMENT, class_name TEXT NOT NULL UNIQUE, department TEXT NOT NULL, teacher_id INTEGER REFERENCES Teachers(teacher_id) ON DELETE SET NULL, created_at DATETIME DEFAULT CURRENT_TIMESTAMP);");
                } catch (SQLException ignored) {}
            }

            // 2. Run Schema & Seed Scripts
            runSqlScript("database/schema.sql");
            runSqlScript("database/seed.sql");
            System.out.println("[DatabaseManager] Database initialized and synchronized with schema and seed data at: " + path);

        } catch (Exception e) {
            System.err.println("[DatabaseManager] SQLite initialization error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static boolean isSqlServer() {
        return isSqlServer;
    }

    public static Connection getConnection() throws SQLException {
        if (dbUrl == null) {
            init(null);
        }
        return DriverManager.getConnection(dbUrl);
    }

    public static void runSqlScript(String scriptPath) {
        File file = new File(scriptPath);
        if (!file.exists()) return;
        try {
            byte[] bytes = Files.readAllBytes(file.toPath());
            String sqlContent = new String(bytes, StandardCharsets.UTF_8);
            StringBuilder cleanSql = new StringBuilder();
            for (String line : sqlContent.split("\\r?\\n")) {
                String trimmed = line.trim();
                if (!trimmed.startsWith("--")) {
                    cleanSql.append(line).append("\n");
                }
            }
            String[] statements = cleanSql.toString().split(";");
            try (Connection conn = getConnection()) {
                try (Statement stmt = conn.createStatement()) {
                    for (String sql : statements) {
                        sql = sql.trim();
                        if (!sql.isEmpty()) {
                            try {
                                stmt.execute(sql);
                            } catch (SQLException e) {
                                // Ignore duplicate table/index or already present constraints
                                String msg = e.getMessage().toLowerCase();
                                if (!msg.contains("already exists") && !msg.contains("duplicate") && !msg.contains("unique constraint")) {
                                    System.err.println("[DatabaseManager] Script notice (" + scriptPath + "): " + e.getMessage());
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[DatabaseManager] Error running script " + scriptPath + ": " + e.getMessage());
        }
    }

    public static String adaptSql(String sql) {
        if (!isSqlServer || sql == null) return sql;
        String res = sql;
        res = res.replace("datetime('now', '+5 minutes')", "DATEADD(minute, 5, GETDATE())");
        res = res.replace("datetime('now', '+1 days')", "DATEADD(day, 1, GETDATE())");
        res = res.replace("datetime('now', '+2 days')", "DATEADD(day, 2, GETDATE())");
        res = res.replace("datetime('now', '+3 days')", "DATEADD(day, 3, GETDATE())");
        res = res.replace("datetime('now', '+5 days')", "DATEADD(day, 5, GETDATE())");
        res = res.replace("datetime('now', '+7 days')", "DATEADD(day, 7, GETDATE())");
        res = res.replace("datetime('now')", "GETDATE()");

        if (res.trim().endsWith("LIMIT 1")) {
            String trimmed = res.trim();
            trimmed = trimmed.substring(0, trimmed.length() - "LIMIT 1".length()).trim();
            if (trimmed.toUpperCase().startsWith("SELECT ")) {
                res = "SELECT TOP 1 " + trimmed.substring(7);
            }
        }
        return res;
    }

    public static List<Map<String, Object>> query(String sql, Object... params) throws SQLException {
        List<Map<String, Object>> results = new ArrayList<>();
        String adaptedSql = adaptSql(sql);
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(adaptedSql)) {
            setParams(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData meta = rs.getMetaData();
                int colCount = meta.getColumnCount();
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    for (int i = 1; i <= colCount; i++) {
                        row.put(meta.getColumnLabel(i), rs.getObject(i));
                    }
                    results.add(row);
                }
            }
        }
        return results;
    }

    public static Map<String, Object> queryOne(String sql, Object... params) throws SQLException {
        List<Map<String, Object>> rows = query(sql, params);
        return rows.isEmpty() ? null : rows.get(0);
    }

    public static int update(String sql, Object... params) throws SQLException {
        String adaptedSql = adaptSql(sql);
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(adaptedSql)) {
            setParams(ps, params);
            return ps.executeUpdate();
        }
    }

    public static long insert(String sql, Object... params) throws SQLException {
        String adaptedSql = adaptSql(sql);
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(adaptedSql, Statement.RETURN_GENERATED_KEYS)) {
            setParams(ps, params);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        return -1;
    }

    public static boolean execute(String sql) throws SQLException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            return stmt.execute(adaptSql(sql));
        }
    }

    public static int executeUpdate(String sql) throws SQLException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            return stmt.executeUpdate(adaptSql(sql));
        }
    }

    public static boolean deleteAssignmentCascade(long assignmentId) throws SQLException {
        try (Connection conn = getConnection()) {
            boolean originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                // 1. Detach RotationalReviews so foreign keys are not violated
                try (PreparedStatement ps = conn.prepareStatement(adaptSql(
                    "UPDATE RotationalReviews SET submission_id = NULL WHERE submission_id IN (SELECT submission_id FROM Submissions WHERE assignment_id = ?)"))) {
                    ps.setLong(1, assignmentId);
                    ps.executeUpdate();
                }

                // 2. Delete GitMetrics
                try (PreparedStatement ps = conn.prepareStatement(adaptSql(
                    "DELETE FROM GitMetrics WHERE submission_id IN (SELECT submission_id FROM Submissions WHERE assignment_id = ?)"))) {
                    ps.setLong(1, assignmentId);
                    ps.executeUpdate();
                }

                // 3. Delete AIAnalysisResults
                try (PreparedStatement ps = conn.prepareStatement(adaptSql(
                    "DELETE FROM AIAnalysisResults WHERE submission_id IN (SELECT submission_id FROM Submissions WHERE assignment_id = ?)"))) {
                    ps.setLong(1, assignmentId);
                    ps.executeUpdate();
                }

                // 4. Delete MoulinetteResults
                try (PreparedStatement ps = conn.prepareStatement(adaptSql(
                    "DELETE FROM MoulinetteResults WHERE submission_id IN (SELECT submission_id FROM Submissions WHERE assignment_id = ?)"))) {
                    ps.setLong(1, assignmentId);
                    ps.executeUpdate();
                }

                // 5. Delete Appeals for grades of this assignment
                try (PreparedStatement ps = conn.prepareStatement(adaptSql(
                    "DELETE FROM Appeals WHERE grade_id IN (SELECT grade_id FROM Grades WHERE submission_id IN (SELECT submission_id FROM Submissions WHERE assignment_id = ?))"))) {
                    ps.setLong(1, assignmentId);
                    ps.executeUpdate();
                }

                // 6. Delete Grades
                try (PreparedStatement ps = conn.prepareStatement(adaptSql(
                    "DELETE FROM Grades WHERE submission_id IN (SELECT submission_id FROM Submissions WHERE assignment_id = ?)"))) {
                    ps.setLong(1, assignmentId);
                    ps.executeUpdate();
                }

                // 7. Delete Submissions
                try (PreparedStatement ps = conn.prepareStatement(adaptSql(
                    "DELETE FROM Submissions WHERE assignment_id = ?"))) {
                    ps.setLong(1, assignmentId);
                    ps.executeUpdate();
                }

                // 8. Delete Assignment
                int affected;
                try (PreparedStatement ps = conn.prepareStatement(adaptSql(
                    "DELETE FROM Assignments WHERE assignment_id = ?"))) {
                    ps.setLong(1, assignmentId);
                    affected = ps.executeUpdate();
                }

                conn.commit();
                return affected > 0;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(originalAutoCommit);
            }
        }
    }

    public static boolean deleteSubmissionCascade(long submissionId) throws SQLException {
        try (Connection conn = getConnection()) {
            boolean originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                // 1. Detach RotationalReviews
                try (PreparedStatement ps = conn.prepareStatement(adaptSql(
                    "UPDATE RotationalReviews SET submission_id = NULL WHERE submission_id = ?"))) {
                    ps.setLong(1, submissionId);
                    ps.executeUpdate();
                }

                // 2. Delete GitMetrics
                try (PreparedStatement ps = conn.prepareStatement(adaptSql(
                    "DELETE FROM GitMetrics WHERE submission_id = ?"))) {
                    ps.setLong(1, submissionId);
                    ps.executeUpdate();
                }

                // 3. Delete AIAnalysisResults
                try (PreparedStatement ps = conn.prepareStatement(adaptSql(
                    "DELETE FROM AIAnalysisResults WHERE submission_id = ?"))) {
                    ps.setLong(1, submissionId);
                    ps.executeUpdate();
                }

                // 4. Delete MoulinetteResults
                try (PreparedStatement ps = conn.prepareStatement(adaptSql(
                    "DELETE FROM MoulinetteResults WHERE submission_id = ?"))) {
                    ps.setLong(1, submissionId);
                    ps.executeUpdate();
                }

                // 5. Delete Appeals
                try (PreparedStatement ps = conn.prepareStatement(adaptSql(
                    "DELETE FROM Appeals WHERE grade_id IN (SELECT grade_id FROM Grades WHERE submission_id = ?)"))) {
                    ps.setLong(1, submissionId);
                    ps.executeUpdate();
                }

                // 6. Delete Grades
                try (PreparedStatement ps = conn.prepareStatement(adaptSql(
                    "DELETE FROM Grades WHERE submission_id = ?"))) {
                    ps.setLong(1, submissionId);
                    ps.executeUpdate();
                }

                // 7. Delete Submission
                int affected;
                try (PreparedStatement ps = conn.prepareStatement(adaptSql(
                    "DELETE FROM Submissions WHERE submission_id = ?"))) {
                    ps.setLong(1, submissionId);
                    affected = ps.executeUpdate();
                }

                conn.commit();
                return affected > 0;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(originalAutoCommit);
            }
        }
    }

    private static void setParams(PreparedStatement ps, Object... params) throws SQLException {
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
        }
    }

    // ==========================================================
    // AUTO SCHEMA SYNCHRONIZATION & MIGRATION (NO SQL NEEDED)
    // ==========================================================

    public static void syncSqlServerSchema(Connection conn) {
        try {
            boolean tablesExist = false;
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'Users'")) {
                if (rs.next() && rs.getInt(1) > 0) {
                    tablesExist = true;
                }
            }

            if (!tablesExist) {
                System.out.println("[DatabaseManager] Tables missing in SQL Server. Auto-initializing schema & seed data from Java...");
                runSqlServerScript(conn, "database/sqlserver_schema_and_seed.sql");
            } else {
                runSqlServerScript(conn, "database/apply_cascade_triggers.sql");
            }

            // Auto-check and add missing columns
            addColumnIfNotExists(conn, "Assignments", "prompt_file_url", "VARCHAR(500)");
            addColumnIfNotExists(conn, "Submissions", "zip_file_path", "VARCHAR(500)");
            addColumnIfNotExists(conn, "Submissions", "sha256_hash", "VARCHAR(64)");
            addColumnIfNotExists(conn, "Submissions", "artifact_type", "VARCHAR(20) DEFAULT 'screenshot'");
            addColumnIfNotExists(conn, "Submissions", "queue_priority", "INT DEFAULT 1");
            addColumnIfNotExists(conn, "Submissions", "retry_count", "INT DEFAULT 0");

            System.out.println("[DatabaseManager] Microsoft SQL Server schema & procedures synchronized successfully from Java!");
        } catch (Exception e) {
            System.err.println("[DatabaseManager] SQL Server auto-sync notice: " + e.getMessage());
        }
    }

    public static void runSqlServerScript(Connection conn, String scriptPath) {
        File file = new File(scriptPath);
        if (!file.exists()) return;
        try {
            byte[] bytes = Files.readAllBytes(file.toPath());
            String sqlContent = new String(bytes, StandardCharsets.UTF_8);
            String[] batches = sqlContent.split("(?i)\\r?\\nGO\\r?\\n");
            try (Statement stmt = conn.createStatement()) {
                for (String batch : batches) {
                    String sql = batch.trim();
                    if (sql.isEmpty()) continue;
                    if (sql.equalsIgnoreCase("USE master;") || sql.equalsIgnoreCase("USE AIGradingDB;")) continue;
                    if (sql.toUpperCase().startsWith("USE ")) {
                        int idx = sql.indexOf(";");
                        if (idx != -1) sql = sql.substring(idx + 1).trim();
                    }
                    if (sql.isEmpty()) continue;
                    try {
                        stmt.execute(sql);
                    } catch (SQLException e) {
                        String msg = e.getMessage().toLowerCase();
                        if (!msg.contains("already exists") && !msg.contains("duplicate") && !msg.contains("there is already an object")) {
                            // Non-fatal
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[DatabaseManager] Error running SQL Server script " + scriptPath + ": " + e.getMessage());
        }
    }

    public static void syncAllDatabaseStructures() {
        try (Connection conn = getConnection()) {
            if (isSqlServer) {
                syncSqlServerSchema(conn);
            } else {
                runSqlScript("database/schema.sql");
                runSqlScript("database/seed.sql");
            }
            System.out.println("[DatabaseManager] Database synchronized successfully from Java!");
        } catch (Exception e) {
            System.err.println("[DatabaseManager] Database sync error: " + e.getMessage());
        }
    }

    // ==========================================================
    // DYNAMIC CRUD OPERATIONS DIRECTLY FROM JAVA
    // ==========================================================

    public static long insertRecord(String tableName, Map<String, Object> fields) throws SQLException {
        if (fields == null || fields.isEmpty()) {
            throw new IllegalArgumentException("Fields map cannot be empty");
        }
        StringBuilder cols = new StringBuilder();
        StringBuilder placeholders = new StringBuilder();
        List<Object> values = new ArrayList<>();
        for (Map.Entry<String, Object> entry : fields.entrySet()) {
            if (cols.length() > 0) {
                cols.append(", ");
                placeholders.append(", ");
            }
            cols.append(isSqlServer ? "[" + entry.getKey() + "]" : entry.getKey());
            placeholders.append("?");
            values.add(entry.getValue());
        }
        String sql = "INSERT INTO " + (isSqlServer ? "[" + tableName + "]" : tableName) +
                     " (" + cols + ") VALUES (" + placeholders + ")";
        return insert(sql, values.toArray());
    }

    public static int updateRecord(String tableName, Map<String, Object> fields, String idColumn, Object idValue) throws SQLException {
        if (fields == null || fields.isEmpty()) {
            throw new IllegalArgumentException("Fields map cannot be empty");
        }
        StringBuilder setClause = new StringBuilder();
        List<Object> values = new ArrayList<>();
        for (Map.Entry<String, Object> entry : fields.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(idColumn)) continue;
            if (setClause.length() > 0) {
                setClause.append(", ");
            }
            setClause.append(isSqlServer ? "[" + entry.getKey() + "] = ?" : entry.getKey() + " = ?");
            values.add(entry.getValue());
        }
        values.add(idValue);
        String sql = "UPDATE " + (isSqlServer ? "[" + tableName + "]" : tableName) +
                     " SET " + setClause + " WHERE " + (isSqlServer ? "[" + idColumn + "]" : idColumn) + " = ?";
        return update(sql, values.toArray());
    }

    public static int deleteRecord(String tableName, String idColumn, Object idValue, boolean cascade) throws SQLException {
        if (cascade && tableName.equalsIgnoreCase("Assignments") && idColumn.equalsIgnoreCase("assignment_id")) {
            long assignId = ((Number) idValue).longValue();
            return deleteAssignmentCascade(assignId) ? 1 : 0;
        }
        if (cascade && tableName.equalsIgnoreCase("Submissions") && idColumn.equalsIgnoreCase("submission_id")) {
            long subId = ((Number) idValue).longValue();
            return deleteSubmissionCascade(subId) ? 1 : 0;
        }
        String sql = "DELETE FROM " + (isSqlServer ? "[" + tableName + "]" : tableName) +
                     " WHERE " + (isSqlServer ? "[" + idColumn + "]" : idColumn) + " = ?";
        return update(sql, idValue);
    }

    public static List<Map<String, Object>> queryAll(String tableName, String orderBy) throws SQLException {
        String sql = "SELECT * FROM " + (isSqlServer ? "[" + tableName + "]" : tableName);
        if (orderBy != null && !orderBy.trim().isEmpty()) {
            sql += " ORDER BY " + orderBy;
        }
        return query(sql);
    }

    public static List<Map<String, Object>> listAllTablesWithCounts() throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        List<String> tableNames = new ArrayList<>();
        try (Connection conn = getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            String[] types = {"TABLE"};
            try (ResultSet rs = meta.getTables(null, null, "%", types)) {
                while (rs.next()) {
                    String name = rs.getString("TABLE_NAME");
                    if (!name.startsWith("sqlite_") && !name.startsWith("trace_xe_") && !name.startsWith("sys")) {
                        tableNames.add(name);
                    }
                }
            }
        }
        Collections.sort(tableNames);
        for (String t : tableNames) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("table_name", t);
            try {
                String countSql = isSqlServer ? "SELECT COUNT(*) AS total FROM [" + t + "]" : "SELECT COUNT(*) AS total FROM " + t;
                Map<String, Object> countRow = queryOne(countSql);
                item.put("row_count", countRow != null ? countRow.get("total") : 0);
            } catch (Exception e) {
                item.put("row_count", -1);
            }
            list.add(item);
        }
        return list;
    }

    public static List<Map<String, Object>> getTableColumnsMetadata(String tableName) throws SQLException {
        List<Map<String, Object>> columns = new ArrayList<>();
        try (Connection conn = getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            try (ResultSet rs = meta.getColumns(null, null, tableName, null)) {
                while (rs.next()) {
                    Map<String, Object> col = new LinkedHashMap<>();
                    col.put("column_name", rs.getString("COLUMN_NAME"));
                    col.put("data_type", rs.getString("TYPE_NAME"));
                    col.put("column_size", rs.getInt("COLUMN_SIZE"));
                    col.put("nullable", rs.getInt("NULLABLE") == DatabaseMetaData.columnNullable);
                    columns.add(col);
                }
            }
        }
        return columns;
    }

    public static void addColumnIfNotExists(Connection conn, String tableName, String columnName, String columnType) {
        try {
            DatabaseMetaData meta = conn.getMetaData();
            boolean exists = false;
            try (ResultSet rs = meta.getColumns(null, null, tableName, columnName)) {
                if (rs.next()) {
                    exists = true;
                }
            }
            if (!exists) {
                try (Statement stmt = conn.createStatement()) {
                    String sql = isSqlServer ?
                        "ALTER TABLE [" + tableName + "] ADD [" + columnName + "] " + columnType :
                        "ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + columnType;
                    stmt.execute(sql);
                    System.out.println("[DatabaseManager] Auto-migrated: Added column " + columnName + " to " + tableName);
                }
            }
        } catch (Exception ignored) {}
    }

    public static void addColumnIfNotExists(String tableName, String columnName, String columnType) {
        try (Connection conn = getConnection()) {
            addColumnIfNotExists(conn, tableName, columnName, columnType);
        } catch (Exception ignored) {}
    }
}
