<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:set var="pageTitle" value="${assignment.title} - AI Grading Platform" scope="request"/>
<jsp:include page="header.jsp"/>

<div class="mb-4">
    <a href="${pageContext.request.contextPath}/assignments" class="btn btn-outline-secondary btn-sm mb-3">
        <i class="bi bi-arrow-left"></i> Quay lại danh sách
    </a>
    <div class="d-flex justify-content-between align-items-start">
        <div>
            <h2 class="fw-bold mb-1">${assignment.title}</h2>
            <div class="text-muted">
                <span><i class="bi bi-person-fill me-1"></i>Giảng viên: <strong>${not empty assignment.teacherName ? assignment.teacherName : "Prof. Robert Davis"}</strong></span>
                <span class="mx-2">•</span>
                <span><i class="bi bi-calendar-event me-1"></i>Tạo ngày: ${assignment.createdAt}</span>
            </div>
        </div>

        <div>
            <c:choose>
                <c:when test="${assignment.expired}">
                    <span class="badge bg-danger fs-6 p-2">
                        <i class="bi bi-exclamation-octagon-fill me-1"></i>Đã Hết Hạn
                    </span>
                </c:when>
                <c:otherwise>
                    <span class="badge bg-success fs-6 p-2">
                        <i class="bi bi-check-circle-fill me-1"></i>Đang Mở Nộp Bài
                    </span>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</div>

<div class="row">
    <!-- Left Column: Details -->
    <div class="col-lg-7 mb-4">
        <div class="card card-custom p-4 mb-4">
            <h5 class="fw-bold border-bottom pb-2 mb-3">
                <i class="bi bi-info-circle text-primary me-2"></i>Thông Tin Chi Tiết
            </h5>

            <div class="row mb-3">
                <div class="col-sm-4 text-muted">Hạn nộp bài:</div>
                <div class="col-sm-8 fw-bold ${assignment.expired ? 'text-danger' : 'text-success'}">
                    ${assignment.formattedDeadline}
                </div>
            </div>

            <div class="row mb-3">
                <div class="col-sm-4 text-muted">Điểm tối đa:</div>
                <div class="col-sm-8 fw-semibold">${assignment.maxPoints} Điểm</div>
            </div>

            <c:if test="${not empty assignment.prototypeUrl}">
                <div class="row mb-3">
                    <div class="col-sm-4 text-muted">Prototype URL:</div>
                    <div class="col-sm-8">
                        <a href="${assignment.prototypeUrl}" target="_blank" class="text-decoration-none">
                            <i class="bi bi-box-arrow-up-right me-1"></i>${assignment.prototypeUrl}
                        </a>
                    </div>
                </div>
            </c:if>

            <c:if test="${not empty assignment.promptFileUrl}">
                <div class="row mb-3">
                    <div class="col-sm-4 text-muted">Đề bài / Prompt:</div>
                    <div class="col-sm-8">
                        <code>${assignment.promptFileUrl}</code>
                    </div>
                </div>
            </c:if>

            <div class="mb-3">
                <div class="text-muted mb-1">Tiêu chí chấm điểm (Rubric):</div>
                <pre class="bg-light p-3 rounded border font-monospace small mb-0">${assignment.rubricJson}</pre>
            </div>
        </div>

        <!-- Submissions List for this assignment -->
        <div class="card card-custom p-4">
            <h5 class="fw-bold border-bottom pb-2 mb-3">
                <i class="bi bi-list-check text-primary me-2"></i>Danh Sách Bài Đã Nộp (${submissions.size()})
            </h5>

            <c:choose>
                <c:when test="${not empty submissions}">
                    <div class="table-responsive">
                        <table class="table table-sm table-hover align-middle mb-0">
                            <thead class="table-light">
                                <tr>
                                    <th>#ID</th>
                                    <th>Sinh Viên / Nhóm</th>
                                    <th>Định Dạng</th>
                                    <th>Thời Gian Nộp</th>
                                    <th>Trạng Thái</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach items="${submissions}" var="s">
                                    <tr>
                                        <td>#${s.submissionId}</td>
                                        <td>
                                            <div class="fw-semibold">${not empty s.studentName ? s.studentName : "Alex Nguyen"}</div>
                                            <small class="text-muted">${not empty s.teamCode ? s.teamCode : "G5"}</small>
                                        </td>
                                        <td><span class="badge bg-secondary">${s.artifactType}</span></td>
                                        <td><small class="text-muted">${s.submittedAt}</small></td>
                                        <td>
                                            <span class="badge ${s.status == 'graded' ? 'bg-success' : 'bg-primary'}">${s.status}</span>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="text-center py-4 text-muted">
                        <i class="bi bi-folder2-open fs-2 text-secondary d-block mb-1"></i>
                        Chưa có sinh viên nào nộp bài cho đề bài này.
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>

    <!-- Right Column: Submission Form (Student Action) -->
    <div class="col-lg-5">
        <div class="card card-custom p-4 shadow-sm border-top border-4 border-primary">
            <h5 class="fw-bold mb-3">
                <i class="bi bi-cloud-arrow-up-fill text-primary me-2"></i>Nộp Bài Trực Tuyến
            </h5>

            <c:choose>
                <c:when test="${assignment.expired}">
                    <!-- EXCEPTION PATH ALERT FOR EXPIRED DEADLINE -->
                    <div class="alert alert-danger" role="alert">
                        <i class="bi bi-x-octagon-fill me-2 fs-5"></i>
                        <strong>Hạn nộp bài đã kết thúc!</strong>
                        <div class="small mt-1">Hệ thống đã khóa cổng nộp cho bài tập này. Bạn không thể nộp bài trễ hạn.</div>
                    </div>
                    <button class="btn btn-secondary w-100 py-2" disabled>
                        <i class="bi bi-lock-fill me-1"></i> Đã Khóa Cổng Nộp
                    </button>
                </c:when>
                <c:otherwise>
                    <p class="text-muted small">
                        Hạn nộp còn hiệu lực. Bạn có thể nộp mã nguồn dạng file nén ZIP hoặc liên kết Repository.
                    </p>

                    <form action="${pageContext.request.contextPath}/submissions" method="POST">
                        <input type="hidden" name="assignmentId" value="${assignment.assignmentId}">

                        <div class="mb-3">
                            <label for="artifactType" class="form-label fw-semibold">Loại Tài Liệu Nộp</label>
                            <select class="form-select" id="artifactType" name="artifactType">
                                <option value="zip">Tệp Nén Mã Nguồn (.zip)</option>
                                <option value="git_repo">Kho Mã Nguồn Git Repo</option>
                                <option value="screenshot">Ảnh Chụp Giao Diện (Screenshot)</option>
                            </select>
                        </div>

                        <div class="mb-3">
                            <label for="filePath" class="form-label fw-semibold">Đường Dẫn Tệp Nộp</label>
                            <input type="text" class="form-control" id="filePath" name="filePath"
                                   placeholder="uploads/submissions/my_project.zip"
                                   value="uploads/submissions/assignment_${assignment.assignmentId}_submission.zip" required>
                        </div>

                        <div class="mb-3">
                            <label for="sha256" class="form-label fw-semibold">Mã Kiểm Tra Toàn Vẹn (SHA-256 Hash)</label>
                            <input type="text" class="form-control font-monospace small" id="sha256" name="sha256"
                                   placeholder="64 ký tự hex sha-256 hash"
                                   value="e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855">
                            <div class="form-text">Bảo vệ tính toàn vẹn và chống tấn công Zip Slip.</div>
                        </div>

                        <button type="submit" class="btn btn-success w-100 py-2 fw-semibold">
                            <i class="bi bi-check2-circle me-1"></i> Xác Nhận Nộp Bài
                        </button>
                    </form>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</div>

<jsp:include page="footer.jsp"/>
