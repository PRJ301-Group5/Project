/**
 * AI Grading Platform - Lecturer Dashboard, Grading Studio & Appeals Review
 */

let lecturerAssignmentsPage = 1;
let currentGradingSubmissionId = null;

async function renderTeacherDashboard(container) {
  container.innerHTML = `
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 2rem;">
      <div>
        <h2>Lecturer Course Management</h2>
        <p style="color: var(--text-secondary);">Manage classes, design rubrics, upload prompts, and oversee student submissions</p>
      </div>
      <div style="display: flex; gap: 0.75rem;">
        <button class="btn btn-secondary" onclick="openCreateClassModal()">
          + Create Class
        </button>
        <button class="btn btn-primary" onclick="openCreateAssignmentModal()">
          + Create New Assignment
        </button>
      </div>
    </div>

    <!-- Toolbar: Search, Filter, Sort, Pagination -->
    <div class="toolbar glass-card" style="padding: 1rem 1.25rem;">
      <div class="toolbar-left">
        <input type="text" id="assign-search" class="input-search" placeholder="Search assignments..." oninput="handleAssignSearch()">
        <select id="assign-filter-subject" class="select-filter" onchange="loadTeacherAssignments()">
          <option value="">All Subjects</option>
          <option value="1">ANC111 - Advanced Network & Cloud</option>
          <option value="2">ABC222 - Applied Basic Cloud</option>
          <option value="3">PRJ301 - Java Web Development</option>
        </select>
      </div>
      <div class="toolbar-right">
        <select id="assign-sort" class="select-filter" onchange="loadTeacherAssignments()">
          <option value="created_at_desc">Newest First</option>
          <option value="deadline_asc">Deadline (Urgent First)</option>
          <option value="title_asc">Title (A-Z)</option>
        </select>
        <span id="assign-pagination-info" style="font-size: 0.85rem; color: var(--text-secondary);"></span>
        <button id="btn-prev-page" class="btn btn-secondary btn-sm" onclick="changeAssignPage(-1)">‹</button>
        <button id="btn-next-page" class="btn btn-secondary btn-sm" onclick="changeAssignPage(1)">›</button>
      </div>
    </div>

    <!-- Assignments Table -->
    <div id="teacher-assignments-table">
      <div class="glass-card" style="text-align: center; color: var(--text-muted);">Loading assignments...</div>
    </div>
  `;

  loadTeacherAssignments();
}

let searchDebounce = null;
function handleAssignSearch() {
  clearTimeout(searchDebounce);
  searchDebounce = setTimeout(() => {
    lecturerAssignmentsPage = 1;
    loadTeacherAssignments();
  }, 300);
}

function changeAssignPage(delta) {
  lecturerAssignmentsPage = Math.max(1, lecturerAssignmentsPage + delta);
  loadTeacherAssignments();
}

async function loadTeacherAssignments() {
  const search = document.getElementById('assign-search')?.value.trim() || '';
  const subjectId = document.getElementById('assign-filter-subject')?.value || '';
  const sortVal = document.getElementById('assign-sort')?.value || 'created_at_desc';

  let sortBy = 'created_at';
  let order = 'desc';
  if (sortVal === 'deadline_asc') { sortBy = 'deadline'; order = 'asc'; }
  else if (sortVal === 'title_asc') { sortBy = 'title'; order = 'asc'; }

  const url = `/assignments?page=${lecturerAssignmentsPage}&limit=10&search=${encodeURIComponent(search)}&subject_id=${subjectId}&sort_by=${sortBy}&order=${order}`;

  try {
    const res = await apiCall(url);
    const container = document.getElementById('teacher-assignments-table');
    const pageInfo = document.getElementById('assign-pagination-info');
    const prevBtn = document.getElementById('btn-prev-page');
    const nextBtn = document.getElementById('btn-next-page');

    if (res.success && res.data.items.length > 0) {
      const pag = res.data.pagination;
      if (pageInfo) pageInfo.textContent = `Page ${pag.page} of ${pag.pages || 1} (${pag.total} total)`;
      if (prevBtn) prevBtn.disabled = pag.page <= 1;
      if (nextBtn) nextBtn.disabled = pag.page >= pag.pages;

      container.innerHTML = `
        <div class="table-container">
          <table>
            <thead>
              <tr>
                <th>Subject</th>
                <th>Assignment Title</th>
                <th>Prompt & Materials</th>
                <th>Submissions</th>
                <th>Deadline</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              ${res.data.items.map(a => `
                <tr>
                  <td><span class="subject-code">${escapeHtml(a.subject_code || 'ANC111')}</span></td>
                  <td>
                    <strong>${escapeHtml(a.title)}</strong>
                    <div style="font-size: 0.8rem; color: var(--text-muted);">${escapeHtml(a.subject_name || '')}</div>
                  </td>
                  <td>
                    <div style="display: flex; flex-direction: column; gap: 0.3rem;">
                      ${a.prompt_file_url ? `
                        <a href="${escapeHtml(a.prompt_file_url)}" download class="btn btn-secondary btn-sm" style="padding: 0.2rem 0.5rem; font-size: 0.75rem; color: var(--accent-cyan);">
                          📥 Prompt Doc
                        </a>
                      ` : ''}
                      ${a.prototype_url ? `
                        <a href="${escapeHtml(a.prototype_url)}" target="_blank" class="btn btn-secondary btn-sm" style="padding: 0.2rem 0.5rem; font-size: 0.75rem;">
                          Spec ↗
                        </a>
                      ` : ''}
                      ${!a.prompt_file_url && !a.prototype_url ? '<span style="color: var(--text-muted);">None</span>' : ''}
                    </div>
                  </td>
                  <td>
                    <span class="badge" style="background: rgba(99, 102, 241, 0.15); color: #A5B4FC;">
                      ${a.submission_count || 0} Submissions
                    </span>
                  </td>
                  <td><span style="color: var(--text-secondary);">${escapeHtml(a.deadline)}</span></td>
                  <td>
                    <div style="display: flex; gap: 0.4rem;">
                      <button class="btn btn-secondary btn-sm" onclick="switchViewToSubmissionsFor(${a.assignment_id})">Grade</button>
                      <button class="btn btn-secondary btn-sm" onclick="openEditAssignmentModal(${a.assignment_id})">Edit</button>
                      <button class="btn btn-danger btn-sm" onclick="promptDeleteAssignment(${a.assignment_id}, '${escapeHtml(a.title)}')">Delete</button>
                    </div>
                  </td>
                </tr>
              `).join('')}
            </tbody>
          </table>
        </div>
      `;
    } else {
      if (pageInfo) pageInfo.textContent = 'Page 1 of 1 (0 total)';
      container.innerHTML = `<div class="empty-state">No assignments found matching criteria.</div>`;
    }
  } catch (err) {
    showToast(err.message, 'error');
  }
}

function promptDeleteAssignment(id, title) {
  confirmAction('Delete Assignment', `Are you sure you want to delete assignment "${title}"? This will also remove all student submissions.`, async () => {
    try {
      await apiCall(`/assignments/${id}`, 'DELETE');
      showToast('Assignment deleted', 'info');
      loadTeacherAssignments();
    } catch (err) {
      showToast(err.message, 'error');
    }
  });
}

function switchViewToSubmissionsFor(assignmentId) {
  switchView('submissions');
  setTimeout(() => {
    const sel = document.getElementById('submissions-filter-assign');
    if (sel) {
      sel.value = assignmentId;
      loadTeacherSubmissions();
    }
  }, 100);
}

// Submissions List & Grading Studio
async function renderTeacherSubmissions(container) {
  container.innerHTML = `
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 2rem;">
      <div>
        <h2>Submissions & AI Grading Studio</h2>
        <p style="color: var(--text-secondary);">Evaluate student implementations against prototypes with automated AI vision</p>
      </div>
    </div>

    <!-- Toolbar -->
    <div class="toolbar glass-card" style="padding: 1rem 1.25rem;">
      <div class="toolbar-left">
        <select id="submissions-filter-assign" class="select-filter" onchange="loadTeacherSubmissions()">
          <option value="">All Assignments</option>
        </select>
        <select id="submissions-filter-status" class="select-filter" onchange="loadTeacherSubmissions()">
          <option value="">All Statuses</option>
          <option value="submitted">Submitted (Pending Grade)</option>
          <option value="graded">Graded</option>
        </select>
      </div>
      <div class="toolbar-right">
        <button class="btn btn-secondary btn-sm" onclick="loadTeacherSubmissions()">↻ Refresh</button>
      </div>
    </div>

    <div id="teacher-submissions-table">
      <div class="glass-card" style="text-align: center; color: var(--text-muted);">Loading submissions...</div>
    </div>
  `;

  // Populate assignments dropdown
  try {
    const aRes = await apiCall('/assignments?limit=100');
    const sel = document.getElementById('submissions-filter-assign');
    if (aRes.success && sel) {
      aRes.data.items.forEach(a => {
        const opt = document.createElement('option');
        opt.value = a.assignment_id;
        opt.textContent = `${a.subject_code || 'CS'}: ${a.title}`;
        sel.appendChild(opt);
      });
    }
  } catch (e) {}

  loadTeacherSubmissions();
}

async function loadTeacherSubmissions() {
  const assignId = document.getElementById('submissions-filter-assign')?.value || '';
  const status = document.getElementById('submissions-filter-status')?.value || '';

  const url = `/submissions?assignment_id=${assignId}&status=${status}`;
  try {
    const res = await apiCall(url);
    const container = document.getElementById('teacher-submissions-table');
    if (res.success && res.data.length > 0) {
      container.innerHTML = `
        <div class="table-container">
          <table>
            <thead>
              <tr>
                <th>Student</th>
                <th>Assignment</th>
                <th>Submitted At</th>
                <th>AI Similarity</th>
                <th>Score</th>
                <th>Status</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              ${res.data.map(s => `
                <tr>
                  <td>
                    <strong>${escapeHtml(s.student_name)}</strong>
                    <div style="font-size: 0.8rem; color: var(--text-muted);">${escapeHtml(s.student_code)} • ${escapeHtml(s.class_name)}</div>
                  </td>
                  <td>${escapeHtml(s.assignment_title)}</td>
                  <td style="color: var(--text-secondary);">${escapeHtml(s.submitted_at)}</td>
                  <td>
                    ${s.similarity_score !== null && s.similarity_score !== undefined ? `
                      <span style="font-weight: 700; color: var(--accent-emerald);">${s.similarity_score}%</span>
                    ` : '<span style="color: var(--text-muted);">Not graded</span>'}
                  </td>
                  <td>
                    ${s.grade_score !== null && s.grade_score !== undefined ? `
                      <strong style="color: var(--accent-emerald); font-size: 1rem;">${s.grade_score}/10</strong>
                    ` : '<span style="color: var(--text-muted);">-</span>'}
                  </td>
                  <td><span class="badge badge-${s.status}">${s.status}</span></td>
                  <td>
                    <button class="btn btn-ai btn-sm" onclick="openTeacherGradingStudio(${s.submission_id})">
                      ⚡ Open Studio
                    </button>
                  </td>
                </tr>
              `).join('')}
            </tbody>
          </table>
        </div>
      `;
    } else {
      container.innerHTML = `<div class="empty-state">No submissions found matching filter.</div>`;
    }
  } catch (err) {
    showToast(err.message, 'error');
  }
}

// Open Teacher Grading Studio Modal
async function openTeacherGradingStudio(submissionId) {
  currentGradingSubmissionId = submissionId;
  const savedApiKey = localStorage.getItem('gemini_api_key') || '';

  try {
    const res = await apiCall(`/submissions/${submissionId}`);
    if (!res.success) throw new Error(res.error);
    const sub = res.data;

    let elementsArr = [];
    try {
      if (sub.detected_elements) {
        elementsArr = typeof sub.detected_elements === 'string' ? JSON.parse(sub.detected_elements) : sub.detected_elements;
      }
    } catch (e) {}

    const modalContent = document.getElementById('grading-studio-content');
    modalContent.innerHTML = `
      <div class="modal-header">
        <div>
          <h3>AI Grading Studio & Side-by-Side Verification</h3>
          <p style="color: var(--text-secondary); font-size: 0.9rem;">
            Student: <strong>${escapeHtml(sub.student_name)}</strong> (${escapeHtml(sub.student_code)}) | 
            Assignment: <strong>${escapeHtml(sub.assignment_title)}</strong>
          </p>
        </div>
        <button class="modal-close" onclick="closeModal('grading-studio-modal')">&times;</button>
      </div>

      <!-- Side-by-side Visual Comparison -->
      <div class="grading-studio">
        <!-- Student Screenshot -->
        <div class="visual-preview-box">
          <div class="preview-header">
            <span>Student Submission Screenshot</span>
            <a href="${escapeHtml(sub.screenshot_path)}" target="_blank" style="font-size: 0.8rem;">Full Size ↗</a>
          </div>
          <div class="preview-body">
            <img src="${escapeHtml(sub.screenshot_path)}" alt="Student Submission" onerror="this.src='data:image/svg+xml;utf8,<svg xmlns=\\'http://www.w3.org/2000/svg\\' width=\\'100\\' height=\\'100\\'><text x=\\'50%\\' y=\\'50%\\' fill=\\'gray\\' text-anchor=\\'middle\\'>Student Screenshot</text></svg>'">
          </div>
        </div>

        <!-- Reference Prototype -->
        <div class="visual-preview-box">
          <div class="preview-header">
            <span>Reference Prototype / Design Spec</span>
            ${sub.prototype_url ? `<a href="${escapeHtml(sub.prototype_url)}" target="_blank" style="font-size: 0.8rem;">Full Size ↗</a>` : ''}
          </div>
          <div class="preview-body">
            ${sub.prototype_url ? `<img src="${escapeHtml(sub.prototype_url)}" alt="Prototype Spec" onerror="this.src='data:image/svg+xml;utf8,<svg xmlns=\\'http://www.w3.org/2000/svg\\' width=\\'100\\' height=\\'100\\'><text x=\\'50%\\' y=\\'50%\\' fill=\\'gray\\' text-anchor=\\'middle\\'>Prototype Spec</text></svg>'">` : '<div style="color: var(--text-muted);">No prototype image URL provided</div>'}
          </div>
        </div>
      </div>

      <!-- AI Engine Trigger Section -->
      <div class="glass-card" style="margin-top: 1.5rem; background: rgba(99, 102, 241, 0.05); border: 1px solid rgba(99, 102, 241, 0.25);">
        <div style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 1rem;">
          <div style="flex: 1; min-width: 260px;">
            <label class="form-label" style="display: flex; align-items: center; gap: 0.5rem;">
              <span>Google Gemini API Key</span>
              <span style="font-size: 0.75rem; color: var(--text-muted);">(Optional - fallback evaluator active if blank)</span>
            </label>
            <input type="password" id="studio-api-key" class="form-control" placeholder="AIzaSy..." value="${escapeHtml(savedApiKey)}" onchange="localStorage.setItem('gemini_api_key', this.value.trim())">
          </div>
          <div style="align-self: flex-end;">
            <button id="btn-trigger-ai" class="btn btn-ai" onclick="triggerAiGrading(${submissionId})">
              ⚡ Execute AI Automated Grading
            </button>
          </div>
        </div>
      </div>

      <!-- AI Evaluation Result -->
      <div class="ai-results-panel" id="studio-ai-results">
        <div class="ai-header">
          <div class="similarity-metric">
            <div class="score-circle" id="studio-circle" style="--sim-val: ${sub.similarity_score || 0};">
              <div class="score-circle-inner">
                <span class="number" id="studio-sim-number">${sub.similarity_score || 0}%</span>
                <span class="label">SIMILARITY</span>
              </div>
            </div>
            <div>
              <h4>Multimodal AI Vision & Rubric Analysis</h4>
              <p style="color: var(--text-secondary); font-size: 0.85rem;" id="studio-ai-status">
                ${sub.similarity_score ? 'Evaluation completed with layout analysis' : 'Click "Execute AI Automated Grading" to analyze.'}
              </p>
            </div>
          </div>
        </div>

        <div>
          <strong style="font-size: 0.85rem; color: var(--text-secondary); text-transform: uppercase;">Detected UI Elements:</strong>
          <div class="elements-cloud" id="studio-elements-cloud">
            ${elementsArr.length > 0 ? elementsArr.map(el => `<span class="element-tag">${escapeHtml(el)}</span>`).join('') : '<span style="color: var(--text-muted);">No elements detected yet</span>'}
          </div>
        </div>

        <div style="margin-top: 1rem;">
          <strong style="font-size: 0.85rem; color: var(--text-secondary); text-transform: uppercase;">Layout Audit Report:</strong>
          <div id="studio-layout-report" style="background: rgba(0,0,0,0.2); padding: 1rem; border-radius: var(--radius-md); font-family: var(--font-mono); font-size: 0.85rem; white-space: pre-wrap; margin-top: 0.4rem; color: #CBD5E1;">
            ${escapeHtml(sub.layout_report || 'Report will be generated when AI grading is executed.')}
          </div>
        </div>

        <div style="margin-top: 1rem;">
          <strong style="font-size: 0.85rem; color: var(--text-secondary); text-transform: uppercase;">Constructive Feedback & Suggestions:</strong>
          <div id="studio-ai-feedback" style="background: rgba(99, 102, 241, 0.08); border-left: 3px solid var(--primary); padding: 1rem; border-radius: var(--radius-sm); font-size: 0.9rem; margin-top: 0.4rem;">
            ${escapeHtml(sub.ai_feedback || 'No feedback generated yet.')}
          </div>
        </div>
      </div>

      <!-- Lecturer Final Grade & Feedback Override -->
      <div class="glass-card" style="margin-top: 1.5rem;">
        <h4 style="margin-bottom: 1rem;">Lecturer Grade Approval & Feedback</h4>
        <div class="grid-2">
          <div class="form-group">
            <label class="form-label">Final Score (0.0 - 10.0)</label>
            <input type="number" id="studio-score" class="form-control" step="0.1" min="0" max="10" placeholder="e.g. 9.2" value="${sub.grade_score !== null && sub.grade_score !== undefined ? sub.grade_score : ''}">
          </div>
          <div class="form-group">
            <label class="form-label">Lecturer Comments for Student</label>
            <textarea id="studio-comment" class="form-control" placeholder="Provide additional feedback or praise for the student...">${escapeHtml(sub.grade_comment || '')}</textarea>
          </div>
        </div>
        <div style="text-align: right; margin-top: 1rem;">
          <button class="btn btn-primary" onclick="handleSaveGrade(${submissionId})">
            Save & Publish Grade
          </button>
        </div>
      </div>
    `;

    openModal('grading-studio-modal');
  } catch (err) {
    showToast(err.message, 'error');
  }
}

async function triggerAiGrading(submissionId) {
  const apiKey = document.getElementById('studio-api-key').value.trim();
  const btn = document.getElementById('btn-trigger-ai');
  btn.disabled = true;
  btn.textContent = '⏳ Analyzing Layout with AI...';

  try {
    const res = await apiCall('/ai/grade', 'POST', {
      submission_id: submissionId,
      api_key: apiKey
    });

    const data = res.data;
    showToast('AI analysis completed successfully!', 'success');

    // Update UI elements in studio
    document.getElementById('studio-circle').style.setProperty('--sim-val', data.similarity_score);
    document.getElementById('studio-sim-number').textContent = `${data.similarity_score}%`;
    document.getElementById('studio-ai-status').textContent = 'AI vision analysis completed!';

    let elements = [];
    try {
      elements = typeof data.detected_elements === 'string' ? JSON.parse(data.detected_elements) : data.detected_elements;
    } catch (e) {}

    document.getElementById('studio-elements-cloud').innerHTML = elements.map(el => `<span class="element-tag">${escapeHtml(el)}</span>`).join('');
    document.getElementById('studio-layout-report').textContent = data.layout_report;
    document.getElementById('studio-ai-feedback').textContent = data.ai_feedback;

    // Prefill score if not manually set
    const scoreInput = document.getElementById('studio-score');
    if (!scoreInput.value && data.suggested_score) {
      scoreInput.value = data.suggested_score;
    }

  } catch (err) {
    showToast(err.message, 'error');
  } finally {
    btn.disabled = false;
    btn.textContent = '⚡ Execute AI Automated Grading';
  }
}

async function handleSaveGrade(submissionId) {
  const score = parseFloat(document.getElementById('studio-score').value);
  const comment = document.getElementById('studio-comment').value.trim();

  if (isNaN(score) || score < 0 || score > 10) {
    showToast('Please enter a valid score between 0.0 and 10.0', 'error');
    return;
  }

  try {
    await apiCall('/grades', 'POST', {
      submission_id: submissionId,
      score,
      comment
    });

    showToast('Grade saved and published!', 'success');
    closeModal('grading-studio-modal');
    loadTeacherSubmissions();
  } catch (err) {
    showToast(err.message, 'error');
  }
}

// Lecturer Appeals Management
async function renderTeacherAppeals(container) {
  container.innerHTML = `
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 2rem;">
      <div>
        <h2>Grade Appeals Review Queue</h2>
        <p style="color: var(--text-secondary);">Review student dispute requests, reassess grades, and post resolution notes</p>
      </div>
      <div>
        <button class="btn btn-secondary btn-sm" onclick="renderTeacherAppeals(document.getElementById('view-container'))">↻ Refresh</button>
      </div>
    </div>
    <div id="teacher-appeals-table">
      <div class="glass-card" style="text-align: center; color: var(--text-muted);">Loading appeals...</div>
    </div>
  `;

  try {
    const res = await apiCall('/appeals');
    const containerEl = document.getElementById('teacher-appeals-table');
    if (res.success && res.data.length > 0) {
      containerEl.innerHTML = `
        <div class="table-container">
          <table>
            <thead>
              <tr>
                <th>Student</th>
                <th>Assignment</th>
                <th>Current Score</th>
                <th>Student Reason</th>
                <th>Status</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              ${res.data.map(a => `
                <tr>
                  <td>
                    <strong>${escapeHtml(a.student_name)}</strong>
                    <div style="font-size: 0.8rem; color: var(--text-muted);">${escapeHtml(a.student_code)}</div>
                  </td>
                  <td>${escapeHtml(a.assignment_title)}</td>
                  <td><strong style="color: var(--accent-emerald);">${a.score}/10</strong></td>
                  <td style="max-width: 320px; color: var(--text-secondary);">${escapeHtml(a.reason)}</td>
                  <td>
                    <span class="badge ${a.status === 'approved' ? 'badge-graded' : a.status === 'rejected' ? 'badge-appealed' : 'badge-grading'}">
                      ${a.status}
                    </span>
                  </td>
                  <td>
                    ${a.status === 'pending' ? `
                      <button class="btn btn-primary btn-sm" onclick="openReviewAppealModal(${a.appeal_id}, '${escapeHtml(a.student_name)}', ${a.score})">
                        Review Appeal
                      </button>
                    ` : '<span style="color: var(--text-muted);">Resolved</span>'}
                  </td>
                </tr>
              `).join('')}
            </tbody>
          </table>
        </div>
      `;
    } else {
      containerEl.innerHTML = `<div class="empty-state">No student appeals in the queue.</div>`;
    }
  } catch (err) {
    showToast(err.message, 'error');
  }
}

function openReviewAppealModal(appealId, studentName, currentScore) {
  document.getElementById('review-appeal-id').value = appealId;
  document.getElementById('review-appeal-title').textContent = `Review Appeal from ${studentName}`;
  document.getElementById('review-appeal-new-score').value = currentScore;
  document.getElementById('review-appeal-comment').value = '';
  openModal('review-appeal-modal');
}

async function handleResolveAppeal(status) {
  const appealId = document.getElementById('review-appeal-id').value;
  const newScore = parseFloat(document.getElementById('review-appeal-new-score').value);
  const comment = document.getElementById('review-appeal-comment').value.trim();

  try {
    await apiCall(`/appeals/${appealId}`, 'PUT', {
      status,
      new_score: isNaN(newScore) ? undefined : newScore,
      comment
    });

    closeModal('review-appeal-modal');
    showToast(`Appeal marked as ${status}`, 'success');
    renderTeacherAppeals(document.getElementById('view-container'));
  } catch (err) {
    showToast(err.message, 'error');
  }
}

// Assignment Modal (Create / Edit)
let editingAssignmentId = null;

function openCreateAssignmentModal() {
  editingAssignmentId = null;
  document.getElementById('assign-modal-title').textContent = 'Create New Assignment';
  document.getElementById('assign-form-title').value = '';
  document.getElementById('assign-form-subject').value = '1';
  document.getElementById('assign-form-proto').value = '';
  const fileInput = document.getElementById('assign-form-file');
  if (fileInput) fileInput.value = '';
  const fileNameDisplay = document.getElementById('assign-form-file-name');
  if (fileNameDisplay) fileNameDisplay.textContent = 'No file chosen';

  // Default deadline 3 days in future formatted YYYY-MM-DDTHH:MM
  const d = new Date(Date.now() + 3 * 24 * 3600 * 1000);
  document.getElementById('assign-form-deadline').value = d.toISOString().slice(0, 16);
  document.getElementById('assign-form-rubric').value = JSON.stringify({
    criteria: [
      { name: "Layout & Alignment", weight: 30, description: "Consistent margins and flex alignment" },
      { name: "Color & Typography", weight: 30, description: "Matches design palette and font hierarchy" },
      { name: "Functional Elements", weight: 40, description: "All interactive cards, tables, and search bars exist" }
    ]
  }, null, 2);

  openModal('assignment-modal');
}

async function openEditAssignmentModal(assignmentId) {
  editingAssignmentId = assignmentId;
  document.getElementById('assign-modal-title').textContent = 'Edit Assignment';

  try {
    const res = await apiCall(`/assignments/${assignmentId}`);
    if (!res.success) throw new Error(res.error);
    const a = res.data;

    document.getElementById('assign-form-title').value = a.title;
    document.getElementById('assign-form-subject').value = a.subject_id || '1';
    document.getElementById('assign-form-proto').value = a.prototype_url || '';
    document.getElementById('assign-form-deadline').value = a.deadline.replace(' ', 'T').slice(0, 16);
    document.getElementById('assign-form-rubric').value = a.rubric_json || '{}';

    const fileNameDisplay = document.getElementById('assign-form-file-name');
    if (fileNameDisplay) {
      fileNameDisplay.textContent = a.prompt_file_url ? 'Current: ' + a.prompt_file_url : 'No file uploaded';
    }

    openModal('assignment-modal');
  } catch (err) {
    showToast(err.message, 'error');
  }
}

async function handleSaveAssignmentSubmit(e) {
  e.preventDefault();
  const title = document.getElementById('assign-form-title').value.trim();
  const subjectId = parseInt(document.getElementById('assign-form-subject').value);
  const proto = document.getElementById('assign-form-proto').value.trim();
  const deadline = document.getElementById('assign-form-deadline').value;
  const rubricStr = document.getElementById('assign-form-rubric').value.trim();

  let rubricJson = {};
  try {
    rubricJson = JSON.parse(rubricStr);
  } catch (err) {
    showToast('Invalid JSON in Rubric Criteria field', 'error');
    return;
  }

  const payload = {
    title,
    subject_id: subjectId,
    prototype_url: proto,
    deadline: deadline.replace('T', ' ') + ':00',
    rubric_json: rubricJson
  };

  const fileInput = document.getElementById('assign-form-file');
  if (fileInput && fileInput.files && fileInput.files[0]) {
    const file = fileInput.files[0];
    const reader = new FileReader();
    reader.onload = async () => {
      payload.prompt_file_data = reader.result;
      payload.prompt_file_name = file.name;
      await submitAssignmentPayload(payload);
    };
    reader.readAsDataURL(file);
  } else {
    await submitAssignmentPayload(payload);
  }
}

async function submitAssignmentPayload(payload) {
  try {
    if (editingAssignmentId) {
      await apiCall(`/assignments/${editingAssignmentId}`, 'PUT', payload);
      showToast('Assignment updated successfully', 'success');
    } else {
      await apiCall('/assignments', 'POST', payload);
      showToast('Assignment created with prompt material!', 'success');
    }
    closeModal('assignment-modal');
    loadTeacherAssignments();
  } catch (err) {
    showToast(err.message, 'error');
  }
}

// Class Management (Requirement: Create Classes)
function openCreateClassModal() {
  document.getElementById('class-form-name').value = '';
  document.getElementById('class-form-dept').value = 'Software Engineering';
  openModal('class-modal');
}

async function handleSaveClassSubmit(e) {
  e.preventDefault();
  const name = document.getElementById('class-form-name').value.trim();
  const dept = document.getElementById('class-form-dept').value.trim();

  if (!name) {
    showToast('Class name is required', 'error');
    return;
  }

  try {
    await apiCall('/classes', 'POST', {
      class_name: name,
      department: dept
    });
    closeModal('class-modal');
    showToast(`Class '${name}' created successfully!`, 'success');
  } catch (err) {
    showToast(err.message, 'error');
  }
}
