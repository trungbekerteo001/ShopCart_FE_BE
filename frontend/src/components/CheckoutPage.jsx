import { useEffect, useMemo, useState } from 'react';
import * as cartService from '../services/cartService.js';
import * as inventoryService from '../services/inventoryService.js';
import * as orderService from '../services/orderService.js';
import { calculateOrderPrice, formatCurrency, resolveCoupon } from '../utils/priceCalculation.js';

const SHIPPING_FEE = 50_000;

function emptyCart() {
  return {
    success: true,
    message: '',
    cartTotal: 0,
    items: []
  };
}

function mapCartItemsToOrderItems(items = []) {
  return items.map((item) => ({
    productId: item.productId,
    quantity: Number(item.quantity || 0)
  }));
}

export default function CheckoutPage({ userId = 'user01', onBackToCart }) {
  const [cart, setCart] = useState(emptyCart());
  const [couponCode, setCouponCode] = useState('');
  const [appliedCoupon, setAppliedCoupon] = useState(null);
  const [shippingAddress, setShippingAddress] = useState('123 Nguyen Trai, TP.HCM');
  const [paymentMethod, setPaymentMethod] = useState('COD');
  const [inventoryWarning, setInventoryWarning] = useState('');
  const [message, setMessage] = useState('');
  const [messageType, setMessageType] = useState('success');
  const [orderResult, setOrderResult] = useState(null);
  const [loading, setLoading] = useState(false);

  async function loadCart() {
    try {
      const data = await cartService.getCart(userId);
      setCart(data);
    } catch (error) {
      setMessageType('error');
      setMessage(error.message);
    }
  }

  useEffect(() => {
    loadCart();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [userId]);

  const cartItems = cart?.items || [];
  const price = useMemo(
    () => calculateOrderPrice(cartItems, appliedCoupon, SHIPPING_FEE),
    [cartItems, appliedCoupon]
  );

  function showMessage(type, text) {
    setMessageType(type);
    setMessage(text);
  }

  function handleApplyCoupon() {
    const coupon = resolveCoupon(couponCode);

    if (!coupon) {
      setAppliedCoupon(null);
      showMessage('error', 'Ma giam gia khong hop le');
      return;
    }

    setAppliedCoupon(coupon);
    showMessage('success', `Ap dung ma ${coupon.code} thanh cong`);
  }

  async function handleCheckInventory() {
    if (cartItems.length === 0) {
      setInventoryWarning('Gio hang dang trong');
      return false;
    }

    try {
      const result = await inventoryService.checkStockForItems(cartItems);

      if (!result.available) {
        setInventoryWarning('Mot so san pham khong du ton kho');
        return false;
      }

      setInventoryWarning('Tat ca san pham deu con du hang');
      return true;
    } catch (error) {
      setInventoryWarning(error.message);
      return false;
    }
  }

  async function handlePlaceOrder() {
    if (!shippingAddress.trim()) {
      showMessage('error', 'Dia chi giao hang khong duoc de trong');
      return;
    }

    try {
      setLoading(true);
      const stockAvailable = await handleCheckInventory();

      if (!stockAvailable) {
        showMessage('error', 'Khong the dat hang vi ton kho khong du');
        return;
      }

      const request = {
        items: mapCartItemsToOrderItems(cartItems),
        couponCode: appliedCoupon?.code || null,
        shippingFee: SHIPPING_FEE,
        shippingAddress,
        paymentMethod
      };

      const response = await orderService.createOrder(userId, request);
      setOrderResult(response);
      showMessage('success', 'Dat hang thanh cong');
    } catch (error) {
      showMessage('error', error.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <section className="checkout-page">
      <header className="cart-header">
        <div>
          <p className="eyebrow">ShopCart Frontend</p>
          <h1>Checkout</h1>
          <p className="subtitle">Demo frontend-checkout ket noi API Order va Inventory.</p>
        </div>
        {onBackToCart && (
          <button type="button" className="secondary-btn" data-testid="back-to-cart-btn" onClick={onBackToCart}>
            Quay lai gio hang
          </button>
        )}
      </header>

      {message && (
        <div
          className={`message ${messageType}`}
          data-testid={messageType === 'error' ? 'checkout-error-toast' : 'checkout-success-toast'}
        >
          {message}
        </div>
      )}

      <div className="checkout-grid">
        <div className="card">
          <div className="table-header">
            <h2>Thong tin don hang</h2>
            <span data-testid="checkout-item-count">{cartItems.length} san pham</span>
          </div>

          {cartItems.length === 0 ? (
            <p className="empty" data-testid="checkout-empty-message">Gio hang dang trong</p>
          ) : (
            <div className="checkout-items">
              {cartItems.map((item) => (
                <div className="checkout-item" key={item.productId}>
                  <div>
                    <strong>{item.productName}</strong>
                    <p>So luong: {item.quantity}</p>
                  </div>
                  <span>{formatCurrency(item.lineTotal)}đ</span>
                </div>
              ))}
            </div>
          )}

          <div className="field checkout-field">
            <label htmlFor="shipping-address">Dia chi giao hang</label>
            <input
              id="shipping-address"
              data-testid="shipping-address-input"
              value={shippingAddress}
              onChange={(event) => setShippingAddress(event.target.value)}
            />
          </div>

          <div className="field checkout-field">
            <label htmlFor="payment-method">Phuong thuc thanh toan</label>
            <select
              id="payment-method"
              data-testid="payment-method-select"
              value={paymentMethod}
              onChange={(event) => setPaymentMethod(event.target.value)}
            >
              <option value="COD">COD</option>
              <option value="BANK_TRANSFER">Chuyen khoan</option>
            </select>
          </div>

          <button
            type="button"
            className="secondary-btn"
            data-testid="check-inventory-btn"
            disabled={loading || cartItems.length === 0}
            onClick={handleCheckInventory}
          >
            Kiem tra ton kho
          </button>

          {inventoryWarning && (
            <p className="inventory-warning" data-testid="inventory-warning">
              {inventoryWarning}
            </p>
          )}
        </div>

        <aside className="card checkout-summary">
          <h2>Tong ket thanh toan</h2>

          <div className="coupon-row">
            <input
              data-testid="coupon-input"
              placeholder="SALE10"
              value={couponCode}
              onChange={(event) => setCouponCode(event.target.value)}
            />
            <button type="button" className="secondary-btn" data-testid="apply-coupon-btn" onClick={handleApplyCoupon}>
              Ap dung
            </button>
          </div>

          <div className="summary-row">
            <span>Tam tinh</span>
            <strong data-testid="subtotal-display">{formatCurrency(price.subtotal)}đ</strong>
          </div>
          <div className="summary-row">
            <span>Giam gia</span>
            <strong data-testid="discount-display">-{formatCurrency(price.discount)}đ</strong>
          </div>
          <div className="summary-row">
            <span>Phi van chuyen</span>
            <strong data-testid="shipping-display">{formatCurrency(price.shipping)}đ</strong>
          </div>
          <div className="summary-row total-row">
            <span>Tong thanh toan</span>
            <strong data-testid="total-display">{formatCurrency(price.total)}đ</strong>
          </div>

          <button
            type="button"
            className="primary-btn full-width"
            data-testid="place-order-btn"
            disabled={loading || cartItems.length === 0}
            onClick={handlePlaceOrder}
          >
            Dat hang
          </button>

          {orderResult && (
            <div className="order-result" data-testid="order-success">
              <strong>Dat hang thanh cong</strong>
              <p>Ma don: {orderResult.orderId}</p>
              <p>Trang thai: {orderResult.status}</p>
            </div>
          )}
        </aside>
      </div>
    </section>
  );
}
