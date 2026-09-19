<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${pageTitle != null ? pageTitle : "Hệ Thống Chấm Bài AI - PRJ301"}</title>
    <!-- Google Fonts & Bootstrap 5 -->
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css">
    <style>
        body {
            font-family: 'Inter', sans-serif;
            background-color: #f8fafc;
            color: #1e293b;
            min-height: 100vh;
            display: flex;
            flex-direction: column;
        }
        .navbar-custom {
            background: linear-gradient(135deg, #1e3a8a 0%, #3b82f6 100%);
            box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
        }
        .card-custom {
            border: none;
            border-radius: 12px;
            box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05), 0 2px 4px -1px rgba(0, 0, 0, 0.03);
            background: #ffffff;
        }
        .btn-primary-custom {
            background-color: #2563eb;
            border-color: #2563eb;
            font-weight: 500;
            border-radius: 8px;
        }
        .btn-primary-custom:hover {
            background-color: #1d4ed8;
            border-color: #1d4ed8;
        }
        .badge-teacher {
            background-color: #f59e0b;
            color: #ffffff;
        }
        .badge-student {
            background-color: #10b981;
            color: #ffffff;
        }
        .badge-admin {
            background-color: #ef4444;
            color: #ffffff;
        }
    </style>
</head>
<body>

<!-- Navigation Bar -->
<nav class="navbar navbar-expand-lg navbar-dark navbar-custom mb-4">
    <div class="container">
        <a class="navbar-brand fw-bold d-flex align-items-center" href="${pageContext.request.contextPath}/assignments">
            <i class="bi bi-cpu-fill me-2 fs-4"></i>
            <span>AI Grading System</span>
            <small class="badge bg-light text-primary ms-2 fs-6">PRJ301</small>
        </a>
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav">
            <span class="navbar-toggler-icon"></span>
        </button>

        <div class="collapse navbar-collapse" id="navbarNav">
            <ul class="navbar-nav me-auto">
                <c:if test="${not empty sessionScope.user}">
                    <li class="nav-item">
                        <a class="nav-link" href="${pageContext.request.contextPath}/assignments">
                            <i class="bi bi-journal-text me-1"></i> Danh Sách Bài Tập
                        </a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" href="${pageContext.request.contextPath}/teams">
                            <i class="bi bi-people me-1"></i> Nhóm Nghiên Cứu
                        </a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" href="${pageContext.request.contextPath}/submissions">
                            <i class="bi bi-file-earmark-check me-1"></i> Bài Nộp
                        </a>
                    </li>
                </c:if>
            </ul>

            <ul class="navbar-nav">
                <c:choose>
                    <c:when test="${not empty sessionScope.user}">
                        <li class="nav-item dropdown">
                            <a class="nav-link dropdown-toggle text-white d-flex align-items-center" href="#" role="button" data-bs-toggle="dropdown">
                                <i class="bi bi-person-circle fs-5 me-2"></i>
                                <span>${sessionScope.user.fullName}</span>
                                <span class="badge ms-2 ${sessionScope.user.role == 'teacher' ? 'badge-teacher' : (sessionScope.user.role == 'admin' ? 'badge-admin' : 'badge-student')}">
                                    ${sessionScope.user.role}
                                </span>
                            </a>
                            <ul class="dropdown-menu dropdown-menu-end shadow">
                                <li><h6 class="dropdown-header">${sessionScope.user.email}</h6></li>
                                <li><hr class="dropdown-divider"></li>
                                <li>
                                    <a class="dropdown-item text-danger" href="${pageContext.request.contextPath}/logout">
                                        <i class="bi bi-box-arrow-right me-2"></i> Đăng Xuất
                                    </a>
                                </li>
                            </ul>
                        </li>
                    </c:when>
                    <c:otherwise>
                        <li class="nav-item">
                            <a class="btn btn-outline-light btn-sm px-3" href="${pageContext.request.contextPath}/login">
                                <i class="bi bi-box-arrow-in-right me-1"></i> Đăng Nhập
                            </a>
                        </li>
                    </c:otherwise>
                </c:choose>
            </ul>
        </div>
    </div>
</nav>

<div class="container flex-grow-1">
    <!-- Flash Messages -->
    <c:if test="${not empty param.message}">
        <div class="alert alert-success alert-dismissible fade show shadow-sm" role="alert">
            <i class="bi bi-check-circle-fill me-2"></i> ${param.message}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    </c:if>
    <c:if test="${not empty param.error}">
        <div class="alert alert-danger alert-dismissible fade show shadow-sm" role="alert">
            <i class="bi bi-exclamation-triangle-fill me-2"></i> ${param.error}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    </c:if>
    <c:if test="${not empty errorMessage}">
        <div class="alert alert-danger alert-dismissible fade show shadow-sm" role="alert">
            <i class="bi bi-exclamation-triangle-fill me-2"></i> ${errorMessage}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    </c:if>
