async function placeOrder() {
  if (!confirm('장바구니의 상품을 모두 주문하시겠습니까?')) return;

  try {
    const res = await Auth.fetchWithAuth('/api/orders', { method: 'POST' });
    if (!res || !res.ok) throw new Error();

    showToast('주문이 완료되었습니다!', 'success');
    setTimeout(() => window.location.href = '/orders', 1000);
  } catch {
    showToast('주문에 실패했습니다.', 'error');
  }
}

async function loadOrders() {
  const loading = document.getElementById('orders-loading');
  const empty   = document.getElementById('orders-empty');
  const list    = document.getElementById('orders-list');

  try {
    const res = await Auth.fetchWithAuth('/api/orders');
    if (!res || !res.ok) throw new Error();

    const orders = await res.json();
    loading.classList.add('hidden');

    if (!orders.length) {
      empty.classList.remove('hidden');
      return;
    }

    list.classList.remove('hidden');
    list.innerHTML = orders.map(order => `
      <div class="card bg-base-100 shadow">
        <div class="card-body">
          <div class="flex justify-between items-start">
            <div>
              <h3 class="font-bold text-lg">주문 #${order.orderId}</h3>
              <p class="text-sm text-base-content/50">
                ${new Date(order.createdAt).toLocaleDateString('ko-KR')}
              </p>
            </div>
            <div class="flex flex-col items-end gap-2">
              <span class="badge ${order.status === 'ORDERED' ? 'badge-success' : 'badge-error'}">
                ${order.status === 'ORDERED' ? '주문완료' : '취소됨'}
              </span>
              <span class="font-bold text-primary">${order.totalPrice.toLocaleString()}원</span>
            </div>
          </div>

          <div class="divider my-1"></div>

          <ul class="text-sm space-y-1">
            ${order.items.map(i => `
              <li class="flex justify-between">
                <span>${i.productName} × ${i.quantity}</span>
                <span>${i.subtotal.toLocaleString()}원</span>
              </li>
            `).join('')}
          </ul>

          ${order.status === 'ORDERED' ? `
            <div class="card-actions justify-end mt-3">
              <button onclick="cancelOrder(${order.orderId})"
                      class="btn btn-error btn-sm btn-outline">주문 취소</button>
            </div>
          ` : ''}
        </div>
      </div>
    `).join('');

  } catch {
    loading.classList.add('hidden');
    showToast('주문 내역을 불러오지 못했습니다.', 'error');
  }
}

async function cancelOrder(orderId) {
  if (!confirm('주문을 취소하시겠습니까?')) return;

  try {
    const res = await Auth.fetchWithAuth(`/api/orders/${orderId}/cancel`, { method: 'PATCH' });
    if (!res || !res.ok) throw new Error();
    showToast('주문이 취소되었습니다.', 'info');
    loadOrders();
  } catch {
    showToast('주문 취소에 실패했습니다.', 'error');
  }
}
