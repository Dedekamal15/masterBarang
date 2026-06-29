import { useEffect, useState } from 'react';
import { Package, CheckCircle, BookOpen } from 'lucide-react';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, PieChart, Pie, Cell, Legend } from 'recharts';
import StatCard from '../components/ui/StatCard';
import LoadingSpinner from '../components/ui/LoadingSpinner';
import ErrorState from '../components/ui/ErrorState';
import { fetchStats } from '../api/statsApi';

const PIE_COLORS = ['#22c55e', '#3b82f6', '#eab308'];

export default function Dashboard() {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const load = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await fetchStats();
      setData(res);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, []);

  if (loading) return <LoadingSpinner message="Memuat dashboard..." />;
  if (error) return <ErrorState message={error} onRetry={load} />;
  if (!data) return null;

  const pieData = [
    { name: 'Tersedia', value: data.availableCount || 0 },
    { name: 'Dipinjam', value: data.borrowedCount || 0 },
    { name: 'Perbaikan', value: data.maintenanceCount || 0 },
  ];

  return (
    <div className="space-y-6">
      {/* 3 Stat Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <StatCard
          title="Total Aset"
          value={data.totalAssets}
          icon={Package}
          color="blue"
        />
        <StatCard
          title="Tersedia"
          value={data.availableCount}
          icon={CheckCircle}
          color="green"
        />
        <StatCard
          title="Dipinjam"
          value={data.borrowedCount}
          icon={BookOpen}
          color="yellow"
        />
      </div>

      {/* Charts */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Bar Chart: Kategori */}
        <div className="bg-white rounded-xl border border-gray-200 p-5">
          <h3 className="text-sm font-semibold text-gray-700 mb-4">Distribusi per Kategori</h3>
          {data.categories && data.categories.length > 0 ? (
            <ResponsiveContainer width="100%" height={280}>
              <BarChart data={data.categories}>
                <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
                <XAxis dataKey="category" tick={{ fontSize: 11 }} angle={-20} textAnchor="end" height={60} />
                <YAxis tick={{ fontSize: 11 }} />
                <Tooltip />
                <Bar dataKey="count" fill="#3b82f6" radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          ) : (
            <p className="text-gray-400 text-sm py-10 text-center">Belum ada data kategori</p>
          )}
        </div>

        {/* Pie Chart: Status */}
        <div className="bg-white rounded-xl border border-gray-200 p-5">
          <h3 className="text-sm font-semibold text-gray-700 mb-4">Status Aset</h3>
          <ResponsiveContainer width="100%" height={280}>
            <PieChart>
              <Pie
                data={pieData}
                cx="50%"
                cy="50%"
                innerRadius={60}
                outerRadius={100}
                paddingAngle={3}
                dataKey="value"
              >
                {pieData.map((_entry, idx) => (
                  <Cell key={idx} fill={PIE_COLORS[idx]} />
                ))}
              </Pie>
              <Tooltip />
              <Legend />
            </PieChart>
          </ResponsiveContainer>
        </div>
      </div>

      {/* Info tambahan */}
      <div className="bg-white rounded-xl border border-gray-200 p-5">
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4 text-center">
          <div>
            <p className="text-2xl font-bold text-gray-800">{data.totalTransactions ?? 0}</p>
            <p className="text-xs text-gray-500">Total Transaksi</p>
          </div>
          <div>
            <p className="text-2xl font-bold text-gray-800">{data.todayTransactions ?? 0}</p>
            <p className="text-xs text-gray-500">Transaksi Hari Ini</p>
          </div>
          <div>
            <p className="text-2xl font-bold text-gray-800">{data.totalAssets ?? 0}</p>
            <p className="text-xs text-gray-500">Total Aset</p>
          </div>
          <div>
            <p className="text-2xl font-bold text-yellow-600">{data.maintenanceCount ?? 0}</p>
            <p className="text-xs text-gray-500">Dalam Perbaikan</p>
          </div>
        </div>
      </div>
    </div>
  );
}
