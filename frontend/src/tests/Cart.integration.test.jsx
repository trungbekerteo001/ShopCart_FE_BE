import React from 'react';
import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { beforeEach, describe, expect, test, vi } from 'vitest';
import CartComponent from '../components/CartComponent.jsx';
import * as cartService from '../services/cartService.js';

vi.mock('../services/cartService.js');

const emptyCartResponse = {
  success: true,
  message: '',
  cartTotal: 0,
  items: []
};

const cartWithLaptop = {
  success: true,
  message: 'Them vao gio hang thanh cong',
  cartTotal: 30_000_000,
  items: [
    {
      productId: 'P001',
      productName: 'Laptop Dell',
      price: 15_000_000,
      quantity: 2,
      lineTotal: 30_000_000
    }
  ]
};

describe('Cart Component Integration Tests', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    cartService.getCart.mockResolvedValue(emptyCartResponse);
  });

  test('TC_CART_INT_001: Hien thi gio hang rong khi chua co san pham', async () => {
    render(<CartComponent userId="user01" />);

    await waitFor(() => {
      expect(screen.getByTestId('empty-cart-message')).toHaveTextContent('Gio hang dang trong');
    });
  });

  test('TC_CART_INT_002: Them san pham thanh cong va hien thi tong tien', async () => {
    cartService.addToCart.mockResolvedValue(cartWithLaptop);

    render(<CartComponent userId="user01" />);

    fireEvent.change(screen.getByTestId('quantity-input'), { target: { value: '2' } });
    fireEvent.click(screen.getByTestId('add-to-cart-btn'));

    await waitFor(() => {
      expect(cartService.addToCart).toHaveBeenCalledWith('user01', {
        productId: 'P001',
        quantity: 2
      });
      expect(screen.getByTestId('success-toast')).toHaveTextContent('Them vao gio hang thanh cong');
      expect(screen.getByTestId('cart-badge')).toHaveTextContent('2');
      expect(screen.getByTestId('cart-total')).toHaveTextContent('30.000.000');
    });
  });

  test('TC_CART_INT_003: Hien thi loi khi so luong vuot ton kho', async () => {
    render(<CartComponent userId="user01" />);

    fireEvent.change(screen.getByTestId('quantity-input'), { target: { value: '999' } });
    fireEvent.click(screen.getByTestId('add-to-cart-btn'));

    await waitFor(() => {
      expect(screen.getByTestId('error-toast')).toHaveTextContent('So luong vuot qua ton kho hien tai');
      expect(cartService.addToCart).not.toHaveBeenCalled();
    });
  });
});
