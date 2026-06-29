import { useState, useEffect, useCallback } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { Eye, FileText, Image } from 'lucide-react';
import DataTable from '../components/ui/DataTable';
import Pagination from '../components/ui/Pagination';
import Badge from '../components/ui/Badge';
import ExportButton from '../components/ui/ExportButton';
import { fetchTransactions } from '../api/transactionsApi';
import { downloadTransactionsCSV } from '../api/exportApi';
import { TX_TYPE_COLORS, TX_TYPE_LABELS } from '../utils/formatStatus';
import { formatEpoch } from '../utils/formatDate';

const TX_TYPE_OPTIONS = [
  { value: '', label: 'Semua Tipe' },
  { value: 'CHECK_OUT', label: 'Keluar' },
  { value: 'CHECK_IN', label: 'Masuk' },
];

export default function TransactionsList() {
  const navigate = useNavigate();
  const [data, setData] = useState({ data: [], pagination: { total: 0, page: 1, limit: 20, totalPages: 0 } });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const [txType, setTxType] = useState('');
  const [fromDate, setFromDate] = useState('');
  const [toDate, setToDate] = useState('');
  const [page, setPage] = useState(1);

  const toEpoch = (dateStr) => {
    if (!dateStr) return undefined;
    return new Date(dateStr).getTime();
  };

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await fetchTransactions({
        tx_type: txType || undefined,
        from_ms: toEpoch(fromDate),
        to_ms: toEpoch(toDate),
        page,
        limit: 20,
      });
      setData(res);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }, [txType, fromDate, toDate, page]);

  useEffect(() => { load(); }, [load]);
  useEffect(() => { setPage(1); }, [txType, fromDate, toDate]);

  const columns = [
    { key: 'asset_name', label: 'Nama Barang', render: (row) => (
      <Link to={`/assets/${row.asset_id}`} className="text-blue-600 hover:text-blue-800 font-medium">
        {row.asset_name}
      </Link>
    )},
    { key: 'asset_serial_number', label: 'Serial Number' },
    { key: 'type', label: 'Tipe', render: (row) => (
      <Badge label={TX_TYPE_LABELS[row.type] || row.type} colorClass={TX_TYPE_COLORS[row.type]} />
    )},
    { key: 'recipient_name', label: 'Penerima' },
    { key: 'destination', label: 'Tujuan' },
    { key: 'timestamp_ms', label: 'Waktu', render: (row) => formatEpoch(row.timestamp_ms) },
    { key: 'evidence', label: 'Bukti', render: (row) => {
      if (!row.evidence_filename) return <span className="text-gray-300">—</span>;
      return row.evidence_type === 'PDF'
        ? <FileText className="w-4 h-4 text-red-500" />
        : <Image className="w-4 h-4 text-green-500" />;
    }},
    { key: 'actions', label: 'Aksi', render: (row) => (
      <button
        onClick={() => navigate(`/transactions/${row.id}`)}
        className="p-1.5 text-gray-500 hover:text-blue-600 hover:bg-blue-50 rounded-lg transition"
      >
        <Eye className="w-4 h-4" />
      </button>
    )},
  ];

  return (
    <div className="space-y-4">
      {/* Header */}
      <div className="flex items-center justify-between">
        <p className="text-sm text-gray-500">Total: {data.pagination?.total ?? 0} transaksi</p>
        <ExportButton onClick={() => downloadTransactionsCSV({
          tx_type: txType || undefined,
          from_ms: toEpoch(fromDate),
          to_ms: toEpoch(toDate),
        })} />
      </div>

      {/* Filters */}
      <div className="grid grid-cols-1 sm:grid-cols-4 gap-3">
        <select
          value={txType}
          onChange={(e) => setTxType(e.target.value)}
          className="px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 outline-none"
        >
          {TX_TYPE_OPTIONS.map((opt) => (
            <option key={opt.value} value={opt.value}>{opt.label}</option>
          ))}
        </select>
        <div>
          <label className="block text-xs text-gray-500 mb-1">Dari Tanggal</label>
          <input
            type="date"
            value={fromDate}
            onChange={(e) => setFromDate(e.target.value)}
            className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 outline-none"
          />
        </div>
        <div>
          <label className="block text-xs text-gray-500 mb-1">Sampai Tanggal</label>
          <input
            type="date"
            value={toDate}
            onChange={(e) => setToDate(e.target.value)}
            className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 outline-none"
          />
        </div>
      </div>

      {/* Table */}
      <DataTable
        columns={columns}
        data={data.data}
        loading={loading}
        error={error}
        onRetry={load}
        emptyMessage="Belum ada transaksi"
        emptyDescription="Belum ada transaksi yang tercatat"
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
