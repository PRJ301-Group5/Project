package com.aigrading.servlet;

import com.aigrading.dao.UserDAO;
import com.aigrading.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

/**
 * Controller handling user login, Session establishment, Cookie management,
 * and exception validation paths.
 */
@WebServlet(name = "LoginServlet", urlPatterns = {"/login"})
public class LoginServlet extends HttpServlet {
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("user") != null) {
            response.sendRedirect(request.getContextPath() + "/assignments");
            return;
        }

        // Check for remembered email cookie
        String savedEmail = "";
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if ("savedEmail".equals(c.getName())) {
                    savedEmail = c.getValue();
                    break;
                }
            }
        }
        request.setAttribute("savedEmail", savedEmail);
        request.getRequestDispatcher("/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String rememberMe = request.getParameter("rememberMe");

        // Exception path 1: Missing or blank input
        if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            request.setAttribute("errorMessage", "Vui lòng nhập đầy đủ Email và Mật khẩu!");
            request.setAttribute("savedEmail", email != null ? email.trim() : "");
            request.getRequestDispatcher("/login.jsp").forward(request, response);
            return;
        }

        try {
            User user = userDAO.authenticate(email.trim(), password);

            // Exception path 2: Authentication failure (invalid credentials)
            if (user == null) {
                request.setAttribute("errorMessage", "Email hoặc mật khẩu không chính xác. Vui lòng kiểm tra lại!");
                request.setAttribute("savedEmail", email.trim());
                request.getRequestDispatcher("/login.jsp").forward(request, response);
                return;
            }

            // SUCCESS PATH: Establish Session State
            HttpSession session = request.getSession(true);
            session.setAttribute("user", user);
            session.setAttribute("userId", user.getUserId());
            session.setAttribute("userRole", user.getRole());
            session.setAttribute("userName", user.getFullName());

            // Handle Cookie State (Remember Me)
            if ("on".equalsIgnoreCase(rememberMe) || "true".equalsIgnoreCase(rememberMe)) {
                Cookie cookie = new Cookie("savedEmail", user.getEmail());
                cookie.setMaxAge(7 * 24 * 60 * 60); // 7 days
                cookie.setPath(request.getContextPath().isEmpty() ? "/" : request.getContextPath());
                response.addCookie(cookie);
            } else {
                // Clear cookie if unchecked
                Cookie cookie = new Cookie("savedEmail", "");
                cookie.setMaxAge(0);
                cookie.setPath(request.getContextPath().isEmpty() ? "/" : request.getContextPath());
                response.addCookie(cookie);
            }

            // Redirect to assignments page
            response.sendRedirect(request.getContextPath() + "/assignments");

        } catch (Exception e) {
            // Exception path 3: Database / System exception
            request.setAttribute("errorMessage", "Lỗi hệ thống khi đăng nhập: " + e.getMessage());
            request.getRequestDispatcher("/login.jsp").forward(request, response);
        }
    }
}
