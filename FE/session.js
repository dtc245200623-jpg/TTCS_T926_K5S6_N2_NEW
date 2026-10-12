/**
 * FE/session.js
 * Quản lý trạng thái phiên đăng nhập
 *
 * Ticket KN-16: [FE] Quản lý trạng thái phiên đăng nhập
 *
 * Chức năng:
 * 1. Lưu / lấy / xóa session
 * 2. Theo dõi hoạt động của người dùng
 * 3. Tự động kiểm tra và gia hạn session
 * 4. Xử lý session hết hạn
 * 5. Logout
 * 6. Gửi token kèm request API
 * 7. Lưu nháp form
 */

const SessionManager = (function () {
  const API_BASE = `${window.location.origin}/api/auth`;

  /*
   * Các endpoint này là nơi FE giao tiếp với BE.
   *
   * Khi BE hoàn thành API thật, chỉ cần sửa các URL ở đây
   * nếu tên endpoint của BE khác.
   */
  const API = {
    ME: `${API_BASE}/me`,

    // API kiểm tra session hợp lệ; hệ thống hiện dùng JWT stateless, không có refresh token.
    REFRESH: `${API_BASE}/me`,

    // API logout / thu hồi session.
    // TODO: Đổi lại URL này theo API BE thực tế.
    LOGOUT: `${API_BASE}/revoke-sessions`,
  };

  // Key lưu trong localStorage
  const KEY_TOKEN = "ats_jwt_token";
  const KEY_USER = "ats_user_info";
  const KEY_LAST_ACTIVE = "ats_last_active";

  /*
   * Kiểm tra session mỗi 30 giây.
   */
  const HEARTBEAT_INTERVAL = 30 * 1000;

  /*
   * Nếu người dùng không thao tác trong 15 phút
   * thì FE coi session là hết hạn.
   *
   * Nếu team có quy định thời gian khác thì sửa ở đây.
   */
  const IDLE_TIMEOUT = 15 * 60 * 1000;

  let heartbeatTimer = null;
  let isUserActive = false;
  let activityListenersRegistered = false;
  let onStatusCallback = null;

  // Lưu thông tin phiên đăng nhập
  function saveSession(token, user) {
    localStorage.setItem(KEY_TOKEN, token);

    localStorage.setItem(KEY_USER, JSON.stringify(user));

    localStorage.setItem(KEY_LAST_ACTIVE, Date.now().toString());
  }

  // Lấy token
  function getToken() {
    return localStorage.getItem(KEY_TOKEN);
  }

  // Lấy thông tin user
  function getUser() {
    const raw = localStorage.getItem(KEY_USER);

    if (!raw) {
      return null;
    }

    try {
      return JSON.parse(raw);
    } catch (error) {
      console.warn("[SessionManager] Không đọc được thông tin user.");

      return null;
    }
  }

  // Xóa session local
  function clearSession() {
    localStorage.removeItem(KEY_TOKEN);
    localStorage.removeItem(KEY_USER);
    localStorage.removeItem(KEY_LAST_ACTIVE);

    stopSessionMonitoring();
  }

  // Kiểm tra đã đăng nhập chưa
  function isLoggedIn() {
    return !!getToken();
  }

  // Chuyển về login khi session không còn hợp lệ
  function handleUnauthorized(reason = "expired") {
    clearSession();

    window.location.href = `index.html?reason=${encodeURIComponent(reason)}`;
  }

  // Gửi request kèm token và xử lý lỗi 401
  async function fetchWithAuth(url, options = {}) {
    const token = getToken();

    if (!token) {
      handleUnauthorized("expired");
      throw new Error("Chưa đăng nhập.");
    }

    const headers = {
      ...(options.headers || {}),
    };

    headers["Authorization"] = `Bearer ${token}`;

    if (
      !headers["Content-Type"] &&
      options.body &&
      typeof options.body === "string"
    ) {
      headers["Content-Type"] = "application/json";
    }

    try {
      const response = await fetch(url, {
        ...options,
        headers,
      });

      if (response.status === 401) {
        console.warn("[SessionManager] Server trả về 401.");

        handleUnauthorized("expired");

        throw new Error("Phiên đăng nhập đã hết hạn hoặc không còn hợp lệ.");
      }

      return response;
    } catch (error) {
      throw error;
    }
  }

  // Kiểm tra và gia hạn session
  async function verifyAndRenewSession() {
    const token = getToken();

    if (!token) {
      return false;
    }

    try {
      /*
       * Đây là API REFRESH riêng.
       *
       * Không dùng /me để giả định rằng session được gia hạn.
       */
      const response = await fetchWithAuth(API.REFRESH, { method: "GET" });

      if (!response.ok) {
        console.warn("[SessionManager] Không thể gia hạn session.");

        return false;
      }

      const data = await response.json();

      if (data.success) {
        // Nếu BE trả token mới thì cập nhật token
        if (data.data && data.data.token) {
          localStorage.setItem(KEY_TOKEN, data.data.token);
        }

        // Nếu BE trả user mới thì cập nhật user
        if (data.data && data.data.user) {
          localStorage.setItem(KEY_USER, JSON.stringify(data.data.user));
        }

        // Cập nhật thời điểm hoạt động cuối
        localStorage.setItem(KEY_LAST_ACTIVE, Date.now().toString());

        // Cập nhật trạng thái trên giao diện
        if (onStatusCallback) {
          onStatusCallback({
            status: "active",
            message: "Phiên hoạt động – Đã xác thực với máy chủ.",
            lastRenewed: new Date().toLocaleTimeString("vi-VN"),
          });
        }

        return true;
      }

      return false;
    } catch (error) {
      /*
       * Nếu lỗi mạng thì chưa tự động logout ngay
       * vì có thể server tạm thời không kết nối được.
       */
      console.warn("[SessionManager] Lỗi khi gia hạn session:", error);

      return false;
    }
  }

  // Theo dõi hoạt động của người dùng
  function registerActivityListeners() {
    // Tránh đăng ký event nhiều lần
    if (activityListenersRegistered) {
      return;
    }

    const events = ["mousemove", "keydown", "click", "scroll", "touchstart"];

    const onActivity = () => {
      isUserActive = true;

      localStorage.setItem(KEY_LAST_ACTIVE, Date.now().toString());
    };

    events.forEach((eventName) => {
      window.addEventListener(eventName, onActivity, { passive: true });
    });

    activityListenersRegistered = true;
  }

  // Bắt đầu giám sát session
  function startSessionMonitoring(statusCallback) {
    onStatusCallback = statusCallback;

    registerActivityListeners();

    // Kiểm tra session ngay khi vào dashboard
    verifyAndRenewSession();

    // Xóa timer cũ nếu có
    if (heartbeatTimer) {
      clearInterval(heartbeatTimer);
    }

    // Chạy kiểm tra định kỳ
    heartbeatTimer = setInterval(async () => {
      // Người dùng có hoạt động
      if (isUserActive) {
        isUserActive = false;

        await verifyAndRenewSession();

        return;
      }

      // Người dùng không hoạt động
      const lastActive = parseInt(
        localStorage.getItem(KEY_LAST_ACTIVE) || "0",
        10,
      );

      // Nếu chưa có lastActive thì bắt đầu tính từ hiện tại
      if (!lastActive) {
        localStorage.setItem(KEY_LAST_ACTIVE, Date.now().toString());

        return;
      }

      const idleTime = Date.now() - lastActive;

      // Session hết hạn
      if (idleTime >= IDLE_TIMEOUT) {
        console.warn(
          "[SessionManager] Người dùng không hoạt động quá thời gian cho phép.",
        );

        handleUnauthorized("expired");

        return;
      }

      // Session vẫn còn nhưng đang idle
      if (onStatusCallback) {
        const remainingMs = IDLE_TIMEOUT - idleTime;

        const remainingMins = Math.ceil(remainingMs / 60000);

        onStatusCallback({
          status: "idle",
          message: `Không có thao tác. Phiên sẽ hết hạn sau ${remainingMins} phút.`,
          lastRenewed: null,
        });
      }
    }, HEARTBEAT_INTERVAL);
  }

  // Dừng giám sát session
  function stopSessionMonitoring() {
    if (heartbeatTimer) {
      clearInterval(heartbeatTimer);
      heartbeatTimer = null;
    }
  }

  // Đăng xuất
  async function logout(revokeAll = false) {
    const token = getToken();

    try {
      if (token && revokeAll) {
        /*
         * FE gửi yêu cầu logout lên Server.
         *
         * BE sẽ chịu trách nhiệm làm token/session
         * mất hiệu lực phía server.
         */
        await fetch(API.LOGOUT, {
          method: "POST",

          headers: {
            Authorization: `Bearer ${token}`,

            "Content-Type": "application/json",
          },

          body: JSON.stringify({
            revokeAll: revokeAll,
          }),
        });
      }
    } catch (error) {
      /*
       * Dù server lỗi thì FE vẫn phải xóa session local.
       */
      console.warn("[SessionManager] Không thể gọi API logout:", error);
    } finally {
      clearSession();

      window.location.href = `index.html?reason=${
        revokeAll ? "revoked_all" : "logout"
      }`;
    }
  }

  // Lưu nháp
  function saveDraft(key, data) {
    try {
      localStorage.setItem(
        `draft_${key}`,

        JSON.stringify({
          timestamp: Date.now(),
          data: data,
        }),
      );
    } catch (error) {
      console.warn("[SessionManager] Không thể lưu draft:", error);
    }
  }

  // Lấy nháp
  function getDraft(key) {
    try {
      const raw = localStorage.getItem(`draft_${key}`);

      return raw ? JSON.parse(raw) : null;
    } catch (error) {
      return null;
    }
  }

  // Xóa nháp
  function clearDraft(key) {
    localStorage.removeItem(`draft_${key}`);
  }

  return {
    saveSession,
    getToken,
    getUser,
    clearSession,
    isLoggedIn,
    handleUnauthorized,
    fetchWithAuth,
    verifyAndRenewSession,
    startSessionMonitoring,
    stopSessionMonitoring,
    logout,
    saveDraft,
    getDraft,
    clearDraft,
  };
})();
