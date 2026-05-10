import { test } from '@playwright/test';
import CartPage from './pages/CartPage';

test.describe('Cart E2E Tests', () => {
  let cartPage: CartPage;

  test.beforeEach(async ({ page }) => {
    cartPage = new CartPage(page);
    await cartPage.mockEmptyCart();
    await cartPage.mockAddToCartSuccess();
  });

  test('TC_CART_E2E_001: Them san pham vao gio hang thanh cong', async () => {
    await cartPage.goto();

    await cartPage.addProductToCart(2);

    await cartPage.expectCartBadge(2);
    await cartPage.expectSuccessMessage('Them vao gio hang thanh cong');
    await cartPage.expectCartTotal('30.000.000');
  });

  test('TC_CART_E2E_002: Hien thi validation khi vuot ton kho', async () => {
    await cartPage.goto();

    await cartPage.addProductToCart(999);

    await cartPage.expectErrorMessage('So luong vuot qua ton kho hien tai');
  });
});
