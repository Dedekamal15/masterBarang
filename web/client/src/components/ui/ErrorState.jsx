import { AlertTriangle } from 'lucide-react';

export default function ErrorState({ message = 'Terjadi kesalahan', onRetry }) {
  return (
    <div className="flex flex-col items-center justify-center py-20 text-red-400">
      <AlertTriangle className="w-16 h-16 mb-4" />
      <p className="text-lg font-medium text-red-600">{message}</p>
      {onRetry && (
        <button
          onClick={onRetry}
          className="mt-4 px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition"
        >
          Coba Lagi
        </button>
      )}
    </div>
  );
}
