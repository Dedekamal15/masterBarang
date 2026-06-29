import { PackageOpen } from 'lucide-react';

export default function EmptyState({ message = 'Belum ada data', description = '' }) {
  return (
    <div className="flex flex-col items-center justify-center py-20 text-gray-400">
      <PackageOpen className="w-16 h-16 mb-4" />
      <p className="text-lg font-medium">{message}</p>
      {description && <p className="text-sm mt-1">{description}</p>}
    </div>
  );
}
