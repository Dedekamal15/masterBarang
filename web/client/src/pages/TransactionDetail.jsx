import { useState, useEffect, useCallback } from 'react';
import { useParams, Link } from 'react-router-dom';
import { ArrowLeft, MapPin, Download, FileText, Image } from 'lucide-react';
import Badge from '../components/ui/Badge';
import LoadingSpinner from '../components/ui/LoadingSpinner';
import ErrorState from '../components/ui/ErrorState';
import { fetchTransactionById } from '../api/transactionsApi';
import { TX_TYPE_COLORS, TX_TYPE_LABELS } from '../utils/formatStatus';
import { formatEpoch } from '../utils/formatDate';
import LocationMap from '../components/maps/LocationMap';

export default function TransactionDetail() {
  const { id } = useParams();
  const [tx, setTx] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await fetchTransactionById(id);
      setTx(res.data);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => { load(); }, [load]);

  if (loading) return <LoadingSpinner />;
  if (error) return <ErrorState message={error} onRetry={load} />;
  if (!tx) return null;

  const evidenceUrl = `/api/v1/transactions/${tx.id}/evidence`;
  const hasEvidence = !!tx.evidence_filename;
  const isPDF = tx.evidence_type === 'PDF';
  const hasLocation = tx.latitude && tx.longitude;

  return (
    <div className="space-y-6">
      {/* Back */}
      <Link to="/transactions" className="inline-flex items-center gap-1.5 text-sm text-gray-500 hover:text-gray-700">
        <ArrowLeft className="w-4 h-4" />
        Kembali ke daftar transaksi
      </Link>

      {/* Detail Card */}
      <div className="bg-white rounded-xl border border-gray-200 p-6">
        <div className="flex items-start justify-between mb-6">
          <div>
            <h1 className="text-xl font-bold text-gray-900">Detail Transaksi</h1>
            <p className="text-sm text-gray-500 font-mono">ID: {tx.id}</p>
          </div>
          <Badge
            label={TX_TYPE_LABELS[tx.type] || tx.type}
            colorClass={TX_TYPE_COLORS[tx.type]}
          />
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4 text-sm">
          <div className="space-y-3">
            <div>
              <span className="text-gray-500">Nama Barang:</span>
              <Link to={`/assets/${tx.asset_id}`} className="ml-1 font-medium text-blue-600 hover:text-blue-800">
                {tx.asset_name}
              </Link>
            </div>
            <div>
              <span className="text-gray-500">Serial Number:</span>
              <span className="ml-1 font-medium">{tx.asset_serial_number}</span>
            </div>
            <div>
              <span className="text-gray-500">Penerima:</span>
              <span className="ml-1 font-medium">{tx.recipient_name || '—'}</span>
            </div>
            <div>
              <span className="text-gray-500">Tujuan:</span>
              <span className="ml-1 font-medium">{tx.destination || '—'}</span>
            </div>
          </div>
          <div className="space-y-3">
            <div>
              <span className="text-gray-500">Waktu:</span>
              <span className="ml-1 font-medium">{formatEpoch(tx.timestamp_ms)}</span>
            </div>
            <div>
              <span className="text-gray-500">Catatan:</span>
              <span className="ml-1">{tx.notes || '—'}</span>
            </div>
            {hasLocation && (
              <div className="flex items-center gap-2 text-green-600">
                <MapPin className="w-4 h-4" />
                <span>{tx.latitude}, {tx.longitude}</span>
                {tx.gps_accuracy_meters && (
                  <span className="text-gray-400 text-xs">±{tx.gps_accuracy_meters}m</span>
                )}
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Evidence */}
      {hasEvidence && (
        <div className="bg-white rounded-xl border border-gray-200 p-6">
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-lg font-semibold text-gray-900">Bukti Transaksi</h2>
            <a
              href={evidenceUrl}
              download
              className="flex items-center gap-2 px-3 py-1.5 bg-gray-100 hover:bg-gray-200 rounded-lg text-sm text-gray-700 transition"
            >
              <Download className="w-4 h-4" />
              Download
            </a>
          </div>

          {isPDF ? (
            <div className="border border-gray-200 rounded-lg p-8 text-center">
              <FileText className="w-12 h-12 text-red-500 mx-auto mb-3" />
              <p className="text-sm font-medium">File PDF</p>
              <a
                href={evidenceUrl}
                target="_blank"
                rel="noopener noreferrer"
                className="text-blue-600 hover:text-blue-800 text-sm underline mt-1 inline-block"
              >
                Buka PDF
              </a>
            </div>
          ) : (
            <div className="rounded-lg overflow-hidden border border-gray-200">
              <img
                src={evidenceUrl}
                alt="Bukti transaksi"
                className="w-full max-h-96 object-contain bg-gray-50"
              />
            </div>
          )}
        </div>
      )}

      {/* Location Map */}
      {hasLocation && (
        <div className="bg-white rounded-xl border border-gray-200 p-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">Lokasi Transaksi</h2>
          <div className="h-64 rounded-lg overflow-hidden border border-gray-200">
            <LocationMap latitude={tx.latitude} longitude={tx.longitude} />
          </div>
        </div>
      )}
    </div>
  );
}
