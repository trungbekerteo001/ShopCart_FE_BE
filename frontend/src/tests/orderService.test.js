import { beforeEach, describe, expect, test, vi } from 'vitest';
import axios from 'axios';
import {
  cancelOrder,
  createOrder,
  getOrderById
} from '../services/orderService.js';

vi.mock('axios', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn()
  }
}));

const API_BASE_URL = 'http://localhost:8080';
const userHeaders = {
  'Content-Type': 'application/json',
  'X-USER-ID': 'user01'
};

describe('Order Service API Tests', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  test('TC_ORDER_SERVICE_001: createOrder goi POST dung endpoint, body va header', async () => {
    const request = {
      items: [{ productId: 'P001', quantity: 2 }],
      couponCode: 'SALE10',
      shippingFee: 50_000,
      shippingAddress: '123 Nguyen Trai, TP.HCM',
      paymentMethod: 'COD'
    };
    const mockOrder = {
      orderId: 'ORD-001',
      status: 'PENDING',
      totalPrice: 27_050_000
    };
    axios.post.mockResolvedValue({ data: mockOrder });

    const result = await createOrder('user01', request);

    expect(axios.post).toHaveBeenCalledWith(`${API_BASE_URL}/api/orders`, request, {
      headers: userHeaders
    });
    expect(result).toEqual(mockOrder);
  });

  test('TC_ORDER_SERVICE_002: createOrder dung user mac dinh khi userId rong', async () => {
    const request = { items: [{ productId: 'P002', quantity: 1 }] };
    axios.post.mockResolvedValue({ data: { orderId: 'ORD-002' } });

    await createOrder('', request);

    expect(axios.post).toHaveBeenCalledWith(`${API_BASE_URL}/api/orders`, request, {
      headers: userHeaders
    });
  });

  test('TC_ORDER_SERVICE_003: getOrderById goi dung endpoint', async () => {
    const mockOrder = {
      orderId: 'ORD-001',
      status: 'PENDING',
      totalPrice: 30_550_000
    };
    axios.get.mockResolvedValue({ data: mockOrder });

    const result = await getOrderById('ORD-001');

    expect(axios.get).toHaveBeenCalledWith(`${API_BASE_URL}/api/orders/ORD-001`);
    expect(result.status).toBe('PENDING');
  });

  test('TC_ORDER_SERVICE_004: cancelOrder goi dung endpoint huy don', async () => {
    const mockResponse = {
      orderId: 'ORD-001',
      status: 'CANCELLED'
    };
    axios.put.mockResolvedValue({ data: mockResponse });

    const result = await cancelOrder('ORD-001');

    expect(axios.put).toHaveBeenCalledWith(`${API_BASE_URL}/api/orders/ORD-001/cancel`);
    expect(result.status).toBe('CANCELLED');
  });

  test('TC_ORDER_SERVICE_005: createOrder nem loi tu response message khi API loi', async () => {
    axios.post.mockRejectedValue({
      response: { data: { message: 'Ton kho khong du de tao don hang' } }
    });

    await expect(createOrder('user01', { items: [] })).rejects.toThrow('Ton kho khong du de tao don hang');
  });

  test('TC_ORDER_SERVICE_006: getOrderById nem loi tu error.message khi loi mang', async () => {
    axios.get.mockRejectedValue(new Error('Network Error'));

    await expect(getOrderById('ORD-404')).rejects.toThrow('Network Error');
  });

  test('TC_ORDER_SERVICE_007: cancelOrder nem loi mac dinh khi error khong co message', async () => {
    axios.put.mockRejectedValue({});

    await expect(cancelOrder('ORD-001')).rejects.toThrow('Co loi xay ra khi goi API Order');
  });
});
