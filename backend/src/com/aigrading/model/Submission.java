package com.aigrading.model;

import java.io.Serializable;
import java.sql.Timestamp;

public class Submission implements Serializable {
    private static final long serialVersionUID = 1L;

    private int submissionId;
    private Integer studentId;
    private String studentName;
    private Integer teamId;
    private String teamCode;
    private int assignmentId;
    private String assignmentTitle;
    private String screenshotPath;
    private String zipFilePath;
    private String sha256Hash;
    private String artifactType;
    private int queuePriority = 1;
    private int retryCount = 0;
    private Timestamp submittedAt;
    private String status = "submitted";

    public Submission() {}

    public int getSubmissionId() { return submissionId; }
    public void setSubmissionId(int submissionId) { this.submissionId = submissionId; }

    public Integer getStudentId() { return studentId; }
    public void setStudentId(Integer studentId) { this.studentId = studentId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public Integer getTeamId() { return teamId; }
    public void setTeamId(Integer teamId) { this.teamId = teamId; }

    public String getTeamCode() { return teamCode; }
    public void setTeamCode(String teamCode) { this.teamCode = teamCode; }

    public int getAssignmentId() { return assignmentId; }
    public void setAssignmentId(int assignmentId) { this.assignmentId = assignmentId; }

    public String getAssignmentTitle() { return assignmentTitle; }
    public void setAssignmentTitle(String assignmentTitle) { this.assignmentTitle = assignmentTitle; }

    public String getScreenshotPath() { return screenshotPath; }
    public void setScreenshotPath(String screenshotPath) { this.screenshotPath = screenshotPath; }

    public String getZipFilePath() { return zipFilePath; }
    public void setZipFilePath(String zipFilePath) { this.zipFilePath = zipFilePath; }

    public String getSha256Hash() { return sha256Hash; }
    public void setSha256Hash(String sha256Hash) { this.sha256Hash = sha256Hash; }

    public String getArtifactType() { return artifactType; }
    public void setArtifactType(String artifactType) { this.artifactType = artifactType; }

    public int getQueuePriority() { return queuePriority; }
    public void setQueuePriority(int queuePriority) { this.queuePriority = queuePriority; }

    public int getRetryCount() { return retryCount; }
    public void setRetryCount(int retryCount) { this.retryCount = retryCount; }

    public Timestamp getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(Timestamp submittedAt) { this.submittedAt = submittedAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
