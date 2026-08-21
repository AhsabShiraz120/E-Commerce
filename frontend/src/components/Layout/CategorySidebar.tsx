import { useNavigate } from 'react-router-dom';
import { useListCategoriesQuery } from '@/store/api/catalogApi';
import { useAppDispatch, useAppSelector } from '@/store/hooks';
import { toggleCategory } from '@/store/slices/catalogSlice';
import clsx from 'clsx';

/**
 * The 19-category sidebar from the wireframes. Clicking a category
 * toggles it in the catalog filter and navigates to /catalog.
 */
export function CategorySidebar({ className }: { className?: string }) {
  const { data: categories, isLoading } = useListCategoriesQuery();
  const active = useAppSelector((s) => s.catalog.categories);
  const dispatch = useAppDispatch();
  const navigate = useNavigate();

  function onClick(slug: string) {
    dispatch(toggleCategory(slug));
    navigate('/catalog');
  }

  return (
    <aside className={clsx('w-full lg:w-56 shrink-0', className)}>
      <div className="card sticky top-20">
        <h3 className="text-sm font-semibold mb-3">Categories</h3>
        {isLoading ? (
          <div className="space-y-2">
            {Array.from({ length: 10 }).map((_, i) => (
              <div key={i} className="h-4 bg-surface2 rounded animate-pulse" />
            ))}
          </div>
        ) : (
          <ul className="space-y-0.5 text-sm">
            {categories?.map((c) => (
              <li key={c.id}>
                <button
                  onClick={() => onClick(c.slug)}
                  className={clsx(
                    'block w-full text-left px-2 py-1 rounded transition-colors',
                    active.includes(c.slug)
                      ? 'bg-accent/20 text-accent'
                      : 'text-muted hover:text-body hover:bg-surface2',
                  )}
                >
                  {c.name}
                </button>
              </li>
            ))}
          </ul>
        )}
      </div>
    </aside>
  );
}
