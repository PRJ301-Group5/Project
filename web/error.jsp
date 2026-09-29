<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:set var="pageTitle" value="Lỗi Hệ Thống - AI Grading Platform" scope="request"/>
<jsp:include page="header.jsp"/>

<div class="row justify-content-center mt-5">
    <div class="col-md-7 col-lg-6">
        <div class="card card-custom p-4 text-center shadow-sm border-top border-4 border-danger">
            <div class="mb-3">
                <i class="bi bi-exclamation-triangle-fill text-danger" style="font-size: 4rem;"></i>
            </div>
            <h4 class="fw-bold text-danger mb-2">Đã Xảy Ra Lỗi Xử Lý</h4>
            <p class="text-muted mb-4">
                Hệ thống phát hiện một ngoại lệ hoặc yêu cầu không hợp lệ trong quá trình thực thi.
            </p>

            <div class="alert alert-danger text-start font-monospace small mb-4">
                <strong><i class="bi bi-bug me-1"></i>Chi tiết lỗi (Exception Info):</strong>
                <div class="mt-1">
                    <c:choose>
                        <c:when test="${not empty errorMessage}">
                            ${errorMessage}
                        </c:when>
                        <c:when test="${not empty exception}">
                            ${exception.message}
                        </c:when>
                        <c:otherwise>
                            Không tìm thấy tài nguyên hoặc bạn không có đủ thẩm quyền truy cập (Mã lỗi: 403 / 404 / 500).
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>

            <div class="d-flex justify-content-center gap-3">
                <a href="javascript:history.back()" class="btn btn-outline-secondary px-4">
                    <i class="bi bi-arrow-left me-1"></i> Quay Lại Trang Trước
                </a>
                <a href="${pageContext.request.contextPath}/assignments" class="btn btn-primary-custom px-4">
                    <i class="bi bi-house-door me-1"></i> Về Trang Chủ
                </a>
            </div>
        </div>
    </div>
</div>

<jsp:include page="footer.jsp"/>
