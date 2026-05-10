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

describe('Checkout Component Extra Coverage Tests', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    cartService.getCart.mockResolvedValue(cartWithItems);
    inventoryService.checkStockForItems.mockResolvedValue({ available: true, results: [], unavailableItems: [] });
  });

  test('TC_CHECKOUT_EXTRA_001: Ap dung coupon khong hop le thi hien thi loi', async () => {
    render(<CheckoutPage userId="user01" />);

    await waitFor(() => expect(screen.getByTestId('coupon-input')).toBeInTheDocument());
    fireEvent.change(screen.getByTestId('coupon-input'), { target: { value: 'INVALID' } });
    fireEvent.click(screen.getByTestId('apply-coupon-btn'));

    await waitFor(() => {
      expect(screen.getByTestId('checkout-error-toast')).toHaveTextContent('Ma giam gia khong hop le');
      expect(screen.getByTestId('discount-display')).toHaveTextContent('-0');
    });
  });

  test('TC_CHECKOUT_EXTRA_002: Khong cho dat hang khi dia chi giao hang rong', async () => {
    render(<CheckoutPage userId="user01" />);

    await waitFor(() => expect(screen.getByTestId('place-order-btn')).not.toBeDisabled());
    fireEvent.change(screen.getByTestId('shipping-address-input'), { target: { value: '   ' } });
    fireEvent.click(screen.getByTestId('place-order-btn'));

    await waitFor(() => {
      expect(screen.getByTestId('checkout-error-toast')).toHaveTextContent('Dia chi giao hang khong duoc de trong');
      expect(orderService.createOrder).not.toHaveBeenCalled();
    });
  });

  test('TC_CHECKOUT_EXTRA_003: Hien thi gio hang rong khi cart khong co san pham', async () => {
    cartService.getCart.mockResolvedValue({ success: true, message: '', cartTotal: 0, items: [] });

    render(<CheckoutPage userId="user01" />);

    await waitFor(() => {
      expect(screen.getByTestId('checkout-empty-message')).toHaveTextContent('Gio hang dang trong');
      expect(screen.getByTestId('check-inventory-btn')).toBeDisabled();
      expect(screen.getByTestId('place-order-btn')).toBeDisabled();
    });
  });

  test('TC_CHECKOUT_EXTRA_004: Goi callback quay lai gio hang', async () => {
    const onBackToCart = vi.fn();

    render(<CheckoutPage userId="user01" onBackToCart={onBackToCart} />);

    const backButton = await screen.findByTestId('back-to-cart-btn');
    fireEvent.click(backButton);

    expect(onBackToCart).toHaveBeenCalledTimes(1);
  });

  test('TC_CHECKOUT_EXTRA_005: Hien thi thong bao khi kiem tra ton kho thanh cong', async () => {
    render(<CheckoutPage userId="user01" />);

    await waitFor(() => expect(screen.getByTestId('check-inventory-btn')).not.toBeDisabled());
    fireEvent.click(screen.getByTestId('check-inventory-btn'));

    await waitFor(() => {
      expect(screen.getByTestId('inventory-warning')).toHaveTextContent('Tat ca san pham deu con du hang');
    });
  });

  test('TC_CHECKOUT_EXTRA_006: Hien thi loi khi khong tai duoc gio hang checkout', async () => {
    cartService.getCart.mockRejectedValue(new Error('Khong tai duoc gio hang checkout'));

    render(<CheckoutPage userId="user01" />);

    await waitFor(() => {
      expect(screen.getByTestId('checkout-error-toast')).toHaveTextContent('Khong tai duoc gio hang checkout');
    });
  });
});
