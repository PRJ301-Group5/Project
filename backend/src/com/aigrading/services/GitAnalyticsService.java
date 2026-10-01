package com.aigrading.services;

import com.aigrading.db.DatabaseManager;
import com.aigrading.model.GitMetric;
import com.aigrading.util.JsonHelper;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.*;

/**
 * Git Analytics Teamwork Assessor (Nhóm 5)
 * Analyzes commit history, computes lines of code churn, time dispersion entropy,
 * semantic commit quality, module ownership, and Individual Contribution Score (ICS).
 * Detects Free-riding anomalies and integrates with AI for pedagogical reporting.
 */
public class GitAnalyticsService {

    public static class RawCommit {
        public String hash;
        public String authorName;
        public String authorEmail;
        public long timestamp;
        public String message;
        public int linesAdded;
        public int linesDeleted;
        public List<String> filesChanged = new ArrayList<>();

        public RawCommit(String hash, String authorName, String authorEmail, long timestamp, String message, int linesAdded, int linesDeleted) {
            this.hash = hash;
            this.authorName = authorName;
            this.authorEmail = authorEmail;
            this.timestamp = timestamp;
            this.message = message;
            this.linesAdded = linesAdded;
            this.linesDeleted = linesDeleted;
        }
    }

    /**
     * Parse raw git log or simulated git commit stream into RawCommit objects.
     */
    public static List<RawCommit> parseGitLog(String gitLogText) {
        List<RawCommit> commits = new ArrayList<>();
        if (gitLogText == null || gitLogText.trim().isEmpty()) return commits;

        String[] blocks = gitLogText.split("---COMMIT---");
        for (String block : blocks) {
            String trimmed = block.trim();
            if (trimmed.isEmpty()) continue;

            String hash = "c_" + UUID.randomUUID().toString().substring(0, 8);
            String author = "Unknown";
            String email = "";
            long timestamp = System.currentTimeMillis();
            String msg = "";
            int added = 0;
            int deleted = 0;
            List<String> files = new ArrayList<>();

            String[] lines = trimmed.split("\\r?\\n");
            for (String line : lines) {
                line = line.trim();
                if (line.startsWith("commit:")) hash = line.substring(7).trim();
                else if (line.startsWith("author:")) author = line.substring(7).trim();
                else if (line.startsWith("email:")) email = line.substring(6).trim();
                else if (line.startsWith("date:")) {
                    try { timestamp = Long.parseLong(line.substring(5).trim()); } catch (Exception ignored) {}
                }
                else if (line.startsWith("message:")) msg = line.substring(8).trim();
                else if (line.startsWith("added:")) {
                    try { added = Integer.parseInt(line.substring(6).trim()); } catch (Exception ignored) {}
                }
                else if (line.startsWith("deleted:")) {
                    try { deleted = Integer.parseInt(line.substring(8).trim()); } catch (Exception ignored) {}
                }
                else if (line.startsWith("file:")) files.add(line.substring(5).trim());
            }

            RawCommit rc = new RawCommit(hash, author, email, timestamp, msg, added, deleted);
            rc.filesChanged = files;
            commits.add(rc);
        }
        return commits;
    }

    /**
     * Compute individual contribution scores (ICS) and anomalies across all members.
     */
    public static List<GitMetric> analyzeTeamCommits(int submissionId, List<RawCommit> commits, long deadlineTimestamp) {
        Map<String, List<RawCommit>> authorMap = new LinkedHashMap<>();
        for (RawCommit c : commits) {
            String key = c.authorName.trim();
            authorMap.computeIfAbsent(key, k -> new ArrayList<>()).add(c);
        }

        List<GitMetric> metricsList = new ArrayList<>();
        if (authorMap.isEmpty()) return metricsList;

        int totalTeamCommits = commits.size();
        int totalTeamAdded = 0;
        int totalTeamDeleted = 0;
        for (RawCommit c : commits) {
            totalTeamAdded += c.linesAdded;
            totalTeamDeleted += c.linesDeleted;
        }

        // First pass: Calculate raw parameters for each author
        for (Map.Entry<String, List<RawCommit>> entry : authorMap.entrySet()) {
            String author = entry.getKey();
            List<RawCommit> authorCommits = entry.getValue();

            int authorTotalCommits = authorCommits.size();
            int authorAdded = 0;
            int authorDeleted = 0;
            int conventionalCommitCount = 0;
            int coreModuleTouchCount = 0;
            int last12HourCommits = 0;

            // Track time distribution in 4 buckets (weeks/phases)
            int[] timeBuckets = new int[4];
            long minTime = Long.MAX_VALUE;
            long maxTime = Long.MIN_VALUE;
            for (RawCommit c : authorCommits) {
                if (c.timestamp < minTime) minTime = c.timestamp;
                if (c.timestamp > maxTime) maxTime = c.timestamp;
            }
            long span = Math.max(1, maxTime - minTime);

            for (RawCommit c : authorCommits) {
                authorAdded += c.linesAdded;
                authorDeleted += c.linesDeleted;

                // Check conventional commits
                String msgLower = c.message.toLowerCase();
                if (msgLower.startsWith("feat:") || msgLower.startsWith("fix:") ||
                    msgLower.startsWith("refactor:") || msgLower.startsWith("test:") ||
                    msgLower.startsWith("docs:") || msgLower.startsWith("chore:")) {
                    conventionalCommitCount++;
                }

                // Check core module touches
                for (String f : c.filesChanged) {
                    String fl = f.toLowerCase();
                    if (fl.contains("servlet") || fl.contains("service") || fl.contains("dao") ||
                        fl.contains("filter") || fl.contains("controller") || fl.contains(".java")) {
                        coreModuleTouchCount++;
                        break;
                    }
                }

                // Check 12-hour panic commit rule
                if (deadlineTimestamp > 0 && (deadlineTimestamp - c.timestamp) <= (12 * 3600 * 1000L) && (deadlineTimestamp >= c.timestamp)) {
                    last12HourCommits++;
                }

                int bucketIdx = (int) Math.min(3, ((c.timestamp - minTime) * 4) / span);
                timeBuckets[bucketIdx]++;
            }

            // Calculate entropy (evenness of commit schedule)
            double entropy = 0.0;
            for (int count : timeBuckets) {
                if (count > 0) {
                    double p = (double) count / authorTotalCommits;
                    entropy -= p * (Math.log(p) / Math.log(4)); // Base 4 logarithm, max = 1.0
                }
            }
            entropy = Math.max(0.1, Math.min(1.0, entropy));

            // Semantic message quality (0 - 100%)
            double semanticScore = authorTotalCommits > 0 ? 
                Math.round(((double) conventionalCommitCount / authorTotalCommits) * 1000.0) / 10.0 : 0.0;

            // Module ownership ratio
            double moduleOwnership = authorTotalCommits > 0 ? 
                Math.round(((double) coreModuleTouchCount / authorTotalCommits) * 100.0) / 100.0 : 0.0;

            int netChurn = (int) (authorAdded * 0.8 - authorDeleted * 0.2);

            // Anomaly Detection
            List<String> anomalyList = new ArrayList<>();
            if (authorTotalCommits <= 2 && authorAdded < 50) {
                anomalyList.add("GHOST_COMMITTER");
            }
            if (authorTotalCommits > 0 && ((double) last12HourCommits / authorTotalCommits) >= 0.75) {
                anomalyList.add("11TH_HOUR_RUSHER");
            }
            if (authorAdded > 200 && semanticScore < 30 && moduleOwnership < 0.1) {
                anomalyList.add("FORMATTING_SPAMMER");
            }

            GitMetric m = new GitMetric();
            m.setSubmissionId(submissionId);
            m.setAuthorName(author);
            m.setAuthorEmail(authorCommits.get(0).authorEmail);
            m.setTotalCommits(authorTotalCommits);
            m.setLinesAdded(authorAdded);
            m.setLinesDeleted(authorDeleted);
            m.setNetChurn(netChurn);
            m.setCommitEntropy(Math.round(entropy * 100.0) / 100.0);
            m.setSemanticScore(semanticScore);
            m.setModuleOwnership(moduleOwnership);
            m.setAnomalyFlags(JsonHelper.toJson(anomalyList));
            metricsList.add(m);
        }

        // Second pass: Calculate normalized Individual Contribution Score (ICS %)
        double totalWeightedPoints = 0.0;
        for (GitMetric m : metricsList) {
            double commitFactor = (double) m.getTotalCommits() / Math.max(1, totalTeamCommits);
            double churnFactor = totalTeamAdded > 0 ? (double) m.getLinesAdded() / totalTeamAdded : 0.0;
            double entropyFactor = m.getCommitEntropy();
            double semanticFactor = m.getSemanticScore() / 100.0;
            double ownershipFactor = m.getModuleOwnership();

            double weightedScore = (0.20 * commitFactor) + (0.30 * churnFactor) + 
                                  (0.20 * entropyFactor) + (0.15 * semanticFactor) + (0.15 * ownershipFactor);
            
            // Penalty if free-riding flags exist
            if (m.getAnomalyFlags() != null && m.getAnomalyFlags().contains("GHOST_COMMITTER")) {
                weightedScore *= 0.5;
            }
            m.setContributionScore(weightedScore);
            totalWeightedPoints += weightedScore;
        }

        // Normalize so sum of ICS across team = 100.0%
        for (GitMetric m : metricsList) {
            double normalizedIcs = totalWeightedPoints > 0 ? 
                Math.round((m.getContributionScore() / totalWeightedPoints) * 1000.0) / 10.0 : 0.0;
            m.setContributionScore(normalizedIcs);
        }

        return metricsList;
    }

    /**
     * Persist calculated Git metrics to database.
     */
    public static void saveMetrics(int submissionId, List<GitMetric> metrics) throws SQLException {
        // Delete previous metrics for this submission
        DatabaseManager.update("DELETE FROM GitMetrics WHERE submission_id = ?", submissionId);

        for (GitMetric m : metrics) {
            DatabaseManager.insert(
                "INSERT INTO GitMetrics (submission_id, user_id, author_name, author_email, total_commits, " +
                "lines_added, lines_deleted, net_churn, commit_entropy, semantic_score, module_ownership, " +
                "contribution_score, anomaly_flags, ai_critique_summary) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                submissionId, m.getUserId(), m.getAuthorName(), m.getAuthorEmail(), m.getTotalCommits(),
                m.getLinesAdded(), m.getLinesDeleted(), m.getNetChurn(), m.getCommitEntropy(), m.getSemanticScore(),
                m.getModuleOwnership(), m.getContributionScore(), m.getAnomalyFlags(), m.getAiCritiqueSummary()
            );
        }
    }

    /**
     * Generate pedagogical AI feedback via Google Gemini / OpenAI API.
     */
    public static String generateAiCritique(List<GitMetric> metrics, String apiKey) {
        StringBuilder summary = new StringBuilder();
        summary.append("Team Members Git Metrics Summary:\n");
        for (GitMetric m : metrics) {
            summary.append(String.format(
                "- %s: %d commits, +%d/-%d lines (Net: %d), Entropy: %.2f, Semantic: %.1f%%, Ownership: %.2f, ICS: %.1f%%, Flags: %s\n",
                m.getAuthorName(), m.getTotalCommits(), m.getLinesAdded(), m.getLinesDeleted(),
                m.getNetChurn(), m.getCommitEntropy(), m.getSemanticScore(), m.getModuleOwnership(),
                m.getContributionScore(), m.getAnomalyFlags()
            ));
        }

        if (apiKey == null || apiKey.trim().isEmpty()) {
            apiKey = System.getenv("GEMINI_API_KEY");
        }

        if (apiKey != null && !apiKey.trim().isEmpty()) {
            try {
                String prompt = "You are a university computer science lecturer evaluating individual student contribution in a software engineering group project. " +
                    "Review the following quantitative Git analytics data. Identify if any student shows signs of free-riding (e.g. ghost committer or rushing trivial commits at the 11th hour). " +
                    "Provide a pedagogical assessment summarizing each student's role and recommended final contribution percentage in Vietnamese.\n\n" + summary.toString();

                String endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + apiKey.trim();
                URL url = new URL(endpoint);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(20000);

                String payload = "{\"contents\":[{\"parts\":[{\"text\":\"" + prompt.replace("\"", "\\\"").replace("\n", "\\n") + "\"}]}]}";
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(payload.getBytes(StandardCharsets.UTF_8));
                }

                if (conn.getResponseCode() == 200) {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                        StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) sb.append(line);
                        
                        Map<String, Object> respMap = JsonHelper.parseObject(sb.toString());
                        if (respMap != null && respMap.containsKey("candidates")) {
                            List<Object> candidates = (List<Object>) respMap.get("candidates");
                            if (!candidates.isEmpty()) {
                                Map<String, Object> firstCandidate = (Map<String, Object>) candidates.get(0);
                                Map<String, Object> content = (Map<String, Object>) firstCandidate.get("content");
                                List<Object> parts = (List<Object>) content.get("parts");
                                Map<String, Object> firstPart = (Map<String, Object>) parts.get(0);
                                return (String) firstPart.get("text");
                            }
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("[GitAnalyticsService] AI API call fallback: " + e.getMessage());
            }
        }

        // Heuristic Pedagogical Fallback when API key is not supplied
        StringBuilder fallback = new StringBuilder();
        fallback.append("BÁO CÁO ĐÁNH GIÁ ĐÓNG GÓP CÁ NHÂN (HỆ THỐNG PHÂN TÍCH NHÓM 5):\n\n");
        for (GitMetric m : metrics) {
            fallback.append(String.format("• %s (ICS: %.1f%%): ", m.getAuthorName(), m.getContributionScore()));
            if (m.getAnomalyFlags() != null && m.getAnomalyFlags().contains("GHOST_COMMITTER")) {
                fallback.append("CẢNH BÁO NGUY CƠ Ỷ LẠI (FREE-RIDER) - Đóng góp dưới 15% tổng khối lượng nhóm. Số lượng commit và mã nguồn thực tế không đáng kể. Đề xuất trừ điểm cá nhân.\n");
            } else if (m.getAnomalyFlags() != null && m.getAnomalyFlags().contains("11TH_HOUR_RUSHER")) {
                fallback.append("CẢNH BÁO COMMIT DỒN DẬP SÁT HẠN - Hơn 75% commit thực hiện trong 12h trước hạn chót. Cần kiểm tra kỹ chất lượng code.\n");
            } else if (m.getContributionScore() >= 45.0) {
                fallback.append("TRỤ CỘT KIẾN TRÚC (CORE ARCHITECT) - Đảm nhiệm phần lớn khối lượng mã nguồn, commit phân bố đều đặn và độ sở hữu module cao.\n");
            } else {
                fallback.append("THÀNH VIÊN ĐÓNG GÓP TÍCH CỰC - Hoàn thành tốt các phần việc được giao với tỷ lệ churn và commit hợp lý.\n");
            }
        }
        return fallback.toString();
    }
}
