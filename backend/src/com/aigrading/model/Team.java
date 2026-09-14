package com.aigrading.model;

import java.sql.Timestamp;

public class Team {
    private int teamId;
    private String teamCode;
    private String teamName;
    private String trackName;
    private String projectTitle;
    private String repoUrl;
    private Timestamp createdAt;

    public Team() {}

    public Team(int teamId, String teamCode, String teamName, String trackName, String projectTitle, String repoUrl) {
        this.teamId = teamId;
        this.teamCode = teamCode;
        this.teamName = teamName;
        this.trackName = trackName;
        this.projectTitle = projectTitle;
        this.repoUrl = repoUrl;
    }

    public int getTeamId() { return teamId; }
    public void setTeamId(int teamId) { this.teamId = teamId; }

    public String getTeamCode() { return teamCode; }
    public void setTeamCode(String teamCode) { this.teamCode = teamCode; }

    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }

    public String getTrackName() { return trackName; }
    public void setTrackName(String trackName) { this.trackName = trackName; }

    public String getProjectTitle() { return projectTitle; }
    public void setProjectTitle(String projectTitle) { this.projectTitle = projectTitle; }

    public String getRepoUrl() { return repoUrl; }
    public void setRepoUrl(String repoUrl) { this.repoUrl = repoUrl; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
