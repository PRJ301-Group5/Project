/**
 * AI Grading Platform - Student Dashboard & Submission Workflows
 */

let currentSubmittingAssignmentId = null;
let uploadedImageDataUrl = null;

async function renderStudentDashboard(container) {
  container.innerHTML = `
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 2rem;">
      <div>
        <h2>Student Academic Portal</h2>
        <p style="color: var(--text-secondary);">Manage assigned subjects, submit work, and track AI-assisted grading</p>
      </div>
      <div style="display: flex; gap: 0.75rem;">
        <button class="btn btn-secondary btn-sm" onclick="renderStudentDashboard(document.getElementById('view-container'))">
          ↻ Refresh
        </button>
      </div>
    </div>

    <!-- Enrolled Subjects Section -->
    <h3 style="margin-bottom: 1rem; display: flex; align-items: center; gap: 0.5rem;">
      <span>Enrolled Subjects</span>
      <span class="brand-badge" id="subject-count">...</span>
    </h3>
    <div class="grid-3" id="student-subjects-grid" style="margin-bottom: 2.5rem;">
      <div class="glass-card" style="text-align: center; color: var(--text-muted);">Loading subjects...</div>
    </div>

    <!-- Active Assignments Section -->
    <h3 style="margin-bottom: 1rem;">Active Course Assignments</h3>
    <div id="student-assignments-list">
      <div class="glass-card" style="text-align: center; color: var(--text-muted);">Loading assignments...</div>
    </div>
  `;

  // Fetch subjects
  try {
    const res = await apiCall('/students/me/subjects');
    const grid = document.getElementById('student-subjects-grid');
    const countBadge = document.getElementById('subject-count');
    if (res.success && res.data.length > 0) {
      countBadge.textContent = `${res.data.length} Courses`;
      grid.innerHTML = res.data.map(s => `
        <div class="subject-card">
          <div class="subject-code">${escapeHtml(s.subject_code)}</div>
          <div class="subject-name">${escapeHtml(s.subject_name)}</div>
          <div class="subject-semester">${escapeHtml(s.semester)}</div>
        </div>
      `).join('');
    } else {
      grid.innerHTML = `<div class="empty-state" style="grid-column: span 3;">No enrolled subjects found.</div>`;
    }
  } catch (err) {
    console.error('Error loading student subjects:', err);
  }

  // Fetch assignments & student submissions
  try {
    const [assignRes, subRes] = await Promise.all([
      apiCall('/assignments?limit=50'),
      apiCall('/submissions')
    ]);

    const containerList = document.getElementById('student-assignments-list');
    if (assignRes.success && assignRes.data.items.length > 0) {
      const submissionsByAssign = {};
      if (subRes.success) {
        subRes.data.forEach(s => {
          submissionsByAssign[s.assignment_id] = s;
        });
      }

      containerList.innerHTML = `
        <div class="table-container">
          <table>
            <thead>
              <tr>
                <th>Subject</th>
                <th>Assignment Title</th>
                <th>Prototype Spec</th>
                <th>Deadline</th>
                <th>Status</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              ${assignRes.data.items.map(a => {
                const sub = submissionsByAssign[a.assignment_id];
                const isOverdue = new Date(a.deadline) < new Date();
                return `
                  <tr>
                    <td><span class="subject-code">${escapeHtml(a.subject_code || 'CS')}</span></td>
                    <td>
                      <strong style="color: var(--text-primary);">${escapeHtml(a.title)}</strong>
                      <div style="font-size: 0.8rem; color: var(--text-muted);">${escapeHtml(a.teacher_name || 'Prof.')}</div>
                    </td>
                    <td>
                      <div style="display: flex; flex-direction: column; gap: 0.35rem;">
                        ${a.prompt_file_url ? `
                          <a href="${escapeHtml(a.prompt_file_url)}" download class="btn btn-secondary btn-sm" style="padding: 0.25rem 0.6rem; color: var(--accent-cyan); font-weight: 600; font-size: 0.8rem;">
                            📥 Download Prompt
                          </a>
                        ` : ''}
                        ${a.prototype_url ? `
                          <a href="${escapeHtml(a.prototype_url)}" target="_blank" class="btn btn-secondary btn-sm" style="padding: 0.25rem 0.6rem; font-size: 0.8rem;">
                            View Spec ↗
                          </a>
                        ` : ''}
                        ${!a.prompt_file_url && !a.prototype_url ? '<span style="color: var(--text-muted);">None</span>' : ''}
                      </div>
                    </td>
                    <td>
                      <span style="color: ${isOverdue ? 'var(--accent-rose)' : 'var(--text-secondary)'};">
                        ${escapeHtml(a.deadline)}
                      </span>
                    </td>
                    <td>
                      ${sub ? `
                        <span class="badge badge-${sub.status}">${sub.status}</span>
                        ${sub.grade_score !== null && sub.grade_score !== undefined ? `<div style="font-weight: 700; color: var(--accent-emerald); font-size: 0.85rem; margin-top: 0.2rem;">Score: ${sub.grade_score}/10</div>` : ''}
                      ` : `
                        <span class="badge" style="background: rgba(255,255,255,0.05); color: var(--text-muted);">Not Submitted</span>
                      `}
                    </td>
                    <td>
                      ${sub ? `
                        <button class="btn btn-secondary btn-sm" onclick="viewSubmissionDetail(${sub.submission_id})">
                          View Result
                        </button>
                      ` : `
                        <button class="btn btn-primary btn-sm" onclick="openSubmitModal(${a.assignment_id}, '${escapeHtml(a.title)}')">
                          Submit Work
                        </button>
                      `}
                    </td>
                  </tr>
                `;
              }).join('')}
            </tbody>
          </table>
        </div>
      `;
    } else {
      containerList.innerHTML = `<div class="empty-state">No active assignments posted at this time.</div>`;
    }
  } catch (err) {
    console.error('Error loading assignments:', err);
  }
}

async function renderStudentSubmissions(container) {
  container.innerHTML = `
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 2rem;">
      <div>
        <h2>My Submitted Work</h2>
        <p style="color: var(--text-secondary);">Track evaluation progress, AI feedback reports, and final scores</p>
      </div>
    </div>
    <div id="student-submissions-list">
      <div class="glass-card" style="text-align: center; color: var(--text-muted);">Loading submissions...</div>
    </div>
  `;

  try {
    const res = await apiCall('/submissions');
    const listEl = document.getElementById('student-submissions-list');
    if (res.success && res.data.length > 0) {
      listEl.innerHTML = `
        <div class="table-container">
          <table>
            <thead>
              <tr>
                <th>Assignment</th>
                <th>Submitted At</th>
                <th>AI Similarity</th>
                <th>Grade</th>
                <th>Status</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              ${res.data.map(s => `
                <tr>
                  <td><strong>${escapeHtml(s.assignment_title)}</strong></td>
                  <td style="color: var(--text-muted);">${escapeHtml(s.submitted_at)}</td>
                  <td>
                    ${s.similarity_score !== null && s.similarity_score !== undefined ? `
                      <span style="color: var(--accent-emerald); font-weight: 700;">${s.similarity_score}%</span>
                    ` : '<span style="color: var(--text-muted);">Pending AI</span>'}
                  </td>
                  <td>
                    ${s.grade_score !== null && s.grade_score !== undefined ? `
                      <span style="font-weight: 700; color: var(--accent-emerald); font-size: 1rem;">${s.grade_score}/10</span>
                    ` : '<span style="color: var(--text-muted);">-</span>'}
                  </td>
                  <td><span class="badge badge-${s.status}">${s.status}</span></td>
                  <td>
                    <button class="btn btn-secondary btn-sm" onclick="viewSubmissionDetail(${s.submission_id})">
                      Inspect Report
                    </button>
                  </td>
                </tr>
              `).join('')}
            </tbody>
          </table>
        </div>
      `;
    } else {
      listEl.innerHTML = `<div class="empty-state">You haven't submitted any assignments yet.</div>`;
    }
  } catch (err) {
    showToast(err.message, 'error');
  }
}

async function renderStudentAppeals(container) {
  container.innerHTML = `
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 2rem;">
      <div>
        <h2>Grade Appeals & Regrades</h2>
        <p style="color: var(--text-secondary);">Review your submitted grade appeals and lecturer resolutions</p>
      </div>
    </div>
    <div id="student-appeals-list">
      <div class="glass-card" style="text-align: center; color: var(--text-muted);">Loading appeals...</div>
    </div>
  `;

  try {
    const res = await apiCall('/appeals');
    const listEl = document.getElementById('student-appeals-list');
    if (res.success && res.data.length > 0) {
      listEl.innerHTML = `
        <div class="table-container">
          <table>
            <thead>
              <tr>
                <th>Assignment</th>
                <th>Current Score</th>
                <th>My Appeal Reason</th>
                <th>Teacher Review</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              ${res.data.map(a => `
                <tr>
                  <td><strong>${escapeHtml(a.assignment_title)}</strong></td>
                  <td><strong style="color: var(--accent-emerald);">${a.score}/10</strong></td>
                  <td style="max-width: 300px; color: var(--text-secondary);">${escapeHtml(a.reason)}</td>
                  <td style="max-width: 300px;">
                    ${a.teacher_comment ? `<div style="color: var(--accent-cyan); font-size: 0.85rem;">"${escapeHtml(a.teacher_comment)}"</div>` : '<span style="color: var(--text-muted);">Awaiting review</span>'}
                  </td>
                  <td>
                    <span class="badge ${a.status === 'approved' ? 'badge-graded' : a.status === 'rejected' ? 'badge-appealed' : 'badge-grading'}">
                      ${a.status}
                    </span>
                  </td>
                </tr>
              `).join('')}
            </tbody>
          </table>
        </div>
      `;
    } else {
      listEl.innerHTML = `<div class="empty-state">No appeals filed yet.</div>`;
    }
  } catch (err) {
    showToast(err.message, 'error');
  }
}

// Submission Modal Handlers
let uploadedZipBase64 = null;
let uploadedZipSha256 = null;
let uploadedZipFilename = null;

function openSubmitModal(assignmentId, assignmentTitle) {
  currentSubmittingAssignmentId = assignmentId;
  uploadedImageDataUrl = null;
  uploadedZipBase64 = null;
  uploadedZipSha256 = null;
  uploadedZipFilename = null;

  document.getElementById('submit-modal-title').textContent = `Submit: ${assignmentTitle}`;
  const preview = document.getElementById('preview-submit-img');
  if (preview) preview.style.display = 'none';
  const shaBox = document.getElementById('preview-sha256-box');
  if (shaBox) shaBox.style.display = 'none';
  document.getElementById('dropzone-placeholder').style.display = 'block';
  document.getElementById('submit-file-input').value = '';

  openModal('submission-modal');
}

async function handleFileSelect(event) {
  const file = event.target.files[0];
  if (!file) return;

  const isZip = file.name.toLowerCase().endsWith('.zip');
  const isImage = file.type.startsWith('image/');

  if (!isZip && !isImage) {
    showToast('Please upload a ZIP source archive or an image file (PNG, JPG, ZIP)', 'error');
    return;
  }

  if (isZip) {
    uploadedImageDataUrl = null;
    uploadedZipFilename = file.name;

    try {
      const buffer = await file.arrayBuffer();
      // Calculate SHA-256 with Web Crypto API
      const hashBuffer = await crypto.subtle.digest('SHA-256', buffer);
      const hashArray = Array.from(new Uint8Array(hashBuffer));
      uploadedZipSha256 = hashArray.map(b => b.toString(16).padStart(2, '0')).join('');

      // Convert buffer to base64 string
      let binary = '';
      const bytes = new Uint8Array(buffer);
      const len = bytes.byteLength;
      for (let i = 0; i < len; i++) {
        binary += String.fromCharCode(bytes[i]);
      }
      uploadedZipBase64 = btoa(binary);

      // Update UI with SHA-256 display
      document.getElementById('dropzone-placeholder').style.display = 'none';
      const preview = document.getElementById('preview-submit-img');
      if (preview) preview.style.display = 'none';

      let shaBox = document.getElementById('preview-sha256-box');
      if (!shaBox) {
        shaBox = document.createElement('div');
        shaBox.id = 'preview-sha256-box';
        document.getElementById('dropzone').appendChild(shaBox);
      }
      shaBox.style.display = 'block';
      shaBox.innerHTML = `
        <div style="font-size: 2.5rem; margin-bottom: 0.5rem;">📦</div>
        <div style="font-weight: 700; color: #fff; font-size: 1.05rem;">${escapeHtml(file.name)}</div>
        <div style="font-size: 0.8rem; color: var(--text-secondary); margin-bottom: 0.75rem;">${(file.size / 1024).toFixed(1)} KB • ZIP Artifact Archive</div>
        <div style="background: rgba(16, 185, 129, 0.1); border: 1px solid rgba(16, 185, 129, 0.3); padding: 0.65rem 1rem; border-radius: var(--radius-md); text-align: left;">
          <div style="font-size: 0.75rem; color: var(--accent-emerald); font-weight: 700; text-transform: uppercase;">Calculated SHA-256 Checksum (Integrity Verified)</div>
          <code style="font-family: var(--font-mono); font-size: 0.78rem; color: #fff; word-break: break-all;">${uploadedZipSha256}</code>
        </div>
      `;

      showToast('Calculated SHA-256 checksum for artifact!', 'success');
    } catch (e) {
      showToast('Error reading ZIP file: ' + e.message, 'error');
    }
  } else {
    // Image selection
    uploadedZipBase64 = null;
    uploadedZipSha256 = null;
    const shaBox = document.getElementById('preview-sha256-box');
    if (shaBox) shaBox.style.display = 'none';

    const reader = new FileReader();
    reader.onload = (e) => {
      uploadedImageDataUrl = e.target.result;
      const preview = document.getElementById('preview-submit-img');
      preview.src = uploadedImageDataUrl;
      preview.style.display = 'block';
      document.getElementById('dropzone-placeholder').style.display = 'none';
    };
    reader.readAsDataURL(file);
  }
}

async function handleConfirmSubmission() {
  if (!currentSubmittingAssignmentId) return;
  if (!uploadedImageDataUrl && !uploadedZipBase64) {
    showToast('Please select or drag-and-drop a ZIP source code archive or screenshot first', 'error');
    return;
  }

  const btn = document.getElementById('btn-submit-upload');
  btn.disabled = true;
  btn.textContent = 'Uploading through SHA-256 Filter...';

  try {
    if (uploadedZipBase64) {
      // ZIP Submission through Sha256IntegrityFilter & Redis Queue
      const reviewerTeamId = state.user && state.user.teamId ? state.user.teamId : 5;
      const res = await apiCall('/submissions/upload-zip', 'POST', {
        assignment_id: currentSubmittingAssignmentId,
        team_id: reviewerTeamId,
        zip_base64: uploadedZipBase64,
        sha256_client: uploadedZipSha256,
        filename: uploadedZipFilename || 'source_code.zip'
      });

      closeModal('submission-modal');
      showToast('Artifact passed SHA-256 Gateway Filter! Dispatched into Redis Job Queue.', 'success');
    } else {
      // Screenshot submission
      const res = await apiCall('/submissions', 'POST', {
        assignment_id: currentSubmittingAssignmentId,
        screenshot_data: uploadedImageDataUrl
      });
      closeModal('submission-modal');
      showToast('Assignment submitted successfully!', 'success');
    }

    renderStudentDashboard(document.getElementById('view-container'));
    checkStudentDeadlines();
  } catch (err) {
    showToast(err.message, 'error');
  } finally {
    btn.disabled = false;
    btn.textContent = 'Submit Assignment';
  }
}

// Inspect Submission & AI Report
async function viewSubmissionDetail(submissionId) {
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

    const detailContainer = document.getElementById('report-modal-content');
    detailContainer.innerHTML = `
      <div class="modal-header">
        <div>
          <h3>Submission Evaluation Report</h3>
          <p style="color: var(--text-secondary); font-size: 0.9rem;">${escapeHtml(sub.assignment_title)} — ${escapeHtml(sub.student_name)} (${escapeHtml(sub.student_code)})</p>
        </div>
        <button class="modal-close" onclick="closeModal('report-modal')">&times;</button>
      </div>

      <div class="grading-studio" style="margin-top: 1rem;">
        <!-- Student Screenshot -->
        <div class="visual-preview-box">
          <div class="preview-header">
            <span>Student Submission Screenshot</span>
            <span class="badge badge-${sub.status}">${sub.status}</span>
          </div>
          <div class="preview-body">
            <img src="${escapeHtml(sub.screenshot_path)}" alt="Student Submission" onerror="this.src='data:image/svg+xml;utf8,<svg xmlns=\\'http://www.w3.org/2000/svg\\' width=\\'100\\' height=\\'100\\'><text x=\\'50%\\' y=\\'50%\\' fill=\\'gray\\' text-anchor=\\'middle\\'>Screenshot</text></svg>'">
          </div>
        </div>

        <!-- Reference Prototype -->
        <div class="visual-preview-box">
          <div class="preview-header">
            <span>Reference Prototype Spec</span>
            ${sub.prototype_url ? `<a href="${escapeHtml(sub.prototype_url)}" target="_blank" style="font-size: 0.8rem;">Open External ↗</a>` : ''}
          </div>
          <div class="preview-body">
            ${sub.prototype_url ? `<img src="${escapeHtml(sub.prototype_url)}" alt="Prototype" onerror="this.src='data:image/svg+xml;utf8,<svg xmlns=\\'http://www.w3.org/2000/svg\\' width=\\'100\\' height=\\'100\\'><text x=\\'50%\\' y=\\'50%\\' fill=\\'gray\\' text-anchor=\\'middle\\'>Prototype Spec</text></svg>'">` : '<div style="color: var(--text-muted);">No prototype image linked</div>'}
          </div>
        </div>
      </div>

      <!-- AI Evaluation Panel -->
      <div class="ai-results-panel">
        <div class="ai-header">
          <div class="similarity-metric">
            <div class="score-circle" style="--sim-val: ${sub.similarity_score || 0};">
              <div class="score-circle-inner">
                <span class="number">${sub.similarity_score || 0}%</span>
                <span class="label">SIMILARITY</span>
              </div>
            </div>
            <div>
              <h4>Multimodal AI Layout Analysis</h4>
              <p style="color: var(--text-secondary); font-size: 0.85rem;">Evaluation against rubric and visual prototype</p>
            </div>
          </div>
          <div>
            ${sub.grade_score !== null && sub.grade_score !== undefined ? `
              <div style="text-align: right;">
                <span style="font-size: 0.8rem; color: var(--text-muted); text-transform: uppercase;">Official Grade</span>
                <div style="font-size: 2rem; font-weight: 700; color: var(--accent-emerald);">${sub.grade_score}/10</div>
              </div>
            ` : '<span class="badge badge-grading">Awaiting Lecturer Approval</span>'}
          </div>
        </div>

        <div>
          <strong style="font-size: 0.85rem; color: var(--text-secondary); text-transform: uppercase;">Detected UI Elements:</strong>
          <div class="elements-cloud">
            ${elementsArr.length > 0 ? elementsArr.map(el => `<span class="element-tag">${escapeHtml(el)}</span>`).join('') : '<span style="color: var(--text-muted);">No elements analyzed</span>'}
          </div>
        </div>

        <div style="margin-top: 1rem;">
          <strong style="font-size: 0.85rem; color: var(--text-secondary); text-transform: uppercase;">Layout Audit Report:</strong>
          <div style="background: rgba(0,0,0,0.2); padding: 1rem; border-radius: var(--radius-md); font-family: var(--font-mono); font-size: 0.85rem; white-space: pre-wrap; margin-top: 0.4rem; color: #CBD5E1;">
            ${escapeHtml(sub.layout_report || 'Report will be generated when AI grading is executed.')}
          </div>
        </div>

        <div style="margin-top: 1rem;">
          <strong style="font-size: 0.85rem; color: var(--text-secondary); text-transform: uppercase;">AI Feedback & Recommendations:</strong>
          <div style="background: rgba(99, 102, 241, 0.08); border-left: 3px solid var(--primary); padding: 1rem; border-radius: var(--radius-sm); font-size: 0.9rem; margin-top: 0.4rem;">
            ${escapeHtml(sub.ai_feedback || 'No feedback available yet.')}
          </div>
        </div>

        ${sub.grade_comment ? `
          <div style="margin-top: 1rem; background: rgba(16, 185, 129, 0.08); border-left: 3px solid var(--accent-emerald); padding: 1rem; border-radius: var(--radius-sm);">
            <strong style="color: var(--accent-emerald); font-size: 0.85rem;">LECTURER FEEDBACK:</strong>
            <p style="margin-top: 0.25rem;">${escapeHtml(sub.grade_comment)}</p>
          </div>
        ` : ''}

        ${state.user.role === 'student' && sub.grade_score !== null && sub.grade_score !== undefined ? `
          <div style="margin-top: 1.5rem; text-align: right;">
            <button class="btn btn-secondary btn-sm" onclick="openAppealModal(${sub.grade_id || sub.submission_id}, '${escapeHtml(sub.assignment_title)}')">
              File Grade Appeal
            </button>
          </div>
        ` : ''}
      </div>
    `;

    openModal('report-modal');
  } catch (err) {
    showToast(err.message, 'error');
  }
}

// Appeal Modal
function openAppealModal(gradeId, title) {
  document.getElementById('appeal-grade-id').value = gradeId;
  document.getElementById('appeal-title').textContent = `Appeal Grade: ${title}`;
  document.getElementById('appeal-reason').value = '';
  openModal('appeal-modal');
}

async function handleConfirmAppeal() {
  const gradeId = document.getElementById('appeal-grade-id').value;
  const reason = document.getElementById('appeal-reason').value.trim();
  if (!reason) {
    showToast('Please state the reason for your grade appeal', 'error');
    return;
  }

  try {
    await apiCall('/appeals', 'POST', {
      grade_id: parseInt(gradeId),
      reason
    });
    closeModal('appeal-modal');
    closeModal('report-modal');
    showToast('Appeal filed successfully with the lecturer', 'success');
    switchView('appeals');
  } catch (err) {
    showToast(err.message, 'error');
  }
}
