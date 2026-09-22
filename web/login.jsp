<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:set var="pageTitle" value="Đăng Nhập - AI Grading Platform" scope="request"/>
<jsp:include page="header.jsp"/>

<div class="row justify-content-center mt-4">
    <div class="col-md-5 col-lg-4">
        <div class="card card-custom p-4 shadow-sm">
            <div class="text-center mb-4">
                <div class="d-inline-flex align-items-center justify-content-center bg-primary text-white rounded-circle mb-3" style="width: 56px; height: 56px;">
                    <i class="bi bi-shield-lock-fill fs-3"></i>
                </div>
                <h4 class="fw-bold">Đăng Nhập Hệ Thống</h4>
                <p class="text-muted small">Nhập thông tin xác thực để bắt đầu phiên làm việc</p>
            </div>

            <form action="${pageContext.request.contextPath}/login" method="POST">
                <div class="mb-3">
                    <label for="email" class="form-label fw-semibold">Địa chỉ Email</label>
                    <div class="input-group">
                        <span class="input-group-text"><i class="bi bi-envelope"></i></span>
                        <input type="email" class="form-control" id="email" name="email"
                               value="${not empty savedEmail ? savedEmail : param.email}"
                               placeholder="name@school.edu" required autofocus>
                    </div>
                </div>

                <div class="mb-3">
                    <label for="password" class="form-label fw-semibold">Mật khẩu</label>
                    <div class="input-group">
                        <span class="input-group-text"><i class="bi bi-key"></i></span>
                        <input type="password" class="form-control" id="password" name="password"
                               placeholder="••••••••" required>
                    </div>
                </div>

                <!-- Cookie Remember Me checkbox -->
                <div class="form-check mb-3">
                    <input class="form-check-input" type="checkbox" id="rememberMe" name="rememberMe"
                           ${not empty savedEmail ? "checked" : ""}>
                    <label class="form-check-label text-muted small" for="rememberMe">
                        Ghi nhớ Email trên trình duyệt này (Lưu Cookie 7 ngày)
                    </label>
                </div>

                <button type="submit" class="btn btn-primary-custom w-100 py-2 mb-3">
                    <i class="bi bi-box-arrow-in-right me-1"></i> Đăng Nhập
                </button>
            </form>

            <div class="border-top pt-3 mt-2">
                <div class="text-muted small mb-2 text-center">Tài khoản thử nghiệm nhanh:</div>
                <div class="d-flex gap-2">
                    <button type="button" class="btn btn-outline-warning btn-sm w-50" onclick="fillTeacher()">
                        <i class="bi bi-mortarboard-fill me-1"></i> Giảng Viên
                    </button>
                    <button type="button" class="btn btn-outline-success btn-sm w-50" onclick="fillStudent()">
                        <i class="bi bi-person-fill me-1"></i> Sinh Viên
                    </button>
                </div>
            </div>
        </div>
    </div>
</div>

<script>
    function fillTeacher() {
        document.getElementById('email').value = 'teacher@school.edu';
        document.getElementById('password').value = 'admin123';
    }
    function fillStudent() {
        document.getElementById('email').value = 'student@school.edu';
        document.getElementById('password').value = 'student123';
    }
</script>

<jsp:include page="footer.jsp"/>
