import sys
import os
from pptx import Presentation
from pptx.util import Inches, Pt
from pptx.enum.text import PP_ALIGN
from pptx.dml.color import RGBColor
from pptx.enum.shapes import MSO_SHAPE

def create_deck():
    prs = Presentation()
    # 16:9 widescreen
    prs.slide_width = Inches(13.333)
    prs.slide_height = Inches(7.5)
    blank_layout = prs.slide_layouts[6]

    # Color definitions
    BG_DARK = RGBColor(15, 23, 42)       # #0f172a Slate 900
    CARD_BG = RGBColor(30, 41, 59)       # #1e293b Slate 800
    CARD_BORDER = RGBColor(51, 65, 85)   # #334155 Slate 700
    ACCENT_INDIGO = RGBColor(99, 102, 241) # #6366f1
    ACCENT_CYAN = RGBColor(6, 182, 212)    # #06b6d4
    ACCENT_EMERALD = RGBColor(16, 185, 129)# #10b981
    ACCENT_AMBER = RGBColor(245, 158, 11)  # #f59e0b
    ACCENT_ROSE = RGBColor(239, 68, 68)    # #ef4444
    TEXT_WHITE = RGBColor(248, 250, 252)   # #f8fafc
    TEXT_MUTED = RGBColor(148, 163, 184)   # #94a3b8

    def set_slide_background(slide):
        bg = slide.shapes.add_shape(MSO_SHAPE.RECTANGLE, 0, 0, prs.slide_width, prs.slide_height)
        bg.fill.solid()
        bg.fill.fore_color.rgb = BG_DARK
        bg.line.fill.background()
        return bg

    def add_header(slide, tag_text, title_text, subtitle_text=""):
        # Tag Badge
        badge = slide.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, Inches(0.8), Inches(0.4), Inches(3.2), Inches(0.35))
        badge.fill.solid()
        badge.fill.fore_color.rgb = RGBColor(30, 58, 138) # Dark blue
        badge.line.color.rgb = ACCENT_CYAN
        tf = badge.text_frame
        tf.word_wrap = True
        p = tf.paragraphs[0]
        p.text = tag_text.upper()
        p.font.size = Pt(10)
        p.font.bold = True
        p.font.color.rgb = ACCENT_CYAN
        p.alignment = PP_ALIGN.CENTER

        # Title
        title_box = slide.shapes.add_textbox(Inches(0.8), Inches(0.8), Inches(11.7), Inches(0.8))
        tf = title_box.text_frame
        tf.word_wrap = True
        p = tf.paragraphs[0]
        p.text = title_text
        p.font.size = Pt(22)
        p.font.bold = True
        p.font.color.rgb = TEXT_WHITE

        if subtitle_text:
            p2 = tf.add_paragraph()
            p2.text = subtitle_text
            p2.font.size = Pt(12)
            p2.font.color.rgb = TEXT_MUTED

    def add_card(slide, left, top, width, height, border_color=CARD_BORDER, bg_color=CARD_BG):
        card = slide.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, left, top, width, height)
        card.fill.solid()
        card.fill.fore_color.rgb = bg_color
        card.line.color.rgb = border_color
        card.line.width = Pt(1.5)
        return card

    # =========================================================================
    # SLIDE 1: TITLE SLIDE
    # =========================================================================
    slide1 = prs.slides.add_slide(blank_layout)
    set_slide_background(slide1)

    # Accent decorative glow
    glow = slide1.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, Inches(0.8), Inches(1.0), Inches(11.7), Inches(5.5))
    glow.fill.solid()
    glow.fill.fore_color.rgb = RGBColor(20, 30, 55)
    glow.line.color.rgb = RGBColor(99, 102, 241)
    glow.line.width = Pt(2)

    # Title container
    t_box = slide1.shapes.add_textbox(Inches(1.2), Inches(1.3), Inches(10.9), Inches(4.8))
    tf1 = t_box.text_frame
    tf1.word_wrap = True

    p = tf1.paragraphs[0]
    p.text = "PEER-REVIEW 1 (TUẦN 3) • PRJ301 & RBL TRACK 5"
    p.font.size = Pt(13)
    p.font.bold = True
    p.font.color.rgb = ACCENT_CYAN
    p.space_after = Pt(14)

    p = tf1.add_paragraph()
    p.text = "ĐÁNH GIÁ DATABASE DESIGN &\nCƠ CHẾ JWT AUTHENTICATION (4.1.3)"
    p.font.size = Pt(28)
    p.font.bold = True
    p.font.color.rgb = TEXT_WHITE
    p.space_after = Pt(18)

    p = tf1.add_paragraph()
    p.text = "Làm chủ JDBC: Nền móng Kỹ thuật Sống còn để AI Truy xuất & Cấu trúc hóa Dữ liệu Học tập"
    p.font.size = Pt(15)
    p.font.color.rgb = ACCENT_EMERALD
    p.space_after = Pt(28)

    p = tf1.add_paragraph()
    p.text = "Nhóm 5: Git Analytics Teamwork Assessor (Định lượng Sự Công bằng & Chống Free-riding)\nGiảng viên hướng dẫn: Bộ môn SE/PRJ301 • Môi trường: Java Web MVC2, SQL Server, Docker & Redis"
    p.font.size = Pt(12)
    p.font.color.rgb = TEXT_MUTED

    # =========================================================================
    # SLIDE 2: BỐI CẢNH & SỨ MỆNH NHÓM 5
    # =========================================================================
    slide2 = prs.slides.add_slide(blank_layout)
    set_slide_background(slide2)
    add_header(slide2, "Bối cảnh Đề tài", "1. Bối cảnh & Sứ mệnh Nhóm 5: Git Analytics Teamwork Assessor",
               "Giải quyết triệt để tình trạng ỷ lại (Free-riding) trong làm việc nhóm qua phân tích lịch sử Git tự động")

    # 3 Columns
    col_w = Inches(3.7)
    h = Inches(4.8)
    top_pos = Inches(1.9)

    # Card 1: Problem
    add_card(slide2, Inches(0.8), top_pos, col_w, h, ACCENT_ROSE)
    tb = slide2.shapes.add_textbox(Inches(0.95), top_pos + Inches(0.15), col_w - Inches(0.3), h - Inches(0.3))
    tf = tb.text_frame
    tf.word_wrap = True
    p = tf.paragraphs[0]
    p.text = "⚠️ VẤN ĐỀ THỰC TẾ"
    p.font.size = Pt(14)
    p.font.bold = True
    p.font.color.rgb = ACCENT_ROSE
    p.space_after = Pt(10)

    bullets = [
        "Hiện tượng Free-rider: Thành viên không viết code nhưng vẫn đứng tên nhận điểm chung.",
        "Commit gian lận & Panic Commits: Dồn commit vào 6-12h trước deadline để đối phó.",
        "Spam khoảng trắng / thư mục vendor: Đổi tên file, commit node_modules, .jar để thổi phồng LoC.",
        "Quá tải của Giảng viên: Không thể duyệt hàng trăm commit log thủ công của từng nhóm."
    ]
    for b in bullets:
        p = tf.add_paragraph()
        p.text = "• " + b
        p.font.size = Pt(11)
        p.font.color.rgb = TEXT_WHITE
        p.space_after = Pt(8)

    # Card 2: Solution
    add_card(slide2, Inches(4.8), top_pos, col_w, h, ACCENT_CYAN)
    tb = slide2.shapes.add_textbox(Inches(4.95), top_pos + Inches(0.15), col_w - Inches(0.3), h - Inches(0.3))
    tf = tb.text_frame
    tf.word_wrap = True
    p = tf.paragraphs[0]
    p.text = "💡 GIẢI PHÁP NHÓM 5"
    p.font.size = Pt(14)
    p.font.bold = True
    p.font.color.rgb = ACCENT_CYAN
    p.space_after = Pt(10)

    bullets = [
        "Git Analytics Engine: Tự động bóc tách commit history từ ZIP/repo.",
        "Bộ chỉ số đa chiều: Commits, Net Churn, Time Entropy, Conventional Commits, Module Ownership.",
        "Công thức điểm ICS %: Chuẩn hóa 100% tỷ lệ đóng góp thực tế trong nhóm.",
        "Phát hiện Anomaly: Gắn cờ tự động GHOST_COMMITTER và 11TH_HOUR_RUSHER."
    ]
    for b in bullets:
        p = tf.add_paragraph()
        p.text = "• " + b
        p.font.size = Pt(11)
        p.font.color.rgb = TEXT_WHITE
        p.space_after = Pt(8)

    # Card 3: Milestone Week 3
    add_card(slide2, Inches(8.8), top_pos, col_w, h, ACCENT_EMERALD)
    tb = slide2.shapes.add_textbox(Inches(8.95), top_pos + Inches(0.15), col_w - Inches(0.3), h - Inches(0.3))
    tf = tb.text_frame
    tf.word_wrap = True
    p = tf.paragraphs[0]
    p.text = "🎯 MỤC TIÊU PEER-REVIEW 1"
    p.font.size = Pt(14)
    p.font.bold = True
    p.font.color.rgb = ACCENT_EMERALD
    p.space_after = Pt(10)

    bullets = [
        "Database Design chuẩn 3NF: 19 bảng bao trùm Users, Teams G1-G7, Submissions, GitMetrics, École 42.",
        "Xác thực JWT Authentication (4.1.3): Stateless token, ký HMAC-SHA256, phân quyền RBAC.",
        "Làm chủ tầng JDBC: Nền móng sống còn để nạp và cấu trúc dữ liệu cho AI.",
        "Kiểm thử tự động: 85/85 test cases pass 100% trên cả SQL Server và SQLite."
    ]
    for b in bullets:
        p = tf.add_paragraph()
        p.text = "• " + b
        p.font.size = Pt(11)
        p.font.color.rgb = TEXT_WHITE
        p.space_after = Pt(8)

    # =========================================================================
    # SLIDE 3: TẦM QUAN TRỌNG CỐT LÕI - LÀM CHỦ JDBC LÀ NỀN MÓNG CHO AI
    # =========================================================================
    slide3 = prs.slides.add_slide(blank_layout)
    set_slide_background(slide3)
    add_header(slide3, "Luận điểm Kỹ thuật Cốt lõi",
               "2. Tầm quan trọng: 'Làm chủ JDBC là Nền móng Kỹ thuật Sống còn cho AI'",
               "Tại sao mô hình AI (Gemini/OpenAI) phụ thuộc hoàn toàn vào chất lượng và tốc độ của Tầng JDBC DAO?")

    # Big Quote Box
    add_card(slide3, Inches(0.8), Inches(1.8), Inches(11.7), Inches(1.1), ACCENT_INDIGO, RGBColor(25, 30, 60))
    tb = slide3.shapes.add_textbox(Inches(1.0), Inches(1.85), Inches(11.3), Inches(1.0))
    tf = tb.text_frame
    tf.word_wrap = True
    p = tf.paragraphs[0]
    p.text = "“Việc làm chủ JDBC là nền móng kỹ thuật sống còn để AI có thể truy xuất và cấu trúc hóa dữ liệu học tập hiệu quả từ hệ thống cơ sở dữ liệu.”"
    p.font.size = Pt(13)
    p.font.bold = True
    p.font.color.rgb = ACCENT_CYAN

    # 3 Detail Cards
    w3 = Inches(3.7)
    h3 = Inches(3.9)
    y3 = Inches(3.1)

    cards_data = [
        ("1. Cung Cấp 'Structured Context' Chuẩn Xác", ACCENT_INDIGO, [
            "AI (LLMs) bị giới hạn bởi Token Window và nhạy cảm với dữ liệu rác.",
            "JDBC DAO chuyển đổi dữ liệu SQL thô (hàng nghìn dòng git commit) thành các DTOs/JSON cô đọng.",
            "Nếu JDBC truy vấn thiếu sót, AI sẽ ảo giác (hallucination) và đánh giá sai năng lực sinh viên."
        ]),
        ("2. Tối Ưu Độ Trễ & Hiệu Năng Thời Gian Thực", ACCENT_EMERALD, [
            "PreparedStatement: Tối ưu query execution plan trên SQL Server.",
            "Connection Pooling: Tái sử dụng socket connection, giảm 90% latency so với mở kết nối mới.",
            "Chỉ số nạp cho AI trong < 50ms, đảm bảo AI Grading trả kết quả nhanh chóng cho sinh viên."
        ]),
        ("3. Bảo Đảm Toàn Vẹn ACID & Chống SQLi", ACCENT_AMBER, [
            "Mã nguồn sinh viên có thể chứa payload tấn công hoặc SQL Injection ngầm.",
            "Tầng JDBC với Parameterized Binding ngăn chặn triệt để SQL Injection ở mức driver.",
            "Giao dịch (Transactions Atomicity): Lưu đồng thời GitMetrics, Submissions, và ví điểm École 42 an toàn."
        ])
    ]

    for i, (ctitle, ccolor, clist) in enumerate(cards_data):
        x = Inches(0.8 + i * 4.0)
        add_card(slide3, x, y3, w3, h3, ccolor)
        tb = slide3.shapes.add_textbox(x + Inches(0.15), y3 + Inches(0.15), w3 - Inches(0.3), h3 - Inches(0.3))
        tf = tb.text_frame
        tf.word_wrap = True
        p = tf.paragraphs[0]
        p.text = ctitle
        p.font.size = Pt(12)
        p.font.bold = True
        p.font.color.rgb = ccolor
        p.space_after = Pt(8)
        for item in clist:
            p = tf.add_paragraph()
            p.text = "• " + item
            p.font.size = Pt(10.5)
            p.font.color.rgb = TEXT_WHITE
            p.space_after = Pt(6)

    # =========================================================================
    # SLIDE 4: THIẾT KẾ CƠ SỞ DỮ LIỆU TOÀN DIỆN (ERD 3NF)
    # =========================================================================
    slide4 = prs.slides.add_slide(blank_layout)
    set_slide_background(slide4)
    add_header(slide4, "Database Architecture", "3. Thiết kế Cơ sở Dữ liệu Toàn diện (ERD 19 Bảng Chuẩn 3NF)",
               "Mô hình hóa hoàn chỉnh cho cả Microsoft SQL Server 2019 và SQLite với quan hệ ràng buộc chặt chẽ")

    # 4 Module Cards
    mod_w = Inches(2.75)
    mod_h = Inches(4.9)
    mod_y = Inches(1.9)

    modules = [
        ("1. IDENTITY & ACADEMIC", ACCENT_INDIGO, [
            "Users: user_id, full_name, email, password_hash, role",
            "Teachers: employee_code, department",
            "Students: student_code, class_name",
            "Subjects: subject_code, semester",
            "StudentSubjects: Đăng ký môn",
            "Classes: Quản lý lớp học"
        ]),
        ("2. TEAMS & ARTIFACTS", ACCENT_CYAN, [
            "Teams: G1 đến G7, track_name, repo_url",
            "TeamMembers: Phân bổ leader / member",
            "Assignments: Prototype URL, Rubric JSON",
            "Submissions: zip_file_path, sha256_hash, priority, status",
            "AIAnalysisResults: similarity, layout"
        ]),
        ("3. GIT ANALYTICS (NHÓM 5)", ACCENT_EMERALD, [
            "GitMetrics: metric_id, submission_id",
            "total_commits, lines_added/deleted",
            "net_churn, commit_entropy (0-1.0)",
            "semantic_score, module_ownership",
            "contribution_score (ICS %)",
            "anomaly_flags: Ghost committer..."
        ]),
        ("4. ÉCOLE 42 & MOULINETTE", ACCENT_AMBER, [
            "RotationalReviews: G1->G2..->G7->G1",
            "Rubric: 30% Sandbox, 40% CleanCode, 30% Defense",
            "CorrectionPointsWallet: balance (5)",
            "CorrectionPointTransactions: ledger",
            "MoulinetteTestCases: Public & Hidden",
            "MoulinetteResults: exec time, logs"
        ])
    ]

    for i, (mtitle, mcolor, mitems) in enumerate(modules):
        x = Inches(0.8 + i * 2.95)
        add_card(slide4, x, mod_y, mod_w, mod_h, mcolor)
        tb = slide4.shapes.add_textbox(x + Inches(0.12), mod_y + Inches(0.12), mod_w - Inches(0.24), mod_h - Inches(0.24))
        tf = tb.text_frame
        tf.word_wrap = True
        p = tf.paragraphs[0]
        p.text = mtitle
        p.font.size = Pt(11)
        p.font.bold = True
        p.font.color.rgb = mcolor
        p.space_after = Pt(8)
        for it in mitems:
            p = tf.add_paragraph()
            p.text = "• " + it
            p.font.size = Pt(9.5)
            p.font.color.rgb = TEXT_WHITE
            p.space_after = Pt(6)

    # =========================================================================
    # SLIDE 5: THIẾT KẾ BẢNG CHUYÊN SÂU: GITMETRICS & CÔNG THỨC ICS
    # =========================================================================
    slide5 = prs.slides.add_slide(blank_layout)
    set_slide_background(slide5)
    add_header(slide5, "Nhóm 5 Deep Dive", "4. Bảng Dữ Liệu Chuyên Sâu GitMetrics & Công thức Điểm ICS %",
               "Định lượng công bằng đóng góp cá nhân dựa trên phân tích commit đa chiều và Entropy thời gian")

    # Left: Formula & Weights
    add_card(slide5, Inches(0.8), Inches(1.9), Inches(5.7), Inches(4.9), ACCENT_CYAN)
    tb = slide5.shapes.add_textbox(Inches(0.95), Inches(2.0), Inches(5.4), Inches(4.7))
    tf = tb.text_frame
    tf.word_wrap = True
    p = tf.paragraphs[0]
    p.text = "CÔNG THỨC ĐIỂM ĐÓNG GÓP (ICS %)"
    p.font.size = Pt(13)
    p.font.bold = True
    p.font.color.rgb = ACCENT_CYAN
    p.space_after = Pt(6)

    p = tf.add_paragraph()
    p.text = "ICS_i = 0.20·C_eff + 0.30·L_net + 0.20·D_time + 0.15·S_msg + 0.15·M_own"
    p.font.size = Pt(11.5)
    p.font.bold = True
    p.font.color.rgb = ACCENT_EMERALD
    p.space_after = Pt(10)

    items_ics = [
        "C_eff (20%): Số commit hữu ích (loại bỏ commit merge, commit rác).",
        "L_net (30%): Net Code Churn (LoC added × 0.8 - LoC deleted × 0.2).",
        "D_time (20%): Entropy phân bố thời gian (chống panic commits).",
        "S_msg (15%): Chuẩn Conventional Commits (feat:, fix:, refactor:).",
        "M_own (15%): Tỷ lệ phụ trách module kiến trúc lõi (Servlet, DAO)."
    ]
    for it in items_ics:
        p = tf.add_paragraph()
        p.text = "• " + it
        p.font.size = Pt(10.5)
        p.font.color.rgb = TEXT_WHITE
        p.space_after = Pt(5)

    # Right: Database Schema & Anomaly
    add_card(slide5, Inches(6.8), Inches(1.9), Inches(5.7), Inches(4.9), ACCENT_INDIGO)
    tb = slide5.shapes.add_textbox(Inches(6.95), Inches(2.0), Inches(5.4), Inches(4.7))
    tf = tb.text_frame
    tf.word_wrap = True
    p = tf.paragraphs[0]
    p.text = "CƠ CHẾ PHÁT HIỆN BẤT THƯỜNG (ANOMALY FLAGS)"
    p.font.size = Pt(13)
    p.font.bold = True
    p.font.color.rgb = ACCENT_INDIGO
    p.space_after = Pt(10)

    anomalies = [
        ("🚨 GHOST_COMMITTER", ACCENT_ROSE, "Đóng góp < 15% tổng khối lượng nhóm. Số commit <= 2, ít dòng code hữu ích. Tự động đề xuất hạ bậc điểm cá nhân."),
        ("⏰ 11TH_HOUR_RUSHER", ACCENT_AMBER, "Hơn 75% commit dồn vào 12h trước deadline, không hoạt động trong 3 tuần phát triển. Giảm 50% trọng số thời gian."),
        ("🧹 FORMATTING_SPAMMER", ACCENT_CYAN, "Đổi tên file hoặc format khoảng trắng trên file CSS/JSON để tăng LoC ảo. Hệ thống lọc bỏ khỏi L_net."),
        ("👑 CORE_ARCHITECT", ACCENT_EMERALD, "Đóng góp > 45% khối lượng kiến trúc cốt lõi, commit phân bố đều đặn và độ sở hữu module cao.")
    ]
    for an_title, an_col, an_desc in anomalies:
        p = tf.add_paragraph()
        p.text = an_title
        p.font.size = Pt(11)
        p.font.bold = True
        p.font.color.rgb = an_col
        p = tf.add_paragraph()
        p.text = an_desc
        p.font.size = Pt(9.5)
        p.font.color.rgb = TEXT_WHITE
        p.space_after = Pt(4)

    # =========================================================================
    # SLIDE 6: TẦNG TRUY XUẤT DỮ LIỆU JDBC NÂNG CAO (DAO PATTERN)
    # =========================================================================
    slide6 = prs.slides.add_slide(blank_layout)
    set_slide_background(slide6)
    add_header(slide6, "Tầng Dữ Liệu JDBC", "5. Tầng Truy Xuất Dữ Liệu JDBC Nâng Cao (Clean DAO Pattern)",
               "Kiến trúc kết nối linh hoạt giữa Microsoft SQL Server 2019 và SQLite với khả năng tự phục hồi")

    # 3 Cards
    w6 = Inches(3.7)
    h6 = Inches(4.9)
    y6 = Inches(1.9)

    jdbc_cards = [
        ("1. Dual Database Strategy", ACCENT_INDIGO, [
            "Hỗ trợ đồng thời SQL Server (Production) và SQLite (Local Dev).",
            "Adapter Pattern: Tự động chuyển đổi hàm thời gian (datetime('now') ➔ DATEADD / GETDATE()).",
            "Xử lý phân trang tương thích: LIMIT/OFFSET trên SQLite ➔ SELECT TOP / OFFSET-FETCH trên SQL Server.",
            "Tự động chuyển tiếp (failover) sang SQLite nếu SQL Server chưa bật."
        ]),
        ("2. Clean DAO & PreparedStatement", ACCENT_EMERALD, [
            "100% truy vấn thực thi qua PreparedStatement với tham số dạng Object... params.",
            "Loại bỏ hoàn toàn lỗ hổng SQL Injection (đã qua kiểm thử payload 'admin' OR '1'='1').",
            "Tách biệt hoàn toàn: Controller chỉ gọi Interface DAO, không viết SQL trong Controller.",
            "Ánh xạ tự động: ResultSet ➔ List<Map<String, Object>> và DTOs Models."
        ]),
        ("3. Connection & Transaction Safe", ACCENT_AMBER, [
            "Quản lý vòng đời kết nối với cú pháp Java 8 Try-with-resources.",
            "Tránh rò rỉ kết nối (Connection Leak) khi có số lượng lớn request đồng thời.",
            "Giao dịch Nguyên tử (Atomic Transactions): conn.setAutoCommit(false), commit() và rollback().",
            "Sẵn sàng lộ trình Sprint sau: Chuyển đổi/tích hợp JPA/Hibernate ORM."
        ])
    ]

    for i, (jtitle, jcolor, jitems) in enumerate(jdbc_cards):
        x = Inches(0.8 + i * 4.0)
        add_card(slide6, x, y6, w6, h6, jcolor)
        tb = slide6.shapes.add_textbox(x + Inches(0.15), y6 + Inches(0.15), w6 - Inches(0.3), h6 - Inches(0.3))
        tf = tb.text_frame
        tf.word_wrap = True
        p = tf.paragraphs[0]
        p.text = jtitle
        p.font.size = Pt(13)
        p.font.bold = True
        p.font.color.rgb = jcolor
        p.space_after = Pt(10)
        for item in jitems:
            p = tf.add_paragraph()
            p.text = "• " + item
            p.font.size = Pt(10.5)
            p.font.color.rgb = TEXT_WHITE
            p.space_after = Pt(8)

    # =========================================================================
    # SLIDE 7: CƠ CHẾ JWT AUTHENTICATION (MỤC 4.1.3)
    # =========================================================================
    slide7 = prs.slides.add_slide(blank_layout)
    set_slide_background(slide7)
    add_header(slide7, "Bảo Mật API", "6. Cơ chế Xác Thực Người Dùng JWT Authentication (Mục 4.1.3)",
               "Xác thực phi trạng thái (Stateless), chữ ký bảo mật HMAC-SHA256 và phân quyền vai trò RBAC")

    # 3 Steps of JWT
    w7 = Inches(3.7)
    h7 = Inches(4.9)
    y7 = Inches(1.9)

    jwt_parts = [
        ("HEADER (Thuật Toán)", ACCENT_INDIGO, [
            "{\n  \"alg\": \"HS256\",\n  \"typ\": \"JWT\"\n}",
            "Khai báo thuật toán mã hóa chữ ký HMAC-SHA256.",
            "Mã hóa Base64URL để đính kèm vào phần đầu của Token chuỗi 3 đoạn."
        ]),
        ("PAYLOAD (Claims)", ACCENT_CYAN, [
            "{\n  \"userId\": 5,\n  \"email\": \"huy.nguyen@school.edu\",\n  \"role\": \"student\",\n  \"teamId\": 5,\n  \"exp\": 1727180000000\n}",
            "Lưu trữ danh tính người dùng và vai trò bảo mật.",
            "Thời hạn sống (TTL): Mặc định 24 giờ (hết hạn tự động từ chối)."
        ]),
        ("SIGNATURE (Chữ Ký Số)", ACCENT_EMERALD, [
            "Signature = HMACSHA256(\n  Base64Url(Header) + \".\" + \n  Base64Url(Payload),\n  JWT_SECRET\n)",
            "Chống can thiệp (Tamper-proof): Bất kỳ sự thay đổi nào vào payload đều làm sai lệch chữ ký.",
            "Khóa bí mật: Đọc từ biến môi trường System.getenv('JWT_SECRET'), không hardcode."
        ])
    ]

    for i, (jtitle, jcolor, jitems) in enumerate(jwt_parts):
        x = Inches(0.8 + i * 4.0)
        add_card(slide7, x, y7, w7, h7, jcolor)
        tb = slide7.shapes.add_textbox(x + Inches(0.15), y7 + Inches(0.15), w7 - Inches(0.3), h7 - Inches(0.3))
        tf = tb.text_frame
        tf.word_wrap = True
        p = tf.paragraphs[0]
        p.text = jtitle
        p.font.size = Pt(13)
        p.font.bold = True
        p.font.color.rgb = jcolor
        p.space_after = Pt(10)
        for item in jitems:
            p = tf.add_paragraph()
            p.text = item
            if "{" in item or "Signature =" in item:
                p.font.size = Pt(9.5)
                p.font.color.rgb = ACCENT_AMBER
            else:
                p.font.size = Pt(10.5)
                p.font.color.rgb = TEXT_WHITE
            p.space_after = Pt(8)

    # =========================================================================
    # SLIDE 8: TẦNG GATEWAY & SERVLET FILTER ARCHITECTURE
    # =========================================================================
    slide8 = prs.slides.add_slide(blank_layout)
    set_slide_background(slide8)
    add_header(slide8, "Gateway Security", "7. Tầng Gateway & Kiến Trúc Servlet Filter (Bảo Mật 2 Lớp)",
               "Kiểm tra bảo mật và toàn vẹn dữ liệu ngay khi nhận request trước khi chuyển vào Controller")

    # 2 Big Cards: JwtAuthFilter & Sha256IntegrityFilter
    col_w8 = Inches(5.7)
    h8 = Inches(4.9)
    y8 = Inches(1.9)

    # Filter 1: JWT
    add_card(slide8, Inches(0.8), y8, col_w8, h8, ACCENT_INDIGO)
    tb = slide8.shapes.add_textbox(Inches(0.95), y8 + Inches(0.15), col_w8 - Inches(0.3), h8 - Inches(0.3))
    tf = tb.text_frame
    tf.word_wrap = True
    p = tf.paragraphs[0]
    p.text = "🛡️ LỚP 1: JwtAuthFilter (Xác thực & RBAC)"
    p.font.size = Pt(13)
    p.font.bold = True
    p.font.color.rgb = ACCENT_INDIGO
    p.space_after = Pt(10)

    jwt_f_items = [
        "Trích xuất Header: Đọc header 'Authorization: Bearer <token>' từ mọi incoming request.",
        "Xác thực chữ ký: Gọi JwtUtil.parseAndVerify(token), kiểm tra tính toàn vẹn và hạn dùng.",
        "Gán Security Context: Lưu trữ thông tin sinh viên / giảng viên vào Request Attributes.",
        "Phân quyền theo vai trò (RBAC):",
        "  - /api/assignments (POST/DELETE) ➔ Chỉ Giáo viên (teacher) hoặc Admin.",
        "  - /api/submissions (POST) ➔ Chỉ Sinh viên (student).",
        "  - /api/peer-review/submit (POST) ➔ Sinh viên thuộc nhóm chấm.",
        "Từ chối truy cập: Trả ngay HTTP 401 Unauthorized hoặc 403 Forbidden nếu không hợp lệ."
    ]
    for it in jwt_f_items:
        p = tf.add_paragraph()
        p.text = it
        p.font.size = Pt(10)
        p.font.color.rgb = TEXT_WHITE
        p.space_after = Pt(5)

    # Filter 2: SHA-256
    add_card(slide8, Inches(6.8), y8, col_w8, h8, ACCENT_CYAN)
    tb = slide8.shapes.add_textbox(Inches(6.95), y8 + Inches(0.15), col_w8 - Inches(0.3), h8 - Inches(0.3))
    tf = tb.text_frame
    tf.word_wrap = True
    p = tf.paragraphs[0]
    p.text = "🔒 LỚP 2: Sha256IntegrityFilter (Cổng Nộp Bài)"
    p.font.size = Pt(13)
    p.font.bold = True
    p.font.color.rgb = ACCENT_CYAN
    p.space_after = Pt(10)

    sha_f_items = [
        "Bắt chặn tại Gateway: Chặn ngay khi sinh viên upload file mã nguồn ZIP.",
        "Băm SHA-256 nhị phân: Đọc trực tiếp byte stream (8KB buffer), tính mã hash 64 ký tự hex.",
        "Đối chiếu Checksum: So khớp mã băm tính toán với mã do client gửi lên (chống can thiệp file/MITM).",
        "Quét an toàn Zip Slip: Kiểm tra tên tệp bên trong ZIP, từ chối ngay lập tức nếu phát hiện ký tự path traversal ('../../') trước khi giải nén.",
        "Kiểm tra Ví Điểm: Gọi PeerReviewService.debitCorrectionPoint(teamId) trừ 1 điểm sửa lỗi trước khi nộp.",
        "Đưa vào Hàng Đợi: Ghi nhận trạng thái 'queued' và đẩy tác vụ vào Redis Job Queue."
    ]
    for it in sha_f_items:
        p = tf.add_paragraph()
        p.text = it
        p.font.size = Pt(10)
        p.font.color.rgb = TEXT_WHITE
        p.space_after = Pt(5)

    # =========================================================================
    # SLIDE 9: QUY TRÌNH ÉCOLE 42 PEER-REVIEW & ĐIỂM SỬA LỖI
    # =========================================================================
    slide9 = prs.slides.add_slide(blank_layout)
    set_slide_background(slide9)
    add_header(slide9, "Mô Hình Đào Tạo", "8. Quy trình Đánh giá Đồng đẳng (École 42) & Điểm Sửa Lỗi",
               "Mô hình chấm chéo vòng tròn khép kín và cơ chế ví điểm sửa lỗi khuyến khích học hỏi đa chiều")

    # 3 Cards: Rotational Topology, Correction Points, Rubric 30/40/30
    w9 = Inches(3.7)
    h9 = Inches(4.9)
    y9 = Inches(1.9)

    e42_cards = [
        ("Vòng Tròn Chấm Chéo Khép Kín", ACCENT_CYAN, [
            "Nguyên lý: Nhóm G(i) chấm nhóm G(i+1) % 7.",
            "G1 ➔ G2 ➔ G3 ➔ G4 ➔ G5 ➔ G6 ➔ G7 ➔ G1.",
            "Không chấm đối ứng: Loại bỏ hoàn toàn tình trạng chấm thiên vị hoặc 'bắt tay nâng điểm' giữa hai nhóm bạn.",
            "Mỗi nhóm bắt buộc phải nghiên cứu sâu kiến trúc hệ thống của nhóm bạn trước buổi vấn đáp."
        ]),
        ("Ví Điểm Sửa Lỗi (Correction Points)", ACCENT_AMBER, [
            "Bắt đầu: Mỗi nhóm/sinh viên được cấp ban đầu 5 điểm sửa lỗi.",
            "Chi phí nộp bài (-1 điểm): Muốn gửi đồ án để người khác chấm, nhóm phải tiêu tốn 1 điểm.",
            "Thưởng đi chấm (+1 điểm): Nhóm cử đại diện đi chấm bài cho nhóm bạn theo lịch luân chuyển được cộng lại 1 điểm.",
            "Hết điểm = Khóa quyền nộp bài: Buộc 100% sinh viên phải đi review code cho bạn."
        ]),
        ("Bộ Tiêu Chuẩn Rubric (30/40/30)", ACCENT_EMERALD, [
            "1. Sandbox Compliance (30%): Build và chạy ổn định trong container Docker cách ly mạng.",
            "2. MVC2 / Clean Code (40%): Phân chia package rõ ràng, DAO/JDBC chuẩn, SHA-256 Filter bảo vệ artifact.",
            "3. Peer Defense (30%): Vấn đáp trực tiếp 15 phút, giải trình logic và trả lời chất vấn phản biện."
        ])
    ]

    for i, (etitle, ecolor, eitems) in enumerate(e42_cards):
        x = Inches(0.8 + i * 4.0)
        add_card(slide9, x, y9, w9, h9, ecolor)
        tb = slide9.shapes.add_textbox(x + Inches(0.15), y9 + Inches(0.15), w9 - Inches(0.3), h9 - Inches(0.3))
        tf = tb.text_frame
        tf.word_wrap = True
        p = tf.paragraphs[0]
        p.text = etitle
        p.font.size = Pt(12.5)
        p.font.bold = True
        p.font.color.rgb = ecolor
        p.space_after = Pt(10)
        for item in eitems:
            p = tf.add_paragraph()
            p.text = "• " + item
            p.font.size = Pt(10.5)
            p.font.color.rgb = TEXT_WHITE
            p.space_after = Pt(8)

    # =========================================================================
    # SLIDE 10: MOULINETTE AUTO-GRADER & DOCKER SANDBOX
    # =========================================================================
    slide10 = prs.slides.add_slide(blank_layout)
    set_slide_background(slide10)
    add_header(slide10, "Chấm Tự Động & Sandbox", "9. Chấm Tự Động Moulinette & Môi Trường Docker Sandbox",
               "Hệ thống kiểm thử tự động hai tầng kết hợp môi trường thực thi an toàn tuyệt đối")

    # 2 Cards: Moulinette Test Cases vs Docker Sandbox
    col_w10 = Inches(5.7)
    h10 = Inches(4.9)
    y10 = Inches(1.9)

    add_card(slide10, Inches(0.8), y10, col_w10, h10, ACCENT_ROSE)
    tb = slide10.shapes.add_textbox(Inches(0.95), y10 + Inches(0.15), col_w10 - Inches(0.3), h10 - Inches(0.3))
    tf = tb.text_frame
    tf.word_wrap = True
    p = tf.paragraphs[0]
    p.text = "🤖 MOULINETTE AUTOMATED TEST SUITE"
    p.font.size = Pt(13)
    p.font.bold = True
    p.font.color.rgb = ACCENT_ROSE
    p.space_after = Pt(10)

    moulinette_items = [
        ("PUBLIC TEST CASES (Smoke Tests)", ACCENT_CYAN, [
            "TC_PUB_01: Kiểm tra tính đúng đắn của mã băm SHA-256 64 ký tự.",
            "TC_PUB_02: Kiểm tra cấu trúc package chuẩn MVC2 (controller, service, dao, model).",
            "TC_PUB_03: Kiểm tra thiết lập cách ly mạng của container Docker (--network none)."
        ]),
        ("HIDDEN TEST CASES (Edge Cases & Security)", ACCENT_AMBER, [
            "TC_HID_01: Zip Slip Path Traversal (Kiểm tra bắt ngoại lệ khi upload '../../' payload).",
            "TC_HID_02: Author Spoofing & Empty Commits (Phát hiện commit giả mạo tác giả).",
            "TC_HID_03: Process Watchdog Timeout (Chống vòng lặp while(true), ngắt sau 45s)."
        ])
    ]
    for grp_title, grp_col, grp_bullets in moulinette_items:
        p = tf.add_paragraph()
        p.text = grp_title
        p.font.size = Pt(11)
        p.font.bold = True
        p.font.color.rgb = grp_col
        p.space_after = Pt(3)
        for b in grp_bullets:
            p = tf.add_paragraph()
            p.text = "  • " + b
            p.font.size = Pt(9.5)
            p.font.color.rgb = TEXT_WHITE
            p.space_after = Pt(3)

    add_card(slide10, Inches(6.8), y10, col_w10, h10, ACCENT_INDIGO)
    tb = slide10.shapes.add_textbox(Inches(6.95), y10 + Inches(0.15), col_w10 - Inches(0.3), h10 - Inches(0.3))
    tf = tb.text_frame
    tf.word_wrap = True
    p = tf.paragraphs[0]
    p.text = "📦 DOCKER SANDBOX ISOLATION"
    p.font.size = Pt(13)
    p.font.bold = True
    p.font.color.rgb = ACCENT_INDIGO
    p.space_after = Pt(10)

    sandbox_items = [
        "Network-Isolated (--network none): Container hoàn toàn không thể truy cập internet hay mạng nội bộ.",
        "Memory Limit (--memory=512m): Chống tràn bộ nhớ và tấn công tiêu hao RAM.",
        "CPU Limit (--cpus=1.0): Chỉ cấp phát tối đa 1 CPU core cho tiến trình biên dịch.",
        "Process Limit (--pids-limit=64): Ngăn chặn triệt để tấn công Fork Bomb làm treo máy chủ.",
        "Watchdog Timer (45s): Ngắt tiến trình cưỡng bức (docker kill) nếu mã nguồn sinh viên chạy quá thời gian.",
        "Local Fallback Runner: Tích hợp bộ chạy cách ly an toàn trên môi trường Windows dev khi chưa bật Docker daemon."
    ]
    for it in sandbox_items:
        p = tf.add_paragraph()
        p.text = "• " + it
        p.font.size = Pt(10)
        p.font.color.rgb = TEXT_WHITE
        p.space_after = Pt(6)

    # =========================================================================
    # SLIDE 11: MINH CHỨNG TRIỂN KHAI & KẾT QUẢ KIỂM THỬ THỰC TẾ
    # =========================================================================
    slide11 = prs.slides.add_slide(blank_layout)
    set_slide_background(slide11)
    add_header(slide11, "Kiểm Thử Thực Tế", "10. Minh Chứng Triển Khai Thực Tế & Kết Quả Kiểm Thử (85/85 Pass)",
               "Xác thực toàn diện trên cả Microsoft SQL Server 2019 và SQLite với kiểm thử tự động 100%")

    # Left: Test Results Box
    add_card(slide11, Inches(0.8), Inches(1.9), Inches(5.7), Inches(4.9), ACCENT_EMERALD)
    tb = slide11.shapes.add_textbox(Inches(0.95), Inches(2.0), Inches(5.4), Inches(4.7))
    tf = tb.text_frame
    tf.word_wrap = True
    p = tf.paragraphs[0]
    p.text = "✅ KẾT QUẢ KIỂM THỬ TỰ ĐỘNG (BACKEND TESTS)"
    p.font.size = Pt(12.5)
    p.font.bold = True
    p.font.color.rgb = ACCENT_EMERALD
    p.space_after = Pt(8)

    test_recap = [
        "TỔNG CỘNG: 85 PASSED, 0 FAILED (100% Assertion Success).",
        "Database Tables: Khởi tạo thành công 19/19 bảng trên SQL Server.",
        "SHA-256 Filter: Kiểm tra đúng checksum 64 hex, từ chối file tampered.",
        "Zip Slip Resilience: Phát hiện và chặn đứng payload '../../'.",
        "JWT Authentication: Ký HMAC-SHA256, xác thực Claims và từ chối token giả mạo.",
        "Git Analytics: Phân tích 3 commits, tính ICS Alice > 70%, bắt cờ GHOST_COMMITTER cho Bob.",
        "École 42: Rubric 30/40/30 tính chuẩn xác 8.9/10, cộng +1 điểm vào ví.",
        "Moulinette: 6/6 test cases (Public & Hidden) vượt qua thành công."
    ]
    for tr in test_recap:
        p = tf.add_paragraph()
        p.text = "✓ " + tr
        p.font.size = Pt(10)
        p.font.color.rgb = TEXT_WHITE
        p.space_after = Pt(5)

    # Right: Live Server Status Box
    add_card(slide11, Inches(6.8), Inches(1.9), Inches(5.7), Inches(4.9), ACCENT_CYAN)
    tb = slide11.shapes.add_textbox(Inches(6.95), Inches(2.0), Inches(5.4), Inches(4.7))
    tf = tb.text_frame
    tf.word_wrap = True
    p = tf.paragraphs[0]
    p.text = "🚀 TRẠNG THÁI SERVER ĐANG HOẠT ĐỘNG"
    p.font.size = Pt(12.5)
    p.font.bold = True
    p.font.color.rgb = ACCENT_CYAN
    p.space_after = Pt(8)

    server_recap = [
        "Endpoint: http://localhost:8080 (Java Backend REST API).",
        "Database Engine: Microsoft SQL Server (localhost\\MAYAO).",
        "Kết nối JDBC: jdbc:sqlserver://localhost:61370;databaseName=AIGradingDB.",
        "Dữ liệu 7 Nhóm: G1 đến G7 đồng bộ hoàn chỉnh trên SQL Server.",
        "Hàng đợi Redis: Worker Daemon nền đang lắng nghe điều phối chấm bài.",
        "Giao diện trực tiếp: Đã tích hợp 3 tab chuyên biệt:",
        "  - Tab Git Analytics (Nhóm 5) - Biểu đồ ICS & Nhận xét AI",
        "  - Tab Peer-Review École 42 - Rubric & Ví điểm sửa lỗi",
        "  - Tab Moulinette Tests - Console chạy test case thời gian thực"
    ]
    for sr in server_recap:
        p = tf.add_paragraph()
        p.text = "• " + sr
        p.font.size = Pt(10)
        p.font.color.rgb = TEXT_WHITE
        p.space_after = Pt(5)

    # =========================================================================
    # SLIDE 12: TỔNG KẾT & KẾ HOẠCH BẢO VỆ ĐỒ ÁN
    # =========================================================================
    slide12 = prs.slides.add_slide(blank_layout)
    set_slide_background(slide12)
    add_header(slide12, "Tổng Kết", "11. Tổng Kết Peer-Review 1 & Lộ Trình Sprint Tiếp Theo",
               "Khẳng định mức độ hoàn thiện kiến trúc nền tảng và lộ trình bứt phá cho các tuần tiếp theo")

    # 3 Summary Cards
    w12 = Inches(3.7)
    h12 = Inches(4.9)
    y12 = Inches(1.9)

    summary_cards = [
        ("KẾT QUẢ ĐẠT ĐƯỢC (TUẦN 3)", ACCENT_EMERALD, [
            "Hoàn thành 100% Database Design chuẩn 3NF (19 bảng).",
            "Cơ chế JWT Authentication (4.1.3) bảo mật, hỗ trợ RBAC.",
            "Tầng Gateway Filter bảo vệ SHA-256 và chống Zip Slip.",
            "Động cơ Git Analytics Nhóm 5 tính điểm công bằng ICS %.",
            "Cơ chế chấm chéo École 42 và Moulinette sẵn sàng vận hành."
        ]),
        ("KẾ HOẠCH SPRINT TIẾP THEO", ACCENT_INDIGO, [
            "Tích hợp Prompting AI nâng cao với Google Gemini 1.5 Flash.",
            "Nâng cấp tầng DAO lên JPA / Hibernate ORM.",
            "Mở rộng bộ Hidden Test Cases Moulinette cho các ca kiểm thử tải.",
            "Triển khai Docker Compose đồng bộ môi trường Web + Redis + SQL Server."
        ]),
        ("CAM KẾT CHẤT LƯỢNG", ACCENT_CYAN, [
            "Code sạch (Clean Code), không scriptlet trong JSP.",
            "Phân tách rõ ràng giữa Controller - Service - DAO - Model.",
            "Tài liệu hóa chi tiết kiến trúc và API trong README.md.",
            "Tự tin bảo vệ xuất sắc trong buổi Peer Defense 15 phút!"
        ])
    ]

    for i, (stitle, scolor, sitems) in enumerate(summary_cards):
        x = Inches(0.8 + i * 4.0)
        add_card(slide12, x, y12, w12, h12, scolor)
        tb = slide12.shapes.add_textbox(x + Inches(0.15), y12 + Inches(0.15), w12 - Inches(0.3), h12 - Inches(0.3))
        tf = tb.text_frame
        tf.word_wrap = True
        p = tf.paragraphs[0]
        p.text = stitle
        p.font.size = Pt(12.5)
        p.font.bold = True
        p.font.color.rgb = scolor
        p.space_after = Pt(10)
        for item in sitems:
            p = tf.add_paragraph()
            p.text = "• " + item
            p.font.size = Pt(10.5)
            p.font.color.rgb = TEXT_WHITE
            p.space_after = Pt(8)

    output_path = "Peer_Review_1_Database_Design_JWT_Auth.pptx"
    prs.save(output_path)
    print(f"Presentation saved successfully to: {os.path.abspath(output_path)}")

if __name__ == "__main__":
    create_deck()
