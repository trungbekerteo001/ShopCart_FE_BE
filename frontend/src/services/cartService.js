import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

function buildHeaders(userId) {
  return {
    'Content-Type': 'application/json',
    'X-USER-ID': userId || 'user01'
  };
}

function normalizeError(error) {
  return error.response?.data?.message || error.message || 'Co loi xay ra khi goi API Cart';
}

export async function getCart(userId = 'user01') {
  try {
    const response = await axios.get(`${API_BASE_URL}/api/cart`, {
      headers: buildHeaders(userId)
    });
    return response.data;
  } catch (error) {
    throw new Error(normalizeError(error));
  }
}

export async function addToCart(userId = 'user01', item) {
  try {
    const response = await axios.post(`${API_BASE_URL}/api/cart/add`, item, {
      headers: buildHeaders(userId)
    });
    return response.data;
  } catch (error) {
    throw new Error(normalizeError(error));
  }
}

export async function updateQuantity(userId = 'user01', item) {
  try {
    const response = await axios.put(`${API_BASE_URL}/api/cart/update`, item, {
      headers: buildHeaders(userId)
    });
    return response.data;
  } catch (error) {
    throw new Error(normalizeError(error));
  }
}

export async function removeFromCart(userId = 'user01', productId) {
  try {
    const response = await axios.delete(`${API_BASE_URL}/api/cart/remove/${productId}`, {
      headers: buildHeaders(userId)
    });
    return response.data;
  } catch (error) {
    throw new Error(normalizeError(error));
  }
}
