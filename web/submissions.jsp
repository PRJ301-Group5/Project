<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:set var="pageTitle" value="Quản Lý Bài Nộp - AI Grading Platform" scope="request"/>
<jsp:include page="header.jsp"/>

<div class="d-flex justify-content-between align-items-center mb-4">
    <div>
        <h3 class="fw-bold mb-1">
            <i class="bi bi-file-earmark-code text-primary me-2"></i>Danh Sách Bài Nộp Của Sinh Viên
        </h3>
        <p class="text-muted small mb-0">Theo dõi trạng thái nộp bài, kiểm tra tính toàn vẹn SHA-256 và kết quả kiểm thử Moulinette</p>
    </div>
</div>

<div class="card card-custom overflow-hidden shadow-sm">
    <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
            <thead class="table-light">
                <tr>
                    <th class="text-center" style="width: 70px;">#ID</th>
                    <th>Bài Tập</th>
                    <th>Sinh Viên</th>
                    <th>Nhóm</th>
                    <th>Định Dạng</th>
                    <th>Mã Băm (SHA-256)</th>
                    <th>Thời Gian Nộp</th>
                    <th class="text-center">Trạng Thái</th>
                </tr>
            </thead>
            <tbody>
                <c:choose>
                    <c:when test="${not empty submissions}">
                        <c:forEach items="${submissions}" var="s">
                            <tr>
                                <td class="text-center fw-bold text-muted">#${s.submissionId}</td>
                                <td>
                                    <a href="${pageContext.request.contextPath}/assignments?action=view&id=${s.assignmentId}" class="fw-semibold text-decoration-none">
                                        ${not empty s.assignmentTitle ? s.assignmentTitle : "Assignment #".concat(s.assignmentId)}
                                    </a>
                                </td>
                                <td>${not empty s.studentName ? s.studentName : "Alex Nguyen"}</td>
                                <td><span class="badge bg-secondary">${not empty s.teamCode ? s.teamCode : "G5"}</span></td>
                                <td><span class="badge bg-light text-dark border">${s.artifactType}</span></td>
                                <td>
                                    <code class="small text-truncate d-inline-block" style="max-width: 140px;" title="${s.sha256Hash}">
                                        ${s.sha256Hash}
                                    </code>
                                </td>
                                <td><small class="text-muted">${s.submittedAt}</small></td>
                                <td class="text-center">
                                    <c:choose>
                                        <c:when test="${s.status == 'graded'}">
                                            <span class="badge bg-success">Đã Chấm Điểm</span>
                                        </c:when>
                                        <c:when test="${s.status == 'grading'}">
                                            <span class="badge bg-warning text-dark">Đang Chấm</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge bg-primary">Đã Nộp Bài</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                            </tr>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <tr>
                            <td colspan="8" class="text-center py-5 text-muted">
                                <i class="bi bi-inbox fs-1 d-block mb-2 text-secondary"></i>
                                Chưa có bài nộp nào trong hệ thống.
                            </td>
                        </tr>
                    </c:otherwise>
                </c:choose>
            </tbody>
        </table>
    </div>
</div>

<jsp:include page="footer.jsp"/>
