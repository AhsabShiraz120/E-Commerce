import { CategorySidebar } from '@/components/Layout/CategorySidebar';
import { BookRail } from '@/components/BookRail';
import {
  useGetRecommendedBooksQuery,
  useListBooksQuery,
} from '@/store/api/catalogApi';
import { useAppSelector } from '@/store/hooks';

export function HomePage() {
  const isSignedIn = useAppSelector((s) => Boolean(s.auth.accessToken));

  const { data: recommended, isLoading: recLoading } = useGetRecommendedBooksQuery(10);
  const { data: bestsellers, isLoading: bestLoading } = useListBooksQuery({ sort: 'popular', size: 10 });
  const { data: newLaunches, isLoading: newLoading }  = useListBooksQuery({ sort: 'newest',  size: 10 });

  return (
    <div className="flex flex-col lg:flex-row gap-6">
      <CategorySidebar className="hidden lg:block" />
      <div className="flex-1 min-w-0 space-y-8">
        <Hero />

        {isSignedIn && (
          <BookRail
            title="Recommended for You"
            books={recommended}
            loading={recLoading}
            emptyMessage="Place your first order to get personalised picks."
          />
        )}

        <BookRail
          title="Bestsellers this Month"
          books={bestsellers?.content}
          loading={bestLoading}
        />

        <BookRail
          title="New Launches"
          books={newLaunches?.content}
          loading={newLoading}
        />
      </div>
    </div>
  );
}

function Hero() {
  return (
    <div className="card bg-gradient-to-br from-accent/20 via-surface to-surface2 border-accent/30">
      <h1 className="text-2xl sm:text-3xl font-semibold">Curated books, warm covers, fast delivery.</h1>
      <p className="text-muted mt-2 max-w-lg">
        Browse by category, mood, or bestseller. Free shipping over ₹499. 48-hour cancel window on every order.
      </p>
    </div>
  );
}
