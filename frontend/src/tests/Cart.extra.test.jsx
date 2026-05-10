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

describe('Cart Component Extra Coverage Tests', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    cartService.getCart.mockResolvedValue(emptyCartResponse);
  });

  test('TC_CART_EXTRA_001: Hien thi loi khi load gio hang that bai', async () => {
    cartService.getCart.mockRejectedValue(new Error('Khong tai duoc gio hang'));

    render(<CartComponent userId="user01" />);

    await waitFor(() => {
      expect(screen.getByTestId('error-toast')).toHaveTextContent('Khong tai duoc gio hang');
    });
  });

  test('TC_CART_EXTRA_002: Cap nhat so luong san pham trong gio thanh cong', async () => {
    const updatedCart = {
      success: true,
      message: 'Cap nhat so luong thanh cong',
      cartTotal: 45_000_000,
      items: [{ ...cartWithLaptop.items[0], quantity: 3, lineTotal: 45_000_000 }]
    };
    cartService.getCart.mockResolvedValue(cartWithLaptop);
    cartService.updateQuantity.mockResolvedValue(updatedCart);

    render(<CartComponent userId="user01" />);

    const quantityInput = await screen.findByTestId('cart-qty-P001');
    fireEvent.change(quantityInput, { target: { value: '3' } });
    fireEvent.blur(quantityInput);

    await waitFor(() => {
      expect(cartService.updateQuantity).toHaveBeenCalledWith('user01', {
        productId: 'P001',
        quantity: 3
      });
      expect(screen.getByTestId('success-toast')).toHaveTextContent('Cap nhat so luong thanh cong');
      expect(screen.getByTestId('cart-badge')).toHaveTextContent('3');
    });
  });

  test('TC_CART_EXTRA_003: Xoa san pham khoi gio hang thanh cong', async () => {
    cartService.getCart.mockResolvedValue(cartWithLaptop);
    cartService.removeFromCart.mockResolvedValue({
      ...emptyCartResponse,
      message: 'Xoa san pham khoi gio hang thanh cong'
    });

    render(<CartComponent userId="user01" />);

    const removeButton = await screen.findByTestId('remove-P001');
    fireEvent.click(removeButton);

    await waitFor(() => {
      expect(cartService.removeFromCart).toHaveBeenCalledWith('user01', 'P001');
      expect(screen.getByTestId('success-toast')).toHaveTextContent('Xoa san pham khoi gio hang thanh cong');
      expect(screen.getByTestId('empty-cart-message')).toHaveTextContent('Gio hang dang trong');
    });
  });

  test('TC_CART_EXTRA_004: Goi callback checkout khi bam chuyen sang checkout', async () => {
    const onCheckout = vi.fn();
    cartService.getCart.mockResolvedValue(cartWithLaptop);

    render(<CartComponent userId="user01" onCheckout={onCheckout} />);

    const checkoutButton = await screen.findByTestId('checkout-link');
    fireEvent.click(checkoutButton);

    expect(onCheckout).toHaveBeenCalledTimes(1);
  });
});
