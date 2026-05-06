import { expect, test } from '@playwright/test';

const cartWithItems = {
  success: true,
  message: '',
  cartTotal: 30500000,
  items: [
    {
      productId: 'P001',
      productName: 'Laptop Dell',
      price: 15000000,
      quantity: 2,
      lineTotal: 30000000
    },
    {
      productId: 'P002',
      productName: 'Mouse Logitech',
      price: 500000,
      quantity: 1,
      lineTotal: 500000
    }
  ]
};

test.describe('Purchase E2E Tests', () => {
  test.beforeEach(async ({ page }) => {
    await page.route('http://localhost:8080/api/cart', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(cartWithItems)
      });
    });

    await page.route('http://localhost:8080/api/inventory/*/check**', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ available: true })
      });
    });

    await page.route('http://localhost:8080/api/orders', async (route) => {
      await route.fulfill({
        status: 201,
        contentType: 'application/json',
        body: JSON.stringify({
          orderId: 'ORD-001',
          status: 'PENDING',
          subtotal: 30500000,
          discount: 3050000,
          shippingFee: 50000,
          totalPrice: 27500000
        })
      });
    });
  });

  test('TC_PURCHASE_E2E_001: Checkout thanh cong voi ma SALE10', async ({ page }) => {
    await page.goto('/');

    await page.getByTestId('checkout-link').click();
    await expect(page.getByRole('heading', { name: 'Checkout' })).toBeVisible();
    await expect(page.getByTestId('subtotal-display')).toContainText('30.500.000');

    await page.getByTestId('coupon-input').fill('SALE10');
    await page.getByTestId('apply-coupon-btn').click();

    await expect(page.getByTestId('discount-display')).toContainText('3.050.000');
    await expect(page.getByTestId('total-display')).toContainText('27.500.000');

    await page.getByTestId('place-order-btn').click();

    await expect(page.getByTestId('checkout-success-toast')).toContainText('Dat hang thanh cong');
    await expect(page.getByTestId('order-success')).toContainText('ORD-001');
  });
});
