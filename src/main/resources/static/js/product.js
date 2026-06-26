async function loadProducts(category = null) {
  const grid = document.getElementById('product-grid');
  grid.innerHTML = '<div class="col-span-full flex justify-center py-12"><span class="loading loading-spinner loading-lg"></span></div>';

  const url = category ? `/api/products?category=${encodeURIComponent(category)}` : '/api/products';

  try {
    const res = await fetch(url);
    const products = await res.json();

    if (!products.length) {
      grid.innerHTML = '<div class="col-span-full text-center py-12 text-base-content/50">상품이 없습니다.</div>';
      return;
    }

    grid.innerHTML = products.map(p => {
      const outOfStock = p.stock === 0;
      return `
      <div class="card shadow-md transition-shadow ${outOfStock ? 'bg-base-200 opacity-70' : 'bg-base-100 hover:shadow-xl cursor-pointer'}"
           onclick="${outOfStock ? '' : `window.location.href='/products/${p.id}'`}">
        <div class="card-body">
          <div class="flex gap-1 flex-wrap">
            <div class="badge badge-secondary badge-sm">${p.category}</div>
            ${outOfStock ? '<div class="badge badge-error badge-sm">재고없음</div>' : ''}
          </div>
          <h2 class="card-title text-base mt-1">${p.name}</h2>
          <p class="text-sm text-base-content/60 line-clamp-2">${p.description || ''}</p>
          <div class="flex justify-between items-center mt-3">
            <span class="text-lg font-bold ${outOfStock ? 'text-base-content/40' : 'text-primary'}">${p.price.toLocaleString()}원</span>
            <span class="text-xs ${outOfStock ? 'text-error font-semibold' : 'text-base-content/40'}">
              ${outOfStock ? '재고없음' : `재고 ${p.stock}`}
            </span>
          </div>
        </div>
      </div>
    `}).join('');

  } catch {
    showToast('상품 목록을 불러오지 못했습니다.', 'error');
  }
}

let currentQty = 1;
let currentProduct = null;

async function loadProductDetail(productId) {
  if (!productId) return;

  try {
    const res = await fetch(`/api/products/${productId}`);
    if (!res.ok) throw new Error();

    currentProduct = await res.json();
    currentQty = 1;

    document.getElementById('product-skeleton').classList.add('hidden');
    document.getElementById('product-detail').classList.remove('hidden');

    document.getElementById('product-name').textContent        = currentProduct.name;
    document.getElementById('product-description').textContent = currentProduct.description || '';
    document.getElementById('product-price').textContent       = currentProduct.price.toLocaleString() + '원';
    document.getElementById('product-stock').textContent       = currentProduct.stock;
    document.getElementById('product-category').textContent    = currentProduct.category;

    const outOfStock = currentProduct.stock === 0;
    document.getElementById('qty-controls').classList.toggle('hidden', outOfStock);
    document.getElementById('out-of-stock-label').classList.toggle('hidden', !outOfStock);
    document.getElementById('add-to-cart-btn').disabled = outOfStock;
    if (outOfStock) {
      document.getElementById('add-to-cart-btn').textContent = '재고없음';
      document.getElementById('add-to-cart-btn').classList.add('btn-disabled');
    }

  } catch {
    showToast('상품 정보를 불러오지 못했습니다.', 'error');
  }
}

function changeQty(delta) {
  if (!currentProduct || currentProduct.stock === 0) return;
  const max = currentProduct.stock;
  currentQty = Math.min(max, Math.max(1, currentQty + delta));
  document.getElementById('qty-display').textContent = currentQty;
}
