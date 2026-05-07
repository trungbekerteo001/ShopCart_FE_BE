import { expect, test } from '@playwright/test';

test.describe('Cart E2E Tests', () => {
  test.beforeEach(async ({ page }) => {
    await page.route('http://localhost:8080/api/cart', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ success: true, message: '', cartTotal: 0, items: [] })
      });
    });

    await page.route('http://localhost:8080/api/cart/add', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          success: true,
          message: 'Them vao gio hang thanh cong',
          cartTotal: 30000000,
          items: [
            {
              productId: 'P001',
              productName: 'Laptop Dell',
              price: 15000000,
              quantity: 2,
              lineTotal: 30000000
            }
          ]
        })
      });
    });
  });

  test('TC_CART_E2E_001: Them san pham vao gio hang thanh cong', async ({ page }) => {
    await page.goto('/');

    await page.getByTestId('quantity-input').fill('2');
    await page.getByTestId('add-to-cart-btn').click();

    await expect(page.getByTestId('cart-badge')).toHaveText('2');
    await expect(page.getByTestId('success-toast')).toContainText('Them vao gio hang thanh cong');
    await expect(page.getByTestId('cart-total')).toContainText('30.000.000');
  });

  test('TC_CART_E2E_002: Hien thi validation khi vuot ton kho', async ({ page }) => {
    await page.goto('/');

    await page.getByTestId('quantity-input').fill('999');
    await page.getByTestId('add-to-cart-btn').click();

    await expect(page.getByTestId('error-toast')).toContainText('So luong vuot qua ton kho hien tai');
  });
});
