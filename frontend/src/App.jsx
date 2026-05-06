import { useState } from 'react';
import CartComponent from './components/CartComponent.jsx';
import CheckoutPage from './components/CheckoutPage.jsx';

export default function App() {
  const [page, setPage] = useState('cart');
  const userId = 'user01';

  return (
    <main className="app-shell">
      {page === 'cart' ? (
        <CartComponent userId={userId} onCheckout={() => setPage('checkout')} />
      ) : (
        <CheckoutPage userId={userId} onBackToCart={() => setPage('cart')} />
      )}
    </main>
  );
}
