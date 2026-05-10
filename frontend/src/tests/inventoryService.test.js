import { beforeEach, describe, expect, test, vi } from 'vitest';
import axios from 'axios';
import {
  checkStock,
  checkStockForItems,
  getInventory
} from '../services/inventoryService.js';

vi.mock('axios', () => ({
  default: {
    get: vi.fn()
  }
}));

const API_BASE_URL = 'http://localhost:8080';

describe('Inventory Service API Tests', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  test('TC_INVENTORY_SERVICE_001: getInventory goi dung endpoint va tra ve data', async () => {
    const mockInventory = {
      success: true,
      productId: 'P001',
      productName: 'Laptop Dell',
      stock: 10,
      available: true
    };
    axios.get.mockResolvedValue({ data: mockInventory });

    const result = await getInventory('P001');

    expect(axios.get).toHaveBeenCalledWith(`${API_BASE_URL}/api/inventory/P001`);
    expect(result).toEqual(mockInventory);
  });

  test('TC_INVENTORY_SERVICE_002: checkStock truyen quantity qua query params', async () => {
    const mockCheckResult = {
      productId: 'P001',
      requestedQuantity: 2,
      available: true
    };
    axios.get.mockResolvedValue({ data: mockCheckResult });

    const result = await checkStock('P001', 2);

    expect(axios.get).toHaveBeenCalledWith(`${API_BASE_URL}/api/inventory/P001/check`, {
      params: { quantity: 2 }
    });
    expect(result.available).toBe(true);
  });

  test('TC_INVENTORY_SERVICE_003: checkStockForItems tra ve available true khi tat ca du hang', async () => {
    axios.get
      .mockResolvedValueOnce({ data: { productId: 'P001', available: true } })
      .mockResolvedValueOnce({ data: { productId: 'P002', available: true } });

    const result = await checkStockForItems([
      { productId: 'P001', quantity: 2 },
      { productId: 'P002', quantity: 1 }
    ]);

    expect(axios.get).toHaveBeenCalledTimes(2);
    expect(result.available).toBe(true);
    expect(result.results).toHaveLength(2);
    expect(result.unavailableItems).toHaveLength(0);
  });

  test('TC_INVENTORY_SERVICE_004: checkStockForItems tra ve unavailableItems khi co san pham khong du hang', async () => {
    axios.get
      .mockResolvedValueOnce({ data: { productId: 'P001', available: false, stock: 1 } })
      .mockResolvedValueOnce({ data: { productId: 'P002', available: true, stock: 50 } });

    const result = await checkStockForItems([
      { productId: 'P001', quantity: 2 },
      { productId: 'P002', quantity: 1 }
    ]);

    expect(result.available).toBe(false);
    expect(result.unavailableItems).toEqual([{ productId: 'P001', available: false, stock: 1 }]);
  });

  test('TC_INVENTORY_SERVICE_005: checkStockForItems xu ly danh sach rong', async () => {
    const result = await checkStockForItems();

    expect(axios.get).not.toHaveBeenCalled();
    expect(result.available).toBe(true);
    expect(result.results).toEqual([]);
    expect(result.unavailableItems).toEqual([]);
  });

  test('TC_INVENTORY_SERVICE_006: getInventory nem loi tu response message khi API loi', async () => {
    axios.get.mockRejectedValue({
      response: { data: { message: 'Khong tim thay san pham trong kho' } }
    });

    await expect(getInventory('INVALID')).rejects.toThrow('Khong tim thay san pham trong kho');
  });

  test('TC_INVENTORY_SERVICE_007: checkStock nem loi mac dinh khi error khong co message', async () => {
    axios.get.mockRejectedValue({});

    await expect(checkStock('P001', 2)).rejects.toThrow('Co loi xay ra khi kiem tra ton kho');
  });
});
