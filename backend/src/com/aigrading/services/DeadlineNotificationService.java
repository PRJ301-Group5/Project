package com.aigrading.services;

import com.aigrading.db.DatabaseManager;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.*;

public class DeadlineNotificationService {
    private static ScheduledExecutorService scheduler;
    private static final List<Map<String, Object>> NOTIFICATION_LOG = new CopyOnWriteArrayList<>();

    public static synchronized void start() {
        if (scheduler != null && !scheduler.isShutdown()) return;
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "Deadline-Notifier-Thread");
            t.setDaemon(true);
            return t;
        });

        // Run check every 30 seconds
        scheduler.scheduleAtFixedRate(DeadlineNotificationService::checkUpcomingDeadlines, 5, 30, TimeUnit.SECONDS);
        System.out.println("[DeadlineNotificationService] Started automatic deadline notification worker.");
    }

    public static synchronized void stop() {
        if (scheduler != null) {
            scheduler.shutdownNow();
            scheduler = null;
        }
    }

    public static List<Map<String, Object>> getPendingNotificationsForStudent(long studentId) {
        try {
            // Find assignments due within 48 hours for subjects the student is enrolled in, where no submission exists
            String sql = "SELECT a.assignment_id, a.title, a.deadline, a.prototype_url, " +
                         "s.subject_code, s.subject_name " +
                         "FROM Assignments a " +
                         "JOIN Subjects s ON a.subject_id = s.subject_id " +
                         "JOIN StudentSubjects ss ON s.subject_id = ss.subject_id " +
                         "WHERE ss.student_id = ? " +
                         "AND a.deadline > datetime('now') " +
                         "AND a.deadline <= datetime('now', '+2 days') " +
                         "AND a.assignment_id NOT IN (SELECT assignment_id FROM Submissions WHERE student_id = ?) " +
                         "ORDER BY a.deadline ASC";

            return DatabaseManager.query(sql, studentId, studentId);
        } catch (SQLException e) {
            System.err.println("[DeadlineNotificationService] Error querying student notifications: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public static void checkUpcomingDeadlines() {
        try {
            // Query imminent assignments (due in <= 24 hours) with students without submissions
            String sql = "SELECT a.assignment_id, a.title, a.deadline, " +
                         "st.student_id, st.student_code, u.full_name, u.email " +
                         "FROM Assignments a " +
                         "JOIN Subjects sub ON a.subject_id = sub.subject_id " +
                         "JOIN StudentSubjects ss ON sub.subject_id = ss.subject_id " +
                         "JOIN Students st ON ss.student_id = st.student_id " +
                         "JOIN Users u ON st.user_id = u.user_id " +
                         "WHERE a.deadline > datetime('now') " +
                         "AND a.deadline <= datetime('now', '+1 days') " +
                         "AND a.assignment_id NOT IN (SELECT assignment_id FROM Submissions WHERE student_id = st.student_id)";

            List<Map<String, Object>> pending = DatabaseManager.query(sql);
            for (Map<String, Object> item : pending) {
                String email = (String) item.get("email");
                String title = (String) item.get("title");
                String student = (String) item.get("full_name");
                String deadline = String.valueOf(item.get("deadline"));

                Map<String, Object> record = new LinkedHashMap<>();
                record.put("student_id", item.get("student_id"));
                record.put("email", email);
                record.put("student_name", student);
                record.put("assignment_title", title);
                record.put("deadline", deadline);
                record.put("timestamp", new Date().toString());
                record.put("message", "URGENT DEADLINE: Assignment '" + title + "' is due soon at " + deadline + ". No submission found yet.");

                // Add to log if not already logged recently
                boolean exists = false;
                for (Map<String, Object> old : NOTIFICATION_LOG) {
                    if (Objects.equals(old.get("student_id"), item.get("student_id")) &&
                        Objects.equals(old.get("assignment_title"), title)) {
                        exists = true;
                        break;
                    }
                }
                if (!exists) {
                    NOTIFICATION_LOG.add(record);
                    System.out.println("[Deadline Alert Email] To: " + email + " (" + student + ") | " + record.get("message"));
                }
            }
        } catch (Exception e) {
            System.err.println("[DeadlineNotificationService] Error checking deadlines: " + e.getMessage());
        }
    }

    public static List<Map<String, Object>> getAllNotificationLogs() {
        return new ArrayList<>(NOTIFICATION_LOG);
    }
}
