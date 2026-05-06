export function calculateOrderPrice(items = [], coupon = null, shippingFee = 0) {
  const normalizedItems = Array.isArray(items) ? items : [];
  const subtotal = normalizedItems.reduce((sum, item) => {
    const price = Number(item.price || 0);
    const quantity = Number(item.quantity || 0);
    return sum + price * quantity;
  }, 0);

  let discount = 0;

  if (coupon?.type === 'PERCENT') {
    const percent = Number(coupon.value || 0);
    discount = Math.round(subtotal * (percent / 100));
  }

  if (coupon?.type === 'FIXED') {
    discount = Number(coupon.value || 0);
  }

  discount = Math.min(discount, subtotal);
  const shipping = Math.max(Number(shippingFee || 0), 0);
  const total = Math.max(subtotal - discount + shipping, 0);

  return {
    subtotal,
    discount,
    shipping,
    total
  };
}

export function checkInventoryAvailability(items = []) {
  const unavailableItems = (Array.isArray(items) ? items : []).filter((item) => {
    const quantity = Number(item.quantity || 0);
    const stock = Number(item.stock ?? Number.MAX_SAFE_INTEGER);
    return quantity > stock;
  });

  return {
    available: unavailableItems.length === 0,
    unavailableItems,
    message: unavailableItems.length === 0
      ? 'Tat ca san pham deu con du hang'
      : 'Mot so san pham khong du ton kho'
  };
}

export function resolveCoupon(code) {
  const normalizedCode = String(code || '').trim().toUpperCase();

  if (!normalizedCode) {
    return null;
  }

  const coupons = {
    SALE10: { code: 'SALE10', type: 'PERCENT', value: 10 },
    SALE20: { code: 'SALE20', type: 'PERCENT', value: 20 },
    FIXED100K: { code: 'FIXED100K', type: 'FIXED', value: 100_000 }
  };

  return coupons[normalizedCode] || null;
}

export function formatCurrency(value) {
  return new Intl.NumberFormat('vi-VN').format(Number(value || 0));
}
