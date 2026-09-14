package com.aigrading.model;

import java.sql.Timestamp;

public class PeerReview {
    private int reviewId;
    private int reviewerTeamId;
    private int targetTeamId;
    private Integer submissionId;
    private double sandboxScore;      // 30%
    private double mvc2CleanCodeScore;// 40%
    private double peerDefenseScore;  // 30%
    private double finalScore;
    private String feedback;
    private String status;            // pending, completed, disputed
    private Timestamp reviewedAt;

    public PeerReview() {}

    public int getReviewId() { return reviewId; }
    public void setReviewId(int reviewId) { this.reviewId = reviewId; }

    public int getReviewerTeamId() { return reviewerTeamId; }
    public void setReviewerTeamId(int reviewerTeamId) { this.reviewerTeamId = reviewerTeamId; }

    public int getTargetTeamId() { return targetTeamId; }
    public void setTargetTeamId(int targetTeamId) { this.targetTeamId = targetTeamId; }

    public Integer getSubmissionId() { return submissionId; }
    public void setSubmissionId(Integer submissionId) { this.submissionId = submissionId; }

    public double getSandboxScore() { return sandboxScore; }
    public void setSandboxScore(double sandboxScore) { this.sandboxScore = sandboxScore; }

    public double getMvc2CleanCodeScore() { return mvc2CleanCodeScore; }
    public void setMvc2CleanCodeScore(double mvc2CleanCodeScore) { this.mvc2CleanCodeScore = mvc2CleanCodeScore; }

    public double getPeerDefenseScore() { return peerDefenseScore; }
    public void setPeerDefenseScore(double peerDefenseScore) { this.peerDefenseScore = peerDefenseScore; }

    public double getFinalScore() { return finalScore; }
    public void setFinalScore(double finalScore) { this.finalScore = finalScore; }

    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(Timestamp reviewedAt) { this.reviewedAt = reviewedAt; }

    public static double calculateFinalScore(double sandbox, double mvc2CleanCode, double defense) {
        return Math.round(((sandbox * 0.30) + (mvc2CleanCode * 0.40) + (defense * 0.30)) * 100.0) / 100.0;
    }
}
