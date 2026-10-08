using System;
using System.Drawing;
using System.IO;
using System.Net;
using System.Text;
using System.Windows.Forms;
using System.Diagnostics;

namespace AIGradingInternalApp {
    public class MainForm : Form {
        private Label lblTitle;
        private Label lblStatus;
        private Button btnCheckServer;
        private Button btnOpenBrowser;
        private Button btnBatchGrade;
        private TextBox txtOutput;
        private TabControl tabControl;

        [STAThread]
        public static void Main() {
            Application.EnableVisualStyles();
            Application.SetCompatibleTextRenderingDefault(false);
            Application.Run(new MainForm());
        }

        public MainForm() {
            InitializeComponent();
        }

        private void InitializeComponent() {
            this.Text = "AI Grading Platform - Internal Administration Tool";
            this.Size = new Size(820, 600);
            this.StartPosition = FormStartPosition.CenterScreen;
            this.BackColor = Color.FromArgb(16, 22, 38);
            this.ForeColor = Color.White;
            this.Font = new Font("Segoe UI", 9.5f, FontStyle.Regular);

            // Header Banner
            Panel pnlHeader = new Panel();
            pnlHeader.Dock = DockStyle.Top;
            pnlHeader.Height = 80;
            pnlHeader.BackColor = Color.FromArgb(24, 34, 56);
            pnlHeader.Padding = new Padding(20, 15, 20, 15);

            lblTitle = new Label();
            lblTitle.Text = "AI Grader Internal Control Center (.exe)";
            lblTitle.Font = new Font("Segoe UI", 14f, FontStyle.Bold);
            lblTitle.ForeColor = Color.FromArgb(248, 250, 252);
            lblTitle.AutoSize = true;
            lblTitle.Location = new Point(20, 15);
            pnlHeader.Controls.Add(lblTitle);

            lblStatus = new Label();
            lblStatus.Text = "System Status: Checking backend connectivity...";
            lblStatus.ForeColor = Color.FromArgb(148, 163, 184);
            lblStatus.AutoSize = true;
            lblStatus.Location = new Point(22, 45);
            pnlHeader.Controls.Add(lblStatus);

            this.Controls.Add(pnlHeader);

            // Top Action Buttons
            Panel pnlActions = new Panel();
            pnlActions.Dock = DockStyle.Top;
            pnlActions.Height = 60;
            pnlActions.BackColor = Color.FromArgb(16, 22, 38);
            pnlActions.Padding = new Padding(20, 12, 20, 12);

            btnCheckServer = CreateButton("Check Server & DB", Color.FromArgb(99, 102, 241), 20, 12);
            btnCheckServer.Click += (s, e) => CheckServerStatus();
            pnlActions.Controls.Add(btnCheckServer);

            btnOpenBrowser = CreateButton("Launch Web Portal", Color.FromArgb(16, 185, 129), 180, 12);
            btnOpenBrowser.Click += (s, e) => {
                try {
                    Process.Start(new ProcessStartInfo("http://localhost:8080") { UseShellExecute = true });
                } catch (Exception ex) {
                    AppendLog("Error opening browser: " + ex.Message);
                }
            };
            pnlActions.Controls.Add(btnOpenBrowser);

            btnBatchGrade = CreateButton("Execute Batch AI Grading", Color.FromArgb(139, 92, 246), 340, 12);
            btnBatchGrade.Click += (s, e) => ExecuteBatchGrading();
            pnlActions.Controls.Add(btnBatchGrade);

            Button btnClear = CreateButton("Clear Log", Color.FromArgb(51, 65, 85), 540, 12);
            btnClear.Click += (s, e) => { txtOutput.Clear(); };
            pnlActions.Controls.Add(btnClear);

            this.Controls.Add(pnlActions);

            // Log Console Box
            GroupBox grpLog = new GroupBox();
            grpLog.Text = "Internal Diagnostics & Activity Log";
            grpLog.ForeColor = Color.FromArgb(148, 163, 184);
            grpLog.Dock = DockStyle.Fill;
            grpLog.Padding = new Padding(15);

            txtOutput = new TextBox();
            txtOutput.Multiline = true;
            txtOutput.ReadOnly = true;
            txtOutput.ScrollBars = ScrollBars.Vertical;
            txtOutput.Dock = DockStyle.Fill;
            txtOutput.BackColor = Color.FromArgb(9, 13, 22);
            txtOutput.ForeColor = Color.FromArgb(165, 180, 252);
            txtOutput.Font = new Font("Consolas", 9.5f, FontStyle.Regular);
            grpLog.Controls.Add(txtOutput);

            this.Controls.Add(grpLog);

            // Trigger initial status check
            this.Shown += (s, e) => CheckServerStatus();
        }

        private Button CreateButton(string text, Color bg, int x, int y) {
            Button btn = new Button();
            btn.Text = text;
            btn.BackColor = bg;
            btn.ForeColor = Color.White;
            btn.FlatStyle = FlatStyle.Flat;
            btn.FlatAppearance.BorderSize = 0;
            btn.Location = new Point(x, y);
            btn.Size = new Size(150, 36);
            btn.Cursor = Cursors.Hand;
            btn.Font = new Font("Segoe UI", 9f, FontStyle.Bold);
            return btn;
        }

        private void AppendLog(string message) {
            string timestamp = DateTime.Now.ToString("yyyy-MM-dd HH:mm:ss");
            txtOutput.AppendText(string.Format("[{0}] {1}\r\n", timestamp, message));
        }

        private void CheckServerStatus() {
            AppendLog("Pinging backend server at http://localhost:8080/api/subjects...");
            try {
                var req = (HttpWebRequest)WebRequest.Create("http://localhost:8080/api/subjects");
                req.Timeout = 3000;
                using (var resp = (HttpWebResponse)req.GetResponse()) {
                    if (resp.StatusCode == HttpStatusCode.OK) {
                        using (var reader = new StreamReader(resp.GetResponseStream(), Encoding.UTF8)) {
                            string content = reader.ReadToEnd();
                            lblStatus.Text = "System Status: Online | Backend & SQLite DB Connected";
                            lblStatus.ForeColor = Color.FromArgb(52, 211, 153);
                            AppendLog("Server Response OK: " + content);
                        }
                    }
                }
            } catch (Exception ex) {
                lblStatus.Text = "System Status: Offline or Starting...";
                lblStatus.ForeColor = Color.FromArgb(251, 113, 133);
                AppendLog("Could not reach backend: " + ex.Message);
                AppendLog("Note: Please ensure the Java server is running (port 8080).");
            }
        }

        private void ExecuteBatchGrading() {
            AppendLog("Starting Batch AI Grading trigger for all pending submissions...");
            try {
                var req = (HttpWebRequest)WebRequest.Create("http://localhost:8080/api/submissions?status=submitted");
                req.Timeout = 5000;
                using (var resp = (HttpWebResponse)req.GetResponse()) {
                    using (var reader = new StreamReader(resp.GetResponseStream(), Encoding.UTF8)) {
                        string json = reader.ReadToEnd();
                        AppendLog("Pending Submissions fetched: " + json);
                    }
                }
            } catch (Exception ex) {
                AppendLog("Batch grading error: " + ex.Message);
            }
        }
    }
}
