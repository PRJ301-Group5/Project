package com.aigrading.servlet;

import com.aigrading.dao.AssignmentDAO;
import com.aigrading.dao.SubmissionDAO;
import com.aigrading.model.Assignment;
import com.aigrading.model.Submission;
import com.aigrading.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

/**
 * Controller handling Assignment Submissions and enforcing Deadlines.
 * Contains critical Exception Path: Expired Deadline rejection.
 */
@WebServlet(name = "SubmissionServlet", urlPatterns = {"/submissions"})
public class SubmissionServlet extends HttpServlet {
    private final SubmissionDAO submissionDAO = new SubmissionDAO();
    private final AssignmentDAO assignmentDAO = new AssignmentDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        try {
            if ("view".equals(action)) {
                int id = Integer.parseInt(request.getParameter("id"));
                Submission s = submissionDAO.getSubmissionById(id);
                request.setAttribute("submission", s);
                request.getRequestDispatcher("/submission-detail.jsp").forward(request, response);
                return;
            }

            request.setAttribute("submissions", submissionDAO.getAllSubmissions());
            request.getRequestDispatcher("/submissions.jsp").forward(request, response);
        } catch (Exception e) {
            request.setAttribute("errorMessage", "Lỗi nạp bài nộp: " + e.getMessage());
            request.getRequestDispatcher("/error.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String assignmentIdStr = request.getParameter("assignmentId");
        String artifactType = request.getParameter("artifactType");
        String filePath = request.getParameter("filePath");
        String sha256 = request.getParameter("sha256");

        if (assignmentIdStr == null || assignmentIdStr.trim().isEmpty()) {
            request.setAttribute("errorMessage", "Thiếu mã bài tập!");
            request.getRequestDispatcher("/error.jsp").forward(request, response);
            return;
        }

        try {
            int assignmentId = Integer.parseInt(assignmentIdStr.trim());
            Assignment assignment = assignmentDAO.getAssignmentById(assignmentId);

            if (assignment == null) {
                request.setAttribute("errorMessage", "Bài tập không tồn tại!");
                request.getRequestDispatcher("/error.jsp").forward(request, response);
                return;
            }

            // CRITICAL EXCEPTION PATH: Expired Deadline Rejection!
            if (assignment.isExpired()) {
                request.setAttribute("errorMessage", "⛔ HẠN NỘP BÀI ĐÃ KẾT THÚC (" + assignment.getFormattedDeadline() + ")! Hệ thống không chấp nhận bài nộp trễ hạn.");
                request.setAttribute("assignment", assignment);
                request.getRequestDispatcher("/assignment-detail.jsp").forward(request, response);
                return;
            }

            // Valid Submission
            Submission s = new Submission();
            s.setAssignmentId(assignmentId);
            s.setStudentId(user.getUserId());
            s.setArtifactType(artifactType != null ? artifactType : "zip");
            s.setZipFilePath(filePath != null ? filePath : "uploads/submissions/assignment_" + assignmentId + "_user_" + user.getUserId() + ".zip");
            s.setSha256Hash(sha256 != null && !sha256.trim().isEmpty() ? sha256 : "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855");
            s.setStatus("submitted");

            int subId = submissionDAO.createSubmission(s);

            response.sendRedirect(request.getContextPath() + "/assignments?action=view&id=" + assignmentId + "&message=" +
                    java.net.URLEncoder.encode("Nộp bài thành công (Mã nộp: #" + subId + ")!", "UTF-8"));

        } catch (Exception e) {
            request.setAttribute("errorMessage", "Lỗi nộp bài: " + e.getMessage());
            request.getRequestDispatcher("/error.jsp").forward(request, response);
        }
    }
}
