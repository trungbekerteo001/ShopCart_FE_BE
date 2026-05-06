import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

function normalizeError(error) {
  return error.response?.data?.message || error.message || 'Co loi xay ra khi kiem tra ton kho';
}

export async function getInventory(productId) {
  try {
    const response = await axios.get(`${API_BASE_URL}/api/inventory/${productId}`);
    return response.data;
  } catch (error) {
    throw new Error(normalizeError(error));
  }
}

export async function checkStock(productId, quantity) {
  try {
    const response = await axios.get(`${API_BASE_URL}/api/inventory/${productId}/check`, {
      params: { quantity }
    });
    return response.data;
  } catch (error) {
    throw new Error(normalizeError(error));
  }
}

export async function checkStockForItems(items = []) {
  const results = await Promise.all(
    items.map((item) => checkStock(item.productId, item.quantity))
  );

  const unavailableItems = results.filter((result) => !result.available);

  return {
    available: unavailableItems.length === 0,
    results,
    unavailableItems
  };
}
