import LoadingSpinner from './LoadingSpinner';
import EmptyState from './EmptyState';
import ErrorState from './ErrorState';

export default function DataTable({
  columns,
  data,
  loading,
  error,
  onRetry,
  emptyMessage = 'Belum ada data',
  emptyDescription = '',
}) {
  if (loading) return <LoadingSpinner />;
  if (error) return <ErrorState message={error} onRetry={onRetry} />;
  if (!data || data.length === 0) return <EmptyState message={emptyMessage} description={emptyDescription} />;

  return (
    <div className="overflow-x-auto rounded-lg border border-gray-200">
      <table className="min-w-full divide-y divide-gray-200">
        <thead className="bg-gray-50">
          <tr>
            {columns.map((col) => (
              <th
                key={col.key}
                className="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider"
                style={col.width ? { width: col.width } : undefined}
              >
                {col.label}
              </th>
            ))}
          </tr>
        </thead>
        <tbody className="bg-white divide-y divide-gray-200">
          {data.map((row, idx) => (
            <tr key={row.id || idx} className="hover:bg-gray-50 transition">
              {columns.map((col) => (
                <td key={col.key} className="px-4 py-3 text-sm text-gray-700 whitespace-nowrap">
                  {col.render ? col.render(row) : row[col.key]}
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
