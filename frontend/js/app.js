/**
 * AI Grading Platform - Core Application & Routing
 */

const API_BASE = '/api';

const state = {
  user: null,
  token: localStorage.getItem('ai_grading_token') || null,
  currentView: 'dashboard',
  imminentDeadlines: []
};

// API Helper
async function apiCall(endpoint, method = 'GET', body = null) {
  const headers = {
    'Content-Type': 'application/json'
  };
  if (state.token) {
    headers['Authorization'] = `Bearer ${state.token}`;
  }

  const config = { method, headers };
  if (body) {
    config.body = JSON.stringify(body);
  }

  try {
    const res = await fetch(`${API_BASE}${endpoint}`, config);
    const data = await res.json();
    if (!res.ok) {
      throw new Error(data.error || `HTTP ${res.status}`);
    }
    return data;
  } catch (err) {
    console.error(`[API Error] ${method} ${endpoint}:`, err);
    throw err;
  }
}

// Toast System
function showToast(message, type = 'info') {
  const container = document.getElementById('toast-container');
  if (!container) return;

  const toast = document.createElement('div');
  toast.className = `toast toast-${type}`;
  toast.innerHTML = `
    <span>${type === 'success' ? '✓' : type === 'error' ? '✕' : 'ℹ'}</span>
    <div>${escapeHtml(message)}</div>
  `;
  container.appendChild(toast);

  setTimeout(() => {
    toast.style.opacity = '0';
    toast.style.transform = 'translateY(10px)';
    toast.style.transition = 'all 0.3s ease';
    setTimeout(() => toast.remove(), 300);
  }, 4000);
}

// Modal Helpers
function openModal(modalId) {
  const modal = document.getElementById(modalId);
  if (modal) {
    modal.classList.add('active');
  }
}

function closeModal(modalId) {
  const modal = document.getElementById(modalId);
  if (modal) {
    modal.classList.remove('active');
  }
}

// Global Confirmation Modal
function confirmAction(title, message, onConfirm) {
  const modal = document.getElementById('confirm-modal');
  document.getElementById('confirm-modal-title').textContent = title;
  document.getElementById('confirm-modal-message').textContent = message;
  const btn = document.getElementById('confirm-modal-btn');
  btn.onclick = () => {
    closeModal('confirm-modal');
    onConfirm();
  };
  openModal('confirm-modal');
}

// Utility: HTML Escaper
function escapeHtml(str) {
  if (!str) return '';
  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;');
}

// Quick Switch Demo Accounts
async function quickLogin(email, password) {
  try {
    const res = await apiCall('/auth/login', 'POST', { email, password });
    if (res.success) {
      setAuthSession(res.data.token, res.data);
      showToast(`Logged in as ${res.data.full_name} (${res.data.role})`, 'success');
      initApp();
    }
  } catch (err) {
    showToast(err.message, 'error');
  }
}

function setAuthSession(token, user) {
  state.token = token;
  state.user = user;
  localStorage.setItem('ai_grading_token', token);
  localStorage.setItem('ai_grading_user', JSON.stringify(user));
}

function clearAuthSession() {
  state.token = null;
  state.user = null;
  localStorage.removeItem('ai_grading_token');
  localStorage.removeItem('ai_grading_user');
}

// Navigation & View Controller
function switchView(viewName) {
  state.currentView = viewName;
  document.querySelectorAll('.nav-link').forEach(link => {
    link.classList.toggle('active', link.dataset.view === viewName);
  });

  const appContainer = document.getElementById('view-container');
  if (!state.user) {
    renderAuthView(appContainer);
    return;
  }

  if (viewName === 'git-analytics') {
    renderGitAnalyticsView(appContainer);
    return;
  } else if (viewName === 'peer-review') {
    renderPeerReviewView(appContainer);
    return;
  } else if (viewName === 'moulinette') {
    renderMoulinetteView(appContainer);
    return;
  }

  if (state.user.role === 'student') {
    if (viewName === 'submissions') {
      renderStudentSubmissions(appContainer);
    } else if (viewName === 'appeals') {
      renderStudentAppeals(appContainer);
    } else {
      renderStudentDashboard(appContainer);
    }
  } else {
    // Teacher or Admin
    if (viewName === 'submissions') {
      renderTeacherSubmissions(appContainer);
    } else if (viewName === 'appeals') {
      renderTeacherAppeals(appContainer);
    } else {
      renderTeacherDashboard(appContainer);
    }
  }
}

// Check Imminent Deadlines for Student
async function checkStudentDeadlines() {
  if (!state.user || state.user.role !== 'student') return;
  try {
    const res = await apiCall('/assignments/imminent');
    if (res.success && res.data.length > 0) {
      state.imminentDeadlines = res.data;
      renderDeadlineAlert(res.data);
    } else {
      const banner = document.getElementById('deadline-banner');
      if (banner) banner.style.display = 'none';
    }
  } catch (err) {
    console.warn('Could not check imminent deadlines:', err);
  }
}

function renderDeadlineAlert(imminentList) {
  const banner = document.getElementById('deadline-banner');
  if (!banner) return;
  const first = imminentList[0];
  banner.style.display = 'flex';
  banner.innerHTML = `
    <div class="deadline-info">
      <div class="deadline-icon">
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="12" cy="12" r="10"></circle>
          <line x1="12" y1="8" x2="12" y2="12"></line>
          <line x1="12" y1="16" x2="12.01" y2="16"></line>
        </svg>
      </div>
      <div class="deadline-text">
        <h4>Urgent Deadline Approaching!</h4>
        <p><strong>${escapeHtml(first.subject_code)}: ${escapeHtml(first.title)}</strong> is due on <strong>${escapeHtml(first.deadline)}</strong>. You have not submitted yet!</p>
      </div>
    </div>
    <div>
      <button class="btn btn-primary btn-sm" onclick="openSubmitModal(${first.assignment_id}, '${escapeHtml(first.title)}')">
        Submit Now
      </button>
    </div>
  `;
}

// App Initialization
async function initApp() {
  // Update Navbar UI
  const navUser = document.getElementById('nav-user');
  const navLinks = document.getElementById('nav-links');

  if (state.token) {
    try {
      const me = await apiCall('/auth/me');
      state.user = me.data;
      localStorage.setItem('ai_grading_user', JSON.stringify(me.data));
    } catch (e) {
      clearAuthSession();
    }
  }

  if (state.user) {
    navUser.innerHTML = `
      <div class="user-profile">
        <div class="user-avatar">${state.user.full_name.charAt(0)}</div>
        <div>
          <div style="font-weight: 600; font-size: 0.85rem;">${escapeHtml(state.user.full_name)}</div>
          <span class="user-role-badge role-${state.user.role}">${state.user.role}</span>
        </div>
      </div>
      <button class="btn btn-secondary btn-sm" onclick="handleLogout()">Logout</button>
    `;

    if (state.user.role === 'student') {
      navLinks.innerHTML = `
        <span class="nav-link active" data-view="dashboard" onclick="switchView('dashboard')">My Subjects & Tasks</span>
        <span class="nav-link" data-view="submissions" onclick="switchView('submissions')">My Submissions</span>
        <span class="nav-link" data-view="git-analytics" onclick="switchView('git-analytics')">Git Analytics (Nhóm 5)</span>
        <span class="nav-link" data-view="peer-review" onclick="switchView('peer-review')">Peer-Review École 42</span>
        <span class="nav-link" data-view="moulinette" onclick="switchView('moulinette')">Moulinette Tests</span>
        <span class="nav-link" data-view="appeals" onclick="switchView('appeals')">Grade Appeals</span>
      `;
      checkStudentDeadlines();
    } else {
      navLinks.innerHTML = `
        <span class="nav-link active" data-view="dashboard" onclick="switchView('dashboard')">Assignments Management</span>
        <span class="nav-link" data-view="submissions" onclick="switchView('submissions')">Submissions & Grading</span>
        <span class="nav-link" data-view="git-analytics" onclick="switchView('git-analytics')">Git Analytics (Nhóm 5)</span>
        <span class="nav-link" data-view="peer-review" onclick="switchView('peer-review')">Peer-Review École 42</span>
        <span class="nav-link" data-view="moulinette" onclick="switchView('moulinette')">Moulinette Tests</span>
        <span class="nav-link" data-view="appeals" onclick="switchView('appeals')">Appeals Queue</span>
      `;
    }
  } else {
    navUser.innerHTML = `
      <button class="btn btn-primary btn-sm" onclick="switchView('auth')">Sign In</button>
    `;
    navLinks.innerHTML = '';
    const banner = document.getElementById('deadline-banner');
    if (banner) banner.style.display = 'none';
  }

  switchView(state.user ? 'dashboard' : 'auth');
}

async function handleLogout() {
  try {
    await apiCall('/auth/logout', 'POST');
  } catch (e) {}
  clearAuthSession();
  showToast('Logged out', 'info');
  initApp();
}

window.addEventListener('DOMContentLoaded', () => {
  initApp();
});
