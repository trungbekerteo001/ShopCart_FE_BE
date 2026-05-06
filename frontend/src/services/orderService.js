import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

function buildHeaders(userId) {
  return {
    'Content-Type': 'application/json',
    'X-USER-ID': userId || 'user01'
  };
}

function normalizeError(error) {
  return error.response?.data?.message || error.message || 'Co loi xay ra khi goi API Order';
}

export async function createOrder(userId = 'user01', orderRequest) {
  try {
    const response = await axios.post(`${API_BASE_URL}/api/orders`, orderRequest, {
      headers: buildHeaders(userId)
    });
    return response.data;
  } catch (error) {
    throw new Error(normalizeError(error));
  }
}

export async function getOrderById(orderId) {
  try {
    const response = await axios.get(`${API_BASE_URL}/api/orders/${orderId}`);
    return response.data;
  } catch (error) {
    throw new Error(normalizeError(error));
  }
}

export async function cancelOrder(orderId) {
  try {
    const response = await axios.put(`${API_BASE_URL}/api/orders/${orderId}/cancel`);
    return response.data;
  } catch (error) {
    throw new Error(normalizeError(error));
  }
}
