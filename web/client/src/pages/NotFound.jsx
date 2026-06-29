import { Link } from 'react-router-dom';
import { Home } from 'lucide-react';

export default function NotFound() {
  return (
    <div className="flex flex-col items-center justify-center py-20 text-gray-500">
      <p className="text-7xl font-bold text-gray-200">404</p>
      <p className="text-lg font-medium mt-2">Halaman tidak ditemukan</p>
      <p className="text-sm mt-1">Halaman yang Anda cari tidak ada</p>
      <Link
        to="/"
        className="mt-6 flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition text-sm"
      >
        <Home className="w-4 h-4" />
        Kembali ke Dashboard
      </Link>
    </div>
  );
}
