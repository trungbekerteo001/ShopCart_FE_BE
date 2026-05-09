import http from 'k6/http';
import { check, group, sleep } from 'k6';

const API_BASE_URL = __ENV.API_BASE_URL || 'http://localhost:8080';
const ORDER_PRODUCT_ID = __ENV.ORDER_PRODUCT_ID || 'P002';
const VUS = Number(__ENV.VUS || 5);
const ITERATIONS = Number(__ENV.ITERATIONS || 5);

export const options = {
  scenarios: {
    checkout_flow: {
      executor: 'per-vu-iterations',
      vus: VUS,
      iterations: ITERATIONS,
      maxDuration: __ENV.MAX_DURATION || '2m',
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.05'],
    http_req_duration: ['p(95)<800'],
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

export function setup() {
  const requiredOrders = VUS * ITERATIONS;
  const res = http.get(`${API_BASE_URL}/api/inventory/${ORDER_PRODUCT_ID}`);

  if (res.status === 200) {
    const stock = Number(res.json('stock'));
    if (Number.isFinite(stock) && stock < requiredOrders) {
      console.warn(
        `Canh bao: stock cua ${ORDER_PRODUCT_ID} = ${stock}, nho hon so order du kien = ${requiredOrders}. `
        + 'Hay restart backend H2 hoac giam VUS/ITERATIONS.'
      );
    }
  } else {
    console.warn(`Khong doc duoc inventory truoc khi test. Status = ${res.status}`);
  }
}

export default function () {
  // Mỗi iteration dùng 1 user riêng. Checkout sẽ trừ tồn kho thật.
  const userId = `perf-checkout-vu${__VU}-iter${__ITER}`;
  const params = jsonParams(userId);

  group('Checkout - check inventory', () => {
    const res = http.get(`${API_BASE_URL}/api/inventory/${ORDER_PRODUCT_ID}/check?quantity=1`, params);

    check(res, {
      'GET /api/inventory/{id}/check status is 200': (r) => r.status === 200,
      'Inventory available before order': (r) => {
        try {
          return r.json('available') === true;
        } catch (_) {
          return false;
        }
      },
    });
  });

  group('Checkout - create order', () => {
    const payload = JSON.stringify({
      items: [
        {
          productId: ORDER_PRODUCT_ID,
          quantity: 1,
        },
      ],
      couponCode: 'SALE10',
      shippingFee: 50000,
      shippingAddress: '123 Nguyen Hue, Quan 1, TP.HCM',
      paymentMethod: 'COD',
    });

    const res = http.post(`${API_BASE_URL}/api/orders`, payload, params);

    check(res, {
      'POST /api/orders status is 201': (r) => r.status === 201,
      'POST /api/orders returns orderId': (r) => {
        try {
          return typeof r.json('orderId') === 'string' && r.json('orderId').startsWith('ORD-');
        } catch (_) {
          return false;
        }
      },
      'POST /api/orders totalPrice > 0': (r) => {
        try {
          return Number(r.json('totalPrice')) > 0;
        } catch (_) {
          return false;
        }
      },
    });
  });

  sleep(1);
}
