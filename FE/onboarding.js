document.addEventListener('DOMContentLoaded', async () => {
  const api = `${window.location.origin}/api/recruitment`;
  const token = () => SessionManager.getToken();
  const list = document.getElementById('recordList');
  const notice = document.getElementById('notice');
  const stageFilter = document.getElementById('stageFilter');
  let records = [];

  if (!SessionManager.isLoggedIn()) {
    window.location.replace('index.html?reason=expired');
    return;
  }
  await AccessControl.loadRole();
  if (!AccessControl.can('MANAGE_ONBOARDING')) {
    AccessControl.deny(list);
    document.getElementById('refreshButton').hidden = true;
    stageFilter.disabled = true;
    return;
  }
  const user = SessionManager.getUser();
  if (user) document.getElementById('userLabel').textContent = user.username || 'Đang đăng nhập';

  async function request(path, options = {}) {
    const response = await fetch(`${api}${path}`, {
      ...options,
      headers: { Authorization: `Bearer ${token()}`, ...(options.body ? { 'Content-Type': 'application/json' } : {}), ...(options.headers || {}) }
    });
    const payload = await response.json().catch(() => ({}));
    if (response.status === 401) {
      SessionManager.handleUnauthorized('expired');
      throw new Error('Phiên đăng nhập đã hết hạn.');
    }
    if (!response.ok || payload.success === false) throw new Error(payload.message || 'Không thể tải dữ liệu.');
    return payload;
  }

  function element(tag, className, text) {
    const node = document.createElement(tag);
    if (className) node.className = className;
    if (text != null) node.textContent = text;
    return node;
  }

  function field(labelText, type, value, options = {}) {
    const label = element('label', 'field', labelText);
    const input = document.createElement('input');
    input.type = type;
    input.value = value || '';
    input.required = Boolean(options.required);
    if (options.min != null) input.min = options.min;
    if (options.step != null) input.step = options.step;
    if (options.readOnly) input.readOnly = true;
    label.append(input);
    return { label, input };
  }

  function showNotice(message, kind = 'success') {
    notice.textContent = message;
    notice.className = `notice visible ${kind}`;
    notice.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
  }

  function refreshCounts() {
    const pending = records.filter(({ application }) => application.stage === 'OFFER').length;
    const hired = records.filter(({ application }) => application.stage === 'HIRED');
    const complete = hired.filter(({ application }) => application.documentsReceived && application.accountReady && application.equipmentReady && application.orientationComplete).length;
    document.getElementById('pendingCount').textContent = pending;
    document.getElementById('hiredCount').textContent = hired.length;
    document.getElementById('completeCount').textContent = complete;
  }

  function renderRecord(record) {
    const app = record.application;
    const card = element('article', 'record-card');
    const head = element('div', 'record-head');
    const identity = element('div');
    identity.append(element('h3', '', app.candidateName), element('p', '', `${app.jobTitle} · ${app.department} · ${app.candidateEmail}`));
    const stage = element('span', `stage ${app.stage === 'HIRED' ? 'hired' : ''}`, app.stage === 'HIRED' ? 'Đã xác nhận nhận việc' : 'Chờ xác nhận offer');
    head.append(identity, stage);
    card.append(head);

    const offerGrid = element('div', 'offer-grid');
    const proposed = field('Mức lương đề xuất (VND)', 'text', record.proposedSalary || 'Chưa có mức lương trong phiếu đánh giá', { readOnly: true });
    const agreed = field('Mức lương đã thống nhất (VND)', 'number', app.agreedSalary || '', { required: app.stage === 'OFFER', min: 1, step: 1000, readOnly: app.stage === 'HIRED' });
    const start = field('Ngày bắt đầu làm việc', 'date', app.startDate || '', { required: app.stage === 'OFFER' });
    offerGrid.append(proposed.label, agreed.label, start.label);
    card.append(offerGrid);

    if (app.stage === 'OFFER') {
      const acceptLabel = element('label', 'salary-accept');
      const accepted = document.createElement('input');
      accepted.type = 'checkbox';
      accepted.required = true;
      acceptLabel.append(accepted, document.createTextNode('Ứng viên đã chấp nhận mức lương và xác nhận muốn nhận việc'));
      card.append(acceptLabel);
    } else {
      card.append(element('p', 'salary-accepted', `Ứng viên đã chấp nhận mức lương ${app.agreedSalary ? `${Number(app.agreedSalary).toLocaleString('vi-VN')} VND` : 'đã thống nhất'}.`));
      const checklist = element('div', 'checklist');
      const tasks = [
        ['documentsReceived', 'Đã nhận hồ sơ giấy tờ'],
        ['accountReady', 'Đã tạo tài khoản làm việc'],
        ['equipmentReady', 'Đã chuẩn bị thiết bị'],
        ['orientationComplete', 'Đã hoàn tất định hướng']
      ];
      for (const [key, label] of tasks) {
        const task = element('label', 'check-item');
        const checkbox = document.createElement('input');
        checkbox.type = 'checkbox';
        checkbox.checked = Boolean(app[key]);
        checkbox.dataset.task = key;
        task.append(checkbox, document.createTextNode(label));
        checklist.append(task);
      }
      card.append(checklist);
    }

    const actions = element('div', 'record-actions');
    const complete = app.stage === 'HIRED' && app.documentsReceived && app.accountReady && app.equipmentReady && app.orientationComplete;
    actions.append(element('small', '', complete ? 'Checklist onboarding đã hoàn tất.' : app.stage === 'HIRED' ? 'Cập nhật các bước chuẩn bị trước ngày nhận việc.' : 'Cần xác nhận lương và ngày bắt đầu để chuyển hồ sơ sang Đã nhận việc.'));
    const save = element('button', 'button primary', app.stage === 'OFFER' ? 'Xác nhận nhận việc' : 'Lưu checklist');
    save.type = 'button';
    save.addEventListener('click', async () => {
      if (!start.input.value) return showNotice('Vui lòng chọn ngày bắt đầu làm việc.', 'error');
      if (app.stage === 'OFFER') {
        const accepted = card.querySelector('.salary-accept input').checked;
        if (!accepted) return showNotice('Chỉ xác nhận nhận việc sau khi ứng viên đã chấp nhận mức lương.', 'error');
        if (!agreed.input.value || Number(agreed.input.value) <= 0) return showNotice('Nhập mức lương hai bên đã thống nhất.', 'error');
        const salaryText = `${Number(agreed.input.value).toLocaleString('vi-VN')} VND`;
        if (!window.confirm(`Xác nhận ${app.candidateName} nhận việc với mức lương ${salaryText}, bắt đầu ngày ${start.input.value}?`)) return;
      }
      const payload = {
        offerAccepted: app.stage === 'OFFER' ? true : null,
        salaryAccepted: app.stage === 'OFFER' ? true : null,
        agreedSalary: agreed.input.value || null,
        startDate: start.input.value,
      };
      card.querySelectorAll('[data-task]').forEach((checkbox) => { payload[checkbox.dataset.task] = checkbox.checked; });
      save.disabled = true;
      save.textContent = 'Đang lưu…';
      try {
        const result = await request(`/applications/${app.id}/onboarding`, { method: 'PATCH', body: JSON.stringify(payload) });
        showNotice(app.stage === 'OFFER' ? 'Đã xác nhận nhận việc và tạo checklist onboarding.' : (result.message || 'Đã lưu checklist onboarding.'));
        await load();
      } catch (error) {
        showNotice(error.message, 'error');
      } finally {
        save.disabled = false;
        save.textContent = app.stage === 'OFFER' ? 'Xác nhận nhận việc' : 'Lưu checklist';
      }
    });
    actions.append(save);
    card.append(actions);
    return card;
  }

  function render() {
    refreshCounts();
    const filtered = stageFilter.value === 'ALL' ? records : records.filter(({ application }) => application.stage === stageFilter.value);
    if (!filtered.length) {
      list.replaceChildren(element('div', 'empty', records.length ? 'Không có hồ sơ phù hợp với bộ lọc.' : 'Chưa có hồ sơ ở bước offer. Hồ sơ đã đạt tuyển sẽ xuất hiện tại đây.'));
      return;
    }
    list.replaceChildren(...filtered.map(renderRecord));
  }

  async function load() {
    list.replaceChildren(element('div', 'empty', 'Đang tải danh sách…'));
    try {
      const result = await request('/onboarding');
      records = result.data || [];
      render();
    } catch (error) {
      list.replaceChildren(element('div', 'empty', error.message));
    }
  }

  stageFilter.addEventListener('change', render);
  document.getElementById('refreshButton').addEventListener('click', load);
  load();
});
