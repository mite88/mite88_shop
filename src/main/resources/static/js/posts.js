let currentPost = null;
let currentPage = 0;

// ── 목록 페이지 ──────────────────────────────────────────

async function loadPosts(page = 0) {
  currentPage = page;
  const list = document.getElementById('post-list');
  try {
    const res = await fetch(`/qna?page=${page}`);
    const data = await res.json();

    if (!data.content || !data.content.length) {
      list.innerHTML = '<div class="text-center py-12 text-base-content/50">등록된 문의가 없습니다.</div>';
    } else {
      list.innerHTML = data.content.map(p => `
        <div class="card bg-base-100 shadow-sm hover:shadow-md cursor-pointer transition-shadow"
             onclick="window.location.href='/qa/${p.id}'">
          <div class="card-body py-4">
            <div class="flex justify-between items-center">
              <h2 class="font-semibold text-base line-clamp-1">${escapeHtml(p.title)}</h2>
              <span class="text-xs text-base-content/40 shrink-0 ml-4">${formatDate(p.createdAt)}</span>
            </div>
            <div class="text-sm text-base-content/50">${escapeHtml(p.authorName)}</div>
          </div>
        </div>
      `).join('');
    }

    renderPagination(data.totalPages, data.number);

    const isAdmin = Auth.isLoggedIn() && parseRole(Auth.getToken()) === 'ADMIN';
    if (isAdmin) {
      const btn = document.getElementById('write-btn');
      if (btn) btn.classList.remove('hidden');
    }
  } catch {
    if (list) list.innerHTML = '<div class="text-center py-12 text-error">게시글을 불러오지 못했습니다.</div>';
  }
}

function renderPagination(totalPages, currentPageNum) {
  const container = document.getElementById('pagination');
  if (!container) return;

  const pages = Math.max(totalPages, 1);
  const buttons = [];
  for (let i = 0; i < pages; i++) {
    const active = i === currentPageNum ? 'btn-active' : '';
    buttons.push(`<button class="btn btn-sm ${active}" onclick="loadPosts(${i})">${i + 1}</button>`);
  }
  container.innerHTML = `<div class="flex gap-1 justify-center mt-6">${buttons.join('')}</div>`;
}

function openWriteModal() {
  document.getElementById('post-title-input').value = '';
  document.getElementById('post-content-input').value = '';
  document.getElementById('write-modal').showModal();
}

function closeWriteModal() {
  document.getElementById('write-modal').close();
}

async function submitPost() {
  const title   = document.getElementById('post-title-input').value.trim();
  const content = document.getElementById('post-content-input').value.trim();
  if (!title || !content) { showToast('제목과 내용을 모두 입력해주세요.', 'warning'); return; }

  if (!Auth.isLoggedIn()) { showToast('로그인이 필요합니다.', 'error'); return; }

  try {
    const res = await Auth.fetchWithAuth('/qna', {
      method: 'POST',
      body: JSON.stringify({ title, content })
    });
    if (!res.ok) throw new Error();
    const post = await res.json();
    closeWriteModal();
    window.location.href = `/qa/${post.id}`;
  } catch {
    showToast('문의 등록에 실패했습니다.', 'error');
  }
}

// ── 상세 페이지 ──────────────────────────────────────────

async function loadPostDetail(postId) {
  if (!postId) return;
  try {
    const res = await fetch(`/qna/${postId}`);
    if (!res.ok) throw new Error();
    currentPost = await res.json();

    document.getElementById('post-skeleton').classList.add('hidden');
    document.getElementById('post-detail').classList.remove('hidden');

    document.getElementById('post-title').textContent   = currentPost.title;
    document.getElementById('post-author').textContent  = currentPost.authorName;
    document.getElementById('post-date').textContent    = formatDate(currentPost.createdAt);
    document.getElementById('post-content').textContent = currentPost.content;

    const username = Auth.isLoggedIn() ? parseUsername(Auth.getToken()) : null;
    const role = Auth.isLoggedIn() ? parseRole(Auth.getToken()) : null;

    if (username && username === currentPost.authorName) {
      document.getElementById('post-actions').classList.remove('hidden');
    }

    const loginPrompt = document.getElementById('comment-login-prompt');
    if (role === 'ADMIN') {
      document.getElementById('comment-write').classList.remove('hidden');
    } else if (Auth.isLoggedIn()) {
      loginPrompt.textContent = '관리자만 답변을 작성할 수 있습니다.';
      loginPrompt.classList.remove('hidden');
    } else {
      loginPrompt.innerHTML = '<a href="/login" class="link">로그인</a> 후 문의를 확인할 수 있습니다.';
      loginPrompt.classList.remove('hidden');
    }

    await loadComments(postId);
  } catch {
    showToast('게시글을 불러오지 못했습니다.', 'error');
  }
}

async function loadComments(postId) {
  const list = document.getElementById('comment-list');
  const count = document.getElementById('comment-count');
  try {
    const res = await fetch(`/qna/${postId}/comments`);
    const comments = await res.json();

    count.textContent = `(${comments.length})`;

    if (!comments.length) {
      list.innerHTML = '<div class="text-sm text-base-content/40 py-4">아직 답변이 없습니다.</div>';
      return;
    }

    const isAdmin = Auth.isLoggedIn() && parseRole(Auth.getToken()) === 'ADMIN';

    list.innerHTML = comments.map(c => `
      <div class="card bg-base-200 shadow-sm">
        <div class="card-body py-3">
          <div class="flex justify-between items-start">
            <div class="flex items-center gap-2">
              <span class="font-semibold text-sm">${escapeHtml(c.authorName)}</span>
              ${c.isAdminAuthor ? '<span class="badge badge-primary badge-sm">관리자</span>' : ''}
              <span class="text-xs text-base-content/40">${formatDate(c.createdAt)}</span>
            </div>
            ${isAdmin ? `<button class="btn btn-ghost btn-xs text-error" onclick="deleteComment(${c.id})">삭제</button>` : ''}
          </div>
          <p class="text-sm whitespace-pre-wrap mt-1">${escapeHtml(c.content)}</p>
        </div>
      </div>
    `).join('');
  } catch {
    if (list) list.innerHTML = '<div class="text-sm text-error">댓글을 불러오지 못했습니다.</div>';
  }
}

async function submitComment() {
  const input = document.getElementById('comment-input');
  const content = input.value.trim();
  if (!content) { showToast('댓글 내용을 입력해주세요.', 'warning'); return; }
  if (!Auth.isLoggedIn()) { showToast('로그인이 필요합니다.', 'error'); return; }

  try {
    const res = await Auth.fetchWithAuth(`/qna/${POST_ID}/comments`, {
      method: 'POST',
      body: JSON.stringify({ content })
    });
    if (!res.ok) throw new Error();
    input.value = '';
    await loadComments(POST_ID);
    showToast('답변이 등록되었습니다.', 'success');
  } catch {
    showToast('답변 등록에 실패했습니다.', 'error');
  }
}

async function deleteComment(commentId) {
  if (!confirm('댓글을 삭제하시겠습니까?')) return;
  try {
    const res = await Auth.fetchWithAuth(`/qna/${POST_ID}/comments/${commentId}`, { method: 'DELETE' });
    if (!res.ok) throw new Error();
    await loadComments(POST_ID);
    showToast('삭제되었습니다.', 'success');
  } catch {
    showToast('삭제에 실패했습니다.', 'error');
  }
}

async function deletePost() {
  if (!confirm('게시글을 삭제하시겠습니까?')) return;
  try {
    const res = await Auth.fetchWithAuth(`/qna/${POST_ID}`, { method: 'DELETE' });
    if (!res.ok) throw new Error();
    window.location.href = '/qa';
  } catch {
    showToast('삭제에 실패했습니다.', 'error');
  }
}

// ── 유틸 ──────────────────────────────────────────────────

function parseUsername(token) {
  try {
    const payload = JSON.parse(atob(token.split('.')[1]));
    return payload.username;
  } catch {
    return null;
  }
}

function parseRole(token) {
  try {
    const payload = JSON.parse(atob(token.split('.')[1]));
    return payload.role;
  } catch {
    return null;
  }
}

function formatDate(dateStr) {
  if (!dateStr) return '';
  const d = new Date(dateStr);
  return d.toLocaleDateString('ko-KR', { year: 'numeric', month: '2-digit', day: '2-digit' });
}

function escapeHtml(str) {
  if (!str) return '';
  return str.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
}
