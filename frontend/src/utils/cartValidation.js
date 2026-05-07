/**
 * Kiem tra du lieu truoc khi them/cap nhat san pham vao gio hang.
 */
export function validateCartItem({ productId, quantity, stock, status = 'ACTIVE' }) {
  if (!productId || String(productId).trim() === '') {
    return { valid: false, error: 'Product ID khong duoc rong' };
  }

  if (quantity === null || quantity === undefined || quantity === '') {
    return { valid: false, error: 'So luong khong duoc rong' };
  }

  const numericQuantity = Number(quantity);

  if (!Number.isInteger(numericQuantity)) {
    return { valid: false, error: 'So luong phai la so nguyen' };
  }

  if (numericQuantity < 1) {
    return { valid: false, error: 'So luong phai lon hon hoac bang 1' };
  }

  if (status !== 'ACTIVE') {
    return { valid: false, error: 'San pham khong dang duoc ban' };
  }

  if (typeof stock === 'number' && numericQuantity > stock) {
    return { valid: false, error: 'So luong vuot qua ton kho hien tai' };
  }

  return { valid: true, error: null };
}

/**
 * Tinh tong gia tri gio hang.
 * coupon co the la:
 * - null: khong giam gia
 * - { type: 'PERCENT', value: 10 }: giam 10%
 * - { type: 'FIXED', value: 100000 }: giam 100.000d
 */
export function calculateCartTotal(items = [], coupon = null) {
  const subtotal = items.reduce((sum, item) => {
    const price = Number(item.price || 0);
    const quantity = Number(item.quantity || 0);
    return sum + price * quantity;
  }, 0);

  let discount = 0;

  if (coupon?.type === 'PERCENT') {
    discount = Math.round((subtotal * Number(coupon.value || 0)) / 100);
  }

  if (coupon?.type === 'FIXED') {
    discount = Number(coupon.value || 0);
  }

  discount = Math.min(discount, subtotal);

  return {
    subtotal,
    discount,
    total: subtotal - discount
  };
}

export function formatCurrency(value) {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
    maximumFractionDigits: 0
  }).format(Number(value || 0));
}
