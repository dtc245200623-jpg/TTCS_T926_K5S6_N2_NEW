document.addEventListener('DOMContentLoaded', async () => {
  const base = `${location.origin}/api/operations`;
  const token = () => SessionManager.getToken();
  const request = async (path, options = {}) => {
    const response = await fetch(`${base}${path}`, { ...options, headers:{ Authorization:`Bearer ${token()}`, ...(options.body ? {'Content-Type':'application/json'} : {}), ...(options.headers || {}) } });
    const payload = await response.json().catch(() => ({}));
    if (response.status === 401) { SessionManager.clearSession(); location.replace('index.html'); throw new Error('Phiên đăng nhập đã hết hạn.'); }
    if (!response.ok || payload.success === false) throw new Error(payload.message || 'Không thể thực hiện yêu cầu.');
    return payload.data;
  };
  const notice = (message, error = false) => { const box = document.getElementById('notice'); box.textContent = message; box.className = `notice visible${error ? ' error' : ''}`; };
  if (!token()) { SessionManager.handleUnauthorized(); return; }
  await AccessControl.loadRole();
  const permissions = { catalogs:'MANAGE_CATALOGS', requisitions:'VIEW_REQUISITIONS', notifications:'VIEW_REQUISITIONS', templates:'MANAGE_NOTIFICATIONS' };
  const tabs = [...document.querySelectorAll('.tabs button')];
  tabs.forEach((tab) => { tab.hidden = !AccessControl.can(permissions[tab.dataset.panel]); });
  const firstTab = tabs.find((tab) => !tab.hidden);
  if (!firstTab) { notice('Tài khoản chưa được cấp quyền vào khu vực này.', true); return; }
  function selectPanel(name) {
    tabs.forEach((tab) => tab.classList.toggle('active', tab.dataset.panel === name));
    document.querySelectorAll('main > .panel').forEach((panel) => { panel.hidden = panel.id !== name; });
    if (name === 'catalogs') loadCatalog();
    if (name === 'requisitions') loadRequisitions();
    if (name === 'notifications') loadNotifications();
    if (name === 'templates') loadTemplates();
  }
  tabs.forEach((tab) => tab.addEventListener('click', () => selectPanel(tab.dataset.panel)));

  const type = document.getElementById('catalogType');
  const typeLabels = { DEPARTMENT:'Phòng ban', JOB_TITLE:'Chức danh', COMPETENCY:'Năng lực', INTERVIEW_QUESTION:'Câu hỏi phỏng vấn' };
  const catalogForm = document.getElementById('catalogForm');
  const catalogSubmit = document.getElementById('catalogSubmit');
  const cancelCatalogEdit = document.getElementById('cancelCatalogEdit');
  let editingCatalogId = null;
  function resetCatalogForm() {
    editingCatalogId = null;
    catalogForm.reset();
    catalogSubmit.textContent = 'Thêm danh mục';
    cancelCatalogEdit.hidden = true;
  }
  async function loadCatalog() {
    const target = document.getElementById('catalogList'); target.replaceChildren();
    try {
      const items = await request(`/catalog/${type.value}`);
      if (!items.length) { target.textContent = 'Danh mục chưa có mục nào.'; return; }
      const table = document.createElement('table'); table.className = 'data-table';
      const head = document.createElement('thead'); const hrow = document.createElement('tr');
      ['TÊN','MÔ TẢ','DẢI LƯƠNG','TRẠNG THÁI',''].forEach((text) => { const th = document.createElement('th'); th.textContent = text; hrow.append(th); }); head.append(hrow);
      const body = document.createElement('tbody');
      items.forEach((item) => {
        const row = document.createElement('tr');
        const salary = item.salaryMin != null || item.salaryMax != null ? `${item.salaryMin ?? '—'} – ${item.salaryMax ?? '—'}` : '—';
        [item.name,item.description,salary,item.active?'Đang dùng':'Đã ẩn'].forEach((value) => { const cell = document.createElement('td'); cell.textContent = value || '—'; row.append(cell); });
        const actionCell = document.createElement('td');
        if (item.active) {
          const editButton = document.createElement('button'); editButton.className='secondary'; editButton.type='button'; editButton.textContent='Sửa';
          editButton.addEventListener('click', () => {
            editingCatalogId = item.id;
            catalogForm.elements.name.value = item.name || '';
            catalogForm.elements.description.value = item.description || '';
            catalogForm.elements.salaryMin.value = item.salaryMin ?? '';
            catalogForm.elements.salaryMax.value = item.salaryMax ?? '';
            catalogSubmit.textContent = 'Lưu thay đổi'; cancelCatalogEdit.hidden = false;
            catalogForm.scrollIntoView({ behavior:'smooth', block:'center' }); catalogForm.elements.name.focus();
          });
          const button = document.createElement('button'); button.className='secondary'; button.type='button'; button.textContent='Ngừng dùng'; button.addEventListener('click', async () => { try { await request(`/catalog/${type.value}/${item.id}`,{method:'DELETE'}); if (editingCatalogId === item.id) resetCatalogForm(); await loadCatalog(); } catch(error) { notice(error.message,true); } }); actionCell.append(editButton, button);
        }
        row.append(actionCell); body.append(row);
      });
      table.append(head,body); target.append(table);
    } catch(error) { notice(error.message,true); }
  }
  type.addEventListener('change', loadCatalog);
  catalogForm.addEventListener('submit', async (event) => {
    event.preventDefault(); const data = Object.fromEntries(new FormData(event.currentTarget));
    data.salaryMin = data.salaryMin ? Number(data.salaryMin) : null; data.salaryMax = data.salaryMax ? Number(data.salaryMax) : null;
    try {
      if (editingCatalogId !== null) {
        await request(`/catalog/${type.value}/${editingCatalogId}`,{method:'PUT',body:JSON.stringify(data)});
        resetCatalogForm(); notice(`Đã cập nhật mục ${typeLabels[type.value]}.`);
      } else {
        await request(`/catalog/${type.value}`,{method:'POST',body:JSON.stringify(data)});
        event.currentTarget.reset(); notice(`Đã thêm mục ${typeLabels[type.value]}.`);
      }
      await loadCatalog();
    }
    catch(error) { notice(error.message,true); }
  });
  cancelCatalogEdit.addEventListener('click', resetCatalogForm);
  type.addEventListener('change', resetCatalogForm);

  const requisitionForm = document.getElementById('requisitionForm');
  requisitionForm.hidden = !AccessControl.can('MANAGE_REQUISITIONS');
  requisitionForm.addEventListener('submit', async (event) => {
    event.preventDefault(); const data = Object.fromEntries(new FormData(event.currentTarget));
    data.headcount = Number(data.headcount); data.salaryMin = data.salaryMin ? Number(data.salaryMin) : null; data.salaryMax = data.salaryMax ? Number(data.salaryMax) : null;
    try { await request('/requisitions',{method:'POST',body:JSON.stringify(data)}); event.currentTarget.reset(); notice('Đã gửi yêu cầu để phê duyệt.'); await loadRequisitions(); }
    catch(error) { notice(error.message,true); }
  });
  async function loadRequisitions() {
    const target = document.getElementById('requisitionList'); target.replaceChildren();
    try {
      const items = await request('/requisitions');
      if (!items.length) { target.innerHTML='<p class="empty">Chưa có yêu cầu tuyển dụng.</p>'; return; }
      const role = AccessControl.getRole();
      items.forEach((item) => {
        const card = document.createElement('article'); card.className='requisition-card';
        const title = document.createElement('h3'); title.textContent=`#${item.id} · ${item.title}`;
        const summary = document.createElement('p'); summary.textContent=`${item.department} · ${item.headcount} vị trí · ${item.status} · ${item.approvalsReceived}/${item.approvalsRequired} phê duyệt`;
        const detail = document.createElement('p'); detail.textContent=`Lý do: ${item.justification}${item.assignedRecruiter ? ` · Recruiter: ${item.assignedRecruiter}` : ''}`;
        card.append(title,summary,detail);
        const actions=document.createElement('div'); actions.className='actions';
        const step=item.approvalsReceived+1;
        const canApprove=AccessControl.can('APPROVE_REQUISITIONS') && item.status==='PENDING_APPROVAL' && (role==='ROLE_ADMIN' || (step===1 && role==='ROLE_HR_MANAGER') || (step>1 && role==='ROLE_APPROVER'));
        if (canApprove) for (const approved of [true,false]) { const button=document.createElement('button'); button.type='button'; button.className=approved?'primary':'secondary'; button.textContent=approved?'Phê duyệt':'Từ chối'; button.addEventListener('click',async()=>{ try { await request(`/requisitions/${item.id}/decision`,{method:'POST',body:JSON.stringify({approved,comment:''})}); notice('Đã ghi nhận quyết định.'); await loadRequisitions(); } catch(error) { notice(error.message,true); } }); actions.append(button); }
        if (item.status==='APPROVED' && AccessControl.can('MANAGE_JOBS')) { const button=document.createElement('button'); button.type='button'; button.className='primary'; button.textContent='Đăng tin công khai'; button.addEventListener('click',async()=>{ try { await request(`/requisitions/${item.id}/publish`,{method:'POST'}); notice('Vị trí đã xuất hiện trên cổng ứng tuyển.'); await loadRequisitions(); } catch(error) { notice(error.message,true); } }); actions.append(button); }
        (item.approvals || []).forEach((approval)=>{ const line=document.createElement('p'); line.textContent=`Cấp ${approval.step}: ${approval.approver} · ${approval.decision}${approval.comment?` · ${approval.comment}`:''}`; card.append(line); });
        if(actions.childElementCount) card.append(actions); target.append(card);
      });
    } catch(error) { notice(error.message,true); }
  }

  async function loadNotifications() {
    const target=document.getElementById('notificationList'); target.replaceChildren();
    try { const items=await request('/notifications'); if(!items.length){target.innerHTML='<p class="empty">Chưa có thông báo.</p>';return;}
      items.forEach((item)=>{const card=document.createElement('article');card.className=`notification-item${item.read?'':' unread'}`;const text=document.createElement('div');const title=document.createElement('strong');title.textContent=item.title;const message=document.createElement('p');message.textContent=item.message;const time=document.createElement('time');time.textContent=new Date(item.createdAt).toLocaleString('vi-VN');text.append(title,message,time);card.append(text);if(!item.read){const button=document.createElement('button');button.className='secondary';button.type='button';button.textContent='Đánh dấu đã đọc';button.addEventListener('click',async()=>{try{await request(`/notifications/${item.id}/read`,{method:'POST'});await loadNotifications();}catch(error){notice(error.message,true);}});card.append(button);}target.append(card);});
    } catch(error) { notice(error.message,true); }
  }
  document.getElementById('refreshNotifications').addEventListener('click', loadNotifications);

  const templateKey=document.getElementById('templateKey');
  let templates=[];
  async function loadTemplates(){try{templates=await request('/email-templates');renderTemplate();}catch(error){notice(error.message,true);}}
  function renderTemplate(){const item=templates.find((template)=>template.templateKey===templateKey.value);if(!item)return;const form=document.getElementById('templateForm');form.elements.subject.value=item.subject;form.elements.body.value=item.body;form.elements.enabled.checked=item.enabled;}
  templateKey.addEventListener('change',renderTemplate);
  document.getElementById('templateForm').addEventListener('submit',async(event)=>{event.preventDefault();const form=event.currentTarget;const data={subject:form.elements.subject.value,body:form.elements.body.value,enabled:form.elements.enabled.checked};try{const saved=await request(`/email-templates/${templateKey.value}`,{method:'PUT',body:JSON.stringify(data)});templates=templates.filter((item)=>item.templateKey!==saved.templateKey).concat(saved);renderTemplate();notice('Đã lưu mẫu email.');}catch(error){notice(error.message,true);}});
  selectPanel(firstTab.dataset.panel);
});
