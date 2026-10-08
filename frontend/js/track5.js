/**
 * RBL Track 5: Git Analytics Teamwork Assessor & École 42 Peer-Review Engine
 * Supports:
 * - Git Commit & Churn Metrics Visualization
 * - Individual Contribution Score (ICS %) Breakdown
 * - Free-Rider Anomaly Alerts (GHOST_COMMITTER, 11TH_HOUR_RUSHER)
 * - Pedagogical AI Critique Box (Gemini / OpenAI)
 * - École 42 Rotational Review Hub (G1 -> G2 -> ... -> G7 -> G1)
 * - 3-Part Rubric (Sandbox 30%, MVC2/CleanCode 40%, Peer Defense 30%)
 * - Correction Points Wallet Management
 * - Moulinette Test Suite Execution
 * - ZIP Artifact Submission with Live Client SHA-256 Hashing
 */

// 1. RENDER: Git Analytics Teamwork Assessor Dashboard
async function renderGitAnalyticsView(container) {
  container.innerHTML = `
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 2rem; flex-wrap: wrap; gap: 1rem;">
      <div>
        <div style="display: flex; align-items: center; gap: 0.75rem; margin-bottom: 0.5rem;">
          <span class="badge" style="background: rgba(99, 102, 241, 0.2); color: #818cf8; border: 1px solid rgba(99, 102, 241, 0.4);">
            RBL Research Track 5
          </span>
          <span class="badge" style="background: rgba(16, 185, 129, 0.2); color: #34d399; border: 1px solid rgba(16, 185, 129, 0.4);">
            PRJ301 MVC2 Engine
          </span>
        </div>
        <h2 style="font-size: 1.85rem; font-weight: 700;">Git Analytics Teamwork Assessor</h2>
        <p style="color: var(--text-secondary);">
          Quantitative Fair Contribution Engine • Eliminating Free-Riding • Automated Commit Analytics
        </p>
      </div>

      <div style="display: flex; gap: 0.75rem;">
        <button class="btn btn-secondary" onclick="openCustomGitLogModal()">
          <span>📝</span> Paste Custom Git Log
        </button>
        <button class="btn btn-primary" onclick="runGitAnalytics(2)">
          <span>⚡</span> Run Git Analytics Engine
        </button>
      </div>
    </div>

    <!-- KPI Summary Row -->
    <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 1.25rem; margin-bottom: 2rem;">
      <div class="card" style="padding: 1.25rem; border-left: 4px solid var(--accent-indigo);">
        <div style="font-size: 0.85rem; color: var(--text-secondary); margin-bottom: 0.25rem;">Active Repository</div>
        <div style="font-size: 1.35rem; font-weight: 700; color: #fff;">Team G5 - GitPulse</div>
        <div style="font-size: 0.8rem; color: var(--accent-cyan); margin-top: 0.25rem;">Branch: main (Clean MVC2)</div>
      </div>

      <div class="card" style="padding: 1.25rem; border-left: 4px solid var(--accent-cyan);">
        <div style="font-size: 0.85rem; color: var(--text-secondary); margin-bottom: 0.25rem;">Total Valid Commits</div>
        <div style="font-size: 1.35rem; font-weight: 700; color: #fff;">64 Commits</div>
        <div style="font-size: 0.8rem; color: var(--text-secondary); margin-top: 0.25rem;">Excluding merges & noise</div>
      </div>

      <div class="card" style="padding: 1.25rem; border-left: 4px solid var(--accent-emerald);">
        <div style="font-size: 0.85rem; color: var(--text-secondary); margin-bottom: 0.25rem;">Net Code Churn</div>
        <div style="font-size: 1.35rem; font-weight: 700; color: var(--accent-emerald);">+2,053 LoC</div>
        <div style="font-size: 0.8rem; color: var(--text-secondary); margin-top: 0.25rem;">2,435 added / 382 deleted</div>
      </div>

      <div class="card" style="padding: 1.25rem; border-left: 4px solid var(--accent-amber);">
        <div style="font-size: 0.85rem; color: var(--text-secondary); margin-bottom: 0.25rem;">Free-Riding Anomaly Status</div>
        <div style="font-size: 1.35rem; font-weight: 700; color: #f87171;">1 Flag Detected</div>
        <div style="font-size: 0.8rem; color: #f87171; margin-top: 0.25rem;">Minh Le (Ghost Committer)</div>
      </div>
    </div>

    <!-- Main Analytics Body: Contribution Table & AI Critique -->
    <div id="git-metrics-container">
      <div class="empty-state">
        <div class="empty-icon">📊</div>
        <p>Loading Team Git Metrics...</p>
      </div>
    </div>
  `;

  loadGitMetrics(2);
}

async function loadGitMetrics(submissionId = 2) {
  const container = document.getElementById('git-metrics-container');
  if (!container) return;

  try {
    const metrics = await apiCall(`/git/metrics?submission_id=${submissionId}`);
    renderGitMetricsTable(container, metrics);
  } catch (err) {
    container.innerHTML = `
      <div class="card" style="padding: 2rem; text-align: center;">
        <p style="color: #f87171;">Could not load Git metrics: ${escapeHtml(err.message)}</p>
        <button class="btn btn-primary" style="margin-top: 1rem;" onclick="runGitAnalytics(${submissionId})">
          Trigger Initial Analytics Run
        </button>
      </div>
    `;
  }
}

function renderGitMetricsTable(container, metrics) {
  if (!metrics || metrics.length === 0) {
    container.innerHTML = `
      <div class="empty-state">
        <div class="empty-icon">📁</div>
        <p>No Git metrics recorded for this submission yet.</p>
        <button class="btn btn-primary" onclick="runGitAnalytics(2)">Run Analysis Now</button>
      </div>
    `;
    return;
  }

  let rowsHtml = '';
  let aiCritiqueText = metrics[0].ai_critique_summary || 'No AI critique compiled yet.';

  metrics.forEach(m => {
    let flagBadges = '';
    let isGhost = false;
    if (m.anomaly_flags) {
      try {
        const flags = JSON.parse(m.anomaly_flags);
        flags.forEach(f => {
          if (f === 'GHOST_COMMITTER' || f === '11TH_HOUR_RUSHER') {
            isGhost = true;
            flagBadges += `<span class="badge" style="background: rgba(239, 68, 68, 0.2); color: #f87171; border: 1px solid rgba(239, 68, 68, 0.4); margin-right: 0.25rem;">⚠️ ${f}</span>`;
          } else {
            flagBadges += `<span class="badge" style="background: rgba(16, 185, 129, 0.2); color: #34d399; margin-right: 0.25rem;">✓ ${f}</span>`;
          }
        });
      } catch (e) {
        flagBadges = `<span class="badge">${escapeHtml(m.anomaly_flags)}</span>`;
      }
    }

    const rowBg = isGhost ? 'background: rgba(239, 68, 68, 0.05);' : '';
    rowsHtml += `
      <tr style="${rowBg}">
        <td>
          <div style="font-weight: 600; color: #fff;">${escapeHtml(m.author_name)}</div>
          <small style="color: var(--text-secondary);">${escapeHtml(m.author_email || 'N/A')}</small>
        </td>
        <td style="text-align: center;"><strong>${m.total_commits}</strong></td>
        <td style="text-align: center; color: var(--accent-emerald);">+${m.lines_added}</td>
        <td style="text-align: center; color: #f87171;">-${m.lines_deleted}</td>
        <td style="text-align: center; font-weight: 600;">${m.net_churn}</td>
        <td style="text-align: center;">${(m.commit_entropy * 100).toFixed(0)}%</td>
        <td style="text-align: center;">${m.semantic_score.toFixed(1)}%</td>
        <td style="text-align: center;">${(m.module_ownership * 100).toFixed(0)}%</td>
        <td style="text-align: center;">
          <div style="display: flex; align-items: center; gap: 0.5rem; justify-content: center;">
            <div style="flex: 1; max-width: 80px; height: 8px; background: rgba(255,255,255,0.1); border-radius: 4px; overflow: hidden;">
              <div style="width: ${Math.min(100, m.contribution_score)}%; height: 100%; background: ${isGhost ? '#f87171' : 'var(--accent-primary)'};"></div>
            </div>
            <strong style="color: ${isGhost ? '#f87171' : 'var(--accent-cyan)'}; font-size: 1.05rem;">
              ${m.contribution_score.toFixed(1)}%
            </strong>
          </div>
        </td>
        <td>${flagBadges || '<span style="color: var(--text-muted);">None</span>'}</td>
      </tr>
    `;
  });

  container.innerHTML = `
    <!-- Metrics Table Card -->
    <div class="card" style="padding: 1.5rem; margin-bottom: 2rem;">
      <h3 style="font-size: 1.15rem; font-weight: 600; margin-bottom: 1rem;">
        Team Contribution & Git Churn Breakdown (Formula-Normalized)
      </h3>
      <div style="overflow-x: auto;">
        <table class="data-table" style="width: 100%; text-align: left;">
          <thead>
            <tr>
              <th>Author / Member</th>
              <th style="text-align: center;">Commits</th>
              <th style="text-align: center;">LoC (+)</th>
              <th style="text-align: center;">LoC (-)</th>
              <th style="text-align: center;">Net Churn</th>
              <th style="text-align: center;">Time Entropy</th>
              <th style="text-align: center;">Semantic Msg</th>
              <th style="text-align: center;">Module Ownership</th>
              <th style="text-align: center;">ICS Contribution %</th>
              <th>Anomaly Flags</th>
            </tr>
          </thead>
          <tbody>
            ${rowsHtml}
          </tbody>
        </table>
      </div>
    </div>

    <!-- AI Pedagogical Assessment Card -->
    <div class="card" style="padding: 1.75rem; border: 1px solid rgba(99, 102, 241, 0.3); background: rgba(99, 102, 241, 0.04);">
      <div style="display: flex; align-items: center; gap: 0.75rem; margin-bottom: 1rem;">
        <div style="width: 36px; height: 36px; border-radius: 8px; background: rgba(99, 102, 241, 0.2); display: flex; align-items: center; justify-content: center; color: #818cf8; font-size: 1.25rem;">
          ✨
        </div>
        <div>
          <h3 style="font-size: 1.15rem; font-weight: 700; color: #fff;">
            Multimodal AI Pedagogical Assessment & Free-Rider Analysis
          </h3>
          <p style="font-size: 0.8rem; color: var(--text-secondary);">
            Powered by Google Gemini / OpenAI • Quantitative synthesis of code churn & commit patterns
          </p>
        </div>
      </div>
      <div style="background: rgba(0,0,0,0.3); padding: 1.25rem; border-radius: var(--radius-md); font-family: var(--font-mono); font-size: 0.88rem; line-height: 1.6; color: #e2e8f0; white-space: pre-wrap;">${escapeHtml(aiCritiqueText)}</div>
    </div>
  `;
}

async function runGitAnalytics(submissionId = 2) {
  showToast('Running Git Analytics Engine & AI synthesis...', 'info');
  try {
    const res = await apiCall('/git/analyze', 'POST', { submission_id: submissionId });
    showToast('Git Analytics completed successfully!', 'success');
    renderGitMetricsTable(document.getElementById('git-metrics-container'), res.metrics);
  } catch (err) {
    showToast('Failed to run Git Analytics: ' + err.message, 'error');
  }
}

// 2. RENDER: École 42 Rotational Peer-Review & Correction Points Hub
async function renderPeerReviewView(container) {
  container.innerHTML = `
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 2rem; flex-wrap: wrap; gap: 1rem;">
      <div>
        <div style="display: flex; align-items: center; gap: 0.75rem; margin-bottom: 0.5rem;">
          <span class="badge" style="background: rgba(16, 185, 129, 0.2); color: #34d399; border: 1px solid rgba(16, 185, 129, 0.4);">
            École 42 Peer-Review Engine
          </span>
          <span class="badge" style="background: rgba(245, 158, 11, 0.2); color: #fbbf24; border: 1px solid rgba(245, 158, 11, 0.4);">
            Closed Cycle: G1 → G2 → ... → G7 → G1
          </span>
        </div>
        <h2 style="font-size: 1.85rem; font-weight: 700;">Rotational Peer-Review & Correction Points</h2>
        <p style="color: var(--text-secondary);">
          Decentralized Evaluation • Correction Point Economy • 30/40/30 Standardized Rubric
        </p>
      </div>

      <div id="correction-points-badge-box">
        <!-- Badge loaded dynamically -->
      </div>
    </div>

    <!-- Rotational Topology Card -->
    <div class="card" style="padding: 1.5rem; margin-bottom: 2rem;">
      <h3 style="font-size: 1.15rem; font-weight: 600; margin-bottom: 0.75rem;">
        Rotational Review Loop Status
      </h3>
      <p style="color: var(--text-secondary); font-size: 0.9rem; margin-bottom: 1.25rem;">
        To eliminate mutual favoritism, groups are evaluated sequentially: Group N evaluates Group N+1.
      </p>
      <div id="rotational-topology-grid" style="display: flex; gap: 0.5rem; flex-wrap: wrap; align-items: center;">
        <span class="badge" style="padding: 0.5rem 0.85rem; font-size: 0.9rem;">G1 (Clean Code) ➔ G2</span>
        <span>➔</span>
        <span class="badge" style="padding: 0.5rem 0.85rem; font-size: 0.9rem;">G2 (Testing) ➔ G3</span>
        <span>➔</span>
        <span class="badge" style="padding: 0.5rem 0.85rem; font-size: 0.9rem;">G3 (Security) ➔ G4</span>
        <span>➔</span>
        <span class="badge" style="padding: 0.5rem 0.85rem; font-size: 0.9rem;">G4 (Similarity) ➔ G5</span>
        <span>➔</span>
        <span class="badge" style="padding: 0.5rem 0.85rem; font-size: 0.9rem; background: rgba(99, 102, 241, 0.2); color: #818cf8; border: 1px solid rgba(99, 102, 241, 0.4);">
          ★ G5 (Git Assessor) ➔ G6
        </span>
        <span>➔</span>
        <span class="badge" style="padding: 0.5rem 0.85rem; font-size: 0.9rem;">G6 (Sandbox) ➔ G7</span>
        <span>➔</span>
        <span class="badge" style="padding: 0.5rem 0.85rem; font-size: 0.9rem;">G7 (Benchmark) ➔ G1</span>
      </div>
    </div>

    <!-- Evaluation Form & Wallet Container -->
    <div style="display: grid; grid-template-columns: 2fr 1fr; gap: 1.5rem; align-items: start;">
      <div id="peer-eval-form-card" class="card" style="padding: 1.75rem;">
        <div class="empty-state"><div class="empty-icon">⏳</div><p>Loading assigned target review...</p></div>
      </div>

      <div id="wallet-history-card" class="card" style="padding: 1.75rem;">
        <div class="empty-state"><div class="empty-icon">👛</div><p>Loading wallet transactions...</p></div>
      </div>
    </div>
  `;

  loadPeerReviewHub();
}

async function loadPeerReviewHub() {
  try {
    const reviewerTeamId = state.user && state.user.teamId ? state.user.teamId : 5;
    const [assignment, wallet] = await Promise.all([
      apiCall(`/peer-review/assignment?reviewer_team_id=${reviewerTeamId}`),
      apiCall(`/correction-points?team_id=${reviewerTeamId}`)
    ]);

    // Render Badge Box
    const badgeBox = document.getElementById('correction-points-badge-box');
    if (badgeBox) {
      badgeBox.innerHTML = `
        <div style="display: flex; align-items: center; gap: 1rem; background: rgba(245, 158, 11, 0.1); border: 1px solid rgba(245, 158, 11, 0.3); padding: 0.75rem 1.25rem; border-radius: var(--radius-lg);">
          <div style="font-size: 2rem;">🪙</div>
          <div>
            <div style="font-size: 0.8rem; color: var(--text-secondary); text-transform: uppercase;">Correction Points Balance</div>
            <div style="font-size: 1.5rem; font-weight: 800; color: #fbbf24;">
              ${wallet.balance} Points
            </div>
          </div>
        </div>
      `;
    }

    // Render Target Evaluation Form
    const formCard = document.getElementById('peer-eval-form-card');
    if (formCard) {
      if (!assignment || !assignment.target_code) {
        formCard.innerHTML = `<div class="empty-state"><p>No active target team assigned for evaluation.</p></div>`;
      } else {
        formCard.innerHTML = `
          <div style="display: flex; justify-content: space-between; align-items: start; margin-bottom: 1.25rem;">
            <div>
              <span class="badge" style="background: rgba(99, 102, 241, 0.2); color: #818cf8; margin-bottom: 0.5rem;">
                Assigned Target Team
              </span>
              <h3 style="font-size: 1.35rem; font-weight: 700; color: #fff;">
                ${escapeHtml(assignment.target_code)}: ${escapeHtml(assignment.target_name)}
              </h3>
              <p style="color: var(--accent-cyan); font-size: 0.9rem;">
                ${escapeHtml(assignment.project_title || 'PRJ301 Project')}
              </p>
            </div>
            <div>
              <span class="badge ${assignment.status === 'completed' ? 'role-teacher' : 'role-student'}">
                Status: ${escapeHtml(assignment.status)}
              </span>
            </div>
          </div>

          <div style="background: rgba(0,0,0,0.2); border-radius: var(--radius-md); padding: 1rem; margin-bottom: 1.5rem; font-size: 0.85rem;">
            <div><strong>Repository:</strong> <a href="${escapeHtml(assignment.repo_url || '#')}" target="_blank" style="color: var(--accent-cyan);">${escapeHtml(assignment.repo_url || 'https://github.com/rbl-2026/prj301-target')}</a></div>
            <div style="margin-top: 0.25rem;"><strong>Artifact SHA-256:</strong> <code style="font-family: var(--font-mono); color: var(--accent-emerald); font-size: 0.8rem;">${escapeHtml(assignment.sha256_hash || 'a8f5b879c93e4d588f6230f81d115e45a57a167096d2fa48e9d50f83e20e8b9a')}</code></div>
          </div>

          <!-- 3-Part Rubric Form -->
          <form onsubmit="handlePeerReviewSubmit(event, ${assignment.review_id}, ${reviewerTeamId})">
            <h4 style="font-size: 1rem; font-weight: 600; margin-bottom: 1rem; color: #fff;">
              Peer Defense Rubric Criteria (Total: 100%)
            </h4>

            <!-- Rubric 1: Sandbox 30% -->
            <div class="form-group" style="margin-bottom: 1.25rem;">
              <div style="display: flex; justify-content: space-between; margin-bottom: 0.25rem;">
                <label class="form-label" style="margin-bottom: 0;">1. Sandbox Compliance (30%)</label>
                <span id="label-sandbox-score" style="font-weight: 700; color: var(--accent-cyan);">9.0 / 10</span>
              </div>
              <small style="color: var(--text-secondary); display: block; margin-bottom: 0.5rem;">
                Builds and runs inside Docker container without network connection, within 512MB RAM.
              </small>
              <input type="range" id="input-sandbox-score" min="0" max="10" step="0.5" value="${assignment.sandbox_score || 9.0}" 
                style="width: 100%; accent-color: var(--accent-primary);"
                oninput="updateRubricLiveScore()">
            </div>

            <!-- Rubric 2: MVC2 / Clean Code 40% -->
            <div class="form-group" style="margin-bottom: 1.25rem;">
              <div style="display: flex; justify-content: space-between; margin-bottom: 0.25rem;">
                <label class="form-label" style="margin-bottom: 0;">2. MVC2 Architecture & Clean Code (40%)</label>
                <span id="label-mvc2-score" style="font-weight: 700; color: var(--accent-cyan);">9.0 / 10</span>
              </div>
              <small style="color: var(--text-secondary); display: block; margin-bottom: 0.5rem;">
                Strict separation of Servlets, Services, DAOs, JSP/JSTL (no scriptlets), SHA-256 filter.
              </small>
              <input type="range" id="input-mvc2-score" min="0" max="10" step="0.5" value="${assignment.mvc2_clean_code_score || 9.0}" 
                style="width: 100%; accent-color: var(--accent-primary);"
                oninput="updateRubricLiveScore()">
            </div>

            <!-- Rubric 3: Peer Defense 30% -->
            <div class="form-group" style="margin-bottom: 1.25rem;">
              <div style="display: flex; justify-content: space-between; margin-bottom: 0.25rem;">
                <label class="form-label" style="margin-bottom: 0;">3. Peer Defense & Q&A (30%)</label>
                <span id="label-defense-score" style="font-weight: 700; color: var(--accent-cyan);">8.5 / 10</span>
              </div>
              <small style="color: var(--text-secondary); display: block; margin-bottom: 0.5rem;">
                15-minute live defense, explaining code logic, edge-case resilience, and answering questions.
              </small>
              <input type="range" id="input-defense-score" min="0" max="10" step="0.5" value="${assignment.peer_defense_score || 8.5}" 
                style="width: 100%; accent-color: var(--accent-primary);"
                oninput="updateRubricLiveScore()">
            </div>

            <!-- Live Final Score Summary Banner -->
            <div style="background: rgba(16, 185, 129, 0.1); border: 1px solid rgba(16, 185, 129, 0.3); padding: 1rem; border-radius: var(--radius-md); display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.25rem;">
              <div>
                <div style="font-size: 0.8rem; color: var(--text-secondary);">Calculated Final Peer Score</div>
                <div style="font-size: 0.75rem; color: var(--text-muted);">(Sandbox × 0.3) + (MVC2 × 0.4) + (Defense × 0.3)</div>
              </div>
              <div id="live-final-score-badge" style="font-size: 1.75rem; font-weight: 800; color: var(--accent-emerald);">
                ${assignment.final_score > 0 ? assignment.final_score.toFixed(2) : '8.85'} / 10
              </div>
            </div>

            <div class="form-group" style="margin-bottom: 1.5rem;">
              <label class="form-label">Reviewer Feedback & Constructive Critique</label>
              <textarea id="peer-eval-feedback" class="form-control" style="min-height: 80px;" placeholder="Provide specific feedback on code architecture, Docker execution, or defense...">${escapeHtml(assignment.feedback || '')}</textarea>
            </div>

            <div style="display: flex; justify-content: flex-end; gap: 0.75rem;">
              <button type="submit" class="btn btn-primary">
                <span>✓</span> Submit Peer Evaluation (+1 Correction Point)
              </button>
            </div>
          </form>
        `;
        updateRubricLiveScore();
      }
    }

    // Render Wallet History
    const walletCard = document.getElementById('wallet-history-card');
    if (walletCard) {
      let txListHtml = '';
      if (wallet.transactions && wallet.transactions.length > 0) {
        wallet.transactions.forEach(tx => {
          const isPos = tx.amount > 0;
          txListHtml += `
            <div style="display: flex; justify-content: space-between; align-items: center; padding: 0.75rem 0; border-bottom: 1px solid var(--border-glass);">
              <div>
                <div style="font-weight: 600; font-size: 0.85rem; color: #fff;">${escapeHtml(tx.description || tx.action_type)}</div>
                <small style="color: var(--text-secondary); font-size: 0.75rem;">${escapeHtml(tx.created_at || 'Just now')}</small>
              </div>
              <div style="font-weight: 700; color: ${isPos ? 'var(--accent-emerald)' : '#f87171'}; font-size: 1.05rem;">
                ${isPos ? '+' : ''}${tx.amount}
              </div>
            </div>
          `;
        });
      } else {
        txListHtml = `<p style="color: var(--text-secondary); font-size: 0.85rem;">No transaction history.</p>`;
      }

      walletCard.innerHTML = `
        <h3 style="font-size: 1.15rem; font-weight: 600; margin-bottom: 1rem;">
          Correction Points Ledger
        </h3>
        <p style="color: var(--text-secondary); font-size: 0.82rem; margin-bottom: 1.25rem;">
          École 42 Rule: -1 point to request project review, +1 point for completing a peer review.
        </p>
        <div style="max-height: 380px; overflow-y: auto;">
          ${txListHtml}
        </div>
      `;
    }

  } catch (err) {
    console.error('Error loading peer review hub:', err);
  }
}

function updateRubricLiveScore() {
  const sb = parseFloat(document.getElementById('input-sandbox-score')?.value || 0);
  const mvc = parseFloat(document.getElementById('input-mvc2-score')?.value || 0);
  const def = parseFloat(document.getElementById('input-defense-score')?.value || 0);

  if (document.getElementById('label-sandbox-score')) document.getElementById('label-sandbox-score').textContent = `${sb.toFixed(1)} / 10`;
  if (document.getElementById('label-mvc2-score')) document.getElementById('label-mvc2-score').textContent = `${mvc.toFixed(1)} / 10`;
  if (document.getElementById('label-defense-score')) document.getElementById('label-defense-score').textContent = `${def.toFixed(1)} / 10`;

  const finalScore = (sb * 0.3) + (mvc * 0.4) + (def * 0.3);
  const finalBadge = document.getElementById('live-final-score-badge');
  if (finalBadge) {
    finalBadge.textContent = `${finalScore.toFixed(2)} / 10`;
  }
}

async function handlePeerReviewSubmit(e, reviewId, reviewerTeamId) {
  e.preventDefault();
  const sb = parseFloat(document.getElementById('input-sandbox-score').value);
  const mvc = parseFloat(document.getElementById('input-mvc2-score').value);
  const def = parseFloat(document.getElementById('input-defense-score').value);
  const feedback = document.getElementById('peer-eval-feedback').value;

  try {
    const res = await apiCall('/peer-review/submit', 'POST', {
      review_id: reviewId,
      reviewer_team_id: reviewerTeamId,
      sandbox_score: sb,
      mvc2_score: mvc,
      defense_score: def,
      feedback: feedback
    });
    showToast(res.reward_message || 'Evaluation submitted and +1 Correction Point granted!', 'success');
    loadPeerReviewHub();
  } catch (err) {
    showToast('Failed to submit evaluation: ' + err.message, 'error');
  }
}

// 3. RENDER: Moulinette Automated Grading Studio
async function renderMoulinetteView(container) {
  container.innerHTML = `
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 2rem; flex-wrap: wrap; gap: 1rem;">
      <div>
        <div style="display: flex; align-items: center; gap: 0.75rem; margin-bottom: 0.5rem;">
          <span class="badge" style="background: rgba(239, 68, 68, 0.2); color: #f87171; border: 1px solid rgba(239, 68, 68, 0.4);">
            Moulinette Auto-Grader
          </span>
          <span class="badge" style="background: rgba(99, 102, 241, 0.2); color: #818cf8; border: 1px solid rgba(99, 102, 241, 0.4);">
            Public + Hidden Test Suites
          </span>
        </div>
        <h2 style="font-size: 1.85rem; font-weight: 700;">Moulinette Automated Grading System</h2>
        <p style="color: var(--text-secondary);">
          Automated assertion engine • Public smoke tests • Hidden exception & security edge-case testing
        </p>
      </div>

      <div>
        <button class="btn btn-primary" onclick="triggerMoulinetteRun(2)">
          <span>▶</span> Run Full Moulinette Test Suite
        </button>
      </div>
    </div>

    <!-- Test Suite Cards Container -->
    <div id="moulinette-results-container">
      <div class="empty-state"><div class="empty-icon">⚙️</div><p>Loading Moulinette test status...</p></div>
    </div>
  `;

  loadMoulinetteResults(2);
}

async function loadMoulinetteResults(submissionId = 2) {
  const container = document.getElementById('moulinette-results-container');
  if (!container) return;

  try {
    const results = await apiCall(`/moulinette/results?submission_id=${submissionId}`);
    renderMoulinetteResults(container, results);
  } catch (err) {
    container.innerHTML = `<div class="card" style="padding: 2rem; text-align: center;"><p style="color: #f87171;">Could not load Moulinette results: ${escapeHtml(err.message)}</p></div>`;
  }
}

function renderMoulinetteResults(container, results) {
  if (!results || results.length === 0) {
    container.innerHTML = `
      <div class="empty-state">
        <div class="empty-icon">🧪</div>
        <p>No Moulinette assertions have been executed for this submission yet.</p>
        <button class="btn btn-primary" onclick="triggerMoulinetteRun(2)">Run Moulinette Now</button>
      </div>
    `;
    return;
  }

  let publicCards = '';
  let hiddenCards = '';
  let passedCount = 0;

  results.forEach(r => {
    const isPass = r.is_passed === 1;
    if (isPass) passedCount++;

    const card = `
      <div style="background: rgba(255,255,255,0.02); border: 1px solid ${isPass ? 'rgba(16, 185, 129, 0.3)' : 'rgba(239, 68, 68, 0.3)'}; border-radius: var(--radius-md); padding: 1.25rem; margin-bottom: 0.75rem;">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.5rem;">
          <div style="display: flex; align-items: center; gap: 0.5rem;">
            <span style="font-weight: 700; color: #fff;">${escapeHtml(r.test_code)}:</span>
            <span style="font-weight: 600; color: var(--text-primary);">${escapeHtml(r.title)}</span>
          </div>
          <div style="display: flex; align-items: center; gap: 0.75rem;">
            <span style="font-size: 0.8rem; color: var(--text-secondary);">${r.execution_time_ms} ms</span>
            <span class="badge ${isPass ? 'role-teacher' : 'role-admin'}">
              ${isPass ? '✓ PASSED' : '✕ FAILED'}
            </span>
          </div>
        </div>
        <div style="font-family: var(--font-mono); font-size: 0.82rem; color: ${isPass ? 'var(--accent-emerald)' : '#f87171'}; background: rgba(0,0,0,0.25); padding: 0.5rem 0.75rem; border-radius: 4px;">
          ${escapeHtml(r.output_log || 'No log output')}
        </div>
      </div>
    `;

    if (r.test_type === 'PUBLIC') {
      publicCards += card;
    } else {
      hiddenCards += card;
    }
  });

  const total = results.length;
  const passRate = ((passedCount / total) * 100).toFixed(0);

  container.innerHTML = `
    <!-- Summary Header Card -->
    <div class="card" style="padding: 1.5rem; margin-bottom: 2rem; display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 1rem;">
      <div>
        <div style="font-size: 0.85rem; color: var(--text-secondary);">Test Suite Score</div>
        <div style="font-size: 2rem; font-weight: 800; color: var(--accent-emerald);">${passedCount} / ${total} Tests Passed (${passRate}%)</div>
      </div>
      <div style="display: flex; gap: 1rem;">
        <div style="text-align: right;">
          <div style="font-size: 0.8rem; color: var(--text-secondary);">Docker Sandbox</div>
          <div style="color: var(--accent-cyan); font-weight: 600;">Network-Isolated (--network none)</div>
        </div>
        <div style="text-align: right;">
          <div style="font-size: 0.8rem; color: var(--text-secondary);">Watchdog Timeout</div>
          <div style="color: var(--accent-cyan); font-weight: 600;">45.0s Strict Cutoff</div>
        </div>
      </div>
    </div>

    <!-- Public Tests -->
    <div class="card" style="padding: 1.5rem; margin-bottom: 1.5rem;">
      <h3 style="font-size: 1.15rem; font-weight: 600; margin-bottom: 1rem; color: #fff; display: flex; align-items: center; gap: 0.5rem;">
        <span>🌐</span> Public Test Suite (Basic Portal Requirements)
      </h3>
      ${publicCards}
    </div>

    <!-- Hidden Tests -->
    <div class="card" style="padding: 1.5rem; border: 1px solid rgba(245, 158, 11, 0.3);">
      <h3 style="font-size: 1.15rem; font-weight: 600; margin-bottom: 1rem; color: #fbbf24; display: flex; align-items: center; gap: 0.5rem;">
        <span>🔒</span> Hidden Test Suite (Edge Cases, Zip Slip, Exception Handling)
      </h3>
      ${hiddenCards}
    </div>
  `;
}

async function triggerMoulinetteRun(submissionId = 2) {
  showToast('Executing Moulinette public and hidden test cases...', 'info');
  try {
    const res = await apiCall('/moulinette/run', 'POST', { submission_id: submissionId });
    showToast('Moulinette test suite execution completed!', 'success');
    loadMoulinetteResults(submissionId);
  } catch (err) {
    showToast('Moulinette run failed: ' + err.message, 'error');
  }
}

// 4. Modal for Custom Git Log Input
function openCustomGitLogModal() {
  let modal = document.getElementById('custom-gitlog-modal');
  if (!modal) {
    modal = document.createElement('div');
    modal.id = 'custom-gitlog-modal';
    modal.className = 'modal-overlay';
    modal.innerHTML = `
      <div class="modal-content" style="max-width: 680px;">
        <div class="modal-header">
          <h3>Paste Custom Git Commit Stream</h3>
          <button class="modal-close" onclick="closeModal('custom-gitlog-modal')">&times;</button>
        </div>
        <p style="color: var(--text-secondary); font-size: 0.85rem; margin-bottom: 1rem;">
          Paste raw git log or git commits formatted with <code>---COMMIT---</code> blocks to test the quantitative scoring formula and free-rider anomaly detection.
        </p>
        <div class="form-group">
          <label class="form-label">Raw Git Log Stream</label>
          <textarea id="custom-gitlog-input" class="form-control" style="font-family: var(--font-mono); font-size: 0.82rem; min-height: 200px;">---COMMIT---
commit: f1a2b3
author: Huy Nguyen
email: huy.nguyen@school.edu
date: 1726000000000
message: feat: establish MVC2 servlet controller and DAO layers
added: 650
deleted: 40
file: src/com/aigrading/server/RequestHandler.java
---COMMIT---
commit: f2b3c4
author: Linh Tran
email: linh.tran@school.edu
date: 1726200000000
message: feat: implement Redis job queue with priority scheduling
added: 480
deleted: 30
file: src/com/aigrading/services/RedisJobQueueService.java
---COMMIT---
commit: f3c4d5
author: Minh Le
email: minh.le@school.edu
date: 1727000000000
message: update readme typo
added: 8
deleted: 2
file: README.md</textarea>
        </div>
        <div style="display: flex; justify-content: flex-end; gap: 0.75rem; margin-top: 1.5rem;">
          <button class="btn btn-secondary" onclick="closeModal('custom-gitlog-modal')">Cancel</button>
          <button class="btn btn-primary" onclick="submitCustomGitLog()">Analyze Git Stream</button>
        </div>
      </div>
    `;
    document.body.appendChild(modal);
  }
  openModal('custom-gitlog-modal');
}

async function submitCustomGitLog() {
  const text = document.getElementById('custom-gitlog-input').value;
  closeModal('custom-gitlog-modal');
  showToast('Analyzing custom git stream with AI Engine...', 'info');
  try {
    const res = await apiCall('/git/analyze', 'POST', {
      submission_id: 2,
      raw_git_log: text
    });
    showToast('Custom Git stream analyzed successfully!', 'success');
    renderGitMetricsTable(document.getElementById('git-metrics-container'), res.metrics);
  } catch (err) {
    showToast('Failed to analyze: ' + err.message, 'error');
  }
}
