document.addEventListener('DOMContentLoaded', async () => {
  const API = window.API_BASE_URL ? window.API_BASE_URL.replace(/\/api\/auth\/?$/, '/api/recruitment') : `${window.location.origin}/api/recruitment`;
  const token = () => (typeof SessionManager !== 'undefined' && SessionManager.getToken()) || sessionStorage.getItem('authToken') || localStorage.getItem('ats_jwt_token');
  const list = document.getElementById('applicationList');
  const labels = { NEW: 'Chờ duyệt', SCREENING: 'Đang xét duyệt', INTERVIEW: 'Phỏng vấn', OFFER: 'Đề nghị nhận việc', HIRED: 'Đã tiếp nhận', REJECTED: 'Từ chối', WITHDRAWN: 'Ứng viên đã rút' };
  let applications = [];
  if (!token()) { window.location.replace('index.html'); return; }
  await AccessControl.loadRole();
  const canView = AccessControl.can('VIEW_APPLICATIONS');
  const canSchedule = AccessControl.can('SCHEDULE_INTERVIEWS');
  const canManagePipeline = AccessControl.can('MANAGE_PIPELINE');
  if (!canView) { AccessControl.deny(list); return; }
  const escapeText = (value) => String(value ?? '').replace(/[&<>"']/g, (ch) => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[ch]));
  async function api(path, options = {}) {
    const response = await fetch(`${API}${path}`, { ...options, headers: { Authorization: `Bearer ${token()}`, ...(options.body ? { 'Content-Type': 'application/json' } : {}) } });
    const payload = await response.json().catch(() => ({}));
    if (response.status === 401) { if (typeof SessionManager !== 'undefined') SessionManager.clearSession(); window.location.replace('index.html'); throw new Error('Phiên đăng nhập hết hạn.'); }
    if (!response.ok || payload.success === false) throw new Error(payload.message || 'Không thể cập nhật hồ sơ.');
    return payload;
  }
  function notify(message, type = 'success') { const box = document.getElementById('notice'); box.textContent = message; box.className = `notice show ${type}`; window.setTimeout(() => box.classList.remove('show'), 4500); }
  function date(value) { return value ? new Date(value).toLocaleString('vi-VN', { dateStyle: 'short', timeStyle: 'short' }) : 'Chưa lên lịch'; }
  function updateStats() {
    document.getElementById('totalCount').textContent = applications.length;
    document.getElementById('pendingCount').textContent = applications.filter((item) => item.stage === 'NEW').length;
    document.getElementById('interviewCount').textContent = applications.filter((item) => item.stage === 'INTERVIEW').length;
    document.getElementById('hiredCount').textContent = applications.filter((item) => item.stage === 'HIRED').length;
  }
  function render() {
    updateStats();
    const query = document.getElementById('searchInput').value.trim().toLocaleLowerCase('vi');
    const filter = document.getElementById('stageFilter').value;
    const rows = applications.filter((a) => (filter === 'ALL' || a.stage === filter) && `${a.candidateName} ${a.candidateEmail} ${a.jobTitle} ${a.department}`.toLocaleLowerCase('vi').includes(query));
    if (!rows.length) { list.innerHTML = `<div class="empty">${applications.length ? 'Không tìm thấy hồ sơ phù hợp.' : 'Chưa có hồ sơ ứng tuyển nào được tiếp nhận.'}</div>`; return; }
    list.innerHTML = rows.map((a) => `<article class="application-card" data-id="${a.id}"><div><div class="candidate-name">${escapeText(a.candidateName)}</div><div class="candidate-email">${escapeText(a.candidateEmail)}</div><div class="applied-date">Nhận hồ sơ: ${escapeText(date(a.appliedAt))}</div></div><div><div class="job-title">${escapeText(a.jobTitle)}</div><div class="department">${escapeText(a.department)}</div></div><div><span class="stage stage-${String(a.stage).toLowerCase()}">${escapeText(labels[a.stage] || a.stage)}</span><div class="applied-date">Phỏng vấn: ${escapeText(date(a.interviewAt))}</div></div><div class="card-actions">${canSchedule ? `<input class="schedule-input" type="datetime-local" aria-label="Ngày giờ phỏng vấn của ${escapeText(a.candidateName)}" value="${a.interviewAt ? escapeText(a.interviewAt.slice(0,16)) : ''}"><button class="action schedule" type="button">Lưu lịch</button>` : ''}${canManagePipeline ? `<div class="action-row"><button class="action approve" type="button">${a.stage === 'NEW' ? 'Duyệt hồ sơ' : 'Đạt vòng'}</button><button class="action reject" type="button">Từ chối</button></div>` : ''}${!canSchedule && !canManagePipeline ? '<small>Chế độ chỉ xem</small>' : ''}</div></article>`).join('');
    list.querySelectorAll('.application-card').forEach((card) => {
      const item = applications.find((a) => String(a.id) === card.dataset.id);
      const buttons = [...card.querySelectorAll('button')];
      const busy = (state) => buttons.forEach((button) => { button.disabled = state; });
      card.querySelector('.schedule')?.addEventListener('click', async () => {
        const input = card.querySelector('.schedule-input');
        if (!input.value) { notify('Chọn ngày và giờ phỏng vấn trước khi lưu.', 'error'); input.focus(); return; }
        busy(true);
        try {
          await api(`/applications/${item.id}/interview`, { method: 'PATCH', body: JSON.stringify({ interviewAt: input.value }) });
          await api(`/applications/${item.id}/stage`, { method: 'PATCH', body: JSON.stringify({ stage: 'INTERVIEW' }) });
          notify(`Đã lưu lịch phỏng vấn cho ${item.candidateName}.`); await load();
        } catch (error) { notify(error.message, 'error'); busy(false); }
      });
      card.querySelector('.approve')?.addEventListener('click', async () => {
        busy(true);
        try { await api(`/applications/${item.id}/stage`, { method: 'PATCH', body: JSON.stringify({ stage: item.stage === 'NEW' ? 'SCREENING' : 'OFFER' }) }); notify(`Đã duyệt hồ sơ ${item.candidateName}.`); await load(); }
        catch (error) { notify(error.message, 'error'); busy(false); }
      });
      card.querySelector('.reject')?.addEventListener('click', async () => {
        busy(true);
        try { await api(`/applications/${item.id}/stage`, { method: 'PATCH', body: JSON.stringify({ stage: 'REJECTED' }) }); notify(`Đã cập nhật hồ sơ ${item.candidateName} thành từ chối.`); await load(); }
        catch (error) { notify(error.message, 'error'); busy(false); }
      });
    });
  }
  async function load() { list.innerHTML = '<div class="loading">Đang tải hồ sơ ứng tuyển…</div>'; try { const result = await api('/applications'); applications = result.data || []; render(); } catch (error) { list.innerHTML = `<div class="empty">${escapeText(error.message)}</div>`; } }
  document.getElementById('searchInput').addEventListener('input', render);
  document.getElementById('stageFilter').addEventListener('change', render);
  document.getElementById('refreshButton').addEventListener('click', load);
  try { const session = JSON.parse(localStorage.getItem('ats_user') || '{}'); document.getElementById('userLabel').textContent = session.username || session.email || 'Phiên làm việc'; } catch (_) { /* use default label */ }
  load();
});
