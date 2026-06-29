import { useState, useEffect, useCallback } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { Eye } from 'lucide-react';
import DataTable from '../components/ui/DataTable';
import SearchBar from '../components/ui/SearchBar';
import Pagination from '../components/ui/Pagination';
import Badge from '../components/ui/Badge';
import ExportButton from '../components/ui/ExportButton';
import { fetchAssets } from '../api/assetsApi';
import { downloadAssetsCSV } from '../api/exportApi';
import { useDebounce } from '../hooks/useDebounce';
import { STATUS_COLORS, STATUS_LABELS } from '../utils/formatStatus';
import { formatDate } from '../utils/formatDate';

const STATUS_OPTIONS = [
  { value: '', label: 'Semua Status' },
  { value: 'AVAILABLE', label: 'Tersedia' },
  { value: 'BORROWED', label: 'Dipinjam' },
  { value: 'MAINTENANCE', label: 'Perbaikan' },
];

export default function AssetsList() {
  const navigate = useNavigate();
  const [data, setData] = useState({ data: [], pagination: { total: 0, page: 1, limit: 20, totalPages: 0 } });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const [search, setSearch] = useState('');
  const [status, setStatus] = useState('');
  const [category, setCategory] = useState('');
  const [page, setPage] = useState(1);

  const debouncedSearch = useDebounce(search);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await fetchAssets({ status: status || undefined, category: category || undefined, search: debouncedSearch || undefined, page, limit: 20 });
      setData(res);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }, [status, category, debouncedSearch, page]);

  useEffect(() => { load(); }, [load]);

  // Reset page when filters change
  useEffect(() => { setPage(1); }, [status, category, debouncedSearch]);

  const columns = [
    { key: 'name', label: 'Nama Barang', render: (row) => (
      <Link to={`/assets/${row.id}`} className="text-blue-600 hover:text-blue-800 font-medium">
        {row.name}
      </Link>
    )},
    { key: 'serial_number', label: 'Serial Number' },
    { key: 'category', label: 'Kategori' },
    { key: 'location', label: 'Lokasi' },
    { key: 'status', label: 'Status', render: (row) => (
      <Badge label={STATUS_LABELS[row.status] || row.status} colorClass={STATUS_COLORS[row.status]} />
    )},
    { key: 'updated_at', label: 'Diupdate', render: (row) => formatDate(row.updated_at) },
    { key: 'actions', label: 'Aksi', render: (row) => (
      <button
        onClick={() => navigate(`/assets/${row.id}`)}
        className="p-1.5 text-gray-500 hover:text-blue-600 hover:bg-blue-50 rounded-lg transition"
      >
        <Eye className="w-4 h-4" />
      </button>
    )},
  ];

  const handleExport = () => {
    downloadAssetsCSV({ status: status || undefined, category: category || undefined, search: debouncedSearch || undefined });
  };

  return (
    <div className="space-y-4">
      {/* Header + Export */}
      <div className="flex items-center justify-between">
        <p className="text-sm text-gray-500">Total: {data.pagination?.total ?? 0} barang</p>
        <ExportButton onClick={handleExport} />
      </div>

      {/* Filters */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
        <SearchBar value={search} onChange={setSearch} placeholder="Cari nama / serial number..." />
        <select
          value={status}
          onChange={(e) => setStatus(e.target.value)}
          className="px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 outline-none"
        >
          {STATUS_OPTIONS.map((opt) => (
            <option key={opt.value} value={opt.value}>{opt.label}</option>
          ))}
        </select>
        <input
          type="text"
          value={category}
          onChange={(e) => setCategory(e.target.value)}
          placeholder="Filter kategori..."
          className="px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 outline-none"
        />
      </div>

      {/* Table */}
      <DataTable
        columns={columns}
        data={data.data}
        loading={loading}
        error={error}
        onRetry={load}
        emptyMessage="Belum ada barang"
        emptyDescription="Belum ada data aset di database"
      />

      {/* Pagination */}
      {data.pagination && (
        <Pagination
          page={data.pagination.page}
          totalPages={data.pagination.totalPages}
          total={data.pagination.total}
          limit={data.pagination.limit}
          onPageChange={setPage}
        />
      )}
    </div>
  );
}
