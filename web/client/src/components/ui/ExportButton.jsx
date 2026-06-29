import { Download } from 'lucide-react';

export default function ExportButton({ onClick, label = 'Download CSV' }) {
  return (
    <button
      onClick={onClick}
      className="flex items-center gap-2 px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 transition text-sm font-medium"
    >
      <Download className="w-4 h-4" />
      {label}
    </button>
  );
}
