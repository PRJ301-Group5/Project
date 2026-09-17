package com.aigrading.dao;

import com.aigrading.db.DatabaseManager;
import com.aigrading.model.Team;
import com.aigrading.model.TeamMember;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Teams and TeamMembers.
 * Uses parameterized queries with PreparedStatement.
 */
public class TeamDAO {

    public List<Team> getAllTeams() throws SQLException {
        List<Team> list = new ArrayList<>();
        String sql = DatabaseManager.isSqlServer() ?
            "SELECT team_id, team_code, team_name, track_name, project_title, repo_url, created_at FROM [Teams] ORDER BY team_id ASC" :
            "SELECT team_id, team_code, team_name, track_name, project_title, repo_url, created_at FROM Teams ORDER BY team_id ASC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapTeam(rs));
            }
        }
        return list;
    }

    public Team getTeamById(int teamId) throws SQLException {
        String sql = DatabaseManager.isSqlServer() ?
            "SELECT team_id, team_code, team_name, track_name, project_title, repo_url, created_at FROM [Teams] WHERE team_id = ?" :
            "SELECT team_id, team_code, team_name, track_name, project_title, repo_url, created_at FROM Teams WHERE team_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, teamId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapTeam(rs);
                }
            }
        }
        return null;
    }

    public int createTeam(Team t) throws SQLException {
        String sql = DatabaseManager.isSqlServer() ?
            "INSERT INTO [Teams] (team_code, team_name, track_name, project_title, repo_url) VALUES (?, ?, ?, ?, ?)" :
            "INSERT INTO Teams (team_code, team_name, track_name, project_title, repo_url) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, t.getTeamCode());
            ps.setString(2, t.getTeamName());
            ps.setString(3, t.getTrackName());
            ps.setString(4, t.getProjectTitle());
            ps.setString(5, t.getRepoUrl());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    t.setTeamId(id);
                    return id;
                }
            }
        }
        return -1;
    }

    public boolean updateTeam(Team t) throws SQLException {
        String sql = DatabaseManager.isSqlServer() ?
            "UPDATE [Teams] SET team_code = ?, team_name = ?, track_name = ?, project_title = ?, repo_url = ? WHERE team_id = ?" :
            "UPDATE Teams SET team_code = ?, team_name = ?, track_name = ?, project_title = ?, repo_url = ? WHERE team_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, t.getTeamCode());
            ps.setString(2, t.getTeamName());
            ps.setString(3, t.getTrackName());
            ps.setString(4, t.getProjectTitle());
            ps.setString(5, t.getRepoUrl());
            ps.setInt(6, t.getTeamId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteTeam(int teamId) throws SQLException {
        // Cascade delete members first
        DatabaseManager.deleteRecord("TeamMembers", "team_id", teamId, false);
        return DatabaseManager.deleteRecord("Teams", "team_id", teamId, false) > 0;
    }

    public List<TeamMember> getTeamMembers(int teamId) throws SQLException {
        List<TeamMember> list = new ArrayList<>();
        String sql = DatabaseManager.isSqlServer() ?
            "SELECT tm.id, tm.team_id, tm.user_id, tm.role_in_team, u.full_name AS user_name, u.email AS user_email " +
            "FROM [TeamMembers] tm JOIN [Users] u ON tm.user_id = u.user_id WHERE tm.team_id = ?" :
            "SELECT tm.id, tm.team_id, tm.user_id, tm.role_in_team, u.full_name AS user_name, u.email AS user_email " +
            "FROM TeamMembers tm JOIN Users u ON tm.user_id = u.user_id WHERE tm.team_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, teamId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    TeamMember m = new TeamMember();
                    m.setId(rs.getInt("id"));
                    m.setTeamId(rs.getInt("team_id"));
                    m.setUserId(rs.getInt("user_id"));
                    m.setRoleInTeam(rs.getString("role_in_team"));
                    m.setUserName(rs.getString("user_name"));
                    m.setUserEmail(rs.getString("user_email"));
                    list.add(m);
                }
            }
        }
        return list;
    }

    private Team mapTeam(ResultSet rs) throws SQLException {
        Team t = new Team();
        t.setTeamId(rs.getInt("team_id"));
        t.setTeamCode(rs.getString("team_code"));
        t.setTeamName(rs.getString("team_name"));
        t.setTrackName(rs.getString("track_name"));
        t.setProjectTitle(rs.getString("project_title"));
        t.setRepoUrl(rs.getString("repo_url"));
        t.setCreatedAt(rs.getTimestamp("created_at"));
        return t;
    }
}
