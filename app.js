/**
 * Competency Framework & Interview Evaluation System
 * Converted and elevated from Java CompetencyFrameworkSystem
 */

// 1. DATA REPOSITORIES (Khung năng lực dùng chung)
const CompetencyRepository = {
  "C01": {
    code: "C01",
    name: "Giao tiếp & Truyền đạt",
    category: "CORE",
    categoryName: "Năng lực Cốt lõi",
    levels: {
      1: "Trình bày rõ ràng ý kiến cá nhân trong phạm vi công việc hàng ngày.",
      2: "Lắng nghe chủ động, viết báo cáo và tài liệu mạch lạc, đúng trọng tâm.",
      3: "Thuyết phục được đối tác/đồng nghiệp trong các cuộc họp chuyên môn, xử lý xung đột nhẹ.",
      4: "Đàm phán hiệu quả các vấn đề phức tạp, giao tiếp chiến lược với các bên liên quan (Stakeholders).",
      5: "Bậc thầy truyền cảm hứng, xây dựng văn hóa giao tiếp cởi mở và minh bạch trong toàn tổ chức."
    }
  },
  "C02": {
    code: "C02",
    name: "Tư duy Giải quyết vấn đề",
    category: "CORE",
    categoryName: "Năng lực Cốt lõi",
    levels: {
      1: "Nhận biết sự cố cơ bản trong phạm vi cá nhân và báo cáo cấp trên kịp thời.",
      2: "Phân tích nguyên nhân gốc rễ đơn giản (5 Whys, Fishbone) và đề xuất phương án khắc phục.",
      3: "Xử lý sự cố phức tạp phát sinh trong vận hành, đưa ra giải pháp cân đối giữa chi phí & thời gian.",
      4: "Dự báo rủi ro tiềm ẩn ở cấp độ hệ thống và xây dựng phương án phòng ngừa chủ động.",
      5: "Định hình phương pháp luận tư duy phân tích chiến lược, giải quyết khủng hoảng cấp tập đoàn."
    }
  },
  "C03": {
    code: "C03",
    name: "Khả năng Tự học & Đổi mới",
    category: "CORE",
    categoryName: "Năng lực Cốt lõi",
    levels: {
      1: "Tiếp thu kiến thức mới khi được đào tạo và hướng dẫn trực tiếp.",
      2: "Chủ động nghiên cứu tài liệu kỹ thuật, cập nhật công nghệ mới vào công việc cá nhân.",
      3: "Áp dụng thành công các công nghệ/công cụ mới giúp tăng hiệu suất cho đội ngũ.",
      4: "Dẫn dắt các thử nghiệm công nghệ đột phá (PoC), thúc đẩy văn hóa R&D.",
      5: "Tiên phong nghiên cứu và bảo vệ luận điểm các xu hướng công nghệ tương lai đón đầu thị trường."
    }
  },
  "F01": {
    code: "F01",
    name: "Lập trình & Phát triển phần mềm",
    category: "FUNCTIONAL",
    categoryName: "Năng lực Chuyên môn",
    levels: {
      1: "Viết code đơn giản dưới sự hướng dẫn, hiểu cú pháp cơ bản và quy chuẩn coding convention.",
      2: "Tự phát triển feature nhỏ/vừa độc lập, viết Unit Test đạt độ phủ tốt, debug cơ bản.",
      3: "Thiết kế module chuẩn Clean Architecture, tối ưu hóa hiệu năng (performance), code review nghiêm ngặt.",
      4: "Thiết kế kiến trúc hệ thống (System Architecture), microservices, xử lý high concurrency và scale lớn.",
      5: "Principal/Chief Architect: Định hình nền tảng công nghệ toàn diện cho nhiều sản phẩm chiến lược."
    }
  },
  "F02": {
    code: "F02",
    name: "Thiết kế CSDL & Tối ưu dữ liệu",
    category: "FUNCTIONAL",
    categoryName: "Năng lực Chuyên môn",
    levels: {
      1: "Viết truy vấn SQL cơ bản (SELECT, JOIN), nắm cấu trúc bảng.",
      2: "Thiết kế ERD chuẩn hóa quan hệ 3NF, index cơ bản, viết stored procedure.",
      3: "Phân tích Execution Plan, tối ưu hóa câu lệnh chậm, áp dụng caching (Redis/Memcached).",
      4: "Thiết kế CSDL phân tán, Sharding, Replication, NoSQL (MongoDB/Elasticsearch/Cassandra).",
      5: "Kiến trúc Data Platform quy mô Big Data, Data Mesh, đảm bảo tính nhất quán cao (ACID/BASE)."
    }
  },
  "F03": {
    code: "F03",
    name: "DevOps & CI/CD Cloud Platform",
    category: "FUNCTIONAL",
    categoryName: "Năng lực Chuyên môn",
    levels: {
      1: "Sử dụng Git thành thạo (branching, pull request, merge conflict resolution).",
      2: "Đóng gói ứng dụng với Docker, hiểu quy trình build CI/CD cơ bản (GitHub Actions/GitLab CI).",
      3: "Thiết lập pipeline tự động hóa deploy, cấu hình Kubernetes (K8s), giám sát (Prometheus/Grafana).",
      4: "Thiết kế hạ tầng Cloud (AWS/Azure/GCP) chuẩn IaC (Terraform), bảo mật DevSecOps, multi-region.",
      5: "Định hình hạ tầng Private/Public/Hybrid Cloud quy mô lớn tối ưu chi phí và độ sẵn sàng 99.99%."
    }
  },
  "M01": {
    code: "M01",
    name: "Quản lý Dự án & Tiến độ",
    category: "MANAGEMENT",
    categoryName: "Năng lực Quản lý",
    levels: {
      1: "Ước lượng thời gian và quản lý tốt task cá nhân trên Jira/Trello.",
      2: "Theo dõi tiến độ module, áp dụng quy trình Scrum/Agile trong nhóm nhỏ.",
      3: "Quản lý toàn bộ vòng đời dự án (Sprint planning, Retrospective, Risk management, Scope control).",
      4: "Quản trị chương trình đa dự án (Program Management), tối ưu phân bổ tài nguyên và chi phí.",
      5: "Quản trị danh mục đầu tư công nghệ (Portfolio Management) gắn liền với chiến lược tăng trưởng doanh nghiệp."
    }
  },
  "M02": {
    code: "M02",
    name: "Lãnh đạo & Phát triển con người",
    category: "MANAGEMENT",
    categoryName: "Năng lực Quản lý",
    levels: {
      1: "Hỗ trợ, hướng dẫn đồng nghiệp mới làm quen với môi trường làm việc.",
      2: "Kèm cặp (Mentoring) nhân viên Junior, chia sẻ kinh nghiệm kỹ thuật định kỳ.",
      3: "Đánh giá hiệu suất (Performance Review), lộ trình phát triển (IDP), đào tạo đội ngũ.",
      4: "Xây dựng đội ngũ kế cận (Succession Planning), truyền lửa và duy trì tỷ lệ gắn kết nhân sự cao.",
      5: "Lãnh đạo chuyển đổi văn hóa, phát triển các thế hệ lãnh đạo cấp trung và cấp cao."
    }
  }
};

// 2. JOB TITLES & REQUIREMENTS MAPPING
const JobTitleRepository = {
  "DEV-SEN": {
    code: "DEV-SEN",
    title: "Lập trình viên (Senior)",
    rankLevel: "Senior",
    department: "Kỹ thuật Công nghệ",
    requirements: [
      { competencyCode: "C01", requiredLevel: 3, weight: 0.15 },
      { competencyCode: "C02", requiredLevel: 3, weight: 0.20 },
      { competencyCode: "F01", requiredLevel: 4, weight: 0.50 },
      { competencyCode: "M02", requiredLevel: 2, weight: 0.15 }
    ]
  },
  "LEAD-TECH": {
    code: "LEAD-TECH",
    title: "Kỹ sư Trưởng (Tech Lead)",
    rankLevel: "Lead",
    department: "Kỹ thuật Công nghệ",
    requirements: [
      { competencyCode: "C01", requiredLevel: 4, weight: 0.15 },
      { competencyCode: "C02", requiredLevel: 4, weight: 0.15 },
      { competencyCode: "F01", requiredLevel: 4, weight: 0.35 },
      { competencyCode: "F03", requiredLevel: 3, weight: 0.15 },
      { competencyCode: "M02", requiredLevel: 3, weight: 0.20 }
    ]
  },
  "DEV-JUN": {
    code: "DEV-JUN",
    title: "Lập trình viên (Junior)",
    rankLevel: "Junior",
    department: "Kỹ thuật Công nghệ",
    requirements: [
      { competencyCode: "C01", requiredLevel: 2, weight: 0.20 },
      { competencyCode: "C02", requiredLevel: 2, weight: 0.20 },
      { competencyCode: "C03", requiredLevel: 2, weight: 0.20 },
      { competencyCode: "F01", requiredLevel: 2, weight: 0.40 }
    ]
  },
  "MGR-ENG": {
    code: "MGR-ENG",
    title: "Quản lý Kỹ thuật (Engineering Manager)",
    rankLevel: "Manager",
    department: "Ban Công nghệ",
    requirements: [
      { competencyCode: "C01", requiredLevel: 4, weight: 0.20 },
      { competencyCode: "C02", requiredLevel: 4, weight: 0.20 },
      { competencyCode: "M01", requiredLevel: 4, weight: 0.25 },
      { competencyCode: "M02", requiredLevel: 4, weight: 0.25 },
      { competencyCode: "F01", requiredLevel: 3, weight: 0.10 }
    ]
  }
};

// 3. APPLICATION STATE
const AppState = {
  currentJobCode: "DEV-SEN",
  candidateName: "Nguyễn Văn A",
  interviewerName: "Trần Văn B (Tech Lead)",
  interviewDate: new Date().toISOString().split("T")[0],
  interviewRound: "Vòng 2 - Đánh giá Chuyên môn & Năng lực",
  
  // Evaluation Scores { [competencyCode]: scoreValue (1-5) }
  scores: {
    "C01": 3,
    "C02": 3,
    "F01": 4,
    "M02": 2
  },

  // Candidate observations per competency
  comments: {
    "C01": "Giao tiếp rõ ràng, tự tin, diễn đạt mạch lạc các vấn đề kỹ thuật.",
    "C02": "Tư duy phân tích nguyên nhân gốc rễ tốt, có dẫn chứng thực tế trong dự án cũ.",
    "F01": "Kiến thức chuyên sâu về Clean Architecture và xử lý concurrency rất chắc chắn.",
    "M02": "Có tinh thần hỗ trợ đồng nghiệp, từng mentor 2 bạn Intern thành công."
  },

  // Final conclusion
  finalDecision: "PASS", // 'PASS' | 'CONSIDER' | 'FAIL'
  generalNotes: "Ứng viên đáp ứng đầy đủ năng lực theo chuẩn khung chức danh Senior Developer. Tư duy phản biện và thái độ học hỏi xuất sắc.",
  suggestedSalary: "35.000.000 - 40.000.000 VNĐ",

  // Saved evaluation history
  history: []
};

// Quick comments suggestion bank
const SUGGESTIONS = {
  "C01": ["Trình bày mạch lạc, tự tin", "Biết lắng nghe và đặt câu hỏi", "Hơi rụt rè khi bảo vệ quan điểm", "Khả năng tiếng Anh tốt"],
  "C02": ["Phân tích có cấu trúc rõ ràng", "Đưa ra giải pháp thực tế", "Cần cải thiện tư duy dự báo rủi ro", "Phản xạ câu hỏi tình huống nhanh"],
  "F01": ["Nắm chắc kiến trúc phần mềm", "Tối ưu hiệu năng tốt", "Code sạch, tuân thủ SOLID", "Kinh nghiệm thực chiến cao"],
  "F02": ["Hiểu sâu về đánh chỉ mục (Index)", "Biết cách tối ưu query nặng", "Chưa có nhiều kinh nghiệm NoSQL"],
  "F03": ["Thành thạo Docker và CI/CD", "Kinh nghiệm triển khai AWS tốt", "Nắm vững quy trình Git Flow"],
  "M01": ["Kỹ năng lập kế hoạch tốt", "Quản lý rủi ro bài bản", "Chưa nhiều kinh nghiệm Scrum Master"],
  "M02": ["Tinh thần kèm cặp đồng đội nhiệt tình", "Có tiềm năng phát triển Lead", "Phong cách quản lý lắng nghe"]
};

// ==========================================
// CORE CALCULATIONS
// ==========================================

function getCurrentJob() {
  return JobTitleRepository[AppState.currentJobCode] || JobTitleRepository["DEV-SEN"];
}

function calculateWeightedScore() {
  const job = getCurrentJob();
  let totalScore = 0;
  let totalWeight = 0;

  job.requirements.forEach(req => {
    const score = AppState.scores[req.competencyCode] || 0;
    totalScore += score * req.weight;
    totalWeight += req.weight;
  });

  return {
    weightedScore: totalScore,
    totalWeight: totalWeight,
    maxPossibleScore: 5.0
  };
}

function getDecisionFromScore(score) {
  if (score >= 3.5) return "PASS";
  if (score >= 2.8) return "CONSIDER";
  return "FAIL";
}

// ==========================================
// UI RENDERING FUNCTIONS
// ==========================================

function initApp() {
  // Load history from localStorage
  const saved = localStorage.getItem("hr_eval_history");
  if (saved) {
    try { AppState.history = JSON.parse(saved); } catch (e) {}
  }

  renderJobSelectOptions();
  renderEvaluationForm();
  renderCompetencyMatrix();
  renderJobManagementTab();
  renderHistoryTab();
  setupSignaturePad();
  setupEventListeners();
}

// Render Job Dropdown
function renderJobSelectOptions() {
  const select = document.getElementById("select-job-title");
  if (!select) return;
  select.innerHTML = "";
  Object.values(JobTitleRepository).forEach(job => {
    const opt = document.createElement("option");
    opt.value = job.code;
    opt.textContent = `${job.title} [${job.rankLevel}] - Mã: ${job.code}`;
    if (job.code === AppState.currentJobCode) opt.selected = true;
    select.appendChild(opt);
  });
}

// Render the entire Evaluation Tab
function renderEvaluationForm() {
  const job = getCurrentJob();
  const { weightedScore, totalWeight } = calculateWeightedScore();

  // 1. Update Candidate Metadata in inputs
  const candInput = document.getElementById("input-candidate-name");
  const interviewerInput = document.getElementById("input-interviewer-name");
  const dateInput = document.getElementById("input-interview-date");
  const roundInput = document.getElementById("input-interview-round");

  if (candInput) candInput.value = AppState.candidateName;
  if (interviewerInput) interviewerInput.value = AppState.interviewerName;
  if (dateInput) dateInput.value = AppState.interviewDate;
  if (roundInput) roundInput.value = AppState.interviewRound;

  // 2. Update Score Gauge Banner
  const scoreValEl = document.getElementById("summary-score-value");
  const decisionBadgeEl = document.getElementById("summary-decision-badge");
  const weightNoticeEl = document.getElementById("summary-weight-notice");

  if (scoreValEl) scoreValEl.textContent = weightedScore.toFixed(2);
  
  if (decisionBadgeEl) {
    decisionBadgeEl.className = "score-decision-badge";
    if (AppState.finalDecision === "PASS") {
      decisionBadgeEl.classList.add("pass");
      decisionBadgeEl.innerHTML = `<i class="fa-solid fa-circle-check"></i> ĐẠT (Đề xuất tuyển dụng)`;
    } else if (AppState.finalDecision === "CONSIDER") {
      decisionBadgeEl.classList.add("consider");
      decisionBadgeEl.innerHTML = `<i class="fa-solid fa-triangle-exclamation"></i> CẦN CÂN NHẮC THÊM`;
    } else {
      decisionBadgeEl.classList.add("fail");
      decisionBadgeEl.innerHTML = `<i class="fa-solid fa-circle-xmark"></i> CHƯA ĐẠT YÊU CẦU`;
    }
  }

  if (weightNoticeEl) {
    const weightPct = Math.round(totalWeight * 100);
    weightNoticeEl.textContent = `Tổng trọng số kiểm tra: ${weightPct}% ${weightPct === 100 ? '✓ Chuẩn' : '⚠️ Cần bằng 100%'}`;
    weightNoticeEl.style.color = weightPct === 100 ? "#a7f3d0" : "#fde68a";
  }

  // 3. Render Matrix Table
  const tableBody = document.getElementById("eval-table-body");
  if (tableBody) {
    tableBody.innerHTML = "";
    job.requirements.forEach((req, idx) => {
      const comp = CompetencyRepository[req.competencyCode];
      if (!comp) return;

      const currentScore = AppState.scores[req.competencyCode] || req.requiredLevel;
      const weightedVal = (currentScore * req.weight).toFixed(2);
      const isMet = currentScore >= req.requiredLevel;

      const tr = document.createElement("tr");
      tr.innerHTML = `
        <td style="font-weight: 600; text-align: center;">${idx + 1}</td>
        <td><span class="badge ${getCategoryBadgeClass(comp.category)}">${comp.code}</span></td>
        <td>
          <div style="font-weight: 600; color: var(--text-main);">${comp.name}</div>
          <div style="font-size: 0.75rem; color: var(--text-muted);">${comp.categoryName}</div>
        </td>
        <td style="text-align: center; font-weight: 700;">${Math.round(req.weight * 100)}%</td>
        <td style="text-align: center;">
          <span class="badge-level">Cấp ${req.requiredLevel}</span>
        </td>
        <td>
          <div class="rating-picker">
            ${[1, 2, 3, 4, 5].map(lvl => `
              <button type="button" class="rating-btn ${currentScore === lvl ? 'active' : ''}" 
                onclick="setScore('${comp.code}', ${lvl})">
                ${lvl}
              </button>
            `).join('')}
          </div>
        </td>
        <td style="text-align: center; font-weight: 700; color: var(--primary);">
          ${weightedVal}
        </td>
        <td style="text-align: center;">
          ${isMet 
            ? `<span style="color: var(--success); font-weight: 700; font-size: 0.8rem;"><i class="fa-solid fa-check"></i> Đạt chuẩn</span>`
            : `<span style="color: var(--danger); font-weight: 700; font-size: 0.8rem;"><i class="fa-solid fa-minus"></i> Dưới chuẩn</span>`
          }
        </td>
      `;
      tableBody.appendChild(tr);
    });
  }

  // 4. Render Detailed Behavioral Assessment Cards
  const cardsContainer = document.getElementById("criteria-cards-container");
  if (cardsContainer) {
    cardsContainer.innerHTML = "";
    job.requirements.forEach((req, idx) => {
      const comp = CompetencyRepository[req.competencyCode];
      if (!comp) return;

      const currentScore = AppState.scores[req.competencyCode] || req.requiredLevel;
      const targetDesc = comp.levels[req.requiredLevel] || "Chưa định nghĩa";
      const userComment = AppState.comments[req.competencyCode] || "";
      const suggestions = SUGGESTIONS[comp.code] || [];

      const card = document.createElement("div");
      card.className = "criterion-card";
      card.innerHTML = `
        <div class="criterion-header">
          <div class="criterion-title-wrap">
            <span style="font-size: 1.1rem; font-weight: 800; color: var(--primary);">${idx + 1}.</span>
            <div>
              <div class="criterion-name">${comp.name}</div>
              <div style="font-size: 0.75rem; color: var(--text-secondary); display: flex; gap: 8px; align-items: center; margin-top: 2px;">
                <span class="badge ${getCategoryBadgeClass(comp.category)}">${comp.code}</span>
                <span>Trọng số: <strong>${Math.round(req.weight * 100)}%</strong></span>
                <span>Yêu cầu vị trí: <strong>Cấp ${req.requiredLevel}/5</strong></span>
              </div>
            </div>
          </div>
          
          <div style="display: flex; align-items: center; gap: 10px;">
            <span style="font-size: 0.8rem; font-weight: 600; color: var(--text-secondary);">Điểm đánh giá:</span>
            <div class="rating-picker">
              ${[1, 2, 3, 4, 5].map(lvl => `
                <button type="button" class="rating-btn ${currentScore === lvl ? 'active' : ''}" 
                  onclick="setScore('${comp.code}', ${lvl})">
                  ${lvl}
                </button>
              `).join('')}
            </div>
          </div>
        </div>

        <div class="behavior-box">
          <span class="behavior-label"><i class="fa-solid fa-bullseye"></i> Hành vi chuẩn Cấp ${req.requiredLevel}:</span>
          <span>${targetDesc}</span>
        </div>

        <div class="form-group" style="margin-bottom: 6px;">
          <label class="form-label" style="display: flex; justify-content: space-between;">
            <span><i class="fa-regular fa-comment-dots"></i> Nhận xét & Bằng chứng quan sát:</span>
            <span style="font-size: 0.75rem; font-weight: 400; color: var(--text-muted);">Gợi ý nhấp để thêm nhanh</span>
          </label>
          <textarea class="form-textarea" id="comment-${comp.code}" 
            placeholder="Ghi lại các câu trả lời, tình huống hoặc dẫn chứng của ứng viên..." 
            onchange="updateComment('${comp.code}', this.value)">${userComment}</textarea>
        </div>

        ${suggestions.length > 0 ? `
          <div class="quick-tags">
            ${suggestions.map(s => `
              <span class="quick-tag" onclick="appendComment('${comp.code}', '${s}')">+ ${s}</span>
            `).join('')}
          </div>
        ` : ''}
      `;
      cardsContainer.appendChild(card);
    });
  }

  // 5. Update Decision Cards Active State
  document.querySelectorAll(".decision-card").forEach(card => {
    const decision = card.dataset.decision;
    if (decision === AppState.finalDecision) {
      card.classList.add("active");
    } else {
      card.classList.remove("active");
    }
  });

  // 6. Update general comments & salary
  const generalNotesEl = document.getElementById("input-general-notes");
  const salaryEl = document.getElementById("input-suggested-salary");
  if (generalNotesEl && !generalNotesEl.matches(':focus')) generalNotesEl.value = AppState.generalNotes;
  if (salaryEl && !salaryEl.matches(':focus')) salaryEl.value = AppState.suggestedSalary;

  // 7. Update Spider/Radar Chart
  renderRadarChart();
}

function getCategoryBadgeClass(category) {
  switch (category) {
    case "CORE": return "badge-core";
    case "FUNCTIONAL": return "badge-func";
    case "MANAGEMENT": return "badge-mgmt";
    default: return "badge-core";
  }
}

// ==========================================
// INTERACTIVE SCORING & EVENT HANDLERS
// ==========================================

window.setScore = function(compCode, score) {
  AppState.scores[compCode] = score;
  
  // Recalculate auto-decision if user hasn't locked it
  const { weightedScore } = calculateWeightedScore();
  AppState.finalDecision = getDecisionFromScore(weightedScore);
  
  renderEvaluationForm();
};

window.updateComment = function(compCode, text) {
  AppState.comments[compCode] = text;
};

window.appendComment = function(compCode, text) {
  const existing = AppState.comments[compCode] || "";
  const updated = existing ? `${existing}. ${text}` : text;
  AppState.comments[compCode] = updated;
  const textarea = document.getElementById(`comment-${compCode}`);
  if (textarea) textarea.value = updated;
};

window.setDecision = function(decision) {
  AppState.finalDecision = decision;
  renderEvaluationForm();
};

window.switchJobTitle = function(jobCode) {
  AppState.currentJobCode = jobCode;
  const job = JobTitleRepository[jobCode];
  
  // Reset scores to target level for convenient testing
  if (job) {
    job.requirements.forEach(req => {
      AppState.scores[req.competencyCode] = req.requiredLevel;
      if (!AppState.comments[req.competencyCode]) {
        AppState.comments[req.competencyCode] = "";
      }
    });
  }

  const { weightedScore } = calculateWeightedScore();
  AppState.finalDecision = getDecisionFromScore(weightedScore);
  renderEvaluationForm();
  showToast(`Đã chuyển sang khung vị trí: ${job.title}`, "success");
};

// ==========================================
// RADAR CHART (SVG Native Rendering)
// ==========================================

function renderRadarChart() {
  const container = document.getElementById("radar-chart-container");
  if (!container) return;

  const job = getCurrentJob();
  const reqs = job.requirements;
  const numAxes = reqs.length;

  if (numAxes < 3) {
    container.innerHTML = `<div style="text-align: center; padding: 40px; color: var(--text-muted);">Cần ít nhất 3 tiêu chí để vẽ biểu đồ Radar.</div>`;
    return;
  }

  const size = 320;
  const center = size / 2;
  const maxRadius = 110;
  const maxLevel = 5;

  const angleStep = (Math.PI * 2) / numAxes;

  let svgHtml = `<svg viewBox="0 0 ${size} ${size}" width="100%" height="100%" style="overflow: visible;">`;

  // Draw concentric polygon rings for level 1 to 5
  for (let lvl = 1; lvl <= maxLevel; lvl++) {
    const radius = (maxRadius / maxLevel) * lvl;
    let points = [];
    for (let i = 0; i < numAxes; i++) {
      const angle = i * angleStep - Math.PI / 2;
      const x = center + radius * Math.cos(angle);
      const y = center + radius * Math.sin(angle);
      points.push(`${x},${y}`);
    }
    svgHtml += `
      <polygon points="${points.join(' ')}" fill="none" stroke="#e2e8f0" stroke-width="1.2" stroke-dasharray="${lvl === maxLevel ? 'none' : '3,3'}" />
      <text x="${center + 4}" y="${center - radius + 11}" fill="#94a3b8" font-size="9" font-weight="600">${lvl}</text>
    `;
  }

  // Draw axis lines and labels
  let targetPoints = [];
  let actualPoints = [];

  reqs.forEach((req, i) => {
    const angle = i * angleStep - Math.PI / 2;
    const endX = center + maxRadius * Math.cos(angle);
    const endY = center + maxRadius * Math.sin(angle);

    // Axis line
    svgHtml += `<line x1="${center}" y1="${center}" x2="${endX}" y2="${endY}" stroke="#cbd5e1" stroke-width="1.2" />`;

    // Coordinates for Target Level
    const targetRadius = (maxRadius / maxLevel) * req.requiredLevel;
    const tx = center + targetRadius * Math.cos(angle);
    const ty = center + targetRadius * Math.sin(angle);
    targetPoints.push(`${tx},${ty}`);

    // Coordinates for Actual Score
    const actualScore = AppState.scores[req.competencyCode] || req.requiredLevel;
    const actualRadius = (maxRadius / maxLevel) * actualScore;
    const ax = center + actualRadius * Math.cos(angle);
    const ay = center + actualRadius * Math.sin(angle);
    actualPoints.push(`${ax},${ay}`);

    // Label positioning
    const labelRadius = maxRadius + 24;
    const lx = center + labelRadius * Math.cos(angle);
    const ly = center + labelRadius * Math.sin(angle);
    const comp = CompetencyRepository[req.competencyCode] || { name: req.competencyCode };
    const shortName = comp.name.length > 15 ? comp.name.substring(0, 13) + '...' : comp.name;

    svgHtml += `
      <text x="${lx}" y="${ly}" text-anchor="middle" dominant-baseline="middle" 
        fill="#1e293b" font-size="10" font-weight="700">
        ${req.competencyCode}
      </text>
      <text x="${lx}" y="${ly + 12}" text-anchor="middle" dominant-baseline="middle" 
        fill="#64748b" font-size="8.5">
        ${shortName}
      </text>
    `;
  });

  // Target Polygon (Blue/Indigo dotted outline)
  svgHtml += `
    <polygon points="${targetPoints.join(' ')}" 
      fill="rgba(14, 165, 233, 0.12)" 
      stroke="#0ea5e9" 
      stroke-width="2" 
      stroke-dasharray="4,4" />
  `;

  // Actual Polygon (Vibrant Indigo with glow)
  svgHtml += `
    <polygon points="${actualPoints.join(' ')}" 
      fill="rgba(79, 70, 229, 0.28)" 
      stroke="#4f46e5" 
      stroke-width="2.5" />
  `;

  // Draw markers for actual points
  actualPoints.forEach(p => {
    const [px, py] = p.split(',');
    svgHtml += `<circle cx="${px}" cy="${py}" r="4" fill="#4f46e5" stroke="#ffffff" stroke-width="2" />`;
  });

  svgHtml += `</svg>`;
  container.innerHTML = svgHtml;
}

// ==========================================
// TAB 2: COMPETENCY MATRIX DICTIONARY
// ==========================================

function renderCompetencyMatrix(filterCategory = 'ALL') {
  const container = document.getElementById("matrix-table-body");
  if (!container) return;

  container.innerHTML = "";
  const list = Object.values(CompetencyRepository);

  const filtered = filterCategory === 'ALL' 
    ? list 
    : list.filter(c => c.category === filterCategory);

  filtered.forEach(comp => {
    const tr = document.createElement("tr");
    tr.innerHTML = `
      <td style="font-weight: 700;">
        <span class="badge ${getCategoryBadgeClass(comp.category)}">${comp.code}</span>
        <div style="margin-top: 4px; font-weight: 700; color: var(--text-main);">${comp.name}</div>
        <div style="font-size: 0.75rem; color: var(--text-muted);">${comp.categoryName}</div>
      </td>
      <td><div style="font-size: 0.8rem; line-height: 1.4;">${comp.levels[1] || '-'}</div></td>
      <td><div style="font-size: 0.8rem; line-height: 1.4;">${comp.levels[2] || '-'}</div></td>
      <td><div style="font-size: 0.8rem; line-height: 1.4;">${comp.levels[3] || '-'}</div></td>
      <td><div style="font-size: 0.8rem; line-height: 1.4;">${comp.levels[4] || '-'}</div></td>
      <td><div style="font-size: 0.8rem; line-height: 1.4;">${comp.levels[5] || '-'}</div></td>
    `;
    container.appendChild(tr);
  });
}

window.filterMatrixCategory = function(cat, btn) {
  document.querySelectorAll(".category-filter-btn").forEach(b => b.classList.remove("active"));
  if (btn) btn.classList.add("active");
  renderCompetencyMatrix(cat);
};

// ==========================================
// TAB 3: JOB ROLES & REQUIREMENT MAPPINGS
// ==========================================

function renderJobManagementTab() {
  const container = document.getElementById("jobs-cards-grid");
  if (!container) return;

  container.innerHTML = "";
  Object.values(JobTitleRepository).forEach(job => {
    const card = document.createElement("div");
    card.className = "card";
    card.style.marginBottom = "16px";
    
    let totalWeight = 0;
    const reqsHtml = job.requirements.map(r => {
      const comp = CompetencyRepository[r.competencyCode] || { name: r.competencyCode };
      totalWeight += r.weight;
      return `
        <div style="display: flex; justify-content: space-between; align-items: center; padding: 6px 0; border-bottom: 1px dashed #f1f5f9; font-size: 0.85rem;">
          <div style="display: flex; gap: 8px; align-items: center;">
            <span class="badge ${getCategoryBadgeClass(comp.category)}">${r.competencyCode}</span>
            <span style="font-weight: 500;">${comp.name}</span>
          </div>
          <div style="display: flex; gap: 12px; align-items: center;">
            <span class="badge-level">Cấp ${r.requiredLevel}</span>
            <span style="font-weight: 700; color: var(--primary); min-width: 40px; text-align: right;">${Math.round(r.weight * 100)}%</span>
          </div>
        </div>
      `;
    }).join("");

    card.innerHTML = `
      <div class="card-header" style="margin-bottom: 12px;">
        <div>
          <div style="font-size: 1.1rem; font-weight: 700; color: var(--text-main);">${job.title}</div>
          <div style="font-size: 0.8rem; color: var(--text-secondary); margin-top: 2px;">
            Mã: <strong>${job.code}</strong> | Cấp bậc: <strong>${job.rankLevel}</strong> | Khối: <strong>${job.department}</strong>
          </div>
        </div>
        <button class="btn btn-outline-primary btn-sm" onclick="switchJobAndEvaluate('${job.code}')">
          <i class="fa-solid fa-file-pen"></i> Dùng mẫu này
        </button>
      </div>

      <div style="margin-top: 8px;">
        <div style="font-size: 0.8rem; font-weight: 700; text-transform: uppercase; color: var(--text-muted); margin-bottom: 6px;">
          Bộ tiêu chí & Trọng số (${Math.round(totalWeight * 100)}%):
        </div>
        ${reqsHtml}
      </div>
    `;
    container.appendChild(card);
  });
}

window.switchJobAndEvaluate = function(jobCode) {
  switchJobTitle(jobCode);
  switchTab('tab-eval');
};

// ==========================================
// TAB 4: EVALUATION HISTORY
// ==========================================

function renderHistoryTab() {
  const container = document.getElementById("history-list-container");
  if (!container) return;

  if (AppState.history.length === 0) {
    container.innerHTML = `
      <div style="text-align: center; padding: 60px 20px; color: var(--text-muted);">
        <i class="fa-solid fa-folder-open" style="font-size: 3rem; margin-bottom: 12px; color: #cbd5e1;"></i>
        <div style="font-size: 1rem; font-weight: 600;">Chưa có phiếu đánh giá nào được lưu</div>
        <div style="font-size: 0.85rem; margin-top: 4px;">Hoàn thành đánh giá ở tab "Phiếu Phỏng Vấn" và bấm "Lưu Phiếu" để lưu trữ vào đây.</div>
      </div>
    `;
    return;
  }

  container.innerHTML = "";
  AppState.history.forEach((item, index) => {
    const card = document.createElement("div");
    card.className = "card";
    card.style.marginBottom = "14px";
    card.innerHTML = `
      <div style="display: flex; justify-content: space-between; align-items: center; flex-wrap: gap; gap: 12px;">
        <div>
          <div style="display: flex; align-items: center; gap: 10px;">
            <h4 style="font-size: 1.05rem; font-weight: 700;">${item.candidateName}</h4>
            <span class="score-decision-badge ${item.finalDecision.toLowerCase()}" style="padding: 2px 10px; font-size: 0.75rem;">
              ${item.finalDecision === 'PASS' ? 'ĐẠT' : (item.finalDecision === 'CONSIDER' ? 'CÂN NHẮC' : 'KHÔNG ĐẠT')}
            </span>
          </div>
          <div style="font-size: 0.85rem; color: var(--text-secondary); margin-top: 4px;">
            Vị trí: <strong>${item.jobTitle}</strong> | Người PV: ${item.interviewerName} | Ngày: ${item.interviewDate}
          </div>
        </div>

        <div style="display: flex; align-items: center; gap: 16px;">
          <div style="text-align: right;">
            <div style="font-size: 1.4rem; font-weight: 800; color: var(--primary);">${item.weightedScore.toFixed(2)}</div>
            <div style="font-size: 0.75rem; color: var(--text-muted);">/ 5.0 Điểm</div>
          </div>
          <button class="btn btn-secondary btn-sm" onclick="loadSavedEvaluation(${index})">
            <i class="fa-solid fa-arrow-rotate-left"></i> Xem lại
          </button>
          <button class="btn btn-secondary btn-sm" style="color: var(--danger);" onclick="deleteSavedEvaluation(${index})">
            <i class="fa-solid fa-trash"></i>
          </button>
        </div>
      </div>
    `;
    container.appendChild(card);
  });
}

window.saveCurrentEvaluation = function() {
  const job = getCurrentJob();
  const { weightedScore } = calculateWeightedScore();

  const record = {
    id: Date.now(),
    candidateName: AppState.candidateName || "Chưa đặt tên",
    jobCode: AppState.currentJobCode,
    jobTitle: job.title,
    interviewerName: AppState.interviewerName,
    interviewDate: AppState.interviewDate,
    interviewRound: AppState.interviewRound,
    scores: { ...AppState.scores },
    comments: { ...AppState.comments },
    finalDecision: AppState.finalDecision,
    generalNotes: AppState.generalNotes,
    suggestedSalary: AppState.suggestedSalary,
    weightedScore: weightedScore,
    savedAt: new Date().toLocaleString("vi-VN")
  };

  AppState.history.unshift(record);
  localStorage.setItem("hr_eval_history", JSON.stringify(AppState.history));
  renderHistoryTab();
  showToast("Đã lưu phiếu phỏng vấn vào lịch sử!", "success");
};

window.loadSavedEvaluation = function(index) {
  const item = AppState.history[index];
  if (!item) return;

  AppState.candidateName = item.candidateName;
  AppState.currentJobCode = item.jobCode;
  AppState.interviewerName = item.interviewerName;
  AppState.interviewDate = item.interviewDate;
  AppState.interviewRound = item.interviewRound || "Vòng 2 - Đánh giá Chuyên môn";
  AppState.scores = { ...item.scores };
  AppState.comments = { ...item.comments };
  AppState.finalDecision = item.finalDecision;
  AppState.generalNotes = item.generalNotes;
  AppState.suggestedSalary = item.suggestedSalary || "";

  renderJobSelectOptions();
  renderEvaluationForm();
  switchTab('tab-eval');
  showToast(`Đã tải dữ liệu của: ${item.candidateName}`, "success");
};

window.deleteSavedEvaluation = function(index) {
  if (confirm("Bạn có chắc chắn muốn xóa phiếu đánh giá này?")) {
    AppState.history.splice(index, 1);
    localStorage.setItem("hr_eval_history", JSON.stringify(AppState.history));
    renderHistoryTab();
    showToast("Đã xóa phiếu đánh giá", "info");
  }
};

// ==========================================
// TEXT FORMAT EXPORT (Matches Java Console Output)
// ==========================================

window.generateJavaStyleReportText = function() {
  const job = getCurrentJob();
  const { weightedScore, totalWeight } = calculateWeightedScore();
  const dateStr = AppState.interviewDate || "21/10/2026";

  let out = "";
  out += "================================================================================\n";
  out += "                    PHIẾU ĐÁNH GIÁ PHỎNG VẤN TUYỂN DỤNG\n";
  out += "================================================================================\n";
  out += `Ứng viên         : ${AppState.candidateName}\n`;
  out += `Vị trí ứng tuyển : ${job.title} (Mã: ${job.code})\n`;
  out += `Cấp bậc          : ${job.rankLevel}\n`;
  out += `Người phỏng vấn  : ${AppState.interviewerName}\n`;
  out += `Ngày phỏng vấn   : ${dateStr}\n`;
  out += `Vòng phỏng vấn   : ${AppState.interviewRound}\n`;
  out += "--------------------------------------------------------------------------------\n";
  out += "STT  | Mã    | Tên Năng Lực                   | Trọng số | Yêu cầu  | Điểm (1-5)\n";
  out += "--------------------------------------------------------------------------------\n";

  let questionsSection = [];

  job.requirements.forEach((req, idx) => {
    const comp = CompetencyRepository[req.competencyCode];
    if (!comp) return;

    const weightPct = Math.round(req.weight * 100);
    const score = AppState.scores[req.competencyCode] || req.requiredLevel;
    const targetDesc = comp.levels[req.requiredLevel] || "";
    const comment = AppState.comments[req.competencyCode] || "....................................................................";

    const sttStr = String(idx + 1).padEnd(4);
    const codeStr = comp.code.padEnd(5);
    const nameStr = comp.name.padEnd(30);
    const weightStr = (weightPct + "%").padStart(6);
    const reqStr = ("Cấp " + req.requiredLevel).padEnd(6);
    const scoreStr = `[ ${score} ]`;

    out += `${sttStr} | ${codeStr} | ${nameStr} | ${weightStr} | ${reqStr} | ${scoreStr}\n`;

    let q = `\n${idx + 1}. ${comp.name} (Mã: ${comp.code} - Trọng số: ${weightPct}%):\n`;
    q += `   * Hành vi chuẩn cấp ${req.requiredLevel}: ${targetDesc}\n`;
    q += `   * Nhận xét ứng viên: ${comment}\n`;
    q += `   * Điểm đánh giá (1 - 5): [ ${score} ]`;
    questionsSection.push(q);
  });

  out += "--------------------------------------------------------------------------------\n";
  out += `Tổng trọng số kiểm tra: ${Math.round(totalWeight * 100)}% (Yêu cầu bằng 100%)\n`;
  out += "================================================================================\n\n";
  out += "CHI TIẾT ĐÁNH GIÁ & GỢI Ý HÀNH VI CẦN QUAN SÁT:\n";
  out += questionsSection.join("\n") + "\n\n";
  out += "================================================================================\n";
  out += "KẾT LUẬN PHỎNG VẤN:\n";
  out += `[${AppState.finalDecision === 'PASS' ? 'X' : ' '}] ĐẠT (Đề xuất nhận việc)    `;
  out += `[${AppState.finalDecision === 'CONSIDER' ? 'X' : ' '}] CẦN CÂN NHẮC    `;
  out += `[${AppState.finalDecision === 'FAIL' ? 'X' : ' '}] KHÔNG ĐẠT\n`;
  out += `Tổng điểm quy đổi theo trọng số: ${weightedScore.toFixed(2)} / 5.0\n`;
  out += `Mức lương đề xuất: ${AppState.suggestedSalary || "Chưa xác định"}\n`;
  out += `Ghi chú chung: ${AppState.generalNotes || "...................................................................................."}\n`;
  out += `Chữ ký người phỏng vấn: ${AppState.interviewerName}\n`;
  out += "================================================================================\n";

  return out;
};

window.copyReportToClipboard = function() {
  const text = generateJavaStyleReportText();
  navigator.clipboard.writeText(text).then(() => {
    showToast("Đã sao chép báo cáo phỏng vấn vào Clipboard!", "success");
  }).catch(() => {
    // fallback
    const textarea = document.createElement("textarea");
    textarea.value = text;
    document.body.appendChild(textarea);
    textarea.select();
    document.execCommand("copy");
    document.body.removeChild(textarea);
    showToast("Đã sao chép báo cáo vào Clipboard!", "success");
  });
};

// ==========================================
// DIGITAL SIGNATURE CANVAS
// ==========================================

let signatureCanvas, sigCtx;
let isDrawing = false;

function setupSignaturePad() {
  signatureCanvas = document.getElementById("signature-pad");
  if (!signatureCanvas) return;

  sigCtx = signatureCanvas.getContext("2d");
  
  // High DPI support
  const rect = signatureCanvas.getBoundingClientRect();
  signatureCanvas.width = rect.width * 2;
  signatureCanvas.height = rect.height * 2;
  sigCtx.scale(2, 2);
  sigCtx.lineWidth = 2.5;
  sigCtx.lineCap = "round";
  sigCtx.lineJoin = "round";
  sigCtx.strokeStyle = "#1e293b";

  function getPos(e) {
    const r = signatureCanvas.getBoundingClientRect();
    const clientX = e.touches ? e.touches[0].clientX : e.clientX;
    const clientY = e.touches ? e.touches[0].clientY : e.clientY;
    return {
      x: clientX - r.left,
      y: clientY - r.top
    };
  }

  function start(e) {
    isDrawing = true;
    const pos = getPos(e);
    sigCtx.beginPath();
    sigCtx.moveTo(pos.x, pos.y);
    e.preventDefault();
  }

  function draw(e) {
    if (!isDrawing) return;
    const pos = getPos(e);
    sigCtx.lineTo(pos.x, pos.y);
    sigCtx.stroke();
    e.preventDefault();
  }

  function stop() {
    isDrawing = false;
  }

  signatureCanvas.addEventListener("mousedown", start);
  signatureCanvas.addEventListener("mousemove", draw);
  window.addEventListener("mouseup", stop);

  signatureCanvas.addEventListener("touchstart", start, { passive: false });
  signatureCanvas.addEventListener("touchmove", draw, { passive: false });
  signatureCanvas.addEventListener("touchend", stop);
}

window.clearSignature = function() {
  if (sigCtx && signatureCanvas) {
    sigCtx.clearRect(0, 0, signatureCanvas.width, signatureCanvas.height);
  }
};

// ==========================================
// NAVIGATION & TOAST UTILITIES
// ==========================================

window.switchTab = function(tabId) {
  document.querySelectorAll(".tab-content").forEach(el => el.classList.remove("active"));
  document.querySelectorAll(".nav-tab-btn").forEach(el => el.classList.remove("active"));

  const target = document.getElementById(tabId);
  if (target) target.classList.add("active");

  const btn = document.querySelector(`[data-tab="${tabId}"]`);
  if (btn) btn.classList.add("active");

  if (tabId === 'tab-eval') {
    renderEvaluationForm();
  }
};

function showToast(message, type = "info") {
  const container = document.getElementById("toast-container");
  if (!container) return;

  const toast = document.createElement("div");
  toast.className = `toast ${type === 'success' ? 'toast-success' : ''}`;
  toast.innerHTML = `<i class="fa-solid ${type === 'success' ? 'fa-check-circle' : 'fa-info-circle'}"></i> <span>${message}</span>`;
  container.appendChild(toast);

  setTimeout(() => {
    toast.style.opacity = '0';
    toast.style.transform = 'translateY(10px)';
    toast.style.transition = 'all 0.3s ease';
    setTimeout(() => toast.remove(), 300);
  }, 3200);
}

// ==========================================
// SAMPLE DATA PRESETS
// ==========================================

window.loadSampleData = function(type = 'senior-pass') {
  if (type === 'senior-pass') {
    AppState.candidateName = "Nguyễn Văn A";
    AppState.currentJobCode = "DEV-SEN";
    AppState.interviewerName = "Trần Văn B (Tech Lead)";
    AppState.interviewDate = "21/10/2026";
    AppState.interviewRound = "Vòng 2 - Đánh giá Năng lực Kỹ thuật";
    AppState.scores = { "C01": 3, "C02": 3, "F01": 4, "M02": 2 };
    AppState.comments = {
      "C01": "Giao tiếp rõ ràng, tự tin, diễn đạt mạch lạc các vấn đề kỹ thuật.",
      "C02": "Phân tích nguyên nhân gốc rễ tốt, có tư duy hệ thống cao.",
      "F01": "Thiết kế kiến trúc chuẩn Clean Architecture, tối ưu hóa database ấn tượng.",
      "M02": "Có tinh thần mentoring tốt, truyền cảm hứng tích cực cho các bạn Junior."
    };
    AppState.finalDecision = "PASS";
    AppState.generalNotes = "Ứng viên đáp ứng xuất sắc các tiêu chí của vị trí Senior. Đề xuất onboard ngay trong tháng.";
    AppState.suggestedSalary = "38.000.000 VNĐ";
  } else if (type === 'junior-consider') {
    AppState.candidateName = "Lê Hoàng C";
    AppState.currentJobCode = "DEV-JUN";
    AppState.interviewerName = "Nguyễn Văn A (Senior Dev)";
    AppState.interviewDate = "21/10/2026";
    AppState.interviewRound = "Vòng 1 - Phỏng vấn Junior";
    AppState.scores = { "C01": 2, "C02": 2, "C03": 3, "F01": 2 };
    AppState.comments = {
      "C01": "Còn hơi run khi phỏng vấn, nhưng trả lời trung thực.",
      "C02": "Tư duy giải thuật logic, cần rèn luyện thêm về kỹ năng debug thực tế.",
      "C03": "Chủ động tự học tốt, tự làm nhiều pet project với React & Node.js.",
      "F01": "Nắm vững JavaScript căn bản, đã biết viết Unit test cơ bản."
    };
    AppState.finalDecision = "CONSIDER";
    AppState.generalNotes = "Ứng viên có thái độ học hỏi và tố chất tốt, tuy nhiên kinh nghiệm dự án còn mỏng. Đề xuất phỏng vấn thêm vòng 2 với Engineering Manager.";
    AppState.suggestedSalary = "12.000.000 - 15.000.000 VNĐ";
  }

  renderJobSelectOptions();
  renderEvaluationForm();
  showToast("Đã nạp dữ liệu ứng viên mẫu!", "success");
};

// ==========================================
// EVENT LISTENERS BINDING
// ==========================================

function setupEventListeners() {
  // Inputs binding
  const candInput = document.getElementById("input-candidate-name");
  if (candInput) {
    candInput.addEventListener("input", (e) => {
      AppState.candidateName = e.target.value;
    });
  }

  const interviewerInput = document.getElementById("input-interviewer-name");
  if (interviewerInput) {
    interviewerInput.addEventListener("input", (e) => {
      AppState.interviewerName = e.target.value;
    });
  }

  const dateInput = document.getElementById("input-interview-date");
  if (dateInput) {
    dateInput.addEventListener("change", (e) => {
      AppState.interviewDate = e.target.value;
    });
  }

  const roundInput = document.getElementById("input-interview-round");
  if (roundInput) {
    roundInput.addEventListener("change", (e) => {
      AppState.interviewRound = e.target.value;
    });
  }

  const generalNotesEl = document.getElementById("input-general-notes");
  if (generalNotesEl) {
    generalNotesEl.addEventListener("input", (e) => {
      AppState.generalNotes = e.target.value;
    });
  }

  const salaryEl = document.getElementById("input-suggested-salary");
  if (salaryEl) {
    salaryEl.addEventListener("input", (e) => {
      AppState.suggestedSalary = e.target.value;
    });
  }

  const jobSelect = document.getElementById("select-job-title");
  if (jobSelect) {
    jobSelect.addEventListener("change", (e) => {
      switchJobTitle(e.target.value);
    });
  }
}

// Bootstrap on DOM ready
document.addEventListener("DOMContentLoaded", initApp);
