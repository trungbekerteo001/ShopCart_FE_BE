import { beforeEach, describe, expect, test, vi } from 'vitest';
import axios from 'axios';
import {
  addToCart,
  getCart,
  removeFromCart,
  updateQuantity
} from '../services/cartService.js';

vi.mock('axios', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn()
  }
}));

const API_BASE_URL = 'http://localhost:8080';
const userHeaders = {
  'Content-Type': 'application/json',
  'X-USER-ID': 'user01'
};

describe('Cart Service API Tests', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  test('TC_CART_SERVICE_001: getCart goi dung endpoint va tra ve data', async () => {
    const mockCart = {
      success: true,
      cartTotal: 30_000_000,
      items: [{ productId: 'P001', quantity: 2 }]
    };
    axios.get.mockResolvedValue({ data: mockCart });

    const result = await getCart('user01');

    expect(axios.get).toHaveBeenCalledWith(`${API_BASE_URL}/api/cart`, {
      headers: userHeaders
    });
    expect(result).toEqual(mockCart);
  });

  test('TC_CART_SERVICE_002: addToCart goi POST voi body va header dung', async () => {
    const item = { productId: 'P001', quantity: 2 };
    const mockResponse = {
      success: true,
      message: 'Them vao gio hang thanh cong',
      cartTotal: 30_000_000
    };
    axios.post.mockResolvedValue({ data: mockResponse });

    const result = await addToCart('user01', item);

    expect(axios.post).toHaveBeenCalledWith(`${API_BASE_URL}/api/cart/add`, item, {
      headers: userHeaders
    });
    expect(result).toEqual(mockResponse);
  });

  test('TC_CART_SERVICE_003: updateQuantity goi PUT voi body va header dung', async () => {
    const item = { productId: 'P002', quantity: 5 };
    const mockResponse = {
      success: true,
      message: 'Cap nhat so luong thanh cong',
      cartTotal: 2_500_000
    };
    axios.put.mockResolvedValue({ data: mockResponse });

    const result = await updateQuantity('user01', item);

    expect(axios.put).toHaveBeenCalledWith(`${API_BASE_URL}/api/cart/update`, item, {
      headers: userHeaders
    });
    expect(result.cartTotal).toBe(2_500_000);
  });

  test('TC_CART_SERVICE_004: removeFromCart goi DELETE dung productId', async () => {
    const mockResponse = {
      success: true,
      message: 'Xoa san pham khoi gio hang thanh cong',
      items: []
    };
    axios.delete.mockResolvedValue({ data: mockResponse });

    const result = await removeFromCart('user01', 'P001');

    expect(axios.delete).toHaveBeenCalledWith(`${API_BASE_URL}/api/cart/remove/P001`, {
      headers: userHeaders
    });
    expect(result.items).toEqual([]);
  });

  test('TC_CART_SERVICE_005: addToCart dung user mac dinh khi userId rong', async () => {
    const item = { productId: 'P001', quantity: 1 };
    axios.post.mockResolvedValue({ data: { success: true } });

    await addToCart('', item);

    expect(axios.post).toHaveBeenCalledWith(`${API_BASE_URL}/api/cart/add`, item, {
      headers: userHeaders
    });
  });

  test('TC_CART_SERVICE_006: nem loi tu response message khi API loi', async () => {
    axios.get.mockRejectedValue({
      response: { data: { message: 'Khong tim thay gio hang' } }
    });

    await expect(getCart('user01')).rejects.toThrow('Khong tim thay gio hang');
  });

  test('TC_CART_SERVICE_007: nem loi tu error.message khi khong co response message', async () => {
    axios.delete.mockRejectedValue(new Error('Network Error'));

    await expect(removeFromCart('user01', 'P001')).rejects.toThrow('Network Error');
  });
});
