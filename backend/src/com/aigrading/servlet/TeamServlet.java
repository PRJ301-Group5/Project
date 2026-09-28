package com.aigrading.servlet;

import com.aigrading.dao.TeamDAO;
import com.aigrading.model.Team;
import com.aigrading.model.TeamMember;
import com.aigrading.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.List;

/**
 * Controller for Team Management CRUD workflow.
 */
@WebServlet(name = "TeamServlet", urlPatterns = {"/teams"})
public class TeamServlet extends HttpServlet {
    private final TeamDAO teamDAO = new TeamDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if (action == null) action = "list";

        try {
            switch (action) {
                case "view": {
                    int id = Integer.parseInt(request.getParameter("id"));
                    Team team = teamDAO.getTeamById(id);
                    List<TeamMember> members = teamDAO.getTeamMembers(id);
                    request.setAttribute("team", team);
                    request.setAttribute("members", members);
                    request.getRequestDispatcher("/team-detail.jsp").forward(request, response);
                    break;
                }
                case "delete": {
                    User user = (User) request.getSession().getAttribute("user");
                    if (user != null && !user.isStudent()) {
                        int id = Integer.parseInt(request.getParameter("id"));
                        teamDAO.deleteTeam(id);
                    }
                    response.sendRedirect(request.getContextPath() + "/teams");
                    break;
                }
                case "list":
                default: {
                    List<Team> list = teamDAO.getAllTeams();
                    request.setAttribute("teams", list);
                    request.getRequestDispatcher("/teams.jsp").forward(request, response);
                    break;
                }
            }
        } catch (Exception e) {
            request.setAttribute("errorMessage", "Lỗi xử lý: " + e.getMessage());
            request.getRequestDispatcher("/error.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String teamCode = request.getParameter("teamCode");
        String teamName = request.getParameter("teamName");
        String trackName = request.getParameter("trackName");
        String projectTitle = request.getParameter("projectTitle");
        String repoUrl = request.getParameter("repoUrl");

        if (teamCode == null || teamCode.trim().isEmpty() || teamName == null || teamName.trim().isEmpty()) {
            request.setAttribute("errorMessage", "Mã nhóm và Tên nhóm không được để trống!");
            request.getRequestDispatcher("/teams.jsp").forward(request, response);
            return;
        }

        try {
            Team t = new Team(0, teamCode.trim(), teamName.trim(), trackName, projectTitle, repoUrl);
            teamDAO.createTeam(t);
            response.sendRedirect(request.getContextPath() + "/teams?message=" +
                    java.net.URLEncoder.encode("Tạo nhóm thành công!", "UTF-8"));
        } catch (Exception e) {
            request.setAttribute("errorMessage", "Lỗi tạo nhóm: " + e.getMessage());
            request.getRequestDispatcher("/error.jsp").forward(request, response);
        }
    }
}
