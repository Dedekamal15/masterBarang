import { useState, useEffect, useCallback } from 'react';
import { useParams, Link } from 'react-router-dom';
import { ArrowLeft, Package, MapPin, Barcode, FileText } from 'lucide-react';
import Badge from '../components/ui/Badge';
import LoadingSpinner from '../components/ui/LoadingSpinner';
import ErrorState from '../components/ui/ErrorState';
import DataTable from '../components/ui/DataTable';
import ExportButton from '../components/ui/ExportButton';
import { fetchAssetById, fetchAssetTransactions } from '../api/assetsApi';
import { downloadTransactionsCSV } from '../api/exportApi';
import { STATUS_COLORS, STATUS_LABELS, TX_TYPE_COLORS, TX_TYPE_LABELS } from '../utils/formatStatus';
import { formatEpoch } from '../utils/formatDate';

export default function AssetDetail() {
  const { id } = useParams();
  const [asset, setAsset] = useState(null);
  const [txData, setTxData] = useState({ data: [], pagination: { total: 0, page: 1, limit: 20, totalPages: 0 } });
  const [loading, setLoading] = useState(true);
  const [txLoading, setTxLoading] = useState(true);
  const [error, setError] = useState(null);
  const [txType, setTxType] = useState('');
  const [txPage, setTxPage] = useState(1);

  const loadAsset = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await fetchAssetById(id);
      setAsset(res.data);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }, [id]);

  const loadTransactions = useCallback(async () => {
    setTxLoading(true);
    try {
      const res = await fetchAssetTransactions(id, { tx_type: txType || undefined, page: txPage, limit: 20 });
      setTxData(res);
    } catch {
      // silent
    } finally {
      setTxLoading(false);
    }
  }, [id, txType, txPage]);

  useEffect(() => { loadAsset(); }, [loadAsset]);
  useEffect(() => { loadTransactions(); }, [loadTransactions]);
  useEffect(() => { setTxPage(1); }, [txType]);

  const txColumns = [
    { key: 'type', label: 'Tipe', render: (row) => (
      <Badge label={TX_TYPE_LABELS[row.type] || row.type} colorClass={TX_TYPE_COLORS[row.type]} />
    )},
    { key: 'recipient_name', label: 'Penerima' },
    { key: 'destination', label: 'Tujuan' },
    { key: 'timestamp_ms', label: 'Waktu', render: (row) => formatEpoch(row.timestamp_ms) },
    { key: 'evidence_filename', label: 'Bukti', render: (row) => row.evidence_filename ? '📎 Ada' : '—' },
    { key: 'actions', label: 'Aksi', render: (row) => (
      <Link to={`/transactions/${row.id}`} className="text-blue-600 hover:text-blue-800 text-xs">
        Detail
      </Link>
    )},
  ];

  if (loading) return <LoadingSpinner />;
  if (error) return <ErrorState message={error} onRetry={loadAsset} />;
  if (!asset) return null;

  return (
    <div className="space-y-6">
      {/* Back button */}
      <Link to="/assets" className="inline-flex items-center gap-1.5 text-sm text-gray-500 hover:text-gray-700">
        <ArrowLeft className="w-4 h-4" />
        Kembali ke daftar barang
      </Link>

      {/* Detail Card */}
      <div className="bg-white rounded-xl border border-gray-200 p-6">
        <div className="flex items-start justify-between mb-6">
          <div className="flex items-center gap-4">
            <div className="p-3 bg-blue-50 rounded-xl">
              <Package className="w-8 h-8 text-blue-600" />
            </div>
            <div>
              <h1 className="text-xl font-bold text-gray-900">{asset.name}</h1>
              <p className="text-sm text-gray-500">{asset.category}</p>
            </div>
          </div>
          <Badge label={STATUS_LABELS[asset.status] || asset.status} colorClass={STATUS_COLORS[asset.status]} />
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4 text-sm">
          <div className="space-y-3">
            <div className="flex items-center gap-2">
              <Barcode className="w-4 h-4 text-gray-400" />
              <span className="text-gray-500">Serial Number:</span>
              <span className="font-medium">{asset.serial_number}</span>
            </div>
            <div className="flex items-center gap-2">
              <MapPin className="w-4 h-4 text-gray-400" />
              <span className="text-gray-500">Lokasi:</span>
              <span className="font-medium">{asset.location || '—'}</span>
            </div>
          </div>
          <div className="space-y-3">
            <div className="flex items-center gap-2">
              <FileText className="w-4 h-4 text-gray-400" />
              <span className="text-gray-500">Deskripsi:</span>
              <span className="font-medium">{asset.description || '—'}</span>
            </div>
            <div>
              <span className="text-gray-500">Dibuat:</span>
              <span className="ml-1">{formatEpoch(asset.created_at)}</span>
            </div>
            <div>
              <span className="text-gray-500">Diupdate:</span>
              <span className="ml-1">{formatEpoch(asset.updated_at)}</span>
            </div>
          </div>
        </div>
      </div>

      {/* Riwayat Transaksi */}
      <div className="bg-white rounded-xl border border-gray-200 p-6">
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-lg font-semibold text-gray-900">
            Riwayat Transaksi
            <span className="ml-2 text-sm text-gray-500">({txData.pagination?.total ?? 0})</span>
          </h2>
          <div className="flex items-center gap-3">
            <select
              value={txType}
              onChange={(e) => setTxType(e.target.value)}
              className="px-3 py-1.5 border border-gray-300 rounded-lg text-sm outline-none"
            >
              <option value="">Semua Tipe</option>
              <option value="CHECK_OUT">Keluar</option>
              <option value="CHECK_IN">Masuk</option>
            </select>
            <ExportButton
              onClick={() => downloadTransactionsCSV({ ...(txType ? { tx_type: txType } : {}) })}
              label="Export CSV"
            />
          </div>
        </div>

        <DataTable
          columns={txColumns}
          data={txData.data}
          loading={txLoading}
          emptyMessage="Belum ada transaksi untuk barang ini"
        />
      </div>
    </div>
  );
}
