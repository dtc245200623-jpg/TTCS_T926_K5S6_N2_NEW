document.addEventListener('DOMContentLoaded', () => {
  const api = `${location.origin}/api/public`;
  const jobList = document.getElementById('jobList');
  const panel = document.getElementById('applicationPanel');
  const form = document.getElementById('applicationForm');
  const result = document.getElementById('trackingResult');
  const labels = { NEW:'Đã nhận hồ sơ', SCREENING:'Đang sàng lọc', INTERVIEW:'Đang phỏng vấn', OFFER:'Đã gửi đề nghị', HIRED:'Đã nhận việc', REJECTED:'Không tiếp tục', WITHDRAWN:'Đã rút hồ sơ' };

  async function read(response) {
    const payload = await response.json().catch(() => ({}));
    if (!response.ok || payload.success === false) throw new Error(payload.message || 'Không thể hoàn thành yêu cầu.');
    return payload.data;
  }
  function showResult(text, kind = '') {
    result.replaceChildren();
    const message = document.createElement('p'); message.className = kind; message.textContent = text; result.append(message);
  }
  async function loadJobs() {
    try {
      const response = await read(await fetch(`${api}/jobs`));
      if (!response.length) { jobList.innerHTML = '<p class="notice">Hiện chưa có vị trí đang tuyển.</p>'; return; }
      jobList.replaceChildren(...response.map((job) => {
        const card = document.createElement('article'); card.className = 'job-card';
        const title = document.createElement('h2'); title.textContent = job.title;
        const meta = document.createElement('div'); meta.className = 'meta'; meta.textContent = `${job.department} · ${job.headcount} vị trí`;
        const description = document.createElement('p'); description.textContent = job.description || 'Vui lòng gửi hồ sơ để tìm hiểu thêm về công việc.';
        const button = document.createElement('button'); button.className = 'primary'; button.type = 'button'; button.textContent = 'Ứng tuyển';
        button.addEventListener('click', () => {
          document.getElementById('jobId').value = job.id;
          document.getElementById('selectedJobTitle').textContent = job.title;
          panel.hidden = false; panel.scrollIntoView({ behavior:'smooth', block:'start' });
        });
        card.append(title, meta, description, button); return card;
      }));
    } catch (error) { jobList.innerHTML = ''; showResult(error.message, 'error'); }
  }
  document.getElementById('closeApplication').addEventListener('click', () => { panel.hidden = true; form.reset(); });
  form.addEventListener('submit', async (event) => {
    event.preventDefault();
    const button = document.getElementById('submitApplication'); button.disabled = true;
    const data = Object.fromEntries(new FormData(form).entries());
    const jobId = data.jobId; delete data.jobId; const resume = data.resume; delete data.resume;
    const body = new FormData(); body.append('application', new Blob([JSON.stringify(data)], { type:'application/json' })); body.append('resume', resume);
    try {
      const receipt = await read(await fetch(`${api}/jobs/${jobId}/applications`, { method:'POST', body }));
      document.getElementById('trackingCode').value = receipt.trackingCode;
      panel.hidden = true; form.reset();
      showResult(`Đã gửi hồ sơ cho vị trí “${receipt.jobTitle}”. Mã tra cứu của bạn: ${receipt.trackingCode}`, 'success');
      document.querySelector('.tracking-panel').scrollIntoView({ behavior:'smooth', block:'center' });
    } catch (error) { showResult(error.message, 'error'); }
    finally { button.disabled = false; }
  });
  document.getElementById('trackingForm').addEventListener('submit', async (event) => {
    event.preventDefault(); const code = document.getElementById('trackingCode').value.trim();
    try {
      const item = await read(await fetch(`${api}/applications/${encodeURIComponent(code)}`));
      showResult(`${item.candidateName} · ${item.jobTitle} · ${labels[item.stage] || item.stage}${item.interviewAt ? ` · Lịch phỏng vấn: ${new Date(item.interviewAt).toLocaleString('vi-VN')}` : ''}`);
      if (!['HIRED','REJECTED','WITHDRAWN'].includes(item.stage)) {
        const button = document.createElement('button'); button.type = 'button'; button.className = 'secondary'; button.textContent = 'Rút hồ sơ';
        button.addEventListener('click', async () => {
          button.disabled = true;
          try { const updated = await read(await fetch(`${api}/applications/${encodeURIComponent(code)}/withdraw`, { method:'POST' })); showResult(`Hồ sơ đã chuyển sang trạng thái: ${labels[updated.stage]}.`, 'success'); }
          catch (error) { showResult(error.message, 'error'); }
        }); result.append(button);
      }
    } catch (error) { showResult(error.message, 'error'); }
  });
  loadJobs();
});
