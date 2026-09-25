<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:set var="pageTitle" value="Quản Lý Bài Tập - AI Grading Platform" scope="request"/>
<jsp:include page="header.jsp"/>

<div class="d-flex justify-content-between align-items-center mb-4">
    <div>
        <h3 class="fw-bold mb-1">
            <i class="bi bi-journal-code text-primary me-2"></i>Danh Sách Bài Tập
        </h3>
        <p class="text-muted small mb-0">Quản lý và nộp bài tập kiểm thử tự động với Moulinette & AI</p>
    </div>

    <c:if test="${not empty sessionScope.user and not sessionScope.user.student}">
        <a href="${pageContext.request.contextPath}/assignments?action=create" class="btn btn-primary-custom">
            <i class="bi bi-plus-circle me-1"></i> Tạo Bài Tập Mới
        </a>
    </c:if>
</div>

<!-- Search & Filter Card -->
<div class="card card-custom p-3 mb-4">
    <form action="${pageContext.request.contextPath}/assignments" method="GET" class="row g-2 align-items-center">
        <input type="hidden" name="action" value="list">
        <div class="col-md-9">
            <div class="input-group">
                <span class="input-group-text bg-white"><i class="bi bi-search text-muted"></i></span>
                <input type="text" class="form-control" name="search"
                       value="${searchKeyword}" placeholder="Tìm kiếm theo tiêu đề bài tập hoặc tên giảng viên...">
            </div>
        </div>
        <div class="col-md-3 d-flex gap-2">
            <button type="submit" class="btn btn-primary w-100">
                <i class="bi bi-funnel me-1"></i> Tìm Kiếm
            </button>
            <c:if test="${not empty searchKeyword}">
                <a href="${pageContext.request.contextPath}/assignments" class="btn btn-outline-secondary">
                    <i class="bi bi-x-circle"></i>
                </a>
            </c:if>
        </div>
    </form>
</div>

<!-- Assignments Table -->
<div class="card card-custom overflow-hidden shadow-sm">
    <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
            <thead class="table-light">
                <tr>
                    <th scope="col" style="width: 70px;" class="text-center">#ID</th>
                    <th scope="col">Tiêu Đề Bài Tập</th>
                    <th scope="col">Giảng Viên</th>
                    <th scope="col">Hạn Nộp (Deadline)</th>
                    <th scope="col" class="text-center">Trạng Thái</th>
                    <th scope="col" class="text-end pe-4">Thao Tác (CRUD)</th>
                </tr>
            </thead>
            <tbody>
                <c:choose>
                    <c:when test="${not empty assignments}">
                        <c:forEach items="${assignments}" var="a">
                            <tr>
                                <td class="text-center fw-semibold text-muted">${a.assignmentId}</td>
                                <td>
                                    <a href="${pageContext.request.contextPath}/assignments?action=view&id=${a.assignmentId}" class="fw-bold text-decoration-none text-primary">
                                        ${a.title}
                                    </a>
                                    <div class="text-muted small">
                                        <i class="bi bi-calendar3 me-1"></i>Tạo ngày: ${a.createdAt}
                                    </div>
                                </td>
                                <td>
                                    <div class="d-flex align-items-center">
                                        <i class="bi bi-person-workspace text-secondary me-2"></i>
                                        <span>${not empty a.teacherName ? a.teacherName : "Prof. Robert Davis"}</span>
                                    </div>
                                </td>
                                <td>
                                    <span class="font-monospace text-dark">${a.formattedDeadline}</span>
                                </td>
                                <td class="text-center">
                                    <c:choose>
                                        <c:when test="${a.expired}">
                                            <span class="badge bg-danger">
                                                <i class="bi bi-clock-history me-1"></i>Đã Hết Hạn
                                            </span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge bg-success">
                                                <i class="bi bi-check-circle me-1"></i>Đang Mở
                                            </span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td class="text-end pe-4">
                                    <div class="btn-group btn-group-sm">
                                        <a href="${pageContext.request.contextPath}/assignments?action=view&id=${a.assignmentId}"
                                           class="btn btn-outline-primary" title="Xem chi tiết">
                                            <i class="bi bi-eye"></i>
                                        </a>

                                        <c:if test="${not empty sessionScope.user and not sessionScope.user.student}">
                                            <a href="${pageContext.request.contextPath}/assignments?action=edit&id=${a.assignmentId}"
                                               class="btn btn-outline-warning" title="Chỉnh sửa (Update)">
                                                <i class="bi bi-pencil"></i>
                                            </a>
                                            <a href="${pageContext.request.contextPath}/assignments?action=delete&id=${a.assignmentId}"
                                               class="btn btn-outline-danger" title="Xóa bài tập (Delete Cascade)"
                                               onclick="return confirm('Bạn có chắc chắn muốn xóa bài tập #${a.assignmentId} không? Tất cả bài nộp và kết quả liên quan sẽ được tự động xóa an toàn!');">
                                                <i class="bi bi-trash"></i>
                                            </a>
                                        </c:if>
                                    </div>
                                </td>
                            </tr>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <tr>
                            <td colspan="6" class="text-center py-5 text-muted">
                                <i class="bi bi-inbox fs-1 d-block mb-2 text-secondary"></i>
                                <div>Chưa có bài tập nào phù hợp trong hệ thống.</div>
                            </td>
                        </tr>
                    </c:otherwise>
                </c:choose>
            </tbody>
        </table>
    </div>
</div>

<jsp:include page="footer.jsp"/>
