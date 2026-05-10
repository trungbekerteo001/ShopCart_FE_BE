import { test } from '@playwright/test';
import CheckoutPage from './pages/CheckoutPage';

test.describe('Purchase E2E Tests', () => {
  let checkoutPage: CheckoutPage;

  test.beforeEach(async ({ page }) => {
    checkoutPage = new CheckoutPage(page);
    await checkoutPage.mockCartWithItems();
    await checkoutPage.mockInventoryAvailable(true);
    await checkoutPage.mockCreateOrderSuccess();
  });

  test('TC_PURCHASE_E2E_001: Checkout thanh cong voi ma SALE10', async () => {
    await checkoutPage.gotoCart();
    await checkoutPage.goToCheckout();

    await checkoutPage.expectSubtotal('30.500.000');

    await checkoutPage.applyCoupon('SALE10');

    await checkoutPage.expectDiscount('3.050.000');
    await checkoutPage.expectTotal('27.500.000');

    await checkoutPage.placeOrder();

    await checkoutPage.expectSuccessMessage('Dat hang thanh cong');
    await checkoutPage.expectOrderSuccess('ORD-001');
  });
});
