import { useParams } from 'react-router-dom';

export function OrderConfirmationPage() {
  const { id } = useParams();
  return (
    <div>
      <h1 className="text-2xl font-semibold mb-2">Order Placed!</h1>
      <p className="text-muted">Order #{id} — green-check confirmation UI lands on branch 13.</p>
    </div>
  );
}
