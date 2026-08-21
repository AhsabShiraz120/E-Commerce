import { createBrowserRouter, Navigate } from 'react-router-dom';
import { AppLayout } from './components/Layout/AppLayout';
import { HomePage } from './pages/HomePage';
import { CatalogPage } from './pages/CatalogPage';
import { BookDetailPage } from './pages/BookDetailPage';
import { LoginPage } from './pages/LoginPage';
import { CartPage } from './pages/CartPage';
import { CheckoutPage } from './pages/CheckoutPage';
import { OrderConfirmationPage } from './pages/OrderConfirmationPage';
import { OrdersPage } from './pages/OrdersPage';
import { WishlistPage } from './pages/WishlistPage';

export const router = createBrowserRouter([
  {
    path: '/',
    element: <AppLayout />,
    children: [
      { index: true,                              element: <HomePage /> },
      { path: 'catalog',                          element: <CatalogPage /> },
      { path: 'books/:id',                        element: <BookDetailPage /> },
      { path: 'cart',                             element: <CartPage /> },
      { path: 'checkout',                         element: <CheckoutPage /> },
      { path: 'orders',                           element: <OrdersPage /> },
      { path: 'orders/:id/confirmation',          element: <OrderConfirmationPage /> },
      { path: 'wishlist',                         element: <WishlistPage /> },
      { path: '*',                                element: <Navigate to="/" replace /> },
    ],
  },
  { path: '/login', element: <LoginPage /> },
]);
