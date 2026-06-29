export default function LoadingSpinner({ message = 'Memuat data...' }) {
  return (
    <div className="flex flex-col items-center justify-center py-20">
      <div className="w-10 h-10 border-4 border-blue-600 border-t-transparent rounded-full animate-spin" />
      <p className="mt-4 text-gray-500 text-sm">{message}</p>
    </div>
  );
}
