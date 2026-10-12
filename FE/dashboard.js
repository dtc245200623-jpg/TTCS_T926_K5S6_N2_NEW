document.addEventListener('DOMContentLoaded', () => {
    const API_BASE = window.API_BASE_URL || `${window.location.origin}/api/auth`;
    const RECRUITMENT_API = window.API_BASE_URL
        ? window.API_BASE_URL.replace(/\/api\/auth\/?$/, '/api/recruitment')
        : `${window.location.origin}/api/recruitment`;
    const tokenKey = 'authToken';
    const currentToken = () => (typeof SessionManager !== 'undefined' && SessionManager.getToken())
        || sessionStorage.getItem(tokenKey) || localStorage.getItem('ats_jwt_token');
    const clearAuth = () => {
        if (typeof SessionManager !== 'undefined') SessionManager.clearSession();
        sessionStorage.removeItem(tokenKey);
        sessionStorage.removeItem('authUsername');
    };
    const token = currentToken();
    let currentRole = (typeof SessionManager !== 'undefined' && SessionManager.getUser()?.role) || 'ROLE_USER';
    let editingRecordId = null;
    const alertBox = document.getElementById('pageAlert');
    const connectionLabel = document.getElementById('connectionLabel');

    if (!token) {
        window.location.replace('index.html');
        return;
    }

    function setAlert(message, type = 'success') {
        alertBox.textContent = message;
        alertBox.className = `alert visible ${type}`;
        alertBox.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
    }

    async function api(path, options = {}, baseUrl = API_BASE) {
        const response = await fetch(`${baseUrl}${path}`, {
            ...options,
            headers: {
                Authorization: `Bearer ${currentToken() || token}`,
                ...(options.body ? { 'Content-Type': 'application/json' } : {}),
                ...options.headers
            }
        });
        const payload = await response.json().catch(() => ({}));
        if (response.status === 401) {
            clearAuth();
            window.location.replace('index.html');
            throw new Error('Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.');
        }
        if (!response.ok || payload.success === false) {
            throw new Error(payload.message || 'Yêu cầu chưa thực hiện được. Vui lòng thử lại.');
        }
        return payload;
    }

    const workflow = { view: 'jobs', jobs: [], candidates: [], applications: [] };
    const can = (permission) => AccessControl.can(permission);
    function applyUiPermissions() {
        const tabPermissions = { jobs:'VIEW_JOBS', candidates:'VIEW_CANDIDATES', applications:'VIEW_APPLICATIONS', pipeline:'MANAGE_PIPELINE', reports:'VIEW_REPORTS' };
        document.querySelectorAll('.workflow-tab').forEach(tab => { tab.hidden = !can(tabPermissions[tab.dataset.view]); });
        document.getElementById('navCandidates').hidden = !can('VIEW_CANDIDATES');
        document.getElementById('navPipeline').hidden = !can('MANAGE_PIPELINE');
        document.getElementById('navReports').hidden = !can('VIEW_REPORTS');
        document.getElementById('navOperations').hidden = !(['MANAGE_CATALOGS','VIEW_REQUISITIONS','MANAGE_NOTIFICATIONS'].some(can));
        document.querySelector('a[href="processing.html"]').hidden = !can('VIEW_APPLICATIONS');
        document.querySelector('a[href="onboarding.html"]').hidden = !can('MANAGE_ONBOARDING');
        document.querySelector('a[href="evaluation.html"]').hidden = !can('VIEW_EVALUATIONS');
        document.querySelector('a[href="employee.html"]').hidden = !can('APPLY_FOR_JOB');
        const visibleTab = [...document.querySelectorAll('.workflow-tab')].find(tab => !tab.hidden);
        if (visibleTab && !can(tabPermissions[workflow.view])) workflow.view = visibleTab.dataset.view;
        document.getElementById('newRecordButton').hidden = !can(({jobs:'MANAGE_JOBS',candidates:'MANAGE_CANDIDATES',applications:'CREATE_APPLICATION',pipeline:'CREATE_APPLICATION'})[workflow.view]);
    }
    const workflowTitles = { jobs: 'Vị trí tuyển dụng', candidates: 'Ứng viên', applications: 'Hồ sơ ứng tuyển', pipeline: 'Pipeline', reports: 'Báo cáo tuyển dụng' };
    const stages = [
        ['NEW', 'Mới'], ['SCREENING', 'Sàng lọc'], ['INTERVIEW', 'Phỏng vấn'],
        ['OFFER', 'Offer'], ['HIRED', 'Đã nhận việc'], ['REJECTED', 'Từ chối'], ['WITHDRAWN', 'Ứng viên đã rút']
    ];
    const stageLabel = (key) => stages.find(([value]) => value === key)?.[1] || key;
    const workflowContent = document.getElementById('workflowContent');
    const workspaceHint = document.getElementById('workspaceHint');
    const recordDialog = document.getElementById('recordDialog');

    function makeCell(row, value, tag = 'td') {
        const cell = document.createElement(tag);
        cell.textContent = value == null || value === '' ? '—' : String(value);
        row.append(cell);
        return cell;
    }

    function editRecordButton(view, item) {
        return (row) => {
            const cell = document.createElement('td');
            const button = document.createElement('button');
            button.type = 'button'; button.className = 'stage-select'; button.textContent = 'Sửa';
            button.addEventListener('click', () => openCreateDialog(view, item));
            cell.append(button); row.append(cell);
        };
    }

    function renderTable(headers, rows) {
        if (!rows.length) {
            workflowContent.innerHTML = '';
            const empty = document.createElement('div');
            empty.className = 'empty-state';
            empty.textContent = 'Chưa có dữ liệu. Hãy tạo mục đầu tiên để bắt đầu luồng tuyển dụng.';
            workflowContent.append(empty);
            return;
        }
        const table = document.createElement('table');
        table.className = 'data-table';
        const thead = document.createElement('thead');
        const headRow = document.createElement('tr');
        headers.forEach((header) => makeCell(headRow, header, 'th'));
        thead.append(headRow);
        const tbody = document.createElement('tbody');
        rows.forEach((values) => {
            const row = document.createElement('tr');
            values.forEach((value) => {
                if (typeof value === 'function') value(row);
                else makeCell(row, value);
            });
            tbody.append(row);
        });
        table.append(thead, tbody);
        workflowContent.replaceChildren(table);
    }

    function renderWorkflow() {
        const { view, jobs, candidates, applications } = workflow;
        applyUiPermissions();
        document.querySelectorAll('.workflow-tab').forEach((tab) => tab.classList.toggle('active', tab.dataset.view === view));
        document.getElementById('newRecordLabel').textContent = view === 'candidates' ? 'Thêm ứng viên' : view === 'jobs' ? 'Tạo vị trí' : 'Thêm hồ sơ';
        workspaceHint.textContent = view === 'jobs' ? 'Quản lý vị trí và số lượng cần tuyển.'
            : view === 'candidates' ? 'Tạo và theo dõi hồ sơ ứng viên.'
                : view === 'applications' ? 'Gắn ứng viên với vị trí tuyển dụng.'
                    : view === 'reports' ? 'Tổng hợp phễu tuyển dụng, tỷ lệ chuyển đổi và nguồn ứng viên.'
                    : 'Cập nhật giai đoạn để theo dõi tiến độ từng ứng viên.';

        if (view === 'reports') {
            const counts = Object.fromEntries(stages.map(([stage]) => [stage, applications.filter((item) => item.stage === stage).length]));
            const hired = applications.filter((item) => item.stage === 'HIRED');
            const completedHires = hired.filter((item) => item.startDate && item.appliedAt);
            const averageDays = completedHires.length
                ? Math.round(completedHires.reduce((sum, item) => sum + (new Date(item.startDate) - new Date(item.appliedAt)) / 86400000, 0) / completedHires.length)
                : null;
            const sourceCounts = candidates.reduce((result, candidate) => {
                const source = candidate.source || 'Chưa xác định';
                result[source] = (result[source] || 0) + 1;
                return result;
            }, {});
            const cards = [
                ['Vị trí đang tuyển', jobs.filter((job) => job.status === 'OPEN').length, 'Trên tổng ' + jobs.length + ' vị trí'],
                ['Ứng viên', candidates.length, 'Hồ sơ ứng viên trong hệ thống'],
                ['Hồ sơ tuyển dụng', applications.length, 'Đang theo dõi trong pipeline'],
                ['Đã nhận việc', hired.length, averageDays == null ? 'Chưa đủ dữ liệu time-to-hire' : `Trung bình ${averageDays} ngày`]
            ];
            const report = document.createElement('div');
            report.className = 'report-view';
            const cardGrid = document.createElement('div');
            cardGrid.className = 'report-card-grid';
            cards.forEach(([label, value, detail]) => {
                const card = document.createElement('article');
                card.className = 'report-card';
                const title = document.createElement('span'); title.textContent = label;
                const metric = document.createElement('strong'); metric.textContent = value;
                const caption = document.createElement('small'); caption.textContent = detail;
                card.append(title, metric, caption); cardGrid.append(card);
            });
            report.append(cardGrid);
            const stageRows = stages.map(([stage, label]) => [label, counts[stage], applications.length ? `${Math.round(counts[stage] * 100 / applications.length)}%` : '0%']);
            report.append(createReportTable('Phễu tuyển dụng', ['GIAI ĐOẠN', 'SỐ HỒ SƠ', 'TỶ LỆ'], stageRows));
            const sources = Object.entries(sourceCounts).sort((a, b) => b[1] - a[1]).map(([source, count]) => [source, count, candidates.length ? `${Math.round(count * 100 / candidates.length)}%` : '0%']);
            report.append(createReportTable('Nguồn ứng viên', ['NGUỒN', 'ỨNG VIÊN', 'TỶ TRỌNG'], sources));
            workflowContent.replaceChildren(report);
        } else if (view === 'jobs') {
            const canEditJobs = can('MANAGE_JOBS');
            renderTable(['VỊ TRÍ', 'PHÒNG BAN', 'SỐ LƯỢNG', 'TRẠNG THÁI', 'NGÀY TẠO', ...(canEditJobs ? ['THAO TÁC'] : [])], jobs.map((job) => [
                job.title, job.department, job.headcount, job.status === 'OPEN' ? 'Đang tuyển' : job.status,
                job.createdAt ? new Date(job.createdAt).toLocaleDateString('vi-VN') : '—',
                ...(canEditJobs ? [editRecordButton('jobs', job)] : [])
            ]));
        } else if (view === 'candidates') {
            const canEditCandidates = can('MANAGE_CANDIDATES');
            renderTable(['HỌ TÊN', 'EMAIL', 'ĐIỆN THOẠI', 'NGUỒN', 'CV', 'NGÀY TẠO', ...(canEditCandidates ? ['THAO TÁC'] : [])], candidates.map((candidate) => [
                candidate.fullName, candidate.email, candidate.phone, candidate.source,
                (row) => {
                    const cell = document.createElement('td');
                    if (candidate.resumeFileName) {
                        const button = document.createElement('button'); button.type = 'button'; button.className = 'stage-select'; button.textContent = 'Tải CV';
                        button.addEventListener('click', async () => {
                            button.disabled = true;
                            try {
                                const response = await fetch(`${RECRUITMENT_API}/candidates/${candidate.id}/resume`, { headers: { Authorization: `Bearer ${currentToken() || token}` } });
                                if (!response.ok) throw new Error('Không thể tải CV.');
                                const blob = await response.blob(); const url = URL.createObjectURL(blob);
                                const link = document.createElement('a'); link.href = url; link.download = candidate.resumeFileName; link.click(); URL.revokeObjectURL(url);
                            } catch (error) { setAlert(error.message, 'error'); }
                            finally { button.disabled = false; }
                        });
                        cell.append(button);
                    } else cell.textContent = 'Chưa có';
                    row.append(cell);
                },
                candidate.createdAt ? new Date(candidate.createdAt).toLocaleDateString('vi-VN') : '—',
                ...(canEditCandidates ? [editRecordButton('candidates', candidate)] : [])
            ]));
        } else {
            const ordered = [...applications].sort((a, b) => view === 'pipeline' ? stages.findIndex(([key]) => key === a.stage) - stages.findIndex(([key]) => key === b.stage) : 0);
            renderTable(view === 'pipeline' ? ['ỨNG VIÊN', 'VỊ TRÍ', 'PHÒNG BAN', 'GIAI ĐOẠN'] : ['ỨNG VIÊN', 'VỊ TRÍ', 'EMAIL', 'GIAI ĐOẠN', 'NGÀY ỨNG TUYỂN'], ordered.map((application) => {
                const values = [application.candidateName, application.jobTitle];
                if (view === 'pipeline') values.push(application.department);
                else values.push(application.candidateEmail);
                values.push((row) => {
                    const cell = document.createElement('td');
                    if (!can('MANAGE_PIPELINE')) { cell.textContent = stageLabel(application.stage); row.append(cell); return; }
                    const select = document.createElement('select');
                    select.className = 'stage-select';
                    select.setAttribute('aria-label', `Giai đoạn của ${application.candidateName}`);
                    stages.forEach(([value, label]) => {
                        const option = document.createElement('option');
                        option.value = value;
                        option.textContent = label;
                        option.selected = application.stage === value;
                        select.append(option);
                    });
                    select.addEventListener('change', async () => {
                        select.disabled = true;
                        try {
                            await api(`/applications/${application.id}/stage`, { method: 'PATCH', body: JSON.stringify({ stage: select.value }) }, RECRUITMENT_API);
                            setAlert(`Đã chuyển ${application.candidateName} sang giai đoạn “${stageLabel(select.value)}”.`);
                            await loadRecruitment();
                        } catch (error) {
                            setAlert(error.message, 'error');
                            select.disabled = false;
                        }
                    });
                    cell.append(select);
                    row.append(cell);
                });
                if (view !== 'pipeline') values.push(application.appliedAt ? new Date(application.appliedAt).toLocaleDateString('vi-VN') : '—');
                return values;
            }));
        }
    }

    function createReportTable(title, headers, rows) {
        const section = document.createElement('section');
        section.className = 'report-table-section';
        const heading = document.createElement('h3'); heading.textContent = title;
        const table = document.createElement('table'); table.className = 'data-table';
        const thead = document.createElement('thead'); const headRow = document.createElement('tr');
        headers.forEach((header) => makeCell(headRow, header, 'th'));
        thead.append(headRow);
        const tbody = document.createElement('tbody');
        rows.forEach((values) => { const row = document.createElement('tr'); values.forEach((value) => makeCell(row, value)); tbody.append(row); });
        table.append(thead, tbody); section.append(heading, table);
        return section;
    }

    async function loadRecruitment() {
        workflowContent.innerHTML = '<p class="loading-line">Đang tải dữ liệu từ backend…</p>';
        try {
            const [jobs, candidates, applications] = await Promise.all([
                api('/jobs', {}, RECRUITMENT_API),
                can('VIEW_CANDIDATES') ? api('/candidates', {}, RECRUITMENT_API) : Promise.resolve({data: []}),
                can('VIEW_APPLICATIONS') ? api('/applications', {}, RECRUITMENT_API) : Promise.resolve({data: []})
            ]);
            workflow.jobs = jobs.data || [];
            workflow.candidates = candidates.data || [];
            workflow.applications = applications.data || [];
            renderWorkflow();
            connectionLabel.textContent = 'Đã kết nối';
        } catch (error) {
            workflowContent.innerHTML = '';
            const message = document.createElement('p');
            message.className = 'empty-state';
            message.textContent = error.name === 'TypeError' ? 'Không kết nối được API tuyển dụng. Hãy tải lại trang sau khi backend chạy.' : error.message;
            workflowContent.append(message);
            connectionLabel.textContent = 'Lỗi API';
        }
    }

    function addDialogField(container, labelText, name, type = 'text', options = {}) {
        const label = document.createElement('label');
        label.textContent = labelText;
        let control;
        if (type === 'textarea') control = document.createElement('textarea');
        else if (type === 'select') control = document.createElement('select');
        else control = document.createElement('input');
        control.name = name;
        if (type !== 'textarea' && type !== 'select') control.type = type;
        if (options.required) control.required = true;
        if (options.min != null) control.min = options.min;
        if (options.maxLength != null) control.maxLength = options.maxLength;
        if (options.placeholder) control.placeholder = options.placeholder;
        if (options.value != null) control.value = options.value;
        (options.choices || []).forEach(([value, text]) => {
            const option = document.createElement('option');
            option.value = value;
            option.textContent = text;
            control.append(option);
        });
        label.append(control);
        container.append(label);
    }

    function openCreateDialog(targetView = workflow.view === 'pipeline' ? 'applications' : workflow.view, item = null) {
        const view = targetView;
        editingRecordId = item?.id ?? null;
        const createPermission = { jobs:'MANAGE_JOBS', candidates:'MANAGE_CANDIDATES', applications:'CREATE_APPLICATION' }[view];
        if (!can(createPermission)) { setAlert('Vai trò hiện tại không có quyền tạo mục này.', 'error'); return; }
        const fields = document.getElementById('dialogFields');
        fields.replaceChildren();
        document.getElementById('dialogTitle').textContent = item ? (view === 'jobs' ? 'Sửa vị trí tuyển dụng' : 'Sửa hồ sơ ứng viên') : view === 'jobs' ? 'Tạo vị trí tuyển dụng' : view === 'candidates' ? 'Thêm ứng viên' : 'Thêm hồ sơ vào quy trình';
        if (view === 'jobs') {
            addDialogField(fields, 'Tên vị trí', 'title', 'text', { required: true, maxLength: 160, placeholder: 'Ví dụ: Chuyên viên tuyển dụng', value: item?.title });
            addDialogField(fields, 'Phòng ban', 'department', 'text', { required: true, maxLength: 120, placeholder: 'Ví dụ: Nhân sự', value: item?.department });
            addDialogField(fields, 'Số lượng cần tuyển', 'headcount', 'number', { required: true, min: 1, value: item?.headcount ?? 1 });
            addDialogField(fields, 'Mô tả', 'description', 'textarea', { maxLength: 4000, placeholder: 'Mô tả ngắn về vị trí', value: item?.description });
        } else if (view === 'candidates') {
            addDialogField(fields, 'Họ và tên', 'fullName', 'text', { required: true, maxLength: 160, value: item?.fullName });
            addDialogField(fields, 'Email', 'email', 'email', { required: true, maxLength: 254, value: item?.email });
            addDialogField(fields, 'Số điện thoại', 'phone', 'tel', { maxLength: 40, value: item?.phone });
            addDialogField(fields, 'Nguồn ứng viên', 'source', 'text', { maxLength: 100, value: item?.source || 'Trực tiếp', placeholder: 'Ví dụ: Giới thiệu nội bộ' });
        } else {
            if (!workflow.candidates.length || !workflow.jobs.length) {
                fields.textContent = 'Trước tiên hãy tạo ít nhất một hồ sơ ứng viên và một vị trí tuyển dụng.';
                document.getElementById('saveRecordButton').disabled = true;
            } else {
                document.getElementById('saveRecordButton').disabled = false;
                addDialogField(fields, 'Ứng viên', 'candidateId', 'select', { required: true, choices: workflow.candidates.map((candidate) => [candidate.id, `${candidate.fullName} · ${candidate.email}`]) });
                addDialogField(fields, 'Vị trí tuyển dụng', 'jobId', 'select', { required: true, choices: workflow.jobs.map((job) => [job.id, `${job.title} · ${job.department}`]) });
            }
        }
        recordDialog.showModal();
    }

    document.querySelectorAll('.workflow-tab').forEach((tab) => tab.addEventListener('click', () => {
        workflow.view = tab.dataset.view;
        renderWorkflow();
    }));
    document.querySelectorAll('[data-workspace-view]').forEach((link) => link.addEventListener('click', () => {
        workflow.view = link.dataset.workspaceView;
        renderWorkflow();
    }));
    document.getElementById('newRecordButton').addEventListener('click', openCreateDialog);
    document.getElementById('refreshWorkspace').addEventListener('click', loadRecruitment);
    document.getElementById('closeDialog').addEventListener('click', () => recordDialog.close());
    document.getElementById('cancelDialog').addEventListener('click', () => recordDialog.close());
    document.getElementById('recordForm').addEventListener('submit', async (event) => {
        event.preventDefault();
        const view = workflow.view === 'pipeline' ? 'applications' : workflow.view;
        const form = event.currentTarget;
        const payload = Object.fromEntries(new FormData(form).entries());
        if (view === 'jobs') payload.headcount = Number(payload.headcount);
        if (view === 'applications') {
            payload.candidateId = Number(payload.candidateId);
            payload.jobId = Number(payload.jobId);
        }
        const button = document.getElementById('saveRecordButton');
        button.disabled = true;
        button.textContent = 'Đang lưu…';
        try {
            const method = editingRecordId === null ? 'POST' : 'PUT';
            const path = editingRecordId === null ? `/${view}` : `/${view}/${editingRecordId}`;
            const response = await api(path, { method, body: JSON.stringify(payload) }, RECRUITMENT_API);
            recordDialog.close();
            form.reset();
            editingRecordId = null;
            setAlert(response.message || 'Đã lưu dữ liệu.');
            await loadRecruitment();
        } catch (error) {
            setAlert(error.message, 'error');
        } finally {
            button.disabled = false;
            button.textContent = 'Lưu';
        }
    });

    function initials(name) {
        return (name || '?').trim().slice(0, 1).toLocaleUpperCase('vi');
    }

    function displayRole(role) {
        const roles = {
            ROLE_ADMIN: 'Quản trị viên', ROLE_RECRUITER: 'Nhân viên tuyển dụng',
            ROLE_HR_MANAGER: 'Trưởng phòng Nhân sự', ROLE_HIRING_MANAGER: 'Trưởng bộ phận', ROLE_INTERVIEWER: 'Người phỏng vấn', ROLE_APPROVER: 'Người duyệt', ROLE_USER: 'Người dùng'
        };
        return roles[role] || role || 'Chưa phân vai trò';
    }

    async function loadProfile() {
        try {
            const response = await api('/me?includeAvatar=true');
            const profile = response.data || {};
            currentRole = profile.role || currentRole;
            AccessControl.setRole(currentRole);
            applyUiPermissions();
            const savedUser = typeof SessionManager !== 'undefined' && SessionManager.getUser();
            const username = profile.username || sessionStorage.getItem('authUsername') || savedUser?.username || 'Tài khoản';
            const role = displayRole(profile.role);
            const created = profile.createdAt ? new Date(profile.createdAt).toLocaleDateString('vi-VN') : '—';
            const avatar = initials(username);

            document.getElementById('welcomeName').textContent = username;
            document.getElementById('statUsername').textContent = username;
            document.getElementById('statRole').textContent = role;
            document.getElementById('statApi').textContent = 'Đã kết nối';
            document.getElementById('apiDetail').textContent = 'Phiên JWT hợp lệ';
            document.getElementById('profileName').textContent = username;
            document.getElementById('profileEmail').textContent = profile.email || 'Chưa khai báo email';
            document.getElementById('detailUsername').textContent = username;
            document.getElementById('detailRole').textContent = role;
            document.getElementById('detailCreated').textContent = created;
            document.getElementById('avatarButton').textContent = avatar;
            document.getElementById('profileAvatar').textContent = avatar;
            if (profile.avatarData) {
                const profileImage = document.createElement('img');
                profileImage.src = profile.avatarData;
                profileImage.alt = '';
                const buttonImage = profileImage.cloneNode();
                document.getElementById('profileAvatar').replaceChildren(profileImage);
                document.getElementById('avatarButton').replaceChildren(buttonImage);
            }
            connectionLabel.textContent = 'Đã kết nối';
        } catch (error) {
            document.getElementById('statApi').textContent = 'Mất kết nối';
            document.getElementById('apiDetail').textContent = 'Không tải được dữ liệu tài khoản';
            connectionLabel.textContent = 'Không khả dụng';
            if (error.name === 'TypeError') setAlert('Không thể kết nối backend. Hãy kiểm tra dịch vụ trên cổng 8080.', 'error');
            else if (sessionStorage.getItem(tokenKey)) setAlert(error.message, 'error');
        }
    }

    document.getElementById('todayLabel').textContent = new Intl.DateTimeFormat('vi-VN', {
        weekday: 'long', day: 'numeric', month: 'long', year: 'numeric'
    }).format(new Date());

    document.getElementById('avatarButton').addEventListener('click', () => {
        if (window.confirm('Bạn muốn đăng xuất khỏi thiết bị này?')) {
            clearAuth();
            window.location.replace('index.html?reason=logout');
        }
    });

    document.getElementById('passwordForm').addEventListener('submit', async (event) => {
        event.preventDefault();
        const button = document.getElementById('changePasswordButton');
        const newPassword = document.getElementById('newPassword').value;
        const confirmPassword = document.getElementById('confirmPassword').value;
        if (newPassword !== confirmPassword) {
            setAlert('Mật khẩu xác nhận không khớp.', 'error');
            return;
        }
        button.disabled = true;
        button.textContent = 'Đang cập nhật…';
        try {
            const result = await api('/change-password', {
                method: 'POST',
                body: JSON.stringify({
                    currentPassword: document.getElementById('currentPassword').value,
                    newPassword,
                    confirmPassword
                })
            });
            const changed = result.data || {};
            if (changed.newAccessToken) {
                sessionStorage.setItem(tokenKey, changed.newAccessToken);
                if (typeof SessionManager !== 'undefined') SessionManager.saveSession(changed.newAccessToken, SessionManager.getUser());
            }
            event.target.reset();
            setAlert(result.message || changed.message || 'Đã cập nhật mật khẩu.');
        } catch (error) {
            setAlert(error.message, 'error');
        } finally {
            button.disabled = false;
            button.textContent = 'Cập nhật mật khẩu';
        }
    });

    document.getElementById('revokeButton').addEventListener('click', async (event) => {
        if (!window.confirm('Thu hồi mọi phiên đang đăng nhập? Bạn sẽ cần đăng nhập lại trên thiết bị này.')) return;
        const button = event.currentTarget;
        button.disabled = true;
        try {
            await api('/revoke-sessions', { method: 'POST' });
            clearAuth();
            window.location.replace('index.html?reason=revoked_all');
        } catch (error) {
            setAlert(error.message, 'error');
            button.disabled = false;
        }
    });

    function setActiveNav(id) {
        document.querySelectorAll('.nav-link').forEach((link) => link.classList.toggle('active', link.id === id));
    }
    document.getElementById('navOverview').addEventListener('click', () => {
        workflow.view = 'jobs';
        setActiveNav('navOverview');
        renderWorkflow();
    });
    document.getElementById('navCandidates').addEventListener('click', () => {
        workflow.view = 'candidates';
        setActiveNav('navCandidates');
        renderWorkflow();
    });
    document.getElementById('navPipeline').addEventListener('click', () => {
        workflow.view = 'pipeline';
        setActiveNav('navPipeline');
        renderWorkflow();
    });

    loadProfile().then(loadRecruitment);
});
