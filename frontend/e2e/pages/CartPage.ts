import { expect, type Locator, type Page } from '@playwright/test';

export type CartItemFixture = {
  productId: string;
  productName: string;
  price: number;
  quantity: number;
  lineTotal: number;
};

export class CartPage {
  readonly page: Page;
  readonly productSelect: Locator;
  readonly quantityInput: Locator;
  readonly addToCartButton: Locator;
  readonly cartBadge: Locator;
  readonly successToast: Locator;
  readonly errorToast: Locator;
  readonly cartTotal: Locator;
  readonly emptyCartMessage: Locator;
  readonly checkoutLink: Locator;

  constructor(page: Page) {
    this.page = page;
    this.productSelect = page.getByTestId('product-select');
    this.quantityInput = page.getByTestId('quantity-input');
    this.addToCartButton = page.getByTestId('add-to-cart-btn');
    this.cartBadge = page.getByTestId('cart-badge');
    this.successToast = page.getByTestId('success-toast');
    this.errorToast = page.getByTestId('error-toast');
    this.cartTotal = page.getByTestId('cart-total');
    this.emptyCartMessage = page.getByTestId('empty-cart-message');
    this.checkoutLink = page.getByTestId('checkout-link');
  }

  async goto() {
    await this.page.goto('/');
  }

  async selectProduct(productId: string) {
    await this.productSelect.selectOption(productId);
  }

  async fillQuantity(quantity: string | number) {
    await this.quantityInput.fill(String(quantity));
  }

  async clickAddToCart() {
    await this.addToCartButton.click();
  }

  async addProductToCart(quantity: string | number, productId = 'P001') {
    await this.selectProduct(productId);
    await this.fillQuantity(quantity);
    await this.clickAddToCart();
  }

  async expectCartBadge(value: string | number) {
    await expect(this.cartBadge).toHaveText(String(value));
  }

  async expectSuccessMessage(text: string | RegExp) {
    await expect(this.successToast).toContainText(text);
  }

  async expectErrorMessage(text: string | RegExp) {
    await expect(this.errorToast).toContainText(text);
  }

  async expectCartTotal(text: string | RegExp) {
    await expect(this.cartTotal).toContainText(text);
  }

  async mockEmptyCart() {
    await this.page.route('**/api/cart', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ success: true, message: '', cartTotal: 0, items: [] })
      });
    });
  }

  async mockAddToCartSuccess(items: CartItemFixture[] = [CartPage.defaultLaptopItem()]) {
    const cartTotal = items.reduce((sum, item) => sum + item.lineTotal, 0);

    await this.page.route('**/api/cart/add', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          success: true,
          message: 'Them vao gio hang thanh cong',
          cartTotal,
          items
        })
      });
    });
  }

  static defaultLaptopItem(): CartItemFixture {
    return {
      productId: 'P001',
      productName: 'Laptop Dell',
      price: 15000000,
      quantity: 2,
      lineTotal: 30000000
    };
  }
}

export default CartPage;
