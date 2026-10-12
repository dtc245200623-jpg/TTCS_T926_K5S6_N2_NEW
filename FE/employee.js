document.addEventListener('DOMContentLoaded', async () => {
    const apiBase = window.API_BASE_URL
        ? window.API_BASE_URL.replace(/\/api\/auth\/?$/, '/api/recruitment')
        : `${window.location.origin}/api/recruitment`;
    const authApiBase = window.API_BASE_URL || `${window.location.origin}/api/auth`;
    const currentToken = () => (typeof SessionManager !== 'undefined' && SessionManager.getToken())
        || sessionStorage.getItem('authToken') || localStorage.getItem('ats_jwt_token');
    const clearAuth = () => {
        if (typeof SessionManager !== 'undefined') SessionManager.clearSession();
        sessionStorage.removeItem('authToken');
        sessionStorage.removeItem('authUsername');
    };
    const token = currentToken();
    const jobsRoot = document.getElementById('jobList');
    const applicationsRoot = document.getElementById('applicationList');
    const notice = document.getElementById('employeeNotice');
    const search = document.getElementById('searchJobs');
    const departmentFilter = document.getElementById('departmentFilter');
    let jobs = [];
    let applications = [];

    if (!token) {
        window.location.replace('index.html');
        return;
    }
    await AccessControl.loadRole();
    if (!AccessControl.can('APPLY_FOR_JOB')) {
        AccessControl.deny(jobsRoot, 'Vai trò hiện tại không có quyền truy cập cổng ứng tuyển nội bộ.');
        applicationsRoot.replaceChildren();
        search.disabled = true;
        departmentFilter.disabled = true;
        return;
    }

    const stageNames = {
        NEW: 'Đã tiếp nhận', SCREENING: 'Đang sàng lọc', INTERVIEW: 'Phỏng vấn',
        OFFER: 'Đề xuất nhận việc', HIRED: 'Đã nhận việc', REJECTED: 'Chưa phù hợp'
    };

    function showNotice(message, kind = 'success') {
        notice.textContent = message;
        notice.className = `notice visible ${kind}`;
        notice.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
    }

    async function request(path, options = {}, baseUrl = apiBase) {
        const response = await fetch(`${baseUrl}${path}`, {
            ...options,
            headers: {
                Authorization: `Bearer ${currentToken() || token}`,
                ...(options.body ? { 'Content-Type': 'application/json' } : {}),
                ...options.headers
            }
        });
        const result = await response.json().catch(() => ({}));
        if (response.status === 401) {
            clearAuth();
            window.location.replace('index.html');
            throw new Error('Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.');
        }
        if (!response.ok || result.success === false) throw new Error(result.message || 'Không thể hoàn tất yêu cầu.');
        return result;
    }

    function element(tag, className, text) {
        const node = document.createElement(tag);
        if (className) node.className = className;
        if (text !== undefined) node.textContent = text;
        return node;
    }

    function initials(value) { return (value || 'NV').trim().slice(0, 2).toLocaleUpperCase('vi'); }
    function formatDate(value) { return value ? new Date(value).toLocaleDateString('vi-VN') : '—'; }

    function populateDepartments() {
        const selected = departmentFilter.value;
        const departments = [...new Set(jobs.map((job) => job.department).filter(Boolean))].sort((a, b) => a.localeCompare(b, 'vi'));
        departmentFilter.replaceChildren(new Option('Tất cả phòng ban', ''));
        departments.forEach((name) => departmentFilter.add(new Option(name, name)));
        departmentFilter.value = departments.includes(selected) ? selected : '';
    }

    function renderJobs() {
        const term = search.value.trim().toLocaleLowerCase('vi');
        const department = departmentFilter.value;
        const filtered = jobs.filter((job) => {
            const matchesText = `${job.title} ${job.department} ${job.description || ''}`.toLocaleLowerCase('vi').includes(term);
            return matchesText && (!department || job.department === department);
        });
        document.getElementById('jobCount').textContent = String(filtered.length);
        if (!filtered.length) {
            jobsRoot.replaceChildren(element('div', 'empty-state', jobs.length
                ? 'Không tìm thấy vị trí phù hợp với bộ lọc.'
                : 'Hiện chưa có vị trí nội bộ đang tuyển. Hãy quay lại sau để xem cơ hội mới.'));
            return;
        }

        const appliedJobIds = new Set(applications.map((application) => application.jobId));
        const cards = filtered.map((job) => {
            const card = element('article', 'job-card');
            const top = element('div', 'job-card-top');
            top.append(element('span', 'department-icon', initials(job.department)), element('span', 'open-badge', 'ĐANG TUYỂN'));
            const title = element('h3', '', job.title);
            const dept = element('p', 'job-department', job.department);
            const meta = element('div', 'job-meta');
            meta.append(element('span', '', `${job.headcount || 1} vị trí`));
            meta.append(element('span', '', `Đăng ngày ${formatDate(job.createdAt)}`));
            const description = element('p', 'job-description', job.description || 'Cơ hội phát triển nghề nghiệp trong nội bộ công ty.');
            const bottom = element('div', 'job-card-bottom');
            const date = element('span', 'job-date', 'Ứng tuyển nội bộ');
            const alreadyApplied = appliedJobIds.has(job.id);
            const button = element('button', 'apply-button', alreadyApplied ? 'Đã ứng tuyển' : 'Ứng tuyển →');
            button.type = 'button';
            button.disabled = alreadyApplied;
            if (!alreadyApplied) button.addEventListener('click', () => apply(job, button));
            bottom.append(date, button);
            card.append(top, title, dept, meta, description, bottom);
            return card;
        });
        jobsRoot.replaceChildren(...cards);
    }

    function renderApplications() {
        if (!applications.length) {
            applicationsRoot.replaceChildren(element('div', 'empty-state', 'Bạn chưa ứng tuyển vị trí nào. Khám phá cơ hội đang tuyển ở phía trên.'));
            return;
        }
        const table = element('table', 'applications-table');
        const header = element('thead');
        const headRow = element('tr');
        ['VỊ TRÍ', 'PHÒNG BAN', 'NGÀY ỨNG TUYỂN', 'TRẠNG THÁI'].forEach((text) => headRow.append(element('th', '', text)));
        header.append(headRow);
        const body = element('tbody');
        applications.forEach((application) => {
            const row = element('tr');
            row.append(element('td', '', application.jobTitle), element('td', '', application.department),
                element('td', '', formatDate(application.appliedAt)));
            const status = element('span', `status-pill stage-${(application.stage || '').toLowerCase()}`, stageNames[application.stage] || application.stage);
            const statusCell = element('td');
            statusCell.append(status);
            row.append(statusCell);
            body.append(row);
        });
        table.append(header, body);
        applicationsRoot.replaceChildren(table);
    }

    async function apply(job, button) {
        button.disabled = true;
        button.textContent = 'Đang gửi…';
        try {
            const result = await request(`/jobs/${job.id}/apply`, { method: 'POST' });
            showNotice(result.message || `Đã gửi ứng tuyển vị trí ${job.title}.`);
            await loadData();
        } catch (error) {
            showNotice(error.message, 'error');
            button.disabled = false;
            button.textContent = 'Ứng tuyển →';
        }
    }

    async function loadData() {
        jobsRoot.replaceChildren(element('div', 'loading-state', 'Đang tải cơ hội nội bộ…'));
        applicationsRoot.replaceChildren(element('div', 'loading-state', 'Đang tải hồ sơ ứng tuyển…'));
        try {
            const [jobResponse, applicationResponse, profileResponse] = await Promise.all([
                request('/jobs'), request('/my-applications'), request('/me', {}, authApiBase)
            ]);
            jobs = (jobResponse.data || []).filter((job) => !job.status || job.status === 'OPEN');
            applications = applicationResponse.data || [];
            const profile = profileResponse.data || {};
            const savedUser = typeof SessionManager !== 'undefined' && SessionManager.getUser();
            const name = profile.username || sessionStorage.getItem('authUsername') || savedUser?.username || 'Nhân viên';
            document.getElementById('employeeName').textContent = name;
            document.getElementById('logoutButton').textContent = initials(name);
            populateDepartments();
            renderJobs();
            renderApplications();
        } catch (error) {
            jobsRoot.replaceChildren(element('div', 'empty-state', error.name === 'TypeError'
                ? 'Không thể kết nối backend. Hãy kiểm tra dịch vụ đang chạy tại cổng 8080.' : error.message));
            applicationsRoot.replaceChildren(element('div', 'empty-state', 'Không tải được hồ sơ ứng tuyển.'));
        }
    }

    search.addEventListener('input', renderJobs);
    departmentFilter.addEventListener('change', renderJobs);
    document.getElementById('logoutButton').addEventListener('click', () => {
        if (!window.confirm('Đăng xuất khỏi thiết bị này?')) return;
        clearAuth();
        window.location.replace('index.html?reason=logout');
    });
    loadData();
});
