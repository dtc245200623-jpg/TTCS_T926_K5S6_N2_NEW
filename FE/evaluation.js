document.addEventListener('DOMContentLoaded', async () => {
  const api = `${window.location.origin}/api/recruitment`;
  const auth = () => ({ Authorization: `Bearer ${SessionManager.getToken()}`, 'Content-Type': 'application/json' });
  const select = document.getElementById('applicationSelect');
  const status = document.getElementById('autoSaveText');
  const comments = document.getElementById('evalComments');
  const salary = document.getElementById('salaryOffer');
  const decision = document.getElementById('finalDecision');
  const banner = document.getElementById('draftBanner');
  const ratings = { skill: 0, problem_solving: 0 };
  let activeId = null;
  let applications = [];

  if (!SessionManager.isLoggedIn()) {
    window.location.replace('index.html?reason=expired');
    return;
  }
  await AccessControl.loadRole();
  if (!AccessControl.can('VIEW_EVALUATIONS')) {
    AccessControl.deny(document.querySelector('main'), 'Vai trò hiện tại không có quyền xem phiếu đánh giá.');
    return;
  }
  const canSubmit = AccessControl.can('SUBMIT_EVALUATIONS');
  if (!canSubmit) {
    document.getElementById('btnSubmitEval').hidden = true;
    document.getElementById('btnManualSave').hidden = true;
    document.getElementById('btnClearDraft').hidden = true;
    [comments, salary, decision, ...document.querySelectorAll('.rating-btn')].forEach((control) => { control.disabled = true; });
  }
  const user = SessionManager.getUser();
  if (user) document.getElementById('usernameDisplay').textContent = `${user.username} (${user.role || 'Recruiter'})`;

  SessionManager.startSessionMonitoring((info) => {
    const label = document.getElementById('sessionStatusText');
    if (label) label.textContent = info.message || `Phiên hoạt động – Đã xác thực lúc ${info.lastRenewed}`;
  });

  async function request(path, options = {}) {
    const response = await fetch(`${api}${path}`, { ...options, headers: { ...auth(), ...(options.headers || {}) } });
    const payload = await response.json().catch(() => ({}));
    if (response.status === 401) {
      SessionManager.handleUnauthorized('expired');
      throw new Error('Phiên đăng nhập đã hết hạn.');
    }
    if (!response.ok || payload.success === false) throw new Error(payload.message || 'Không thể tải dữ liệu.');
    return payload;
  }

  function currentDraft() {
    return { ratings: { ...ratings }, comments: comments.value, salary: salary.value, decision: decision.value };
  }

  function applyValues(data = {}) {
    ratings.skill = Number(data.skillScore ?? data.ratings?.skill ?? 0);
    ratings.problem_solving = Number(data.problemSolvingScore ?? data.ratings?.problem_solving ?? 0);
    comments.value = data.comments || '';
    salary.value = data.salaryOffer ?? data.salary ?? '';
    decision.value = data.decision || '';
    document.querySelectorAll('.rating-btn').forEach((button) => {
      const group = button.closest('.rating-group');
      button.classList.toggle('active', Number(button.dataset.val) === ratings[group.dataset.criterion]);
    });
  }

  function saveLocalDraft() {
    if (!activeId) return;
    SessionManager.saveDraft(`candidate_eval_${activeId}`, currentDraft());
    status.textContent = `Bản nháp đã lưu trên thiết bị lúc ${new Date().toLocaleTimeString('vi-VN')}`;
  }

  async function loadEvaluation(id) {
    activeId = id ? Number(id) : null;
    banner.style.display = 'none';
    applyValues({});
    if (!activeId) {
      document.getElementById('candidateLabel').textContent = 'Chưa có hồ sơ để đánh giá';
      status.textContent = 'Hãy tạo hồ sơ ứng tuyển trước khi đánh giá.';
      return;
    }
    const app = applications.find((item) => item.id === activeId);
    document.getElementById('candidateLabel').textContent = app ? `${app.candidateName} · ${app.jobTitle}` : 'Hồ sơ ứng tuyển';
    try {
      const result = await request(`/applications/${activeId}/evaluation`);
      const saved = result.data;
      const draft = SessionManager.getDraft(`candidate_eval_${activeId}`);
      if (draft?.data && (!saved || new Date(draft.timestamp) > new Date(saved.updatedAt))) {
        applyValues(draft.data);
        banner.style.display = 'flex';
        status.textContent = `Đã khôi phục bản nháp lúc ${new Date(draft.timestamp).toLocaleTimeString('vi-VN')}`;
      } else if (saved) {
        applyValues(saved);
        status.textContent = `Phiếu đã lưu trên hệ thống lúc ${new Date(saved.updatedAt).toLocaleString('vi-VN')}`;
      } else {
        status.textContent = 'Chưa có phiếu đánh giá. Nội dung đang nhập sẽ được lưu thành bản nháp trên thiết bị.';
      }
    } catch (error) {
      status.textContent = error.message;
    }
  }

  document.querySelectorAll('.rating-group').forEach((group) => {
    group.querySelectorAll('.rating-btn').forEach((button) => button.addEventListener('click', () => {
      ratings[group.dataset.criterion] = Number(button.dataset.val);
      group.querySelectorAll('.rating-btn').forEach((item) => item.classList.toggle('active', item === button));
      saveLocalDraft();
    }));
  });
  [comments, salary].forEach((field) => field.addEventListener('input', saveLocalDraft));
  decision.addEventListener('change', saveLocalDraft);
  select.addEventListener('change', () => loadEvaluation(select.value));

  document.getElementById('btnManualSave').addEventListener('click', () => {
    saveLocalDraft();
    alert('Đã lưu bản nháp trên thiết bị này. Chọn “Hoàn tất” để gửi phiếu lên hệ thống.');
  });
  document.getElementById('btnClearDraft').addEventListener('click', () => {
    if (!activeId) return;
    SessionManager.clearDraft(`candidate_eval_${activeId}`);
    banner.style.display = 'none';
    applyValues({});
    status.textContent = 'Đã xóa bản nháp trên thiết bị.';
  });
  document.getElementById('btnSubmitEval').addEventListener('click', async () => {
    if (!activeId) return alert('Chọn hồ sơ ứng tuyển trước khi nộp phiếu.');
    if (!decision.value) return alert('Vui lòng chọn quyết định tuyển dụng.');
    if (!ratings.skill || !ratings.problem_solving) return alert('Vui lòng chấm điểm cả hai tiêu chí.');
    const button = document.getElementById('btnSubmitEval');
    button.disabled = true;
    try {
      await request(`/applications/${activeId}/evaluation`, {
        method: 'PUT',
        body: JSON.stringify({
          skillScore: ratings.skill,
          problemSolvingScore: ratings.problem_solving,
          comments: comments.value,
          salaryOffer: salary.value,
          decision: decision.value
        })
      });
      SessionManager.clearDraft(`candidate_eval_${activeId}`);
      banner.style.display = 'none';
      status.textContent = 'Phiếu đánh giá đã được lưu trên hệ thống.';
      alert('Đã lưu phiếu đánh giá cho hồ sơ được chọn.');
    } catch (error) {
      status.textContent = error.message;
      alert(error.message);
    } finally {
      button.disabled = false;
    }
  });
  document.getElementById('btnLogout').addEventListener('click', async () => SessionManager.logout(false));
  document.getElementById('btnRevokeAll').addEventListener('click', async () => {
    if (!confirm('Thu hồi phiên đăng nhập trên mọi thiết bị?')) return;
    await SessionManager.logout(true);
  });
  document.getElementById('btnTestExpire').addEventListener('click', () => SessionManager.handleUnauthorized('revoked'));

  request('/applications').then((result) => {
    applications = result.data || [];
    select.replaceChildren(new Option('Chọn hồ sơ ứng tuyển', ''));
    applications.forEach((item) => select.add(new Option(`${item.candidateName} · ${item.jobTitle} · ${item.stage}`, item.id)));
    if (!applications.length) {
      select.disabled = true;
      document.getElementById('candidateLabel').textContent = 'Chưa có hồ sơ ứng tuyển';
      status.textContent = 'Tạo hồ sơ ứng tuyển trước trong Bảng điều khiển hoặc Xử lý hồ sơ.';
      return;
    }
    select.disabled = false;
    select.value = applications[0].id;
    loadEvaluation(select.value);
  }).catch((error) => { status.textContent = error.message; });
});
