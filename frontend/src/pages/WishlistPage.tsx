import { Link } from 'react-router-dom';
import { Trash2, ShoppingCart } from 'lucide-react';
import { BookCard } from '@/components/BookCard';
import { useGetWishlistQuery, useRemoveWishlistItemMutation } from '@/store/api/wishlistApi';
import { useAddCartItemMutation } from '@/store/api/cartApi';
import { useAppSelector } from '@/store/hooks';

export function WishlistPage() {
  const isSignedIn = useAppSelector((s) => Boolean(s.auth.accessToken));
  const { data: wishlist, isLoading } = useGetWishlistQuery(undefined, { skip: !isSignedIn });
  const [removeItem] = useRemoveWishlistItemMutation();
  const [addToCart, { isLoading: adding }] = useAddCartItemMutation();

  if (!isSignedIn) {
    return (
      <div className="card text-center py-10">
        <p className="text-muted mb-4">Sign in to see your wishlist.</p>
        <Link to="/login" className="btn-primary">Sign in</Link>
      </div>
    );
  }

  if (isLoading) return <p className="text-muted">Loading your wishlist…</p>;

  const items = wishlist?.items ?? [];
  if (items.length === 0) {
    return (
      <div className="card text-center py-12">
        <h1 className="text-2xl font-semibold mb-2">My Wishlist</h1>
        <p className="text-muted">No favourites yet. Start adding books you love.</p>
      </div>
    );
  }

  async function moveToCart(itemId: number, bookId: number) {
    try {
      await addToCart({ bookId, quantity: 1 }).unwrap();
      await removeItem(itemId).unwrap();
    } catch {
      // Toast is optional here; leaving silent so the button just fails
    }
  }

  return (
    <section className="space-y-4">
      <h1 className="text-2xl font-semibold">My Wishlist</h1>
      <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-4">
        {items.map((item) => (
          <div key={item.id} className="space-y-2">
            <BookCard book={item.book} className="w-auto" />
            <div className="flex gap-2">
              <button
                className="btn-primary flex-1 !py-1.5 text-xs"
                onClick={() => moveToCart(item.id, item.book.id)}
                disabled={adding}
              >
                <ShoppingCart className="w-3.5 h-3.5" /> Move to Cart
              </button>
              <button
                className="btn-ghost !py-1.5 !px-2"
                onClick={() => removeItem(item.id)}
                aria-label="Remove"
              >
                <Trash2 className="w-3.5 h-3.5" />
              </button>
            </div>
          </div>
        ))}
      </div>
    </section>
  );
}

