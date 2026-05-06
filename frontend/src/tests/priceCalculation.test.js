import { describe, expect, test } from 'vitest';
import { calculateOrderPrice, checkInventoryAvailability, resolveCoupon } from '../utils/priceCalculation.js';

describe('Price Calculation Unit Tests', () => {
  test('TC_PRICE_001: Tinh tong gia khong co giam gia', () => {
    const items = [
      { price: 15_000_000, quantity: 2 },
      { price: 500_000, quantity: 1 }
    ];

    const result = calculateOrderPrice(items, null, 50_000);

    expect(result.subtotal).toBe(30_500_000);
    expect(result.discount).toBe(0);
    expect(result.shipping).toBe(50_000);
    expect(result.total).toBe(30_550_000);
  });

  test('TC_PRICE_002: Ap dung coupon giam 10 phan tram', () => {
    const items = [{ price: 15_000_000, quantity: 2 }];
    const result = calculateOrderPrice(items, { type: 'PERCENT', value: 10 }, 50_000);

    expect(result.subtotal).toBe(30_000_000);
    expect(result.discount).toBe(3_000_000);
    expect(result.total).toBe(27_050_000);
  });

  test('TC_PRICE_003: Ap dung coupon giam so tien co dinh', () => {
    const items = [{ price: 500_000, quantity: 1 }];
    const result = calculateOrderPrice(items, { type: 'FIXED', value: 100_000 }, 50_000);

    expect(result.discount).toBe(100_000);
    expect(result.total).toBe(450_000);
  });

  test('TC_PRICE_004: Kiem tra ton kho du hang', () => {
    const result = checkInventoryAvailability([
      { productId: 'P001', quantity: 2, stock: 10 },
      { productId: 'P002', quantity: 1, stock: 50 }
    ]);

    expect(result.available).toBe(true);
    expect(result.unavailableItems).toHaveLength(0);
  });

  test('TC_PRICE_005: Kiem tra ton kho khong du hang', () => {
    const result = checkInventoryAvailability([
      { productId: 'P001', quantity: 11, stock: 10 }
    ]);

    expect(result.available).toBe(false);
    expect(result.unavailableItems[0].productId).toBe('P001');
  });

  test('TC_PRICE_006: Resolve coupon hop le va khong hop le', () => {
    expect(resolveCoupon('SALE10')).toEqual({ code: 'SALE10', type: 'PERCENT', value: 10 });
    expect(resolveCoupon('INVALID')).toBeNull();
  });
});
