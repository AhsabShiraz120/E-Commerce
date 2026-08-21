import { useParams } from 'react-router-dom';

export function BookDetailPage() {
  const { id } = useParams();
  return (
    <div>
      <h1 className="text-2xl font-semibold mb-2">Book #{id}</h1>
      <p className="text-muted">Product detail + Related Reads + Reviews land on branch 12.</p>
    </div>
  );
}
