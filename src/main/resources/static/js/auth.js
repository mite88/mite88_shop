const TOKEN_KEY = 'accessToken';
const REFRESH_KEY = 'refreshToken';

const Auth = {
  getToken()        { return localStorage.getItem(TOKEN_KEY); },
  getRefreshToken() { return localStorage.getItem(REFRESH_KEY); },

  setTokens(accessToken, refreshToken) {
    localStorage.setItem(TOKEN_KEY, accessToken);
    localStorage.setItem(REFRESH_KEY, refreshToken);
  },

  clearTokens() {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(REFRESH_KEY);
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

document.addEventListener('DOMContentLoaded', () => {
  const loggedIn = Auth.isLoggedIn();
  document.querySelectorAll('[data-auth="guest"]').forEach(el => {
    el.classList.toggle('hidden', loggedIn);
  });
  document.querySelectorAll('[data-auth="user"]').forEach(el => {
    el.classList.toggle('hidden', !loggedIn);
  });
});
