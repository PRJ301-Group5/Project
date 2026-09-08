# AI Grading Platform & Git Analytics Teamwork Assessor
## PRJ301 Java Web Enterprise • RBL Track 5 • École 42 Peer-Review • Moulinette Engine

Hệ thống đánh giá học thuật tự động kết hợp **Java Web MVC2**, **Microsoft SQL Server / SQLite Relational Database (19 bảng chuẩn 3NF)**, **Gateway Servlet Filter SHA-256**, **Docker Sandbox Execution Engine**, **Hàng đợi bất đồng bộ Redis**, và **Bộ phân tích Git Analytics (Nhóm 5 - Đánh giá đóng góp cá nhân & chống ỷ lại / free-riding)**.

---

## 🌟 Tính Năng Nổi Bật & Các Trụ Cột Kỹ Thuật

### 1. Nhóm 5: Git Analytics Teamwork Assessor (Đánh giá Đóng góp Cá nhân)
- **Định lượng sự công bằng**: Phân tích lịch sử commit/push thực tế trên Git để xác định tỷ lệ đóng góp của từng thành viên.
- **Bộ chỉ số toàn diện**:
  - $C_{eff}$: Số commit hữu ích (lọc bỏ merge tự động và commit rác).
  - $L_{net}$: Net Code Churn ($L_{added} \times 0.8 - L_{deleted} \times 0.2$).
  - $D_{time}$: Time Entropy (độ phân bố commit đều đặn qua các tuần vs dồn sát hạn).
  - $S_{msg}$: Điểm chuẩn ngữ nghĩa Conventional Commits (`feat:`, `fix:`, `refactor:`, `test:`).
  - $M_{own}$: Độ sở hữu các module kiến trúc cốt lõi (Servlets, Services, DAOs).
- **Công thức Điểm Đóng Góp Cá Nhân (Individual Contribution Score - ICS %)**:
  $$ICS_i = 0.20 \cdot \widehat{C}_{eff, i} + 0.30 \cdot \widehat{L}_{net, i} + 0.20 \cdot D_{time, i} + 0.15 \cdot S_{msg, i} + 0.15 \cdot M_{own, i}$$
- **Phát hiện Bất thường & Free-rider**:
  - Cảnh báo cờ đỏ `GHOST_COMMITTER`: Đóng góp dưới 15% khối lượng nhóm.
  - Cảnh báo cờ đỏ `11TH_HOUR_RUSHER`: Hơn 75% commit dồn vào 12h trước deadline.
  - Cảnh báo `FORMATTING_SPAMMER`: Thay đổi nhiều dòng nhưng chỉ sửa khoảng trắng hoặc file cấu hình.
- **Tích hợp AI Sư phạm (Google Gemini / OpenAI)**: Diễn giải dữ liệu phân tích thô thành báo cáo sư phạm khách quan, tóm tắt vai trò và đề xuất điều chỉnh điểm số.

### 2. Cổng Nộp Bài & Servlet Filter SHA-256 (Artifact Gateway)
- Sinh viên nộp mã nguồn dưới dạng file **ZIP**.
- `Sha256IntegrityFilter`: Chặn ngay tại gateway, băm SHA-256 từ byte stream nhị phân và đối chiếu với checksum khai báo.
- **Bảo mật chống Zip Slip**: Quét và ngăn chặn các file nén chứa path traversal (`../`) xâm nhập hệ điều hành.

### 3. Môi Trường Thực Thi Docker Sandbox
- Biên dịch và chạy thử mã nguồn trong container Docker biệt lập:
  - `--network none`: Cách ly mạng 100%, ngăn chặn gọi ra internet hoặc truy cập mạng nội bộ.
  - `--memory 512m` & `--cpus 1.0`: Giới hạn bộ nhớ và CPU chống chiếm dụng tài nguyên.
  - `--pids-limit 64`: Ngăn chặn tấn công Fork Bomb.
  - **Watchdog Timeout (45s)**: Tự động tiêu diệt tiến trình nếu gặp vòng lặp vô tận `while(true)`.
  - Tích hợp bộ **Local Process Sandbox Runner** dự phòng an toàn trên môi trường Windows dev.

### 4. Hàng Đợi Chấm Bài Bất Đồng Bộ Redis (Grading Job Queue)
- Điều phối các tác vụ chấm bài và gọi AI không gây nghẽn kết nối người dùng.
- **Priority Queue**: Ưu tiên bài nộp theo độ khẩn cấp deadline.
- **Retry Logic (Exponential Backoff)**: Tự động thử lại tối đa 3 lần (3s, 9s, 27s) khi gặp sự cố tạm thời.

### 5. Quy Trình Chấm Chéo École 42 & Chấm Tự Động Moulinette
- **Vòng tròn chấm chéo (Rotational Review)**: Luồng chấm khép kín $G_1 \to G_2 \to \dots \to G_7 \to G_1$.
- **Ví Điểm Sửa Lỗi (Correction Points Economy)**:
  - Bắt đầu với **5 điểm**.
  - Tiêu tốn **-1 điểm** khi nộp đồ án yêu cầu được chấm.
  - Nhận thưởng **+1 điểm** khi hoàn thành chấm bài cho nhóm bạn theo vòng luân chuyển.
- **Peer-Review Rubric (30/40/30)**:
  $$\text{Final Score} = (\text{Sandbox} \times 0.30) + (\text{MVC2 / Clean Code} \times 0.40) + (\text{Peer Defense} \times 0.30)$$
- **Moulinette Auto-Grader**: Chạy song song bộ Public Test Cases (smoke test, hash check) và Hidden Test Cases (bắt ngoại lệ, tấn công Zip Slip, timeout).

---

## 🏗️ Sơ Đồ Cơ Sở Dữ Liệu Quan Hệ (19 Bảng Chuẩn 3NF)

```
+-----------------------------------------------------------------------------------------+
|                                    DATABASE SCHEMA                                      |
+-----------------------------------------------------------------------------------------+
| Users                 (user_id, full_name, email, password_hash, role, created_at)      |
| Teachers              (teacher_id, user_id, employee_code, department)                  |
| Students              (student_id, user_id, student_code, class_name)                    |
| Subjects              (subject_id, subject_code, subject_name, semester)                 |
| StudentSubjects       (student_id, subject_id)                                          |
| Classes               (class_id, class_name, department, teacher_id)                    |
| Teams (G1 - G7)       (team_id, team_code, team_name, track_name, project_title, repo)  |
| TeamMembers           (id, team_id, user_id, role_in_team)                              |
| Assignments           (assignment_id, teacher_id, subject_id, title, ... rubric_json)   |
| Submissions           (submission_id, student_id, team_id, zip_file_path, sha256_hash)  |
| AIAnalysisResults     (result_id, submission_id, similarity_score, layout_report)       |
| GitMetrics (Nhóm 5)   (metric_id, submission_id, author_name, commits, churn, entropy) |
| RotationalReviews     (review_id, reviewer_team_id, target_team_id, 30/40/30 scores)    |
| CorrectionPointsWallet(wallet_id, user_id, team_id, points_balance)                     |
| CorrectionPointTrans  (trans_id, wallet_id, amount, action_type, description)           |
| MoulinetteTestCases   (test_id, test_code, title, test_type: PUBLIC/HIDDEN, weight)     |
| MoulinetteResults     (result_id, submission_id, test_id, is_passed, exec_time, log)   |
| Grades                (grade_id, submission_id, graded_by, score, comment)               |
| Appeals               (appeal_id, grade_id, student_id, reason, status)                 |
| OtpCodes              (otp_id, email, code, type, expires_at, is_used)                   |
+-----------------------------------------------------------------------------------------+
```

---

## 🚀 Hướng Dẫn Khởi Động & Chạy Thử

### Yêu Cầu Môi Trường
- **Java JDK 8+** (được cài đặt tại `C:\Program Files\Java\jdk1.8.0_202`)
- **Windows OS**

### 1. Khởi Động Backend Server
Nhấp đúp chuột vào `run_server.bat` hoặc thực thi bằng PowerShell:
```powershell
& "C:\Program Files\Java\jdk1.8.0_202\bin\java.exe" "-Djava.library.path=backend/lib" -cp "backend/bin;backend/lib/*" com.aigrading.Main 8080
```
Truy cập trình duyệt tại: **[http://localhost:8080](http://localhost:8080)**

### 2. Biên Dịch Mã Nguồn
```powershell
& "C:\Program Files\Java\jdk1.8.0_202\bin\javac.exe" -encoding UTF-8 -cp "backend/bin;backend/lib/*" -d "backend/bin" (Get-ChildItem -Recurse -Include *.java backend/src, tests | Select-Object -ExpandProperty FullName)
```

### 3. Chạy Toàn Bộ Bộ Kiểm Thử Tự Động (85 Test Cases)
```powershell
& "C:\Program Files\Java\jdk1.8.0_202\bin\java.exe" "-Djava.library.path=backend/lib" -cp "backend/bin;backend/lib/*" tests.BackendTests
```

---

## 🔑 Tài Khoản Trải Nghiệm Mẫu

| Vai Trò | Email | Mật Khẩu | Chi Tiết |
|---|---|---|---|
| **Giảng viên** | `teacher@school.edu` | `Teacher@123` | Prof. Robert Davis (`TCH-2026-01`) |
| **Nhóm 5 - Leader** | `huy.nguyen@school.edu` | `Student@123` | Huy Nguyen (Leader G5 GitPulse - 54.8% ICS) |
| **Nhóm 5 - Thành viên** | `linh.tran@school.edu` | `Student@123` | Linh Tran (Active Dev G5 - 38.2% ICS) |
| **Nhóm 5 - Free-rider** | `minh.le@school.edu` | `Student@123` | Minh Le (Ghost Committer Cờ đỏ - 7.0% ICS) |
| **Sinh viên G1** | `student@school.edu` | `Student@123` | Alex Nguyen (`STU-10901`, G1 Clean Code) |
| **Admin** | `admin@school.edu` | `Admin@123` | Admin Supervisor |

---

## 🧪 Các Tab Chuyên Biệt Trên Giao Diện Web

1. **Git Analytics (Nhóm 5)**:
   - Biểu đồ và bảng thống kê đóng góp cá nhân (LoC+, LoC-, Net Churn, Time Entropy, Semantic Quality).
   - Thước đo tỷ lệ đóng góp (ICS %) chuẩn hóa 100%.
   - Cảnh báo cờ đỏ `GHOST_COMMITTER` và `11TH_HOUR_RUSHER`.
   - Khung nhận xét AI sư phạm tổng hợp từ Google Gemini / OpenAI.
   - Nút chạy thử nghiệm với commit stream tùy biến.
2. **Peer-Review École 42**:
   - Vòng tròn luân chuyển khép kín $G_1 \to G_2 \dots G_7 \to G_1$.
   - Form chấm điểm theo Rubric 30% Sandbox - 40% MVC2/Clean Code - 30% Peer Defense.
   - Ví điểm sửa lỗi (Correction Points Wallet) hiển thị số dư và lịch sử giao dịch.
   - Nộp bài đánh giá được cộng ngay **+1 Correction Point**.
3. **Moulinette Tests**:
   - Danh sách kiểm thử tự động Public Test Cases và Hidden Test Cases.
   - Nút kích hoạt kiểm thử với thời gian thực thi (ms) và terminal log chi tiết.
