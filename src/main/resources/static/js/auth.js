const TOKEN_KEY = 'accessToken';
const REFRESH_KEY = 'refreshToken';

const Auth = {
  getToken()        { return localStorage.getItem(TOKEN_KEY); },
  getRefreshToken() { return localStorage.getItem(REFRESH_KEY); },

  setTokens(accessToken, refreshToken) {
    localStorage.setItem(TOKEN_KEY, accessToken);
    localStorage.setItem(REFRESH_KEY, refreshToken);
    if (typeof SessionManager !== 'undefined') {
      SessionManager.resetExpiry();
      SessionManager.init();
    }
  },

  clearTokens() {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(REFRESH_KEY);
    if (typeof SessionManager !== 'undefined') {
      SessionManager.clear();
    }
  },

  isLoggedIn() { return !!this.getToken(); },

  async fetchWithAuth(url, options = {}) {
    const doFetch = (token) => fetch(url, {
      ...options,
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`,
        ...(options.headers || {})
      }
    });

    let res = await doFetch(this.getToken());

    if (res.status === 401) {
      const refreshed = await this.refresh();
      if (!refreshed) {
        this.clearTokens();
        window.location.href = '/login';
        return;
      }
      res = await doFetch(this.getToken());
    }
    return res;
  },

  async refresh() {
    const refreshToken = this.getRefreshToken();
    if (!refreshToken) return false;
    try {
      const res = await fetch('/api/v1/auth/refresh', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ refreshToken })
      });
      if (!res.ok) return false;
      const { accessToken, refreshToken: newRefresh } = await res.json();
      this.setTokens(accessToken, newRefresh);
      return true;
    } catch {
      return false;
    }
  },

  async logout() {
    if (this.isLoggedIn()) {
      await this.fetchWithAuth('/api/v1/auth/logout', { method: 'POST' });
    }
    this.clearTokens();
    window.location.href = '/';
  }
};

const SessionManager = {
  TIMEOUT_MS: 60 * 60 * 1000, // 비활동 자동 로그아웃 기준 시간 (1시간)
  WARNING_MS: 10 * 60 * 1000, // 경고 모달 표시 기준 잔여 시간 (10분)
  THROTTLE_MS: 10 * 1000,     // 활동 감지 쓰로틀 간격 (10초)
  
  lastActivityTime: 0,
  intervalId: null,

  init() {
    if (!Auth.isLoggedIn()) {
      this.clear();
      return;
    }

    let expiry = localStorage.getItem('sessionExpiryTime');
    if (!expiry) {
      this.resetExpiry();
    }

    this.startInterval();
    this.registerActivityListeners();
  },

  resetExpiry() {
    const newExpiry = Date.now() + this.TIMEOUT_MS;
    localStorage.setItem('sessionExpiryTime', newExpiry);
  },

  getRemainingTime() {
    const expiry = localStorage.getItem('sessionExpiryTime');
    if (!expiry) return 0;
    return Math.max(0, parseInt(expiry, 10) - Date.now());
  },

  registerActivityListeners() {
    const handleActivity = () => {
      if (!Auth.isLoggedIn()) return;
      
      const now = Date.now();
      if (now - this.lastActivityTime < this.THROTTLE_MS) return;

      const remaining = this.getRemainingTime();
      // 잔여 시간이 10분 초과일 때만 자동 연장 — 경고 모달이 뜬 상태에서는 사용자가 직접 연장 버튼을 눌러야 함
      if (remaining > this.WARNING_MS) {
        this.lastActivityTime = now;
        this.resetExpiry();
      }
    };

    ['mousedown', 'keydown', 'scroll', 'touchstart'].forEach(type => {
      document.addEventListener(type, handleActivity, { passive: true });
    });
  },

  startInterval() {
    if (this.intervalId) clearInterval(this.intervalId);
    
    this.intervalId = setInterval(() => {
      if (!Auth.isLoggedIn()) {
        this.clear();
        return;
      }

      const remaining = this.getRemainingTime();
      
      if (remaining <= 0) {
        this.clear();
        this.logout("세션 시간이 만료되어 자동 로그아웃되었습니다.");
        return;
      }

      this.updateUI(remaining);

      // Warning Modal management
      const modal = document.getElementById('session-warning-modal');
      if (modal) {
        if (remaining <= this.WARNING_MS) {
          if (!modal.open) {
            modal.showModal();
          }
          const modalTimer = document.getElementById('modal-timer');
          if (modalTimer) {
            modalTimer.textContent = this.formatTime(remaining);
          }
        } else {
          if (modal.open) {
            modal.close();
          }
        }
      }
    }, 1000);
  },

  formatTime(ms) {
    const totalSecs = Math.floor(ms / 1000);
    const mins = Math.floor(totalSecs / 60);
    const secs = totalSecs % 60;
    return `${String(mins).padStart(2, '0')}:${String(secs).padStart(2, '0')}`;
  },

  updateUI(remaining) {
    const timerEl = document.getElementById('session-timer');
    if (timerEl) {
      timerEl.textContent = this.formatTime(remaining);
    }
  },

  async extendSession() {
    this.resetExpiry();
    showToast("로그인 세션 시간이 연장되었습니다.", "success");
    
    const modal = document.getElementById('session-warning-modal');
    if (modal && modal.open) {
      modal.close();
    }
    
    try {
      await Auth.refresh();
    } catch (e) {
      console.error("Failed to refresh session on server", e);
    }
  },

  clear() {
    if (this.intervalId) {
      clearInterval(this.intervalId);
      this.intervalId = null;
    }
    localStorage.removeItem('sessionExpiryTime');
  },

  async logout(message) {
    this.clear();
    if (message) {
      showToast(message, "warning");
    }
    await Auth.logout();
  }
};

document.addEventListener('DOMContentLoaded', () => {
  const loggedIn = Auth.isLoggedIn();
  document.querySelectorAll('[data-auth="guest"]').forEach(el => {
    el.classList.toggle('hidden', loggedIn);
  });
  document.querySelectorAll('[data-auth="user"]').forEach(el => {
    el.classList.toggle('hidden', !loggedIn);
  });
  if (typeof SessionManager !== 'undefined') {
    SessionManager.init();
  }
});
