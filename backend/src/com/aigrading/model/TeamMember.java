package com.aigrading.model;

import java.io.Serializable;

public class TeamMember implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private int teamId;
    private int userId;
    private String userName;
    private String userEmail;
    private String roleInTeam = "member";

    public TeamMember() {}

    public TeamMember(int id, int teamId, int userId, String roleInTeam) {
        this.id = id;
        this.teamId = teamId;
        this.userId = userId;
        this.roleInTeam = roleInTeam;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getTeamId() { return teamId; }
    public void setTeamId(int teamId) { this.teamId = teamId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getRoleInTeam() { return roleInTeam; }
    public void setRoleInTeam(String roleInTeam) { this.roleInTeam = roleInTeam; }
}
