document.addEventListener('DOMContentLoaded', async () => {
  const base = `${location.origin}/api/auth`;
  const token = SessionManager.getToken();
  if (!token) return SessionManager.handleUnauthorized();

  const request = async (url, options = {}) => {
    const response = await SessionManager.fetchWithAuth(`${base}${url}`, {
      ...options,
      headers: { 'Content-Type': 'application/json', ...(options.headers || {}) },
    });
    const payload = await response.json().catch(() => ({}));
    if (!response.ok || !payload.success) throw new Error(payload.message || 'Không thể thực hiện yêu cầu.');
    return payload.data;
  };
  const message = (id, text, error = false) => {
    const el = document.getElementById(id);
    el.textContent = text;
    el.className = error ? 'error' : 'status';
  };
  const roles = ['ROLE_ADMIN', 'ROLE_RECRUITER', 'ROLE_HR_MANAGER', 'ROLE_HIRING_MANAGER', 'ROLE_INTERVIEWER', 'ROLE_APPROVER'];
  const avatarData = document.getElementById('avatarData');
  const avatarFile = document.getElementById('avatarFile');
  const avatarPreview = document.getElementById('avatarPreview');
  const avatarFallback = document.getElementById('avatarFallback');
  const removeAvatar = document.getElementById('removeAvatar');
  const showAvatar = (data) => {
    avatarData.value = data || '';
    avatarPreview.hidden = !data;
    avatarFallback.hidden = Boolean(data);
    removeAvatar.hidden = !data;
    if (data) avatarPreview.src = data;
    else avatarPreview.removeAttribute('src');
  };
  const createField = (type, value, ariaLabel) => {
    const input = document.createElement('input');
    input.type = type;
    input.value = value || '';
    input.setAttribute('aria-label', ariaLabel);
    input.required = type === 'email' || ariaLabel.startsWith('Họ và tên');
    return input;
  };
  const rowPayload = (user, row) => ({
    fullName: row.querySelector('[data-name]').value.trim(),
    email: row.querySelector('[data-email]').value.trim(),
    phone: row.querySelector('[data-phone]').value.trim(),
    role: row.querySelector('[data-role]').value,
    active: user.active,
  });

  document.getElementById('logout').onclick = () => SessionManager.logout();
  try {
    const me = await request('/me?includeAvatar=true');
    AccessControl.setRole(me.role);
    document.querySelector('#profile [name=fullName]').value = me.fullName || '';
    document.querySelector('#profile [name=email]').value = me.email || '';
    document.querySelector('#profile [name=phone]').value = me.phone || '';
    showAvatar(me.avatarData);
    document.getElementById('username').value = me.username;
    document.getElementById('role').value = me.role;
    document.getElementById('profile').onsubmit = async (event) => {
      event.preventDefault();
      try {
        const updated = await request('/me', { method: 'PUT', body: JSON.stringify(Object.fromEntries(new FormData(event.currentTarget))) });
        showAvatar(updated.avatarData);
        message('profileMsg', 'Đã lưu hồ sơ.');
      } catch (error) { message('profileMsg', error.message, true); }
    };
    avatarFile.addEventListener('change', () => {
      const file = avatarFile.files?.[0];
      if (!file) return;
      if (!['image/png', 'image/jpeg'].includes(file.type) || file.size > 1_048_576) {
        avatarFile.value = '';
        message('profileMsg', 'Chọn ảnh PNG/JPEG có dung lượng tối đa 1 MB.', true);
        return;
      }
      const reader = new FileReader();
      reader.onload = () => showAvatar(String(reader.result));
      reader.onerror = () => message('profileMsg', 'Không đọc được tệp ảnh.', true);
      reader.readAsDataURL(file);
    });
    removeAvatar.addEventListener('click', () => { avatarFile.value = ''; showAvatar(''); });
    if (!AccessControl.can('MANAGE_ACCOUNTS')) return;

    document.getElementById('adminPanel').hidden = false;
    const rows = document.getElementById('rows');
    const search = document.getElementById('accountSearch');

    const renderAccounts = (accounts) => {
      rows.replaceChildren();
      accounts.forEach((user) => {
        const row = document.createElement('tr');
        const nameCell = document.createElement('td');
        const username = document.createElement('strong');
        username.textContent = user.username;
        const fullName = createField('text', user.fullName, `Họ và tên ${user.username}`);
        fullName.dataset.name = '';
        nameCell.append(username, fullName);

        const emailCell = document.createElement('td');
        const email = createField('email', user.email, `Email ${user.username}`);
        email.dataset.email = '';
        emailCell.append(email);

        const phoneCell = document.createElement('td');
        const phone = createField('tel', user.phone, `Số điện thoại ${user.username}`);
        phone.dataset.phone = '';
        phoneCell.append(phone);

        const roleCell = document.createElement('td');
        const role = document.createElement('select');
        role.dataset.role = '';
        role.setAttribute('aria-label', `Vai trò ${user.username}`);
        roles.forEach((value) => {
          const option = new Option(value.replace('ROLE_', ''), value);
          option.selected = value === user.role;
          role.add(option);
        });
        roleCell.append(role);

        const statusCell = document.createElement('td');
        const status = document.createElement('span');
        status.textContent = user.active ? 'Đang hoạt động' : 'Đã khóa';
        status.className = user.active ? 'status-active' : 'status-locked';
        statusCell.append(status);

        const actionCell = document.createElement('td');
        const save = document.createElement('button');
        save.type = 'button';
        save.textContent = 'Lưu thay đổi';
        save.onclick = async () => {
          try {
            await request(`/admin/accounts/${user.id}`, { method: 'PUT', body: JSON.stringify(rowPayload(user, row)) });
            message('accountMsg', `Đã cập nhật tài khoản ${user.username}.`);
            await loadAccounts();
          } catch (error) { message('accountMsg', error.message, true); }
        };
        const toggle = document.createElement('button');
        toggle.type = 'button';
        toggle.className = 'secondary';
        toggle.textContent = user.active ? 'Khóa' : 'Mở khóa';
        toggle.onclick = async () => {
          try {
            const data = rowPayload(user, row);
            data.active = !user.active;
            await request(`/admin/accounts/${user.id}`, { method: 'PUT', body: JSON.stringify(data) });
            message('accountMsg', user.active ? `Đã khóa tài khoản ${user.username}.` : `Đã mở khóa tài khoản ${user.username}.`);
            await loadAccounts();
          } catch (error) { message('accountMsg', error.message, true); }
        };
        actionCell.append(save, toggle);
        row.append(nameCell, emailCell, phoneCell, roleCell, statusCell, actionCell);
        rows.append(row);
      });
      if (!accounts.length) {
        const row = document.createElement('tr');
        const cell = document.createElement('td');
        cell.colSpan = 6;
        cell.textContent = 'Không tìm thấy tài khoản phù hợp.';
        row.append(cell);
        rows.append(row);
      }
    };

    const loadAccounts = async () => {
      try {
        const accounts = await request(`/admin/accounts?q=${encodeURIComponent(search.value.trim())}`);
        renderAccounts(accounts);
      } catch (error) { message('accountMsg', error.message, true); }
    };
    let searchTimer;
    search.addEventListener('input', () => {
      clearTimeout(searchTimer);
      searchTimer = setTimeout(loadAccounts, 250);
    });
    document.getElementById('create').onsubmit = async (event) => {
      event.preventDefault();
      try {
        await request('/admin/accounts', { method: 'POST', body: JSON.stringify(Object.fromEntries(new FormData(event.currentTarget))) });
        event.currentTarget.reset();
        message('createMsg', 'Đã tạo tài khoản.');
        await loadAccounts();
      } catch (error) { message('createMsg', error.message, true); }
    };
    await loadAccounts();
  } catch (error) { message('profileMsg', error.message, true); }
});
