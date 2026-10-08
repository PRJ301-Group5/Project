package com.aigrading;

import com.aigrading.db.DatabaseManager;
import com.aigrading.server.RequestHandler;
import com.aigrading.services.DeadlineNotificationService;
import com.sun.net.httpserver.HttpServer;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) {
        int port = 8080;
        String envPort = System.getenv("PORT");
        if (envPort != null) {
            try {
                port = Integer.parseInt(envPort);
            } catch (NumberFormatException ignored) {}
        }
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {}
        }

        String dbPath = System.getenv("DB_PATH");

        System.out.println("==================================================");
        System.out.println("   AI Grading System - Java Backend Starting     ");
        System.out.println("==================================================");

        // 1. Initialize SQLite Database
        DatabaseManager.init(dbPath);

        // 2. Start Background Services
        DeadlineNotificationService.start();
        com.aigrading.services.RedisJobQueueService.start();

        // 3. Initialize Peer-Review Rotational Cycle (G1 -> G2 -> ... -> G7 -> G1)
        try {
            com.aigrading.services.PeerReviewService.initializeRotationalCycle();
        } catch (Exception e) {
            System.err.println("[Main] Peer review rotation init note: " + e.getMessage());
        }

        // 4. Start HTTP Server
        try {
            new File("uploads").mkdirs();
            new File("uploads/submissions").mkdirs();
            new File("frontend").mkdirs();

            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/", new RequestHandler("frontend", "uploads"));
            server.setExecutor(Executors.newFixedThreadPool(16));
            server.start();

            System.out.println("[Server] AI Grading Tool Backend running at: http://localhost:" + port);
            System.out.println("[Server] Web Frontend available at: http://localhost:" + port + "/");
            System.out.println("[Server] REST API endpoints available at: http://localhost:" + port + "/api/");
            System.out.println("==================================================");

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\n[Server] Shutting down gracefully...");
                server.stop(1);
                DeadlineNotificationService.stop();
                com.aigrading.services.RedisJobQueueService.stop();
            }));

        } catch (Exception e) {
            System.err.println("[Server] Failed to start HTTP server: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
