<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:set var="pageTitle" value="${isEdit ? 'Chỉnh Sửa Bài Tập' : 'Tạo Bài Tập Mới'} - AI Grading Platform" scope="request"/>
<jsp:include page="header.jsp"/>

<div class="row justify-content-center">
    <div class="col-lg-8">
        <div class="card card-custom p-4 shadow-sm">
            <div class="d-flex align-items-center mb-4 pb-2 border-bottom">
                <a href="${pageContext.request.contextPath}/assignments" class="btn btn-outline-secondary btn-sm me-3">
                    <i class="bi bi-arrow-left"></i> Quay Lại
                </a>
                <h4 class="fw-bold mb-0">
                    <c:choose>
                        <c:when test="${isEdit}">
                            <i class="bi bi-pencil-square text-warning me-2"></i>Chỉnh Sửa Bài Tập #${assignment.assignmentId}
                        </c:when>
                        <c:otherwise>
                            <i class="bi bi-plus-circle text-primary me-2"></i>Tạo Bài Tập Mới
                        </c:otherwise>
                    </c:choose>
                </h4>
            </div>

            <form action="${pageContext.request.contextPath}/assignments" method="POST">
                <input type="hidden" name="action" value="${isEdit ? 'update' : 'create'}">
                <c:if test="${isEdit}">
                    <input type="hidden" name="id" value="${assignment.assignmentId}">
                </c:if>

                <!-- Tiêu đề bài tập -->
                <div class="mb-3">
                    <label for="title" class="form-label fw-semibold">
                        Tiêu Đề Bài Tập <span class="text-danger">*</span>
                    </label>
                    <input type="text" class="form-control form-control-lg" id="title" name="title"
                           value="${assignment.title}" placeholder="Ví dụ: Peer-Review 1: Thiết kế Database & JWT Authentication" required>
                    <div class="form-text">Tiêu đề phải rõ ràng và thể hiện yêu cầu bài tập.</div>
                </div>

                <div class="row">
                    <!-- Hạn nộp (Deadline) -->
                    <div class="col-md-6 mb-3">
                        <label for="deadline" class="form-label fw-semibold">
                            Hạn Nộp Bài (Deadline) <span class="text-danger">*</span>
                        </label>
                        <input type="datetime-local" class="form-control" id="deadline" name="deadline"
                               value="${not empty rawDeadline ? rawDeadline : ''}" required>
                        <div class="form-text text-danger">Lưu ý: Hạn nộp không được ở trong quá khứ!</div>
                    </div>

                    <!-- Điểm tối đa -->
                    <div class="col-md-6 mb-3">
                        <label for="maxPoints" class="form-label fw-semibold">Điểm Tối Đa (Max Points)</label>
                        <input type="number" class="form-control" id="maxPoints" name="maxPoints"
                               value="${not empty rawMaxPoints ? rawMaxPoints : (assignment.maxPoints > 0 ? assignment.maxPoints : 100)}"
                               min="1" max="1000" step="1" required>
                    </div>
                </div>

                <!-- Prototype URL -->
                <div class="mb-3">
                    <label for="prototypeUrl" class="form-label fw-semibold">Đường dẫn Prototype / Giao diện mẫu</label>
                    <div class="input-group">
                        <span class="input-group-text"><i class="bi bi-link-45deg"></i></span>
                        <input type="url" class="form-control" id="prototypeUrl" name="prototypeUrl"
                               value="${assignment.prototypeUrl}" placeholder="https://figma.com/... hoặc https://github.com/...">
                    </div>
                </div>

                <!-- Prompt File URL -->
                <div class="mb-3">
                    <label for="promptFileUrl" class="form-label fw-semibold">Đường dẫn Prompt Đề Bài (Prompt File URL)</label>
                    <div class="input-group">
                        <span class="input-group-text"><i class="bi bi-file-earmark-text"></i></span>
                        <input type="text" class="form-control" id="promptFileUrl" name="promptFileUrl"
                               value="${assignment.promptFileUrl}" placeholder="uploads/prompts/assignment_spec.md">
                    </div>
                </div>

                <!-- Rubric JSON -->
                <div class="mb-4">
                    <label for="rubricJson" class="form-label fw-semibold">Tiêu Chí Chấm Điểm Rubric (Định dạng JSON)</label>
                    <textarea class="form-control font-monospace" id="rubricJson" name="rubricJson" rows="3"
                              placeholder='{"ui_match": 30, "test_cases": 40, "code_quality": 30}'>${not empty assignment.rubricJson ? assignment.rubricJson : '{"ui_match": 30, "test_cases": 40, "code_quality": 30}'}</textarea>
                    <div class="form-text">Rubric theo tiêu chuẩn École 42 (Tổng trọng số = 100%).</div>
                </div>

                <!-- Buttons -->
                <div class="d-flex justify-content-end gap-2">
                    <a href="${pageContext.request.contextPath}/assignments" class="btn btn-outline-secondary px-4">
                        Hủy Bỏ
                    </a>
                    <button type="submit" class="btn btn-primary-custom px-4">
                        <c:choose>
                            <c:when test="${isEdit}">
                                <i class="bi bi-check2-circle me-1"></i> Lưu Cập Nhật
                            </c:when>
                            <c:otherwise>
                                <i class="bi bi-plus-lg me-1"></i> Tạo Bài Tập Mới
                            </c:otherwise>
                        </c:choose>
                    </button>
                </div>
            </form>
        </div>
    </div>
</div>

<jsp:include page="footer.jsp"/>
