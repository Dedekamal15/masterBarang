import { useState } from 'react';
import { Download, FileSpreadsheet } from 'lucide-react';
import ExportButton from '../components/ui/ExportButton';
import { downloadAssetsCSV, downloadTransactionsCSV } from '../api/exportApi';

export default function ExportPage() {
  const [assetStatus, setAssetStatus] = useState('');
  const [assetCategory, setAssetCategory] = useState('');
  const [txType, setTxType] = useState('');
  const [fromDate, setFromDate] = useState('');
  const [toDate, setToDate] = useState('');

  const toEpoch = (dateStr) => (dateStr ? new Date(dateStr).getTime() : undefined);

  return (
    <div className="space-y-6">
      {/* Card Export Barang */}
      <div className="bg-white rounded-xl border border-gray-200 p-6">
        <div className="flex items-center gap-4 mb-5">
          <div className="p-3 bg-blue-50 rounded-xl">
            <FileSpreadsheet className="w-6 h-6 text-blue-600" />
          </div>
          <div>
            <h2 className="text-lg font-semibold text-gray-900">Export Data Barang</h2>
            <p className="text-sm text-gray-500">Download daftar barang ke file CSV</p>
          </div>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 mb-5">
          <select
            value={assetStatus}
            onChange={(e) => setAssetStatus(e.target.value)}
            className="px-3 py-2 border border-gray-300 rounded-lg text-sm outline-none"
          >
            <option value="">Semua Status</option>
            <option value="AVAILABLE">Tersedia</option>
            <option value="BORROWED">Dipinjam</option>
            <option value="MAINTENANCE">Perbaikan</option>
          </select>
          <input
            type="text"
            value={assetCategory}
            onChange={(e) => setAssetCategory(e.target.value)}
            placeholder="Filter kategori..."
            className="px-3 py-2 border border-gray-300 rounded-lg text-sm outline-none"
          />
          <ExportButton
            onClick={() => downloadAssetsCSV({
              status: assetStatus || undefined,
              category: assetCategory || undefined,
            })}
            label="Download CSV Barang"
          />
        </div>
      </div>

      {/* Card Export Transaksi */}
      <div className="bg-white rounded-xl border border-gray-200 p-6">
        <div className="flex items-center gap-4 mb-5">
          <div className="p-3 bg-green-50 rounded-xl">
            <FileSpreadsheet className="w-6 h-6 text-green-600" />
          </div>
          <div>
            <h2 className="text-lg font-semibold text-gray-900">Export Data Transaksi</h2>
            <p className="text-sm text-gray-500">Download daftar transaksi ke file CSV</p>
          </div>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-4 gap-4 mb-5">
          <select
            value={txType}
            onChange={(e) => setTxType(e.target.value)}
            className="px-3 py-2 border border-gray-300 rounded-lg text-sm outline-none"
          >
            <option value="">Semua Tipe</option>
            <option value="CHECK_OUT">Keluar</option>
            <option value="CHECK_IN">Masuk</option>
          </select>
          <div>
            <label className="block text-xs text-gray-500 mb-1">Dari Tanggal</label>
            <input
              type="date"
              value={fromDate}
              onChange={(e) => setFromDate(e.target.value)}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm outline-none"
            />
          </div>
          <div>
            <label className="block text-xs text-gray-500 mb-1">Sampai Tanggal</label>
            <input
              type="date"
              value={toDate}
              onChange={(e) => setToDate(e.target.value)}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm outline-none"
            />
          </div>
          <ExportButton
            onClick={() => downloadTransactionsCSV({
              tx_type: txType || undefined,
              from_ms: toEpoch(fromDate),
              to_ms: toEpoch(toDate),
            })}
            label="Download CSV Transaksi"
          />
        </div>
      </div>

      {/* Info */}
      <div className="bg-blue-50 border border-blue-200 rounded-xl p-4 text-sm text-blue-800">
        <p className="font-medium">💡 File CSV</p>
        <p className="mt-1 text-blue-600">
          File CSV dapat dibuka di Microsoft Excel, Google Sheets, atau aplikasi spreadsheet lainnya.
          Format menggunakan UTF-8 dengan BOM untuk mendukung karakter Indonesia.
        </p>
      </div>
    </div>
  );
}
