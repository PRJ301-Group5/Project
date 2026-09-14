package com.aigrading.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Assignment implements Serializable {
    private static final long serialVersionUID = 1L;

    private int assignmentId;
    private int teacherId;
    private String teacherName;
    private Integer subjectId;
    private String subjectCode;
    private String title;
    private String prototypeUrl;
    private String promptFileUrl;
    private Timestamp deadline;
    private String rubricJson;
    private double maxPoints = 100.0;
    private Timestamp createdAt;

    public Assignment() {}

    public Assignment(int assignmentId, int teacherId, String title, Timestamp deadline, double maxPoints) {
        this.assignmentId = assignmentId;
        this.teacherId = teacherId;
        this.title = title;
        this.deadline = deadline;
        this.maxPoints = maxPoints;
    }

    public int getAssignmentId() { return assignmentId; }
    public void setAssignmentId(int assignmentId) { this.assignmentId = assignmentId; }

    public int getTeacherId() { return teacherId; }
    public void setTeacherId(int teacherId) { this.teacherId = teacherId; }

    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }

    public Integer getSubjectId() { return subjectId; }
    public void setSubjectId(Integer subjectId) { this.subjectId = subjectId; }

    public String getSubjectCode() { return subjectCode; }
    public void setSubjectCode(String subjectCode) { this.subjectCode = subjectCode; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getPrototypeUrl() { return prototypeUrl; }
    public void setPrototypeUrl(String prototypeUrl) { this.prototypeUrl = prototypeUrl; }

    public String getPromptFileUrl() { return promptFileUrl; }
    public void setPromptFileUrl(String promptFileUrl) { this.promptFileUrl = promptFileUrl; }

    public Timestamp getDeadline() { return deadline; }
    public void setDeadline(Timestamp deadline) { this.deadline = deadline; }

    public String getRubricJson() { return rubricJson; }
    public void setRubricJson(String rubricJson) { this.rubricJson = rubricJson; }

    public double getMaxPoints() { return maxPoints; }
    public void setMaxPoints(double maxPoints) { this.maxPoints = maxPoints; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public boolean isExpired() {
        return deadline != null && deadline.before(new Date());
    }

    public String getFormattedDeadline() {
        if (deadline == null) return "Không giới hạn";
        return new SimpleDateFormat("yyyy-MM-dd HH:mm").format(deadline);
    }

    @Override
    public String toString() {
        return "Assignment{" + "assignmentId=" + assignmentId + ", title='" + title + '\'' + ", deadline=" + deadline + '}';
    }
}
