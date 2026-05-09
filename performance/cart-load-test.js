import http from 'k6/http';
import { check, group, sleep } from 'k6';

const API_BASE_URL = __ENV.API_BASE_URL || 'http://localhost:8080';
const CART_PRODUCT_ID = __ENV.CART_PRODUCT_ID || 'P002';
const RAMP_UP = __ENV.RAMP_UP || '30s';
const STEADY_DURATION = __ENV.STEADY_DURATION || '1m';
const RAMP_DOWN = __ENV.RAMP_DOWN || '30s';
const TARGET_VUS = Number(__ENV.TARGET_VUS || 10);

export const options = {
  scenarios: {
    cart_flow: {
      executor: 'ramping-vus',
      stages: [
        { duration: RAMP_UP, target: Math.max(1, Math.floor(TARGET_VUS / 2)) },
        { duration: STEADY_DURATION, target: TARGET_VUS },
        { duration: RAMP_DOWN, target: 0 },
      ],
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.05'],
    http_req_duration: ['p(95)<500'],
    checks: ['rate>0.95'],
  },
  summaryTrendStats: ['min', 'avg', 'med', 'p(90)', 'p(95)', 'max'],
};

function jsonParams(userId) {
  return {
    headers: {
      'Content-Type': 'application/json',
      'X-USER-ID': userId,
    },
  };
}

export default function () {
  // Mỗi iteration dùng 1 user riêng để tránh cộng dồn quantity vượt tồn kho.
  const userId = `perf-cart-vu${__VU}-iter${__ITER}`;
  const params = jsonParams(userId);

  group('Cart - add product', () => {
    const payload = JSON.stringify({
      productId: CART_PRODUCT_ID,
      quantity: 1,
    });

    const res = http.post(`${API_BASE_URL}/api/cart/add`, payload, params);

    check(res, {
      'POST /api/cart/add status is 200': (r) => r.status === 200,
      'POST /api/cart/add success true': (r) => {
        try {
          return r.json('success') === true;
        } catch (_) {
          return false;
        }
      },
    });
  });

  group('Cart - get cart', () => {
    const res = http.get(`${API_BASE_URL}/api/cart`, params);

    check(res, {
      'GET /api/cart status is 200': (r) => r.status === 200,
      'GET /api/cart has items': (r) => {
        try {
          return Array.isArray(r.json('items')) && r.json('items').length >= 1;
        } catch (_) {
          return false;
        }
      },
    });
  });

  group('Cart - update quantity', () => {
    const payload = JSON.stringify({
      productId: CART_PRODUCT_ID,
      quantity: 2,
    });

    const res = http.put(`${API_BASE_URL}/api/cart/update`, payload, params);

    check(res, {
      'PUT /api/cart/update status is 200': (r) => r.status === 200,
      'PUT /api/cart/update cartTotal > 0': (r) => {
        try {
          return Number(r.json('cartTotal')) > 0;
        } catch (_) {
          return false;
        }
      },
    });
  });

  sleep(1);
}
