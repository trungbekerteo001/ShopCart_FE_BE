import React from 'react';
import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { beforeEach, describe, expect, test, vi } from 'vitest';
import CheckoutPage from '../components/CheckoutPage.jsx';
import * as cartService from '../services/cartService.js';
import * as inventoryService from '../services/inventoryService.js';
import * as orderService from '../services/orderService.js';

vi.mock('../services/cartService.js');
vi.mock('../services/inventoryService.js');
vi.mock('../services/orderService.js');

const cartWithItems = {
  success: true,
  message: '',
  cartTotal: 30_500_000,
  items: [
    {
      productId: 'P001',
      productName: 'Laptop Dell',
      price: 15_000_000,
      quantity: 2,
      lineTotal: 30_000_000
    },
    {
      productId: 'P002',
      productName: 'Mouse Logitech',
      price: 500_000,
      quantity: 1,
      lineTotal: 500_000
    }
  ]
};

describe('Checkout Integration Tests', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    cartService.getCart.mockResolvedValue(cartWithItems);
    inventoryService.checkStockForItems.mockResolvedValue({ available: true, results: [], unavailableItems: [] });
  });

  test('TC_CHECKOUT_INT_001: Hien thi tong gia chinh xac', async () => {
    render(<CheckoutPage userId="user01" />);

    await waitFor(() => {
      expect(screen.getByTestId('subtotal-display')).toHaveTextContent('30.500.000');
      expect(screen.getByTestId('shipping-display')).toHaveTextContent('50.000');
      expect(screen.getByTestId('total-display')).toHaveTextContent('30.550.000');
    });
  });

  test('TC_CHECKOUT_INT_002: Ap dung ma SALE10 va tinh lai tong tien', async () => {
    render(<CheckoutPage userId="user01" />);

    await waitFor(() => expect(screen.getByTestId('subtotal-display')).toHaveTextContent('30.500.000'));

    fireEvent.change(screen.getByTestId('coupon-input'), { target: { value: 'SALE10' } });
    fireEvent.click(screen.getByTestId('apply-coupon-btn'));

    await waitFor(() => {
      expect(screen.getByTestId('checkout-success-toast')).toHaveTextContent('Ap dung ma SALE10 thanh cong');
      expect(screen.getByTestId('discount-display')).toHaveTextContent('3.050.000');
      expect(screen.getByTestId('total-display')).toHaveTextContent('27.500.000');
    });
  });

  test('TC_CHECKOUT_INT_003: Dat hang thanh cong', async () => {
    orderService.createOrder.mockResolvedValue({
      orderId: 'ORD-001',
      status: 'PENDING',
      totalPrice: 27_500_000
    });

    render(<CheckoutPage userId="user01" />);

    await waitFor(() => expect(screen.getByTestId('place-order-btn')).not.toBeDisabled());

    fireEvent.change(screen.getByTestId('coupon-input'), { target: { value: 'SALE10' } });
    fireEvent.click(screen.getByTestId('apply-coupon-btn'));
    fireEvent.click(screen.getByTestId('place-order-btn'));

    await waitFor(() => {
      expect(inventoryService.checkStockForItems).toHaveBeenCalledWith(cartWithItems.items);
      expect(orderService.createOrder).toHaveBeenCalledWith('user01', expect.objectContaining({
        couponCode: 'SALE10',
        shippingFee: 50_000,
        paymentMethod: 'COD'
      }));
      expect(screen.getByTestId('order-success')).toHaveTextContent('ORD-001');
    });
  });

  test('TC_CHECKOUT_INT_004: Hien thi canh bao khi ton kho khong du', async () => {
    inventoryService.checkStockForItems.mockResolvedValue({
      available: false,
      results: [],
      unavailableItems: [{ productId: 'P001' }]
    });

    render(<CheckoutPage userId="user01" />);

    await waitFor(() => expect(screen.getByTestId('check-inventory-btn')).not.toBeDisabled());
    fireEvent.click(screen.getByTestId('check-inventory-btn'));

    await waitFor(() => {
      expect(screen.getByTestId('inventory-warning')).toHaveTextContent('Mot so san pham khong du ton kho');
    });
  });
});
