<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:set var="pageTitle" value="Quản Lý Nhóm Nghiên Cứu - AI Grading Platform" scope="request"/>
<jsp:include page="header.jsp"/>

<div class="d-flex justify-content-between align-items-center mb-4">
    <div>
        <h3 class="fw-bold mb-1">
            <i class="bi bi-people-fill text-primary me-2"></i>Các Nhóm Nghiên Cứu (7 Tracks)
        </h3>
        <p class="text-muted small mb-0">Hệ thống phân chia đề tài và chấm chéo theo vòng tròn École 42</p>
    </div>

    <c:if test="${not empty sessionScope.user and not sessionScope.user.student}">
        <button class="btn btn-primary-custom" data-bs-toggle="modal" data-bs-target="#createTeamModal">
            <i class="bi bi-plus-circle me-1"></i> Thêm Nhóm Mới
        </button>
    </c:if>
</div>

<!-- Teams Cards Grid -->
<div class="row row-cols-1 row-cols-md-2 row-cols-lg-3 g-4 mb-4">
    <c:forEach items="${teams}" var="t">
        <div class="col">
            <div class="card card-custom h-100 p-3 shadow-sm border-top border-4 ${t.teamCode == 'G5' ? 'border-primary' : 'border-secondary'}">
                <div class="d-flex justify-content-between align-items-center mb-2">
                    <span class="badge ${t.teamCode == 'G5' ? 'bg-primary' : 'bg-dark'} fs-6">${t.teamCode}</span>
                    <c:if test="${t.teamCode == 'G5'}">
                        <span class="badge bg-warning text-dark"><i class="bi bi-star-fill me-1"></i>Dự án của bạn</span>
                    </c:if>
                </div>

                <h5 class="fw-bold mb-1">${t.teamName}</h5>
                <div class="text-primary small fw-semibold mb-2">${t.trackName}</div>
                <p class="text-muted small flex-grow-1 mb-3">${t.projectTitle}</p>

                <div class="border-top pt-2 mt-auto d-flex justify-content-between align-items-center">
                    <a href="${t.repoUrl}" target="_blank" class="text-decoration-none small text-secondary">
                        <i class="bi bi-github me-1"></i>Repository
                    </a>

                    <div class="btn-group btn-group-sm">
                        <a href="${pageContext.request.contextPath}/teams?action=view&id=${t.teamId}" class="btn btn-outline-primary btn-sm">
                            <i class="bi bi-eye me-1"></i>Thành viên
                        </a>
                        <c:if test="${not empty sessionScope.user and not sessionScope.user.student}">
                            <a href="${pageContext.request.contextPath}/teams?action=delete&id=${t.teamId}" class="btn btn-outline-danger btn-sm"
                               onclick="return confirm('Bạn có chắc chắn muốn xóa nhóm ${t.teamCode}?');">
                                <i class="bi bi-trash"></i>
                            </a>
                        </c:if>
                    </div>
                </div>
            </div>
        </div>
    </c:forEach>
</div>

<!-- Modal Create Team -->
<div class="modal fade" id="createTeamModal" tabindex="-1">
    <div class="modal-dialog">
        <div class="modal-content">
            <form action="${pageContext.request.contextPath}/teams" method="POST">
                <div class="modal-header">
                    <h5 class="modal-title fw-bold">Tạo Nhóm Nghiên Cứu Mới</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                </div>
                <div class="modal-body">
                    <div class="mb-3">
                        <label class="form-label fw-semibold">Mã Nhóm (Code)</label>
                        <input type="text" class="form-control" name="teamCode" placeholder="G8" required>
                    </div>
                    <div class="mb-3">
                        <label class="form-label fw-semibold">Tên Nhóm</label>
                        <input type="text" class="form-control" name="teamName" placeholder="Cloud DevOps Team" required>
                    </div>
                    <div class="mb-3">
                        <label class="form-label fw-semibold">Chủ Đề (Track Name)</label>
                        <input type="text" class="form-control" name="trackName" placeholder="CI/CD & Cloud Infrastructure">
                    </div>
                    <div class="mb-3">
                        <label class="form-label fw-semibold">Đề Tài Dự Án</label>
                        <input type="text" class="form-control" name="projectTitle" placeholder="Automated Cloud Sandbox Pipelines">
                    </div>
                    <div class="mb-3">
                        <label class="form-label fw-semibold">Kho Lưu Trữ Git (Repo URL)</label>
                        <input type="url" class="form-control" name="repoUrl" placeholder="https://github.com/team-g8/repo">
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Đóng</button>
                    <button type="submit" class="btn btn-primary-custom">Tạo Nhóm</button>
                </div>
            </form>
        </div>
    </div>
</div>

<jsp:include page="footer.jsp"/>
