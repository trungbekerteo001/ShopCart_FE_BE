import { useEffect, useMemo, useState } from 'react';
import * as cartService from '../services/cartService.js';
import { formatCurrency, validateCartItem } from '../utils/cartValidation.js';

const PRODUCTS = [
  { productId: 'P001', productName: 'Laptop Dell', price: 15_000_000, stock: 10, status: 'ACTIVE' },
  { productId: 'P002', productName: 'Mouse Logitech', price: 500_000, stock: 50, status: 'ACTIVE' },
  { productId: 'P003', productName: 'Keyboard Mechanical', price: 2_000_000, stock: 0, status: 'ACTIVE' },
  { productId: 'P004', productName: 'Old Monitor', price: 3_000_000, stock: 5, status: 'INACTIVE' }
];

function emptyCart() {
  return {
    success: true,
    message: '',
    cartTotal: 0,
    items: []
  };
}

export default function CartComponent({ userId = 'user01' }) {
  const [selectedProductId, setSelectedProductId] = useState('P001');
  const [quantity, setQuantity] = useState(1);
  const [cart, setCart] = useState(emptyCart());
  const [message, setMessage] = useState('');
  const [messageType, setMessageType] = useState('success');
  const [loading, setLoading] = useState(false);

  const selectedProduct = useMemo(
    () => PRODUCTS.find((product) => product.productId === selectedProductId) || PRODUCTS[0],
    [selectedProductId]
  );

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
    // chi load cart khi component duoc render lan dau hoac userId thay doi
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [userId]);

  function showMessage(type, text) {
    setMessageType(type);
    setMessage(text);
  }

  async function handleAddToCart() {
    const validation = validateCartItem({
      productId: selectedProductId,
      quantity,
      stock: selectedProduct.stock,
      status: selectedProduct.status
    });

    if (!validation.valid) {
      showMessage('error', validation.error);
      return;
    }

    try {
      setLoading(true);
      const data = await cartService.addToCart(userId, {
        productId: selectedProductId,
        quantity: Number(quantity)
      });
      setCart(data);
      showMessage('success', data.message || 'Them vao gio hang thanh cong');
    } catch (error) {
      showMessage('error', error.message);
    } finally {
      setLoading(false);
    }
  }

  async function handleUpdateQuantity(productId, newQuantity) {
    const product = PRODUCTS.find((item) => item.productId === productId);
    const validation = validateCartItem({
      productId,
      quantity: newQuantity,
      stock: product?.stock,
      status: product?.status
    });

    if (!validation.valid) {
      showMessage('error', validation.error);
      return;
    }

    try {
      setLoading(true);
      const data = await cartService.updateQuantity(userId, {
        productId,
        quantity: Number(newQuantity)
      });
      setCart(data);
      showMessage('success', data.message || 'Cap nhat so luong thanh cong');
    } catch (error) {
      showMessage('error', error.message);
    } finally {
      setLoading(false);
    }
  }

  async function handleRemove(productId) {
    try {
      setLoading(true);
      const data = await cartService.removeFromCart(userId, productId);
      setCart(data);
      showMessage('success', data.message || 'Xoa san pham khoi gio hang thanh cong');
    } catch (error) {
      showMessage('error', error.message);
    } finally {
      setLoading(false);
    }
  }

  const cartItems = cart?.items || [];
  const cartBadge = cartItems.reduce((sum, item) => sum + Number(item.quantity || 0), 0);

  return (
    <section className="cart-page">
      <header className="cart-header">
        <div>
          <p className="eyebrow">ShopCart Frontend</p>
          <h1>Gio hang</h1>
          <p className="subtitle">Demo frontend-cart ket noi API backend Cart.</p>
        </div>
        <div className="cart-badge" data-testid="cart-badge">
          {cartBadge}
        </div>
      </header>

      <div className="card add-card">
        <div className="field">
          <label htmlFor="product-select">San pham</label>
          <select
            id="product-select"
            data-testid="product-select"
            value={selectedProductId}
            onChange={(event) => setSelectedProductId(event.target.value)}
          >
            {PRODUCTS.map((product) => (
              <option key={product.productId} value={product.productId}>
                {product.productName} - Ton kho: {product.stock} - {product.status}
              </option>
            ))}
          </select>
        </div>

        <div className="field">
          <label htmlFor="quantity-input">So luong</label>
          <input
            id="quantity-input"
            data-testid="quantity-input"
            type="number"
            min="1"
            value={quantity}
            onChange={(event) => setQuantity(event.target.value)}
          />
        </div>

        <button
          data-testid="add-to-cart-btn"
          className="primary-btn"
          type="button"
          disabled={loading}
          onClick={handleAddToCart}
        >
          Them vao gio hang
        </button>
      </div>

      {message && (
        <div
          className={`message ${messageType}`}
          data-testid={messageType === 'error' ? 'error-toast' : 'success-toast'}
        >
          {message}
        </div>
      )}

      <div className="card">
        <div className="table-header">
          <h2>Danh sach san pham trong gio</h2>
          <strong data-testid="cart-total">{formatCurrency(cart?.cartTotal || 0)}</strong>
        </div>

        {cartItems.length === 0 ? (
          <p className="empty" data-testid="empty-cart-message">
            Gio hang dang trong
          </p>
        ) : (
          <table data-testid="cart-table">
            <thead>
              <tr>
                <th>San pham</th>
                <th>Don gia</th>
                <th>So luong</th>
                <th>Thanh tien</th>
                <th>Thao tac</th>
              </tr>
            </thead>
            <tbody>
              {cartItems.map((item) => (
                <tr key={item.productId}>
                  <td>{item.productName}</td>
                  <td>{formatCurrency(item.price)}</td>
                  <td>
                    <input
                      className="qty-input"
                      data-testid={`cart-qty-${item.productId}`}
                      type="number"
                      min="1"
                      defaultValue={item.quantity}
                      onBlur={(event) => handleUpdateQuantity(item.productId, event.target.value)}
                    />
                  </td>
                  <td>{formatCurrency(item.lineTotal)}</td>
                  <td>
                    <button
                      type="button"
                      className="danger-btn"
                      data-testid={`remove-${item.productId}`}
                      onClick={() => handleRemove(item.productId)}
                    >
                      Xoa
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </section>
  );
}
