import React, { useState } from 'react';
import { createRoot } from 'react-dom/client';
import './style.css';

const API = import.meta.env.VITE_API_BASE || '/api';
function App() {
  const [form, setForm] = useState({ fullName: '', email: '', phone: '', source: '', introduction: '', position: '' });
  const [cv, setCv] = useState(null);
  const [busy, setBusy] = useState(false);
  const [notice, setNotice] = useState(null);
  const [lookupCode, setLookupCode] = useState('');
  const [lookupResult, setLookupResult] = useState(null);
  const [lookupError, setLookupError] = useState('');

  const update = e => setForm({ ...form, [e.target.name]: e.target.value });
  async function submit(e) {
    e.preventDefault(); setNotice(null);
    if (!cv) return setNotice({ type: 'error', text: 'Vui lòng đính kèm CV PDF hoặc DOCX.' });
    if (cv.size > 10 * 1024 * 1024) return setNotice({ type: 'error', text: 'CV vượt quá dung lượng tối đa 10MB.' });
    const ext = cv.name.toLowerCase().split('.').pop();
    if (!['pdf', 'docx'].includes(ext)) return setNotice({ type: 'error', text: 'Chỉ chấp nhận CV định dạng PDF hoặc DOCX.' });
    const body = new FormData();
    Object.entries(form).forEach(([key, value]) => body.append(key, value));
    body.append('cv', cv);
    setBusy(true);
    try {
      const response = await fetch(`${API}/applications`, { method: 'POST', body });
      const data = await response.json();
      if (!response.ok) throw new Error(data.error || 'Không thể nộp hồ sơ.');
      setNotice({ type: 'success', text: `${data.message} Mã tra cứu: ${data.trackingCode}. ${data.emailNotice}` });
      setForm({ fullName: '', email: '', phone: '', source: '', introduction: '', position: '' }); setCv(null);
      e.target.reset();
    } catch (err) { setNotice({ type: 'error', text: err.message }); }
    finally { setBusy(false); }
  }
  async function lookup(e) {
    e.preventDefault(); setLookupError(''); setLookupResult(null);
    try {
      const r = await fetch(`${API}/applications/lookup/${encodeURIComponent(lookupCode.trim())}`);
      const data = await r.json();
      if (!r.ok) throw new Error(data.error || 'Không tra cứu được hồ sơ.');
      setLookupResult(data.application);
    } catch (err) { setLookupError(err.message); }
  }
  return <div className="page">
    <header className="hero">
      <div className="brand"><span className="brand-mark">Ứ</span><span>CỔNG ỨNG TUYỂN</span></div>
      <nav><a href="#ung-tuyen">Nộp hồ sơ</a><a href="#tra-cuu">Tra cứu hồ sơ</a></nav>
      <div className="hero-copy"><p className="eyebrow">CƠ HỘI MỚI BẮT ĐẦU TỪ ĐÂY</p><h1>Chia sẻ hành trình.<br/><em>Kiến tạo tương lai.</em></h1><p>Hãy giới thiệu về bản thân và gửi CV để chúng tôi có cơ hội tìm hiểu bạn tốt hơn.</p><a className="cta" href="#ung-tuyen">Bắt đầu ứng tuyển <span>↘</span></a></div>
      <div className="hero-art"><div className="art-ring"></div><div className="art-card"><span>YOUR NEXT CHAPTER</span><strong>Make<br/>it happen.</strong><small>Talent • Growth • Impact</small></div><div className="art-dot dot-one"></div><div className="art-dot dot-two"></div></div>
    </header>
    <main>
      <section className="intro"><div><p className="eyebrow">01 / ỨNG TUYỂN</p><h2>Mỗi hành trình<br/>đều bắt đầu bằng<br/><em>một lời giới thiệu.</em></h2></div><p className="intro-text">Điền thông tin bên dưới và đính kèm CV của bạn. Hãy kiểm tra kỹ email để nhận mã tra cứu hồ sơ sau khi gửi thành công.</p></section>
      <section id="ung-tuyen" className="form-section">
        <div className="section-heading"><div><p className="eyebrow">APPLICATION FORM</p><h2>Thông tin ứng viên</h2></div><span className="required-note">* Thông tin bắt buộc</span></div>
        {notice && <div className={`notice ${notice.type}`} role="status">{notice.text}</div>}
        <form onSubmit={submit}>
          <label className="honeypot" aria-hidden="true">Để trống trường này<input name="website" value={form.website || ''} onChange={update} tabIndex="-1" autoComplete="off"/></label>
          <div className="field-grid">
            <label>Họ và tên <b>*</b><input name="fullName" value={form.fullName} onChange={update} required maxLength="150" placeholder="Nguyễn Văn A"/></label>
            <label>Email <b>*</b><input name="email" type="email" value={form.email} onChange={update} required maxLength="254" placeholder="ban@email.com"/></label>
            <label>Số điện thoại <b>*</b><input name="phone" type="tel" value={form.phone} onChange={update} required maxLength="40" placeholder="09xx xxx xxx"/></label>
            <label>Vị trí ứng tuyển <b>*</b><select name="position" value={form.position} onChange={update} required><option value="">Chọn vị trí</option><option>Frontend Developer</option><option>Backend Developer</option><option>Full-stack Developer</option><option>UI/UX Designer</option><option>Marketing</option><option>Thực tập sinh</option><option>Vị trí khác</option></select></label>
            <label>Nguồn biết tin <b>*</b><select name="source" value={form.source} onChange={update} required><option value="">Bạn biết tin từ đâu?</option><option>Website công ty</option><option>Facebook</option><option>LinkedIn</option><option>Bạn bè giới thiệu</option><option>Trường học</option><option>Nguồn khác</option></select></label>
            <label className="upload-label">CV của bạn <b>*</b><span className="upload-box"><input type="file" accept=".pdf,.docx,application/pdf,application/vnd.openxmlformats-officedocument.wordprocessingml.document" onChange={e => setCv(e.target.files?.[0] || null)} required/><span className="upload-icon">↑</span><strong>{cv ? cv.name : 'Chọn tệp CV để tải lên'}</strong><small>PDF hoặc DOCX · Tối đa 10MB</small></span></label>
          </div>
          <label className="wide-label">Thư giới thiệu <b>*</b><textarea name="introduction" value={form.introduction} onChange={update} required maxLength="5000" rows="5" placeholder="Hãy chia sẻ đôi điều về bản thân, kinh nghiệm và lý do bạn phù hợp với vị trí này..."/></label>
          <div className="form-bottom"><p>Thông tin của bạn được sử dụng cho mục đích tuyển dụng. Vui lòng không tải lên tài liệu chứa thông tin nhạy cảm không cần thiết.</p><button disabled={busy} type="submit">{busy ? 'Đang gửi hồ sơ...' : 'Gửi hồ sơ ứng tuyển'} <span>↗</span></button></div>
        </form>
      </section>
      <section id="tra-cuu" className="lookup-section"><div><p className="eyebrow">02 / THEO DÕI</p><h2>Đã ứng tuyển?<br/><em>Tra cứu hồ sơ của bạn.</em></h2><p>Nhập mã tra cứu được cấp sau khi nộp hồ sơ thành công.</p></div><div className="lookup-card"><form onSubmit={lookup}><label>Mã tra cứu hồ sơ<input value={lookupCode} onChange={e => setLookupCode(e.target.value)} required placeholder="Ví dụ: APP-A1B2C3D4E5F6"/></label><button type="submit">Tra cứu <span>↗</span></button></form>{lookupError && <p className="lookup-error">{lookupError}</p>}{lookupResult && <div className="lookup-result"><strong>Đã tiếp nhận hồ sơ</strong><p>Ứng viên: {lookupResult.full_name}</p><p>Vị trí: {lookupResult.position}</p><p>Mã: {lookupResult.tracking_code}</p><small>Ngày gửi: {new Date(lookupResult.created_at).toLocaleString('vi-VN')}</small></div>}</div></section>
    </main>
    <footer><div className="brand"><span className="brand-mark">Ứ</span><span>CỔNG ỨNG TUYỂN</span></div><span>Ứng tuyển hôm nay, mở lối ngày mai.</span><small>© {new Date().getFullYear()} Applicant Portal</small></footer>
  </div>;
}
createRoot(document.getElementById('root')).render(<App />);
