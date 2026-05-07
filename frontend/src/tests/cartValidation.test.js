import { describe, expect, test } from 'vitest';
import { calculateCartTotal, validateCartItem } from '../utils/cartValidation.js';

describe('Cart Validation Tests', () => {
  test('TC_CART_FE_001: Quantity null thi tra ve loi', () => {
    const result = validateCartItem({ productId: 'P001', quantity: null, stock: 10 });

    expect(result.valid).toBe(false);
    expect(result.error).toBe('So luong khong duoc rong');
  });

  test('TC_CART_FE_002: Quantity bang 0 thi tra ve loi', () => {
    const result = validateCartItem({ productId: 'P001', quantity: 0, stock: 10 });

    expect(result.valid).toBe(false);
    expect(result.error).toBe('So luong phai lon hon hoac bang 1');
  });

  test('TC_CART_FE_003: Quantity vuot ton kho thi tra ve loi', () => {
    const result = validateCartItem({ productId: 'P001', quantity: 11, stock: 10 });

    expect(result.valid).toBe(false);
    expect(result.error).toBe('So luong vuot qua ton kho hien tai');
  });

  test('TC_CART_FE_004: Quantity hop le thi validation thanh cong', () => {
    const result = validateCartItem({ productId: 'P001', quantity: 2, stock: 10 });

    expect(result.valid).toBe(true);
    expect(result.error).toBeNull();
  });
});

describe('Cart Total Calculation Tests', () => {
  test('TC_CART_TOTAL_001: Gio hang rong thi tong tien bang 0', () => {
    const result = calculateCartTotal([]);

    expect(result.subtotal).toBe(0);
    expect(result.discount).toBe(0);
    expect(result.total).toBe(0);
  });

  test('TC_CART_TOTAL_002: Tinh tong gia dung voi nhieu san pham', () => {
    const result = calculateCartTotal([
      { price: 15_000_000, quantity: 2 },
      { price: 500_000, quantity: 1 }
    ]);

    expect(result.subtotal).toBe(30_500_000);
    expect(result.total).toBe(30_500_000);
  });

  test('TC_CART_TOTAL_003: Ap dung ma giam gia theo phan tram', () => {
    const result = calculateCartTotal(
      [{ price: 10_000_000, quantity: 2 }],
      { type: 'PERCENT', value: 10 }
    );

    expect(result.subtotal).toBe(20_000_000);
    expect(result.discount).toBe(2_000_000);
    expect(result.total).toBe(18_000_000);
  });

  test('TC_CART_TOTAL_004: Tong gia sau khi xoa san pham', () => {
    const beforeDelete = [
      { price: 15_000_000, quantity: 2 },
      { price: 500_000, quantity: 1 }
    ];

    const afterDelete = beforeDelete.filter((_, index) => index !== 1);
    const result = calculateCartTotal(afterDelete);

    expect(result.total).toBe(30_000_000);
  });
});
