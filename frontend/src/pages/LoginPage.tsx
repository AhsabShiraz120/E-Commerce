import { BookOpen } from 'lucide-react';
import { Link } from 'react-router-dom';

export function LoginPage() {
  return (
    <div className="min-h-screen bg-bg flex items-center justify-center px-4">
      <div className="card w-full max-w-md">
        <Link to="/" className="flex items-center gap-2 mb-4">
          <BookOpen className="w-6 h-6 text-accent" />
          <span className="text-lg font-semibold">Book Worm</span>
        </Link>
        <h1 className="text-xl font-semibold mb-1">Sign in</h1>
        <p className="text-muted text-sm">The full form (email/password + register + guest) lands on branch 11.</p>
      </div>
    </div>
  );
}
