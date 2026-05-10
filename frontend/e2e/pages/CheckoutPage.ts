import { expect, type Locator, type Page } from '@playwright/test';

export type CheckoutCartItemFixture = {
  productId: string;
  productName: string;
  price: number;
  quantity: number;
  lineTotal: number;
};

export const defaultCheckoutCart = {
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
  ] satisfies CheckoutCartItemFixture[]
};

export class CheckoutPage {
  readonly page: Page;
  readonly checkoutLink: Locator;
  readonly heading: Locator;
  readonly subtotalDisplay: Locator;
  readonly discountDisplay: Locator;
  readonly shippingDisplay: Locator;
  readonly totalDisplay: Locator;
  readonly couponInput: Locator;
  readonly applyCouponButton: Locator;
  readonly checkInventoryButton: Locator;
  readonly inventoryWarning: Locator;
  readonly shippingAddressInput: Locator;
  readonly paymentMethodSelect: Locator;
  readonly placeOrderButton: Locator;
  readonly successToast: Locator;
  readonly errorToast: Locator;
  readonly orderSuccess: Locator;

  constructor(page: Page) {
    this.page = page;
    this.checkoutLink = page.getByTestId('checkout-link');
    this.heading = page.getByRole('heading', { name: 'Checkout' });
    this.subtotalDisplay = page.getByTestId('subtotal-display');
    this.discountDisplay = page.getByTestId('discount-display');
    this.shippingDisplay = page.getByTestId('shipping-display');
    this.totalDisplay = page.getByTestId('total-display');
    this.couponInput = page.getByTestId('coupon-input');
    this.applyCouponButton = page.getByTestId('apply-coupon-btn');
    this.checkInventoryButton = page.getByTestId('check-inventory-btn');
    this.inventoryWarning = page.getByTestId('inventory-warning');
    this.shippingAddressInput = page.getByTestId('shipping-address-input');
    this.paymentMethodSelect = page.getByTestId('payment-method-select');
    this.placeOrderButton = page.getByTestId('place-order-btn');
    this.successToast = page.getByTestId('checkout-success-toast');
    this.errorToast = page.getByTestId('checkout-error-toast');
    this.orderSuccess = page.getByTestId('order-success');
  }

  async gotoCart() {
    await this.page.goto('/');
  }

  async goToCheckout() {
    await this.checkoutLink.click();
    await expect(this.heading).toBeVisible();
  }

  async applyCoupon(code: string) {
    await this.couponInput.fill(code);
    await this.applyCouponButton.click();
  }

  async fillShippingAddress(address: string) {
    await this.shippingAddressInput.fill(address);
  }

  async selectPaymentMethod(method: 'COD' | 'BANK_TRANSFER') {
    await this.paymentMethodSelect.selectOption(method);
  }

  async placeOrder() {
    await this.placeOrderButton.click();
  }

  async expectSubtotal(text: string | RegExp) {
    await expect(this.subtotalDisplay).toContainText(text);
  }

  async expectDiscount(text: string | RegExp) {
    await expect(this.discountDisplay).toContainText(text);
  }

  async expectTotal(text: string | RegExp) {
    await expect(this.totalDisplay).toContainText(text);
  }

  async expectSuccessMessage(text: string | RegExp) {
    await expect(this.successToast).toContainText(text);
  }

  async expectOrderSuccess(text: string | RegExp) {
    await expect(this.orderSuccess).toContainText(text);
  }

  async mockCartWithItems(cart = defaultCheckoutCart) {
    await this.page.route('**/api/cart', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(cart)
      });
    });
  }

  async mockInventoryAvailable(available = true) {
    await this.page.route('**/api/inventory/*/check**', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ available })
      });
    });
  }

  async mockCreateOrderSuccess() {
    await this.page.route('**/api/orders', async (route) => {
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
  }
}

export default CheckoutPage;
