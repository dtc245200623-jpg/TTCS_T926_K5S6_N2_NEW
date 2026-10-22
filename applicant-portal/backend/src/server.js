import express from 'express';
import cors from 'cors';
import helmet from 'helmet';
import rateLimit from 'express-rate-limit';
import mysql from 'mysql2/promise';
import multer from 'multer';
import nodemailer from 'nodemailer';
import crypto from 'node:crypto';
import path from 'node:path';
import fs from 'node:fs';

const app = express();
const PORT = Number(process.env.PORT || 5000);
const uploadDir = process.env.UPLOAD_DIR || path.resolve('uploads');
fs.mkdirSync(uploadDir, { recursive: true });

app.disable('x-powered-by');
app.set('trust proxy', 1);
app.use(helmet());
app.use(cors({ origin: process.env.FRONTEND_ORIGIN || 'http://localhost' }));
app.use(express.json({ limit: '100kb' }));

let pool;
async function getPool() {
  if (!pool) {
    pool = mysql.createPool({
      host: process.env.DB_HOST || '127.0.0.1',
      port: Number(process.env.DB_PORT || 3306),
      user: process.env.DB_USER || 'applicant_app',
      password: process.env.DB_PASSWORD || '',
      database: process.env.DB_NAME || 'applicant_portal',
      waitForConnections: true,
      connectionLimit: 10,
      queueLimit: 0,
      charset: 'utf8mb4'
    });
  }
  return pool;
}

const limiter = rateLimit({
  windowMs: Number(process.env.RATE_LIMIT_WINDOW_MS || 3600000),
  limit: Number(process.env.RATE_LIMIT_MAX || 5),
  standardHeaders: 'draft-7',
  legacyHeaders: false,
  message: { error: 'Bạn đã gửi quá nhiều hồ sơ. Vui lòng thử lại sau.' }
});
const storage = multer.diskStorage({
  destination: (_req, _file, cb) => cb(null, uploadDir),
  filename: (_req, file, cb) => cb(null, `${crypto.randomUUID()}${path.extname(file.originalname).toLowerCase()}`)
});
const upload = multer({
  storage,
  limits: { fileSize: 10 * 1024 * 1024, files: 1 },
  fileFilter: (_req, file, cb) => {
    const ext = path.extname(file.originalname).toLowerCase();
    const valid = ext === '.pdf' || ext === '.docx';
    cb(valid ? null : new Error('CV chỉ được ở định dạng PDF hoặc DOCX.'), valid);
  }
});

function clean(value, max = 1000) {
  return String(value ?? '').trim().slice(0, max);
}
function validEmail(email) {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email) && email.length <= 254;
}
function hashIp(ip) {
  const secret = process.env.IP_HASH_SECRET || 'development-only-change-me';
  return crypto.createHmac('sha256', secret).update(ip).digest('hex');
}
async function hasValidFileSignature(file) {
  const handle = await fs.promises.open(file.path, 'r');
  try {
    const header = Buffer.alloc(8);
    const { bytesRead } = await handle.read(header, 0, header.length, 0);
    if (path.extname(file.originalname).toLowerCase() === '.pdf') {
      return bytesRead >= 5 && header.subarray(0, 5).toString('ascii') === '%PDF-';
    }
    // DOCX is an OOXML ZIP container. Check its container signature instead of
    // trusting the browser-supplied MIME type.
    return bytesRead >= 4 && header[0] === 0x50 && header[1] === 0x4b &&
      [0x03, 0x05, 0x07].includes(header[2]) && [0x04, 0x06, 0x08].includes(header[3]);
  } finally { await handle.close(); }
}
async function sendConfirmation(to, name, code, position) {
  if (!process.env.SMTP_HOST || !process.env.SMTP_USER || !process.env.SMTP_PASS) return false;
  const transporter = nodemailer.createTransport({
    host: process.env.SMTP_HOST,
    port: Number(process.env.SMTP_PORT || 587),
    secure: String(process.env.SMTP_SECURE).toLowerCase() === 'true',
    auth: { user: process.env.SMTP_USER, pass: process.env.SMTP_PASS }
  });
  await transporter.sendMail({
    from: process.env.MAIL_FROM || process.env.SMTP_USER,
    to,
    subject: 'Xác nhận đã nhận hồ sơ ứng tuyển',
    text: `Xin chào ${name},\n\nHệ thống đã nhận hồ sơ ứng tuyển vị trí "${position}".\nMã tra cứu hồ sơ của bạn: ${code}\nVui lòng lưu mã này để tra cứu trạng thái tiếp nhận.\n\nTrân trọng.`
  });
  return true;
}

app.get('/api/health', async (_req, res) => {
  try {
    const db = await getPool();
    await db.query('SELECT 1');
    res.json({ status: 'ok', database: 'connected' });
  } catch {
    res.status(503).json({ status: 'degraded', database: 'unavailable' });
  }
});

app.post('/api/applications', limiter, upload.single('cv'), async (req, res, next) => {
  let filePath = req.file?.path;
  let applicationSaved = false;
  try {
    const fullName = clean(req.body.fullName, 150);
    const email = clean(req.body.email, 254).toLowerCase();
    const phone = clean(req.body.phone, 40);
    const source = clean(req.body.source, 100);
    const introduction = clean(req.body.introduction, 5000);
    const position = clean(req.body.position, 150);

    if (req.body.website) {
      if (filePath) fs.unlink(filePath, () => {});
      return res.status(400).json({ error: 'Không thể xử lý yêu cầu này.' });
    }
    if (!fullName || !email || !phone || !source || !introduction || !position || !req.file) {
      if (filePath) fs.unlink(filePath, () => {});
      return res.status(400).json({ error: 'Vui lòng điền đầy đủ thông tin và đính kèm CV.' });
    }
    if (!validEmail(email)) {
      if (filePath) fs.unlink(filePath, () => {});
      return res.status(400).json({ error: 'Địa chỉ email không hợp lệ.' });
    }
    if (!(await hasValidFileSignature(req.file))) {
      if (filePath) fs.unlink(filePath, () => {});
      return res.status(400).json({ error: 'Nội dung tệp không khớp định dạng PDF hoặc DOCX.' });
    }
    const db = await getPool();
    const [existing] = await db.execute(
      'SELECT tracking_code FROM applications WHERE LOWER(email)=? AND LOWER(position)=? LIMIT 1',
      [email, position.toLowerCase()]
    );
    if (existing.length) {
      if (filePath) fs.unlink(filePath, () => {});
      return res.status(409).json({ error: 'Email này đã nộp hồ sơ cho vị trí trên. Mã hồ sơ đã có: ' + existing[0].tracking_code });
    }

    const code = `APP-${crypto.randomBytes(6).toString('hex').toUpperCase()}`;
    const ip = hashIp(req.ip || req.socket.remoteAddress || 'unknown');
    await db.execute(
      `INSERT INTO applications
       (tracking_code, full_name, email, phone, source, introduction, position,
        cv_original_name, cv_stored_name, cv_mime_type, cv_size_bytes, ip_hash)
       VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)`,
      [code, fullName, email, phone, source, introduction, position,
       path.basename(req.file.originalname), path.basename(req.file.filename),
       path.extname(req.file.originalname).toLowerCase() === '.pdf'
         ? 'application/pdf'
         : 'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
       req.file.size, ip]
    );
    applicationSaved = true;

    let emailSent = false;
    try { emailSent = await sendConfirmation(email, fullName, code, position); }
    catch (err) { console.error('email_send_failed:', err.message); }
    return res.status(201).json({
      message: 'Nộp hồ sơ thành công.',
      trackingCode: code,
      emailSent,
      emailNotice: emailSent ? 'Đã gửi email xác nhận.' : 'Hồ sơ đã lưu; email chưa gửi vì SMTP chưa được cấu hình hoặc đang lỗi.'
    });
  } catch (err) {
    if (filePath && !applicationSaved) fs.unlink(filePath, () => {});
    if (err.code === 'ER_DUP_ENTRY') return res.status(409).json({ error: 'Hồ sơ trùng email và vị trí ứng tuyển.' });
    next(err);
  }
});

app.get('/api/applications/lookup/:code', async (req, res, next) => {
  try {
    const code = clean(req.params.code, 32).toUpperCase();
    const db = await getPool();
    const [rows] = await db.execute(
      'SELECT tracking_code, full_name, position, created_at FROM applications WHERE tracking_code=? LIMIT 1',
      [code]
    );
    if (!rows.length) return res.status(404).json({ error: 'Không tìm thấy hồ sơ với mã tra cứu này.' });
    res.json({ application: rows[0] });
  } catch (err) { next(err); }
});

app.use((err, _req, res, _next) => {
  if (err instanceof multer.MulterError) {
    const message = err.code === 'LIMIT_FILE_SIZE' ? 'CV vượt quá dung lượng tối đa 10MB.' : 'Không thể tải tệp CV.';
    return res.status(400).json({ error: message });
  }
  if (err.message?.includes('PDF hoặc DOCX')) return res.status(400).json({ error: err.message });
  console.error('request_error:', err.message);
  res.status(500).json({ error: 'Máy chủ gặp lỗi. Vui lòng thử lại sau.' });
});

app.listen(PORT, () => console.log(`applicant_api_started port=${PORT}`));
