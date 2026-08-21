import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { BookOpen } from 'lucide-react';

import { useLoginMutation, useRegisterMutation } from '@/store/api/authApi';
import { useAppDispatch } from '@/store/hooks';
import { signedIn } from '@/store/slices/authSlice';

const signInSchema = z.object({
  email: z.string().email(),
  password: z.string().min(8, 'At least 8 characters'),
});
type SignInForm = z.infer<typeof signInSchema>;

const registerSchema = signInSchema.extend({
  firstName: z.string().min(1, 'Required'),
  lastName: z.string().min(1, 'Required'),
});
type RegisterForm = z.infer<typeof registerSchema>;

export function LoginPage() {
  const [mode, setMode] = useState<'signin' | 'register'>('signin');
  return (
    <div className="min-h-screen bg-bg flex items-center justify-center px-4">
      <div className="card w-full max-w-md">
        <Link to="/" className="flex items-center gap-2 mb-6">
          <BookOpen className="w-6 h-6 text-accent" />
          <span className="text-lg font-semibold">Book Worm</span>
        </Link>

        <div className="flex gap-1 mb-4 p-1 bg-surface2 rounded">
          <TabBtn active={mode === 'signin'}   onClick={() => setMode('signin')}>Sign in</TabBtn>
          <TabBtn active={mode === 'register'} onClick={() => setMode('register')}>Create account</TabBtn>
        </div>

        {mode === 'signin' ? <SignInForm /> : <RegisterFormBlock />}

        <p className="text-xs text-muted text-center mt-4">
          Or <Link to="/" className="underline hover:text-body">continue as guest</Link>
        </p>
        <p className="text-xs text-muted text-center mt-4">
          Demo login: <code className="text-body">demo@bookworm.io</code> / <code className="text-body">Demo@123</code>
        </p>
      </div>
    </div>
  );
}

function TabBtn({ active, onClick, children }: { active: boolean; onClick: () => void; children: React.ReactNode }) {
  return (
    <button
      onClick={onClick}
      className={`flex-1 px-3 py-2 text-sm rounded transition-colors ${
        active ? 'bg-surface text-body' : 'text-muted hover:text-body'
      }`}
    >
      {children}
    </button>
  );
}

function SignInForm() {
  const { register, handleSubmit, formState: { errors } } = useForm<SignInForm>({
    resolver: zodResolver(signInSchema),
  });
  const [doLogin, { isLoading, error }] = useLoginMutation();
  const dispatch = useAppDispatch();
  const navigate = useNavigate();

  async function onSubmit(values: SignInForm) {
    try {
      const res = await doLogin(values).unwrap();
      dispatch(signedIn(res));
      navigate('/');
    } catch { /* handled in `error` */ }
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-3">
      <Field label="Email" error={errors.email?.message}>
        <input type="email" autoComplete="email" className={inputCls} {...register('email')} />
      </Field>
      <Field label="Password" error={errors.password?.message}>
        <input type="password" autoComplete="current-password" className={inputCls} {...register('password')} />
      </Field>
      {error && <ApiErrorLine err={error} />}
      <button type="submit" className="btn-primary w-full" disabled={isLoading}>
        {isLoading ? 'Signing in…' : 'Sign in'}
      </button>
    </form>
  );
}

function RegisterFormBlock() {
  const { register, handleSubmit, formState: { errors } } = useForm<RegisterForm>({
    resolver: zodResolver(registerSchema),
  });
  const [doRegister, { isLoading, error }] = useRegisterMutation();
  const dispatch = useAppDispatch();
  const navigate = useNavigate();

  async function onSubmit(values: RegisterForm) {
    try {
      const res = await doRegister(values).unwrap();
      dispatch(signedIn(res));
      navigate('/');
    } catch { /* handled in `error` */ }
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-3">
      <div className="grid grid-cols-2 gap-3">
        <Field label="First name" error={errors.firstName?.message}>
          <input className={inputCls} {...register('firstName')} />
        </Field>
        <Field label="Last name" error={errors.lastName?.message}>
          <input className={inputCls} {...register('lastName')} />
        </Field>
      </div>
      <Field label="Email" error={errors.email?.message}>
        <input type="email" autoComplete="email" className={inputCls} {...register('email')} />
      </Field>
      <Field label="Password" error={errors.password?.message}>
        <input type="password" autoComplete="new-password" className={inputCls} {...register('password')} />
      </Field>
      {error && <ApiErrorLine err={error} />}
      <button type="submit" className="btn-primary w-full" disabled={isLoading}>
        {isLoading ? 'Creating…' : 'Create account'}
      </button>
    </form>
  );
}

const inputCls =
  'w-full px-3 py-2 rounded bg-surface2 border border-border focus:border-accent outline-none text-sm';

function Field({ label, error, children }: { label: string; error?: string; children: React.ReactNode }) {
  return (
    <label className="block">
      <span className="block text-xs text-muted mb-1">{label}</span>
      {children}
      {error && <span className="block text-xs text-danger mt-1">{error}</span>}
    </label>
  );
}

function ApiErrorLine({ err }: { err: unknown }) {
  const message = extractMessage(err) ?? 'Something went wrong. Please try again.';
  return <p className="text-sm text-danger">{message}</p>;
}

function extractMessage(err: unknown): string | undefined {
  if (err && typeof err === 'object' && 'data' in err) {
    const data = (err as { data?: unknown }).data;
    if (data && typeof data === 'object' && 'message' in data) {
      const m = (data as { message?: unknown }).message;
      if (typeof m === 'string') return m;
    }
  }
  return undefined;
}
