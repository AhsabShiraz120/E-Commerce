import { Link, NavLink } from 'react-router-dom';
import { BookOpen, Heart, LogOut, ShoppingCart, User } from 'lucide-react';
import clsx from 'clsx';
import { useAppDispatch, useAppSelector } from '@/store/hooks';
import { signedOut } from '@/store/slices/authSlice';

export function Header() {
  const user = useAppSelector(s => s.auth.user);
  const dispatch = useAppDispatch();

  return (
    <header className="sticky top-0 z-10 bg-surface/95 backdrop-blur border-b border-border">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
        <Link to="/" className="flex items-center gap-2 text-lg font-semibold">
          <BookOpen className="w-6 h-6 text-accent" />
          <span>Book Worm</span>
        </Link>

        <nav className="hidden md:flex items-center gap-6 text-sm">
          <NavItem to="/orders"   label="My Orders" />
          <NavItem to="/wishlist" label="My Wishlist" />
          <NavItem to="/catalog"  label="Catalog" />
        </nav>

        <div className="flex items-center gap-4">
          <Link to="/wishlist" className="text-muted hover:text-body" aria-label="Wishlist">
            <Heart className="w-5 h-5" />
          </Link>
          <Link to="/cart" className="relative text-muted hover:text-body" aria-label="Cart">
            <ShoppingCart className="w-5 h-5" />
          </Link>
          {user ? (
            <div className="flex items-center gap-2">
              <Link to="/orders" className="w-8 h-8 rounded-full bg-accent/20 text-accent flex items-center justify-center text-xs font-semibold" aria-label="Account">
                {(user.firstName?.[0] ?? user.email[0]).toUpperCase()}
              </Link>
              <button
                onClick={() => dispatch(signedOut())}
                className="text-muted hover:text-body"
                aria-label="Sign out"
                title="Sign out"
              >
                <LogOut className="w-4 h-4" />
              </button>
            </div>
          ) : (
            <Link to="/login" className="btn-ghost !py-1.5 !px-3 text-sm">
              <User className="w-4 h-4" /> Sign in
            </Link>
          )}
        </div>
      </div>
    </header>
  );
}

function NavItem({ to, label }: { to: string; label: string }) {
  return (
    <NavLink
      to={to}
      className={({ isActive }) => clsx(
        'hover:text-body transition-colors',
        isActive ? 'text-body' : 'text-muted',
      )}
    >
      {label}
    </NavLink>
  );
}
