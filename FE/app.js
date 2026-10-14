/**
 * HR MasterCore - Web Application Logic
 * Modern Vanilla JavaScript with Reactive State & LocalStorage
 */

// =============================================================================
// 1. DEFAULT SEED DATA (Chuẩn hoá theo yêu cầu nghiệp vụ)
// =============================================================================

const INITIAL_DEPARTMENTS = [
  { id: 'dept_it', name: 'Công nghệ Thông tin & Sản phẩm', code: 'TECH' },
  { id: 'dept_hr', name: 'Nhân sự & Văn hóa Doanh nghiệp', code: 'HR' },
  { id: 'dept_sales', name: 'Kinh doanh & Phát triển thị trường', code: 'SALES' },
  { id: 'dept_fin', name: 'Tài chính - Kế toán', code: 'FIN' }
];

const INITIAL_SALARY_RANGES = [
  { id: 'sal_l1', name: 'Bậc L1 (5.000.000 - 10.000.000 VNĐ)' },
  { id: 'sal_l2', name: 'Bậc L2 (12.000.000 - 20.000.000 VNĐ)' },
  { id: 'sal_l3', name: 'Bậc L3 (22.000.000 - 35.000.000 VNĐ)' },
  { id: 'sal_l4', name: 'Bậc L4 (38.000.000 - 60.000.000 VNĐ)' },
  { id: 'sal_l5', name: 'Bậc L5 (60.000.000 - 90.000.000 VNĐ)' },
  { id: 'sal_l6', name: 'Bậc L6 (90.000.000 - 140.000.000 VNĐ)' },
  { id: 'sal_l7', name: 'Bậc L7 (Thỏa thuận / Executive)' }
];

const INITIAL_COMPETENCIES = [
  {
    id: 'comp_arch',
    code: 'COMP_SYS_ARCH',
    name: 'Kiến trúc & Thiết kế Hệ thống',
    category: 'CHUYEN_MON',
    desc: 'Khả năng thiết kế hệ thống phân tán, chịu tải cao, tính mở rộng và bảo mật thông tin.'
  },
  {
    id: 'comp_coding',
    code: 'COMP_CODING',
    name: 'Kỹ năng Lập trình & Clean Code',
    category: 'CHUYEN_MON',
    desc: 'Viết mã nguồn tối ưu, tuân thủ nguyên lý SOLID, thiết kế Unit Test và tái cấu trúc mã.'
  },
  {
    id: 'comp_problem',
    code: 'COMP_PROBLEM_SOLVING',
    name: 'Tư duy Giải quyết vấn đề (Problem Solving)',
    category: 'TU_DUY',
    desc: 'Phân tích nguyên nhân gốc rễ (Root-cause), đề xuất giải pháp khả thi dưới áp lực.'
  },
  {
    id: 'comp_comm',
    code: 'COMP_COMMUNICATION',
    name: 'Kỹ năng Giao tiếp & Thuyết trình',
    category: 'KY_NANG_MEM',
    desc: 'Trình bày ý tưởng mạch lạc, lắng nghe tích cực và điều chỉnh thông điệp theo đối tượng.'
  },
  {
    id: 'comp_lead',
    code: 'COMP_LEADERSHIP',
    name: 'Năng lực Lãnh đạo & Phát triển Đội ngũ',
    category: 'LANH_DAO',
    desc: 'Định hướng mục tiêu, tạo động lực, huấn luyện (Coaching) và đánh giá hiệu suất nhân sự.'
  },
  {
    id: 'comp_conflict',
    code: 'COMP_CONFLICT_MGMT',
    name: 'Quản lý & Xử lý Xung đột',
    category: 'KY_NANG_MEM',
    desc: 'Trung hòa mâu thuẫn trong tập thể, hướng đến tiếng nói chung trên tinh thần xây dựng.'
  },
  {
    id: 'comp_hr_strat',
    code: 'COMP_HR_STRATEGY',
    name: 'Hoạch định Chiến lược Nhân sự',
    category: 'CHUYEN_MON',
    desc: 'Xây dựng chính sách đãi ngộ (C&B), kế hoạch nhân sự và chuẩn hóa quy trình tuyển dụng.'
  }
];

const INITIAL_QUESTIONS = [
  {
    id: 'ques_1',
    competencyId: 'comp_arch',
    difficulty: 2,
    text: 'Khi một service xử lý thanh toán bị nghẽn (bottleneck) vào giờ cao điểm, bạn sẽ áp dụng kiến trúc và giải pháp gì để giảm tải?',
    behavior: 'Ứng viên phân tích theo: Caching (Redis), Message Queue (Kafka/RabbitMQ), Rate Limiting, Async worker và Database Read-replica.'
  },
  {
    id: 'ques_2',
    competencyId: 'comp_coding',
    difficulty: 2,
    text: 'Hãy mô tả một trường hợp bạn refactor một đoạn legacy code phức tạp và cách bạn đảm bảo hệ thống không phát sinh lỗi?',
    behavior: 'Ứng viên đề cập đến việc viết Unit Test bảo vệ trước, chia nhỏ hàm theo Single Responsibility, chạy CI/CD regression test.'
  },
  {
    id: 'ques_3',
    competencyId: 'comp_problem',
    difficulty: 2,
    text: 'Hãy kể lại tình huống bạn gặp phải một bug nghiêm trọng trên Production mà không thể tái hiện ở môi trường Local/Staging?',
    behavior: 'Ứng viên trình bày theo cấu trúc STAR: Đọc log/APM, cách ly vấn đề, rollback/hotfix, post-mortem và bài học rút ra.'
  },
  {
    id: 'ques_4',
    competencyId: 'comp_comm',
    difficulty: 1,
    text: 'Làm thế nào bạn giải thích một khái niệm kỹ thuật phức tạp cho khách hàng hoặc bên kinh doanh không hiểu về công nghệ?',
    behavior: 'Sử dụng phép ẩn dụ, tập trung vào giá trị nghiệp vụ (Business Value), không lạm dụng thuật ngữ kỹ thuật (jargon).'
  },
  {
    id: 'ques_5',
    competencyId: 'comp_lead',
    difficulty: 3,
    text: 'Khi có một thành viên trong team giảm sút phong độ và gây ảnh hưởng tiến độ chung, bạn sẽ can thiệp như thế nào?',
    behavior: 'Họp 1-on-1 kín đáo, tìm hiểu khó khăn cá nhân/công việc, đặt KPI rõ ràng hỗ trợ theo tuần và theo dõi cải thiện.'
  },
  {
    id: 'ques_6',
    competencyId: 'comp_hr_strat',
    difficulty: 3,
    text: 'Bạn xây dựng chính sách giữ chân nhân tài (Talent Retention) như thế nào trong bối cảnh thị trường cạnh tranh gay gắt?',
    behavior: 'Kết hợp Total Rewards (lương, thưởng ESOP), lộ trình thăng tiến (Career Path), văn hóa ghi nhận và môi trường làm việc linh hoạt.'
  }
];

const INITIAL_JOBS = [
  {
    id: 'job_dev_sr',
    code: 'DEV_SR',
    name: 'Kỹ sư Phần mềm Cao cấp (Senior Backend)',
    level: 'L4',
    departmentId: 'dept_it',
    salaryRangeId: 'sal_l4',
    desc: 'Chịu trách nhiệm thiết kế kiến trúc lõi, tối ưu hiệu năng và hướng dẫn các lập trình viên trẻ.',
    status: 'ACTIVE'
  },
  {
    id: 'job_hr_mgr',
    code: 'HR_MGR',
    name: 'Trưởng phòng Nhân sự (HR Manager)',
    level: 'L6',
    departmentId: 'dept_hr',
    salaryRangeId: 'sal_l6',
    desc: 'Quản trị toàn bộ chiến lược nhân sự, khung năng lực và tối ưu hóa trải nghiệm nhân viên.',
    status: 'ACTIVE'
  },
  {
    id: 'job_sales_lead',
    code: 'SALES_LEAD',
    name: 'Trưởng nhóm Kinh doanh B2B',
    level: 'L5',
    departmentId: 'dept_sales',
    salaryRangeId: 'sal_l5',
    desc: 'Dẫn dắt đội ngũ Sales đạt chỉ tiêu doanh số và mở rộng mạng lưới khách hàng doanh nghiệp.',
    status: 'ACTIVE'
  },
  {
    id: 'job_ba_mid',
    code: 'BA_MID',
    name: 'Chuyên viên Phân tích Nghiệp vụ (Middle BA)',
    level: 'L3',
    departmentId: 'dept_it',
    salaryRangeId: 'sal_l3',
    desc: 'Cầu nối giữa khối kinh doanh và đội kỹ thuật để làm rõ yêu cầu tính năng sản phẩm.',
    status: 'ACTIVE'
  }
];

// Cấu hình mẫu: DEV_SR đã cấu hình chuẩn 100% (35 + 35 + 20 + 10)
const INITIAL_MATRICES = {
  job_dev_sr: [
    { competencyId: 'comp_arch', weight: 35 },
    { competencyId: 'comp_coding', weight: 35 },
    { competencyId: 'comp_problem', weight: 20 },
    { competencyId: 'comp_comm', weight: 10 }
  ],
  job_hr_mgr: [
    { competencyId: 'comp_hr_strat', weight: 40 },
    { competencyId: 'comp_lead', weight: 30 },
    { competencyId: 'comp_comm', weight: 20 },
    { competencyId: 'comp_conflict', weight: 10 }
  ]
};

// =============================================================================
// 2. STATE MANAGER & PERSISTENCE
// =============================================================================

class Store {
  constructor() {
    this.departments = this.load('hrm_departments', INITIAL_DEPARTMENTS);
    this.salaryRanges = this.load('hrm_salary_ranges', INITIAL_SALARY_RANGES);
    this.competencies = this.load('hrm_competencies', INITIAL_COMPETENCIES);
    this.questions = this.load('hrm_questions', INITIAL_QUESTIONS);
    this.jobs = this.load('hrm_jobs', INITIAL_JOBS);
    this.matrices = this.load('hrm_matrices', INITIAL_MATRICES);
  }

  load(key, fallback) {
    try {
      const data = localStorage.getItem(key);
      return data ? JSON.parse(data) : fallback;
    } catch (e) {
      console.warn('LocalStorage error, using fallback', e);
      return fallback;
    }
  }

  save() {
    try {
      localStorage.setItem('hrm_departments', JSON.stringify(this.departments));
      localStorage.setItem('hrm_salary_ranges', JSON.stringify(this.salaryRanges));
      localStorage.setItem('hrm_competencies', JSON.stringify(this.competencies));
      localStorage.setItem('hrm_questions', JSON.stringify(this.questions));
      localStorage.setItem('hrm_jobs', JSON.stringify(this.jobs));
      localStorage.setItem('hrm_matrices', JSON.stringify(this.matrices));
    } catch (e) {
      console.error('Failed to save state to localStorage', e);
    }
  }

  resetToDefault() {
    this.departments = JSON.parse(JSON.stringify(INITIAL_DEPARTMENTS));
    this.salaryRanges = JSON.parse(JSON.stringify(INITIAL_SALARY_RANGES));
    this.competencies = JSON.parse(JSON.stringify(INITIAL_COMPETENCIES));
    this.questions = JSON.parse(JSON.stringify(INITIAL_QUESTIONS));
    this.jobs = JSON.parse(JSON.stringify(INITIAL_JOBS));
    this.matrices = JSON.parse(JSON.stringify(INITIAL_MATRICES));
    this.save();
  }
}

const store = new Store();

// =============================================================================
// 3. TOAST NOTIFICATION UTILITY
// =============================================================================

function showToast(title, message, type = 'success') {
  const container = document.getElementById('toast-container');
  const toast = document.createElement('div');
  toast.className = `toast toast-${type}`;

  const iconMap = {
    success: '✅',
    error: '❌',
    warning: '⚠️'
  };

  toast.innerHTML = `
    <span class="toast-icon">${iconMap[type] || 'ℹ️'}</span>
    <div class="toast-content">
      <div class="toast-title">${title}</div>
      <div class="toast-message">${message}</div>
    </div>
  `;

  container.appendChild(toast);
  setTimeout(() => {
    toast.style.opacity = '0';
    toast.style.transform = 'translateY(10px)';
    setTimeout(() => toast.remove(), 300);
  }, 4000);
}

// =============================================================================
// 4. STATS & OVERVIEW BAR
// =============================================================================

function updateGlobalStats() {
  const totalJobs = store.jobs.length;
  let readyJobsCount = 0;

  store.jobs.forEach(job => {
    const list = store.matrices[job.id] || [];
    const totalWeight = list.reduce((sum, item) => sum + (Number(item.weight) || 0), 0);
    if (totalWeight === 100) readyJobsCount++;
  });

  document.getElementById('stat-total-jobs').textContent = totalJobs;
  document.getElementById('stat-ready-jobs').textContent = `${readyJobsCount} / ${totalJobs}`;
  document.getElementById('stat-total-competencies').textContent = store.competencies.length;
  document.getElementById('stat-total-questions').textContent = store.questions.length;
}

// =============================================================================
// 5. TAB 1: DANH MỤC CHỨC DANH LOGIC
// =============================================================================

function renderJobTable() {
  const tbody = document.getElementById('job-table-body');
  const searchVal = document.getElementById('search-job-input').value.toLowerCase().trim();
  const filterLevel = document.getElementById('filter-job-level').value;
  const filterDept = document.getElementById('filter-job-dept').value;
  const filterStatus = document.getElementById('filter-job-status').value;

  const filteredJobs = store.jobs.filter(job => {
    const matchSearch = job.code.toLowerCase().includes(searchVal) || job.name.toLowerCase().includes(searchVal);
    const matchLevel = !filterLevel || job.level === filterLevel;
    const matchDept = !filterDept || job.departmentId === filterDept;
    const matchStatus = !filterStatus || job.status === filterStatus;
    return matchSearch && matchLevel && matchDept && matchStatus;
  });

  if (filteredJobs.length === 0) {
    tbody.innerHTML = `
      <tr>
        <td colspan="8" style="text-align: center; padding: 2.5rem; color: var(--text-muted);">
          Không tìm thấy chức danh nào phù hợp với bộ lọc hiện tại.
        </td>
      </tr>
    `;
    return;
  }

  tbody.innerHTML = filteredJobs.map(job => {
    const dept = store.departments.find(d => d.id === job.departmentId);
    const salary = store.salaryRanges.find(s => s.id === job.salaryRangeId);
    
    // Check Matrix Status
    const matrix = store.matrices[job.id] || [];
    const totalWeight = matrix.reduce((sum, item) => sum + (Number(item.weight) || 0), 0);
    
    let matrixBadge = '';
    if (matrix.length === 0) {
      matrixBadge = `<span class="pill-matrix empty">Chưa cấu hình (0%)</span>`;
    } else if (totalWeight === 100) {
      matrixBadge = `<span class="pill-matrix ready">✓ Đã đạt 100% (${matrix.length} tiêu chí)</span>`;
    } else {
      matrixBadge = `<span class="pill-matrix incomplete">⚠️ Chưa đạt 100% (${totalWeight}%)</span>`;
    }

    const levelDescriptions = {
      L1: 'Intern/Fresher',
      L2: 'Junior',
      L3: 'Middle',
      L4: 'Senior',
      L5: 'Lead',
      L6: 'Manager',
      L7: 'Director'
    };

    return `
      <tr>
        <td><span class="code-badge">${escapeHtml(job.code)}</span></td>
        <td>
          <strong>${escapeHtml(job.name)}</strong>
          ${job.desc ? `<div style="font-size:0.75rem; color:var(--text-muted); margin-top:2px;">${escapeHtml(job.desc)}</div>` : ''}
        </td>
        <td>
          <span class="badge-level level-${job.level}">
            ${job.level} - ${levelDescriptions[job.level] || ''}
          </span>
        </td>
        <td>${dept ? dept.name : '—'}</td>
        <td style="font-size:0.82rem; color:var(--text-muted);">${salary ? salary.name : 'Chưa gán'}</td>
        <td>${matrixBadge}</td>
        <td>
          <span class="status-badge ${job.status === 'ACTIVE' ? 'status-active' : 'status-inactive'}">
            <span class="status-dot"></span>
            ${job.status === 'ACTIVE' ? 'Hoạt động' : 'Ngưng'}
          </span>
        </td>
        <td class="text-right">
          <div class="table-actions">
            <button class="btn-icon" title="Cấu hình Khung Năng Lực" onclick="openMatrixForJob('${job.id}')">
              ⚖️
            </button>
            <button class="btn-icon" title="Chỉnh sửa" onclick="editJob('${job.id}')">
              ✏️
            </button>
            <button class="btn-icon delete" title="Xóa hoặc Vô hiệu hóa" onclick="deleteJob('${job.id}')">
              🗑️
            </button>
          </div>
        </td>
      </tr>
    `;
  }).join('');
}

// Populate Dept and Salary Dropdowns
function populateCommonDropdowns() {
  const deptSelects = [
    document.getElementById('filter-job-dept'),
    document.getElementById('job-department')
  ];

  deptSelects.forEach(select => {
    if (!select) return;
    const isFilter = select.id.startsWith('filter');
    select.innerHTML = (isFilter ? '<option value="">Tất cả Phòng ban</option>' : '') +
      store.departments.map(d => `<option value="${d.id}">${d.name} (${d.code})</option>`).join('');
  });

  const salarySelect = document.getElementById('job-salary-range');
  if (salarySelect) {
    salarySelect.innerHTML = '<option value="">-- Chưa gán dải lương --</option>' +
      store.salaryRanges.map(s => `<option value="${s.id}">${s.name}</option>`).join('');
  }
}

// =============================================================================
// 6. TAB 2: MA TRẬN NĂNG LỰC & TRỌNG SỐ (CORE BUSINESS RULE: TỔNG = 100%)
// =============================================================================

let currentMatrixJobId = null;
let currentWorkingMatrix = []; // Local working copy of [{ competencyId, weight }]

function initMatrixTab() {
  const select = document.getElementById('select-matrix-job');
  select.innerHTML = store.jobs.map(j => `
    <option value="${j.id}">[${j.code}] ${j.name} (${j.level})</option>
  `).join('');

  if (store.jobs.length > 0) {
    if (!currentMatrixJobId || !store.jobs.some(j => j.id === currentMatrixJobId)) {
      currentMatrixJobId = store.jobs[0].id;
    }
    select.value = currentMatrixJobId;
    loadMatrixForJob(currentMatrixJobId);
  }

  select.addEventListener('change', (e) => {
    currentMatrixJobId = e.target.value;
    loadMatrixForJob(currentMatrixJobId);
  });

  document.getElementById('search-pool-criteria').addEventListener('input', renderAvailablePool);
}

function openMatrixForJob(jobId) {
  currentMatrixJobId = jobId;
  // Switch tab
  switchTab('tab-matrix');
  const select = document.getElementById('select-matrix-job');
  if (select) select.value = jobId;
  loadMatrixForJob(jobId);
}

function loadMatrixForJob(jobId) {
  const job = store.jobs.find(j => j.id === jobId);
  if (!job) return;

  const dept = store.departments.find(d => d.id === job.departmentId);
  document.getElementById('matrix-job-meta').innerHTML = `
    <span class="badge-level level-${job.level}">${job.level}</span>
    <span style="font-size:0.85rem; font-weight:600; color:var(--text-muted);">${dept ? dept.name : ''}</span>
  `;

  // Clone from store or default
  const saved = store.matrices[jobId] || [];
  currentWorkingMatrix = JSON.parse(JSON.stringify(saved));

  renderAssignedCriteria();
  renderAvailablePool();
  updateWeightStatus();
}

function renderAssignedCriteria() {
  const container = document.getElementById('assigned-criteria-list');
  document.getElementById('assigned-count').textContent = currentWorkingMatrix.length;

  if (currentWorkingMatrix.length === 0) {
    container.innerHTML = `
      <div style="text-align: center; padding: 2rem; color: var(--text-muted); background: var(--bg-surface); border: 2px dashed var(--border-light); border-radius: var(--radius-md);">
        Chưa có tiêu chí nào được gán cho chức danh này.<br>
        Vui lòng chọn tiêu chí từ danh sách bên phải để thêm vào.
      </div>
    `;
    return;
  }

  const categoryNames = {
    CHUYEN_MON: 'Chuyên môn',
    KY_NANG_MEM: 'Kỹ năng mềm',
    LANH_DAO: 'Lãnh đạo',
    TU_DUY: 'Tư duy'
  };

  container.innerHTML = currentWorkingMatrix.map((item, index) => {
    const comp = store.competencies.find(c => c.id === item.competencyId);
    if (!comp) return '';

    return `
      <div class="assigned-item" data-index="${index}">
        <div class="assigned-item-header">
          <div class="assigned-item-title">
            <span>${escapeHtml(comp.name)}</span>
            <span class="assigned-category-badge">${categoryNames[comp.category] || comp.category}</span>
          </div>
          <button class="btn-remove-criteria" onclick="removeCriteriaFromMatrix(${index})" title="Xóa tiêu chí này khỏi khung">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M18 6L6 18M6 6l12 12"/></svg>
          </button>
        </div>

        <div class="assigned-controls-row">
          <div class="slider-container">
            <input type="range" class="weight-range-slider" min="5" max="100" step="5"
              value="${item.weight}" 
              oninput="syncWeightValue(${index}, this.value)">
          </div>
          <div class="number-input-group">
            <input type="number" min="1" max="100" value="${item.weight}"
              onchange="syncWeightValue(${index}, this.value)">
            <span>%</span>
          </div>
        </div>
      </div>
    `;
  }).join('');
}

function renderAvailablePool() {
  const poolContainer = document.getElementById('available-criteria-pool');
  const search = document.getElementById('search-pool-criteria').value.toLowerCase().trim();

  // Filter out already assigned
  const assignedIds = new Set(currentWorkingMatrix.map(m => m.competencyId));
  const available = store.competencies.filter(c => {
    if (assignedIds.has(c.id)) return false;
    return c.name.toLowerCase().includes(search) || c.code.toLowerCase().includes(search);
  });

  if (available.length === 0) {
    poolContainer.innerHTML = `
      <div style="text-align: center; padding: 1.5rem; font-size: 0.8rem; color: var(--text-muted);">
        ${store.competencies.length === assignedIds.size ? 'Tất cả tiêu chí đã được đưa vào khung.' : 'Không tìm thấy tiêu chí phù hợp.'}
      </div>
    `;
    return;
  }

  poolContainer.innerHTML = available.map(c => `
    <div class="pool-item">
      <div class="pool-item-info">
        <span class="pool-item-name">${escapeHtml(c.name)}</span>
        <span class="pool-item-meta">${escapeHtml(c.code)}</span>
      </div>
      <button class="btn-add-to-matrix" onclick="addCriteriaToMatrix('${c.id}')" title="Thêm vào khung">+</button>
    </div>
  `).join('');
}

function addCriteriaToMatrix(compId) {
  // Suggest a default weight that tries to fit or 10%
  const currentTotal = currentWorkingMatrix.reduce((sum, i) => sum + (Number(i.weight) || 0), 0);
  const remaining = Math.max(5, 100 - currentTotal);
  currentWorkingMatrix.push({
    competencyId: compId,
    weight: remaining > 0 ? remaining : 10
  });

  renderAssignedCriteria();
  renderAvailablePool();
  updateWeightStatus();
}

function removeCriteriaFromMatrix(index) {
  currentWorkingMatrix.splice(index, 1);
  renderAssignedCriteria();
  renderAvailablePool();
  updateWeightStatus();
}

function syncWeightValue(index, val) {
  let num = parseInt(val, 10);
  if (isNaN(num)) num = 0;
  if (num < 0) num = 0;
  if (num > 100) num = 100;

  currentWorkingMatrix[index].weight = num;

  // Re-render only inputs in the specific row to avoid losing focus if typing
  const itemEl = document.querySelector(`.assigned-item[data-index="${index}"]`);
  if (itemEl) {
    const slider = itemEl.querySelector('.weight-range-slider');
    const input = itemEl.querySelector('input[type="number"]');
    if (slider && slider.value != num) slider.value = num;
    if (input && input.value != num) input.value = num;
  }

  updateWeightStatus();
}

// Check Business Rule: Sum must be exactly 100%
function updateWeightStatus() {
  const total = currentWorkingMatrix.reduce((sum, item) => sum + (Number(item.weight) || 0), 0);
  const totalDisplay = document.getElementById('weight-total-number');
  const fill = document.getElementById('weight-progress-fill');
  const box = document.getElementById('weight-summary-box');
  const statusText = document.getElementById('weight-status-text');
  const hintText = document.getElementById('weight-hint-text');
  const btnSave = document.getElementById('btn-save-matrix');
  const icon = document.getElementById('weight-icon');

  totalDisplay.textContent = `${total}%`;
  fill.style.width = `${Math.min(total, 100)}%`;

  box.classList.remove('state-perfect', 'state-under', 'state-over');

  if (total === 100) {
    box.classList.add('state-perfect');
    icon.textContent = '🌟';
    statusText.textContent = 'Tổng trọng số đạt chuẩn hoàn hảo (100%)';
    hintText.textContent = 'Khung năng lực đã sẵn sàng để Lưu & Kích hoạt cho các vòng phỏng vấn Sprint 6.';
    btnSave.disabled = false;
  } else if (total < 100) {
    box.classList.add('state-under');
    icon.textContent = '⚠️';
    const diff = 100 - total;
    statusText.textContent = `Chưa đủ 100% (Hiện tại: ${total}%)`;
    hintText.textContent = `Bạn cần phân bổ thêm +${diff}% cho các tiêu chí trước khi có thể kích hoạt.`;
    btnSave.disabled = true;
  } else {
    box.classList.add('state-over');
    icon.textContent = '⛔';
    const diff = total - 100;
    statusText.textContent = `Vượt quá 100% (Hiện tại: ${total}%)`;
    hintText.textContent = `Vui lòng giảm bớt -${diff}% từ các tiêu chí để tổng đạt chính xác 100%.`;
    btnSave.disabled = true;
  }
}

// Save Matrix Button Handler
document.getElementById('btn-save-matrix').addEventListener('click', () => {
  const total = currentWorkingMatrix.reduce((sum, item) => sum + (Number(item.weight) || 0), 0);
  if (total !== 100) {
    showToast('Lỗi quy tắc nghiệp vụ', 'Tổng trọng số bắt buộc phải bằng chính xác 100%!', 'error');
    return;
  }

  store.matrices[currentMatrixJobId] = JSON.parse(JSON.stringify(currentWorkingMatrix));
  store.save();
  updateGlobalStats();
  renderJobTable();
  renderSprint6Evaluation();

  showToast('Thành công', 'Đã lưu và kích hoạt bộ khung năng lực chuẩn 100% cho chức danh!', 'success');
});

document.getElementById('btn-reset-matrix').addEventListener('click', () => {
  loadMatrixForJob(currentMatrixJobId);
  showToast('Khôi phục', 'Đã khôi phục lại cấu hình đã lưu trước đó.', 'warning');
});

// =============================================================================
// 7. TAB 3: THƯ VIỆN NĂNG LỰC DÙNG CHUNG
// =============================================================================

function renderCompetencyCards() {
  const container = document.getElementById('competency-cards-container');
  const categoryNames = {
    CHUYEN_MON: 'Chuyên môn',
    KY_NANG_MEM: 'Kỹ năng mềm',
    LANH_DAO: 'Lãnh đạo',
    TU_DUY: 'Tư duy & Đổi mới'
  };

  container.innerHTML = store.competencies.map(c => {
    // Count how many jobs are using this competency
    let usageCount = 0;
    Object.values(store.matrices).forEach(matrix => {
      if (matrix.some(item => item.competencyId === c.id)) usageCount++;
    });

    // Count questions linked
    const quesCount = store.questions.filter(q => q.competencyId === c.id).length;

    return `
      <div class="competency-card">
        <div>
          <div class="competency-card-header">
            <span class="code-badge">${escapeHtml(c.code)}</span>
            <span class="assigned-category-badge">${categoryNames[c.category] || c.category}</span>
          </div>
          <h3 class="competency-title">${escapeHtml(c.name)}</h3>
          <p class="competency-desc">${escapeHtml(c.desc || 'Chưa có mô tả chi tiết.')}</p>
        </div>
        <div class="competency-card-footer">
          <span class="usage-badge" title="Tái sử dụng cho nhiều chức danh">Đang gán cho ${usageCount} chức danh</span>
          <span>${quesCount} câu hỏi PV</span>
        </div>
      </div>
    `;
  }).join('');
}

// =============================================================================
// 8. TAB 4: NGÂN HÀNG CÂU HỎI PHỎNG VẤN
// =============================================================================

function renderQuestions() {
  const container = document.getElementById('question-list-container');
  const search = document.getElementById('search-question-input').value.toLowerCase().trim();
  const filterComp = document.getElementById('filter-question-competency').value;
  const filterDiff = document.getElementById('filter-question-difficulty').value;

  const filtered = store.questions.filter(q => {
    const comp = store.competencies.find(c => c.id === q.competencyId);
    const matchSearch = q.text.toLowerCase().includes(search) || (q.behavior && q.behavior.toLowerCase().includes(search));
    const matchComp = !filterComp || q.competencyId === filterComp;
    const matchDiff = !filterDiff || q.difficulty == filterDiff;
    return matchSearch && matchComp && matchDiff;
  });

  if (filtered.length === 0) {
    container.innerHTML = `
      <div style="text-align:center; padding: 2.5rem; color: var(--text-muted); background: var(--bg-surface); border-radius: var(--radius-lg); border: 1px solid var(--border-light);">
        Không có câu hỏi phỏng vấn nào phù hợp với bộ lọc.
      </div>
    `;
    return;
  }

  const diffLabels = {
    1: 'Cơ bản (Junior)',
    2: 'Nâng cao (Mid/Senior)',
    3: 'Chuyên gia / Lead'
  };

  container.innerHTML = filtered.map(q => {
    const comp = store.competencies.find(c => c.id === q.competencyId);
    return `
      <div class="question-card">
        <div class="question-header">
          <div class="question-tags">
            <span class="tag-competency">${comp ? comp.name : 'Năng lực'}</span>
            <span class="tag-difficulty diff-${q.difficulty}">${diffLabels[q.difficulty] || ''}</span>
          </div>
          <button class="btn-icon delete" onclick="deleteQuestion('${q.id}')" title="Xóa câu hỏi">🗑️</button>
        </div>
        <div class="question-text">
          "${escapeHtml(q.text)}"
        </div>
        ${q.behavior ? `
          <div class="behavior-box">
            <strong>Dấu hiệu nhận diện hành vi (STAR):</strong> ${escapeHtml(q.behavior)}
          </div>
        ` : ''}
      </div>
    `;
  }).join('');
}

function populateQuestionCompetencyFilter() {
  const filterSelect = document.getElementById('filter-question-competency');
  const modalSelect = document.getElementById('ques-competency');

  const options = store.competencies.map(c => `<option value="${c.id}">${c.name}</option>`).join('');

  if (filterSelect) {
    filterSelect.innerHTML = '<option value="">Tất cả Tiêu chí năng lực</option>' + options;
  }
  if (modalSelect) {
    modalSelect.innerHTML = options;
  }
}

// =============================================================================
// 9. TAB 5: SPRINT 6 INTERVIEW EVALUATION SHEET GENERATOR
// =============================================================================

const CANDIDATES = [
  { id: 'cand_1', name: 'Lê Minh Khang', targetJobId: 'job_dev_sr', email: 'khang.le@example.com', exp: '5 năm kinh nghiệm Backend' },
  { id: 'cand_2', name: 'Trần Thị Thu Thảo', targetJobId: 'job_hr_mgr', email: 'thao.tran@example.com', exp: '8 năm HR Generalist & C&B' },
  { id: 'cand_3', name: 'Vũ Hoàng Long', targetJobId: 'job_sales_lead', email: 'long.vu@example.com', exp: '6 năm B2B Tech Sales' }
];

let candidateScores = {}; // Key: candId_compId -> score (1..5)

function renderSprint6Evaluation() {
  const select = document.getElementById('eval-candidate-select');
  const candId = select.value;
  const candidate = CANDIDATES.find(c => c.id === candId);
  const container = document.getElementById('evaluation-sheet-container');
  const reflectBadge = document.getElementById('eval-job-reflect-badge');

  if (!candidate) return;

  const job = store.jobs.find(j => j.id === candidate.targetJobId);
  if (!job) {
    container.innerHTML = 'Không tìm thấy chức danh của ứng viên.';
    return;
  }

  const matrix = store.matrices[job.id] || [];
  const totalWeight = matrix.reduce((sum, item) => sum + (Number(item.weight) || 0), 0);

  reflectBadge.innerHTML = `
    Vị trí ứng tuyển: <strong>${job.name}</strong> 
    <span class="pill-matrix ${totalWeight === 100 ? 'ready' : 'incomplete'}">
      ${totalWeight === 100 ? '✓ Đã kích hoạt 100%' : '⚠️ Chưa hoàn tất 100%'}
    </span>
  `;

  if (matrix.length === 0 || totalWeight !== 100) {
    container.innerHTML = `
      <div style="text-align:center; padding: 3rem; background: var(--bg-surface-muted); border-radius: var(--radius-lg);">
        <span style="font-size: 2.5rem;">⚠️</span>
        <h3 style="margin-top: 0.5rem; color: var(--color-danger);">Khung năng lực chưa hoàn tất cấu hình (Tổng: ${totalWeight}%)</h3>
        <p style="color: var(--text-muted); margin-top: 0.5rem;">
          Theo quy tắc nghiệp vụ, Phiếu phỏng vấn chỉ được sinh ra khi vị trí <strong>${job.name}</strong> có tổng trọng số các tiêu chí đúng bằng 100%.
        </p>
        <button class="btn btn-primary" style="margin-top: 1rem;" onclick="openMatrixForJob('${job.id}')">
          Đi đến Cấu hình Khung Năng Lực 100%
        </button>
      </div>
    `;
    return;
  }

  // Calculate live weighted score
  let totalWeightedScore = 0;
  matrix.forEach(item => {
    const key = `${candidate.id}_${item.competencyId}`;
    const score = candidateScores[key] || 3; // Default 3/5
    totalWeightedScore += (score * (item.weight / 100));
  });

  container.innerHTML = `
    <div class="sheet-header-box">
      <div class="sheet-candidate-info">
        <h3>Ứng viên: ${escapeHtml(candidate.name)}</h3>
        <p>Vị trí: <strong>${escapeHtml(job.name)}</strong> • ${escapeHtml(candidate.exp)} • Email: ${escapeHtml(candidate.email)}</p>
      </div>
      <div class="weighted-score-card">
        <div class="score-title">Tổng điểm phỏng vấn (Weighted Score)</div>
        <div class="score-value" id="eval-total-score">${totalWeightedScore.toFixed(2)} <span style="font-size:1.1rem; color:#15803d;">/ 5.0</span></div>
      </div>
    </div>

    <div class="eval-criteria-body">
      ${matrix.map(item => {
        const comp = store.competencies.find(c => c.id === item.competencyId);
        const questions = store.questions.filter(q => q.competencyId === item.competencyId);
        const key = `${candidate.id}_${item.competencyId}`;
        const currentScore = candidateScores[key] || 3;

        return `
          <div class="eval-criteria-row">
            <div class="eval-criteria-head">
              <span class="eval-criteria-title">${comp ? escapeHtml(comp.name) : 'Tiêu chí'}</span>
              <span class="eval-weight-tag">Trọng số: ${item.weight}%</span>
            </div>

            <p style="font-size:0.82rem; color:var(--text-muted); margin-bottom: 0.75rem;">
              ${comp && comp.desc ? escapeHtml(comp.desc) : ''}
            </p>

            ${questions.length > 0 ? `
              <div class="eval-questions-box">
                <div class="eval-questions-title">💡 Câu hỏi gợi ý từ Ngân hàng câu hỏi:</div>
                ${questions.slice(0, 2).map(q => `
                  <div class="eval-question-item">
                    • <em>${escapeHtml(q.text)}</em>
                  </div>
                `).join('')}
              </div>
            ` : '<div style="font-size:0.78rem; color:var(--text-dim); margin-bottom: 0.75rem;">Chưa có câu hỏi phỏng vấn nào được gắn cho tiêu chí này.</div>'}

            <div class="eval-rating-row">
              <span style="font-size:0.85rem; font-weight:700;">Đánh giá của Interviewer:</span>
              <div class="rating-stars-group">
                ${[1, 2, 3, 4, 5].map(star => `
                  <button type="button" class="star-btn ${currentScore === star ? 'active' : ''}" 
                    onclick="rateCriteria('${candidate.id}', '${item.competencyId}', ${star})">
                    ${star} ★
                  </button>
                `).join('')}
              </div>
            </div>
          </div>
        `;
      }).join('')}
    </div>

    <div style="margin-top: 1.5rem; text-align: right;">
      <button class="btn btn-success btn-lg" onclick="showToast('Sprint 6', 'Đã lưu kết quả phỏng vấn vào hồ sơ ứng viên!', 'success')">
        ✓ Hoàn tất & Chốt Phiếu Đánh Giá
      </button>
    </div>
  `;
}

function rateCriteria(candId, compId, score) {
  candidateScores[`${candId}_${compId}`] = score;
  renderSprint6Evaluation();
}

// =============================================================================
// 10. MODAL HANDLING & CRUD ACTIONS
// =============================================================================

// Job Modal
const modalJob = document.getElementById('modal-job');
const formJob = document.getElementById('form-job');

document.getElementById('btn-add-job').addEventListener('click', () => {
  document.getElementById('modal-job-title').textContent = 'Thêm Chức danh Mới';
  formJob.reset();
  document.getElementById('job-id').value = '';
  document.getElementById('job-code').disabled = false;
  modalJob.classList.add('show');
});

document.getElementById('btn-close-job-modal').addEventListener('click', () => modalJob.classList.remove('show'));
document.getElementById('btn-cancel-job').addEventListener('click', () => modalJob.classList.remove('show'));

formJob.addEventListener('submit', (e) => {
  e.preventDefault();
  const id = document.getElementById('job-id').value;
  const code = document.getElementById('job-code').value.trim().toUpperCase();
  const name = document.getElementById('job-name').value.trim();
  const level = document.getElementById('job-level').value;
  const departmentId = document.getElementById('job-department').value;
  const salaryRangeId = document.getElementById('job-salary-range').value;
  const desc = document.getElementById('job-desc').value.trim();
  const status = document.getElementById('job-status').value;

  // Validation: Unique Code check
  const existing = store.jobs.find(j => j.code.toUpperCase() === code && j.id !== id);
  if (existing) {
    showToast('Trùng mã', `Mã chức danh "${code}" đã tồn tại trên hệ thống!`, 'error');
    return;
  }

  if (id) {
    // Edit
    const index = store.jobs.findIndex(j => j.id === id);
    if (index !== -1) {
      store.jobs[index] = { ...store.jobs[index], code, name, level, departmentId, salaryRangeId, desc, status };
      showToast('Thành công', 'Đã cập nhật thông tin chức danh.', 'success');
    }
  } else {
    // Create new
    const newId = 'job_' + Date.now();
    store.jobs.push({ id: newId, code, name, level, departmentId, salaryRangeId, desc, status });
    // Initialize empty matrix
    store.matrices[newId] = [];
    showToast('Thành công', 'Đã tạo mới chức danh chuẩn hóa.', 'success');
  }

  store.save();
  modalJob.classList.remove('show');
  renderJobTable();
  updateGlobalStats();
  initMatrixTab();
});

function editJob(jobId) {
  const job = store.jobs.find(j => j.id === jobId);
  if (!job) return;

  document.getElementById('modal-job-title').textContent = 'Chỉnh sửa Chức danh';
  document.getElementById('job-id').value = job.id;
  document.getElementById('job-code').value = job.code;
  document.getElementById('job-name').value = job.name;
  document.getElementById('job-level').value = job.level;
  document.getElementById('job-department').value = job.departmentId;
  document.getElementById('job-salary-range').value = job.salaryRangeId || '';
  document.getElementById('job-desc').value = job.desc || '';
  document.getElementById('job-status').value = job.status;

  modalJob.classList.add('show');
}

function deleteJob(jobId) {
  const job = store.jobs.find(j => j.id === jobId);
  if (!job) return;

  // Business Rule check: has active matrix or linked candidates?
  const matrix = store.matrices[jobId] || [];
  if (matrix.length > 0) {
    const confirmInactive = confirm(`Chức danh "${job.name}" đang liên kết với Khung năng lực tuyển dụng.\nTheo quy định nghiệp vụ, không được xóa cứng.\n\nBạn có muốn chuyển sang trạng thái NGƯNG HOẠT ĐỘNG (Inactive) không?`);
    if (confirmInactive) {
      job.status = 'INACTIVE';
      store.save();
      renderJobTable();
      showToast('Đã vô hiệu hóa', `Chức danh "${job.name}" đã chuyển sang Inactive.`, 'warning');
    }
    return;
  }

  if (confirm(`Bạn có chắc chắn muốn xóa vĩnh viễn chức danh "${job.name}"?`)) {
    store.jobs = store.jobs.filter(j => j.id !== jobId);
    delete store.matrices[jobId];
    store.save();
    renderJobTable();
    updateGlobalStats();
    initMatrixTab();
    showToast('Đã xóa', 'Chức danh đã được xóa khỏi hệ thống.', 'success');
  }
}

// Competency Modal
const modalComp = document.getElementById('modal-competency');
const formComp = document.getElementById('form-competency');

document.getElementById('btn-add-competency').addEventListener('click', () => {
  formComp.reset();
  modalComp.classList.add('show');
});

document.getElementById('btn-close-comp-modal').addEventListener('click', () => modalComp.classList.remove('show'));
document.getElementById('btn-cancel-comp').addEventListener('click', () => modalComp.classList.remove('show'));

formComp.addEventListener('submit', (e) => {
  e.preventDefault();
  const code = document.getElementById('comp-code').value.trim().toUpperCase();
  const category = document.getElementById('comp-category').value;
  const name = document.getElementById('comp-name').value.trim();
  const desc = document.getElementById('comp-desc').value.trim();

  // Unique code check
  if (store.competencies.some(c => c.code === code)) {
    showToast('Trùng mã', `Mã năng lực "${code}" đã tồn tại!`, 'error');
    return;
  }

  const newComp = {
    id: 'comp_' + Date.now(),
    code,
    category,
    name,
    desc
  };

  store.competencies.push(newComp);
  store.save();
  modalComp.classList.remove('show');
  renderCompetencyCards();
  renderAvailablePool();
  populateQuestionCompetencyFilter();
  updateGlobalStats();
  showToast('Thành công', 'Đã thêm tiêu chí năng lực vào thư viện dùng chung.', 'success');
});

// Question Modal
const modalQues = document.getElementById('modal-question');
const formQues = document.getElementById('form-question');

document.getElementById('btn-add-question').addEventListener('click', () => {
  formQues.reset();
  populateQuestionCompetencyFilter();
  modalQues.classList.add('show');
});

document.getElementById('btn-close-ques-modal').addEventListener('click', () => modalQues.classList.remove('show'));
document.getElementById('btn-cancel-ques').addEventListener('click', () => modalQues.classList.remove('show'));

formQues.addEventListener('submit', (e) => {
  e.preventDefault();
  const competencyId = document.getElementById('ques-competency').value;
  const difficulty = parseInt(document.getElementById('ques-difficulty').value, 10);
  const text = document.getElementById('ques-text').value.trim();
  const behavior = document.getElementById('ques-behavior').value.trim();

  store.questions.push({
    id: 'ques_' + Date.now(),
    competencyId,
    difficulty,
    text,
    behavior
  });

  store.save();
  modalQues.classList.remove('show');
  renderQuestions();
  updateGlobalStats();
  renderCompetencyCards();
  showToast('Thành công', 'Đã thêm câu hỏi phỏng vấn chuẩn hóa.', 'success');
});

function deleteQuestion(id) {
  if (confirm('Bạn có muốn xóa câu hỏi phỏng vấn này?')) {
    store.questions = store.questions.filter(q => q.id !== id);
    store.save();
    renderQuestions();
    updateGlobalStats();
    renderCompetencyCards();
    showToast('Đã xóa', 'Đã xóa câu hỏi khỏi ngân hàng.', 'success');
  }
}

// Reset Seed Data Button
document.getElementById('btn-seed-data').addEventListener('click', () => {
  if (confirm('Khôi phục toàn bộ dữ liệu mẫu ban đầu? Các thay đổi thử nghiệm sẽ được làm mới.')) {
    store.resetToDefault();
    initApp();
    showToast('Khởi tạo lại', 'Đã khôi phục dữ liệu mẫu chuẩn thành công!', 'success');
  }
});

// =============================================================================
// 11. NAVIGATION TABS LOGIC & UTILITIES
// =============================================================================

function switchTab(tabId) {
  document.querySelectorAll('.tab-btn').forEach(btn => {
    btn.classList.toggle('active', btn.getAttribute('data-tab') === tabId);
  });
  document.querySelectorAll('.tab-content').forEach(content => {
    content.classList.toggle('active', content.id === tabId);
  });

  if (tabId === 'tab-jobs') renderJobTable();
  if (tabId === 'tab-matrix') {
    if (currentMatrixJobId) loadMatrixForJob(currentMatrixJobId);
  }
  if (tabId === 'tab-competencies') renderCompetencyCards();
  if (tabId === 'tab-questions') renderQuestions();
  if (tabId === 'tab-evaluation-sprint6') renderSprint6Evaluation();
}

document.querySelectorAll('.tab-btn').forEach(btn => {
  btn.addEventListener('click', () => {
    switchTab(btn.getAttribute('data-tab'));
  });
});

document.getElementById('eval-candidate-select').addEventListener('change', renderSprint6Evaluation);

// Filter listeners
document.getElementById('search-job-input').addEventListener('input', renderJobTable);
document.getElementById('filter-job-level').addEventListener('change', renderJobTable);
document.getElementById('filter-job-dept').addEventListener('change', renderJobTable);
document.getElementById('filter-job-status').addEventListener('change', renderJobTable);

document.getElementById('search-question-input').addEventListener('input', renderQuestions);
document.getElementById('filter-question-competency').addEventListener('change', renderQuestions);
document.getElementById('filter-question-difficulty').addEventListener('change', renderQuestions);

function escapeHtml(str) {
  if (!str) return '';
  return str.replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#039;');
}

// =============================================================================
// THEME SWITCHER LOGIC (Xanh Da Trời / Xanh Biển Đậm / Xanh Lá)
// =============================================================================

function initTheme() {
  const savedTheme = localStorage.getItem('hrm_theme') || 'sky';
  applyTheme(savedTheme);

  document.querySelectorAll('.theme-chip-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      const theme = btn.getAttribute('data-theme-btn');
      applyTheme(theme);
      const themeNames = {
        sky: 'Xanh Da Trời (Sky Blue)',
        blue: 'Xanh Biển Đậm (Deep Ocean)',
        green: 'Xanh Lá (Emerald Green)'
      };
      showToast('Đổi giao diện', `Đã chuyển sang tông màu: ${themeNames[theme] || theme}`, 'success');
    });
  });
}

function applyTheme(theme) {
  document.documentElement.setAttribute('data-theme', theme);
  localStorage.setItem('hrm_theme', theme);

  document.querySelectorAll('.theme-chip-btn').forEach(btn => {
    btn.classList.toggle('active', btn.getAttribute('data-theme-btn') === theme);
  });
}

// =============================================================================
// 12. INITIALIZATION
// =============================================================================

function initApp() {
  initTheme();
  populateCommonDropdowns();
  populateQuestionCompetencyFilter();
  renderJobTable();
  initMatrixTab();
  renderCompetencyCards();
  renderQuestions();
  renderSprint6Evaluation();
  updateGlobalStats();
}

window.addEventListener('DOMContentLoaded', initApp);

