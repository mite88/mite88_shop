async function loadCart() {
  const loading = document.getElementById('cart-loading');
  const empty   = document.getElementById('cart-empty');
  const content = document.getElementById('cart-content');

  try {
    const res = await Auth.fetchWithAuth('/api/cart');
    if (!res || !res.ok) throw new Error();

    const cart = await res.json();
    loading.classList.add('hidden');

    if (!cart.items || cart.items.length === 0) {
      empty.classList.remove('hidden');
      return;
    }

    content.classList.remove('hidden');
    renderCartItems(cart);

  } catch {
    loading.classList.add('hidden');
    showToast('장바구니를 불러오지 못했습니다.', 'error');
  }
}

function renderCartItems(cart) {
  const tbody = document.getElementById('cart-items');
  const total = document.getElementById('cart-total');
  const orderBtn = document.querySelector('button[onclick="placeOrder()"]');

  let hasStockIssue = false;

  tbody.innerHTML = cart.items.map(item => {
    const noStock = item.stock === 0;
    const overStock = !noStock && item.quantity > item.stock;

    if (noStock || overStock) hasStockIssue = true;

    let qtyCell;
    if (noStock) {
      qtyCell = `<span class="badge badge-error">재고없음</span>`;
    } else {
      const plusDisabled = item.quantity >= item.stock ? 'btn-disabled' : '';
      qtyCell = `
        <div class="flex flex-col gap-1">
          <div class="flex items-center gap-2">
            <button class="btn btn-xs btn-outline"
                    onclick="updateCartItem(${item.cartItemId}, ${item.quantity - 1})">−</button>
            <span class="w-6 text-center">${item.quantity}</span>
            <button class="btn btn-xs btn-outline ${plusDisabled}"
                    onclick="${plusDisabled ? '' : `updateCartItem(${item.cartItemId}, ${item.quantity + 1})`}">+</button>
          </div>
          ${overStock ? `<span class="text-xs text-error">재고 ${item.stock}개만 구매 가능</span>` : ''}
        </div>`;
    }

    return `
    <tr class="${noStock || overStock ? 'bg-error/5' : ''}">
      <td class="font-medium">${item.productName}</td>
      <td>${item.price.toLocaleString()}원</td>
      <td>${qtyCell}</td>
      <td class="font-semibold">${item.subtotal.toLocaleString()}원</td>
      <td>
        <button class="btn btn-error btn-xs"
                onclick="removeCartItem(${item.cartItemId})">삭제</button>
      </td>
    </tr>`;
  }).join('');

  total.textContent = cart.totalPrice.toLocaleString() + '원';

  if (orderBtn) {
    if (hasStockIssue) {
      orderBtn.disabled = true;
      orderBtn.classList.add('btn-disabled');
      orderBtn.title = '재고가 부족한 상품이 있습니다. 수량을 조정해주세요.';
    } else {
      orderBtn.disabled = false;
      orderBtn.classList.remove('btn-disabled');
      orderBtn.title = '';
    }
  }
}

async function addToCart() {
  if (!Auth.isLoggedIn()) {
    showToast('로그인이 필요합니다.', 'warning');
    setTimeout(() => window.location.href = '/login', 1000);
    return;
  }
  if (!currentProduct) return;

  try {
    const res = await Auth.fetchWithAuth('/api/cart/items', {
      method: 'POST',
      body: JSON.stringify({ productId: currentProduct.id, quantity: currentQty })
    });
    if (!res || !res.ok) throw new Error();

    const cart = await res.json();
    const addedItem = cart.items.find(i => i.productId === currentProduct.id);
    // 담긴 수량이 재고 한도와 같으면 → 한도까지 채워진 것
    if (addedItem && addedItem.quantity === addedItem.stock) {
      showToast(`재고 한도(${addedItem.stock}개)까지만 담겼습니다.`, 'warning');
    } else {
      showToast('장바구니에 추가했습니다!', 'success');
    }
  } catch {
    showToast('장바구니 추가에 실패했습니다.', 'error');
  }
}

async function updateCartItem(cartItemId, newQty) {
  if (newQty < 1) {
    await removeCartItem(cartItemId);
    return;
  }
  try {
    const res = await Auth.fetchWithAuth(
      `/api/cart/items/${cartItemId}?quantity=${newQty}`,
      { method: 'PATCH' }
    );
    if (!res || !res.ok) throw new Error();
    const cart = await res.json();
    renderCartItems(cart);
  } catch {
    showToast('수량 변경에 실패했습니다.', 'error');
  }
}

async function removeCartItem(cartItemId) {
  try {
    const res = await Auth.fetchWithAuth(`/api/cart/items/${cartItemId}`, { method: 'DELETE' });
    if (!res || !res.ok) throw new Error();
    const cart = await res.json();

    if (!cart.items || cart.items.length === 0) {
      document.getElementById('cart-content').classList.add('hidden');
      document.getElementById('cart-empty').classList.remove('hidden');
    } else {
      renderCartItems(cart);
    }
    showToast('상품이 삭제되었습니다.', 'info');
  } catch {
    showToast('삭제에 실패했습니다.', 'error');
  }
}
