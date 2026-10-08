/**
 * AI Grading Platform - Authentication View & Handlers
 */

let otpCountdownTimer = null;

function renderAuthView(container) {
  container.innerHTML = `
    <div style="max-width: 480px; margin: 2rem auto;">
      <div class="glass-card" style="padding: 2.5rem;">
        <div style="text-align: center; margin-bottom: 2rem;">
          <div class="brand-logo" style="margin: 0 auto 1rem auto; width: 48px; height: 48px;">
            <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5"/>
            </svg>
          </div>
          <h2>Welcome to AI Grader</h2>
          <p style="color: var(--text-secondary); margin-top: 0.5rem;">Sign in to access your courses and grading workspace</p>
        </div>

        <!-- Auth Tabs -->
        <div style="display: flex; border-bottom: 1px solid var(--border-glass); margin-bottom: 1.5rem;">
          <button id="tab-login" class="nav-link active" style="flex: 1; text-align: center; border-radius: 0;" onclick="switchAuthTab('login')">Sign In</button>
          <button id="tab-register" class="nav-link" style="flex: 1; text-align: center; border-radius: 0;" onclick="switchAuthTab('register')">Register with OTP</button>
          <button id="tab-forgot" class="nav-link" style="flex: 1; text-align: center; border-radius: 0;" onclick="switchAuthTab('forgot')">Forgot Password</button>
        </div>

        <!-- 1. LOGIN FORM -->
        <form id="form-login" onsubmit="handleLoginSubmit(event)">
          <div class="form-group">
            <label class="form-label">Email Address</label>
            <input type="email" id="login-email" class="form-control" placeholder="e.g. teacher@school.edu" required>
          </div>
          <div class="form-group">
            <label class="form-label">Password</label>
            <input type="password" id="login-password" class="form-control" placeholder="••••••••" required>
          </div>
          <button type="submit" class="btn btn-primary" style="width: 100%; margin-top: 0.5rem;">Sign In</button>
        </form>

        <!-- 2. REGISTER FORM -->
        <form id="form-register" style="display: none;" onsubmit="handleRegisterSubmit(event)">
          <div class="form-group">
            <label class="form-label">Full Name</label>
            <input type="text" id="reg-name" class="form-control" placeholder="e.g. John Doe" required>
          </div>
          <div class="form-group">
            <label class="form-label">Email Address</label>
            <div style="display: flex; gap: 0.5rem;">
              <input type="email" id="reg-email" class="form-control" placeholder="e.g. student@school.edu" required>
              <button type="button" id="btn-send-reg-otp" class="btn btn-secondary btn-sm" onclick="sendOtp('register')">Send OTP</button>
            </div>
            <small id="otp-status-reg" style="color: var(--accent-cyan); display: none; margin-top: 0.25rem;"></small>
          </div>
          <div class="form-group">
            <label class="form-label">OTP Verification Code</label>
            <input type="text" id="reg-otp" class="form-control" placeholder="6-digit code sent to email" required>
          </div>
          <div class="form-group">
            <label class="form-label">Role</label>
            <select id="reg-role" class="form-control" onchange="toggleRoleFields()">
              <option value="student">Student</option>
              <option value="teacher">Lecturer / Teacher</option>
            </select>
          </div>
          <div class="form-group" id="group-code">
            <label class="form-label" id="label-code">Student Code</label>
            <input type="text" id="reg-code" class="form-control" placeholder="e.g. STU-10903">
          </div>
          <div class="form-group" id="group-extra">
            <label class="form-label" id="label-extra">Class Name</label>
            <input type="text" id="reg-extra" class="form-control" placeholder="e.g. SE1701">
          </div>
          <div class="form-group">
            <label class="form-label">Password</label>
            <input type="password" id="reg-password" class="form-control" placeholder="Min 6 characters" required>
          </div>
          <button type="submit" class="btn btn-primary" style="width: 100%; margin-top: 0.5rem;">Create Account</button>
        </form>

        <!-- 3. FORGOT PASSWORD FORM -->
        <form id="form-forgot" style="display: none;" onsubmit="handleForgotSubmit(event)">
          <div class="form-group">
            <label class="form-label">Registered Email</label>
            <div style="display: flex; gap: 0.5rem;">
              <input type="email" id="forgot-email" class="form-control" placeholder="e.g. user@school.edu" required>
              <button type="button" id="btn-send-forgot-otp" class="btn btn-secondary btn-sm" onclick="sendOtp('forgot')">Send OTP</button>
            </div>
            <small id="otp-status-forgot" style="color: var(--accent-cyan); display: none; margin-top: 0.25rem;"></small>
          </div>
          <div class="form-group">
            <label class="form-label">OTP Code</label>
            <input type="text" id="forgot-otp" class="form-control" placeholder="6-digit code" required>
          </div>
          <div class="form-group">
            <label class="form-label">New Password</label>
            <input type="password" id="forgot-new-password" class="form-control" placeholder="Min 6 characters" required>
          </div>
          <button type="submit" class="btn btn-primary" style="width: 100%; margin-top: 0.5rem;">Reset Password</button>
        </form>

        <!-- Demo Accounts Fast Login -->
        <div style="margin-top: 2rem; padding-top: 1.5rem; border-top: 1px solid var(--border-glass);">
          <div style="font-size: 0.8rem; color: var(--text-muted); margin-bottom: 0.75rem; text-align: center;">QUICK LOGIN DEMO ACCOUNTS:</div>
          <div style="display: flex; gap: 0.5rem; justify-content: center;">
            <button class="btn btn-secondary btn-sm" onclick="quickLogin('teacher@school.edu', 'Teacher@123')">Teacher</button>
            <button class="btn btn-secondary btn-sm" onclick="quickLogin('student@school.edu', 'Student@123')">Student</button>
            <button class="btn btn-secondary btn-sm" onclick="quickLogin('admin@school.edu', 'Admin@123')">Admin</button>
          </div>
        </div>

      </div>
    </div>
  `;
}

function switchAuthTab(tab) {
  document.getElementById('tab-login').classList.toggle('active', tab === 'login');
  document.getElementById('tab-register').classList.toggle('active', tab === 'register');
  document.getElementById('tab-forgot').classList.toggle('active', tab === 'forgot');

  document.getElementById('form-login').style.display = tab === 'login' ? 'block' : 'none';
  document.getElementById('form-register').style.display = tab === 'register' ? 'block' : 'none';
  document.getElementById('form-forgot').style.display = tab === 'forgot' ? 'block' : 'none';
}

function toggleRoleFields() {
  const role = document.getElementById('reg-role').value;
  const labelCode = document.getElementById('label-code');
  const inputCode = document.getElementById('reg-code');
  const labelExtra = document.getElementById('label-extra');
  const inputExtra = document.getElementById('reg-extra');

  if (role === 'teacher') {
    labelCode.textContent = 'Employee Code';
    inputCode.placeholder = 'e.g. TCH-2026-02';
    labelExtra.textContent = 'Department';
    inputExtra.placeholder = 'e.g. Software Engineering';
  } else {
    labelCode.textContent = 'Student Code';
    inputCode.placeholder = 'e.g. STU-10903';
    labelExtra.textContent = 'Class Name';
    inputExtra.placeholder = 'e.g. SE1701';
  }
}

async function sendOtp(type) {
  const emailInput = document.getElementById(type === 'register' ? 'reg-email' : 'forgot-email');
  const email = emailInput.value.trim();
  const statusEl = document.getElementById(type === 'register' ? 'otp-status-reg' : 'otp-status-forgot');
  const btn = document.getElementById(type === 'register' ? 'btn-send-reg-otp' : 'btn-send-forgot-otp');

  if (!email || !email.includes('@')) {
    showToast('Please enter a valid email address first', 'error');
    return;
  }

  btn.disabled = true;
  btn.textContent = 'Sending...';

  try {
    const res = await apiCall('/auth/send-otp', 'POST', {
      email,
      type: type === 'register' ? 'register' : 'forgot_password'
    });

    statusEl.style.display = 'block';
    statusEl.textContent = `OTP code sent! (For test: ${res.data.dev_code})`;
    showToast(`OTP Code sent to ${email} (Demo code: ${res.data.dev_code})`, 'success');

    // Start 60s cooldown
    let seconds = 60;
    btn.textContent = `${seconds}s`;
    clearInterval(otpCountdownTimer);
    otpCountdownTimer = setInterval(() => {
      seconds--;
      if (seconds <= 0) {
        clearInterval(otpCountdownTimer);
        btn.disabled = false;
        btn.textContent = 'Resend OTP';
      } else {
        btn.textContent = `${seconds}s`;
      }
    }, 1000);

  } catch (err) {
    btn.disabled = false;
    btn.textContent = 'Send OTP';
    showToast(err.message, 'error');
  }
}

async function handleLoginSubmit(e) {
  e.preventDefault();
  const email = document.getElementById('login-email').value.trim();
  const password = document.getElementById('login-password').value;

  try {
    const res = await apiCall('/auth/login', 'POST', { email, password });
    setAuthSession(res.data.token, res.data);
    showToast(`Welcome back, ${res.data.full_name}!`, 'success');
    initApp();
  } catch (err) {
    showToast(err.message, 'error');
  }
}

async function handleRegisterSubmit(e) {
  e.preventDefault();
  const fullName = document.getElementById('reg-name').value.trim();
  const email = document.getElementById('reg-email').value.trim();
  const otpCode = document.getElementById('reg-otp').value.trim();
  const role = document.getElementById('reg-role').value;
  const codeNumber = document.getElementById('reg-code').value.trim();
  const extraField = document.getElementById('reg-extra').value.trim();
  const password = document.getElementById('reg-password').value;

  try {
    const res = await apiCall('/auth/register', 'POST', {
      full_name: fullName,
      email,
      password,
      role,
      code_number: codeNumber,
      extra_field: extraField,
      otp_code: otpCode
    });

    setAuthSession(res.data.token, res.data);
    showToast('Account created successfully!', 'success');
    initApp();
  } catch (err) {
    showToast(err.message, 'error');
  }
}

async function handleForgotSubmit(e) {
  e.preventDefault();
  const email = document.getElementById('forgot-email').value.trim();
  const otpCode = document.getElementById('forgot-otp').value.trim();
  const newPassword = document.getElementById('forgot-new-password').value;

  try {
    await apiCall('/auth/forgot-password', 'POST', {
      email,
      otp_code: otpCode,
      new_password: newPassword
    });

    showToast('Password updated! Please login with your new password.', 'success');
    switchAuthTab('login');
  } catch (err) {
    showToast(err.message, 'error');
  }
}
