package com.aigrading.model;

import java.sql.Timestamp;

public class GitMetric {
    private int metricId;
    private int submissionId;
    private Integer userId;
    private String authorName;
    private String authorEmail;
    private int totalCommits;
    private int linesAdded;
    private int linesDeleted;
    private int netChurn;
    private double commitEntropy;
    private double semanticScore;
    private double moduleOwnership;
    private double contributionScore; // Individual Contribution Score (ICS %)
    private String anomalyFlags;       // JSON array or flag string
    private String aiCritiqueSummary;
    private Timestamp analyzedAt;

    public GitMetric() {}

    public int getMetricId() { return metricId; }
    public void setMetricId(int metricId) { this.metricId = metricId; }

    public int getSubmissionId() { return submissionId; }
    public void setSubmissionId(int submissionId) { this.submissionId = submissionId; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public String getAuthorEmail() { return authorEmail; }
    public void setAuthorEmail(String authorEmail) { this.authorEmail = authorEmail; }

    public int getTotalCommits() { return totalCommits; }
    public void setTotalCommits(int totalCommits) { this.totalCommits = totalCommits; }

    public int getLinesAdded() { return linesAdded; }
    public void setLinesAdded(int linesAdded) { this.linesAdded = linesAdded; }

    public int getLinesDeleted() { return linesDeleted; }
    public void setLinesDeleted(int linesDeleted) { this.linesDeleted = linesDeleted; }

    public int getNetChurn() { return netChurn; }
    public void setNetChurn(int netChurn) { this.netChurn = netChurn; }

    public double getCommitEntropy() { return commitEntropy; }
    public void setCommitEntropy(double commitEntropy) { this.commitEntropy = commitEntropy; }

    public double getSemanticScore() { return semanticScore; }
    public void setSemanticScore(double semanticScore) { this.semanticScore = semanticScore; }

    public double getModuleOwnership() { return moduleOwnership; }
    public void setModuleOwnership(double moduleOwnership) { this.moduleOwnership = moduleOwnership; }

    public double getContributionScore() { return contributionScore; }
    public void setContributionScore(double contributionScore) { this.contributionScore = contributionScore; }

    public String getAnomalyFlags() { return anomalyFlags; }
    public void setAnomalyFlags(String anomalyFlags) { this.anomalyFlags = anomalyFlags; }

    public String getAiCritiqueSummary() { return aiCritiqueSummary; }
    public void setAiCritiqueSummary(String aiCritiqueSummary) { this.aiCritiqueSummary = aiCritiqueSummary; }

    public Timestamp getAnalyzedAt() { return analyzedAt; }
    public void setAnalyzedAt(Timestamp analyzedAt) { this.analyzedAt = analyzedAt; }
}
