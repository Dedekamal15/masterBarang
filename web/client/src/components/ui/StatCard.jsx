export default function StatCard({ title, value, icon: Icon, color = 'blue', loading = false }) {
  const colorMap = {
    blue: 'bg-blue-50 text-blue-600 border-blue-200',
    green: 'bg-green-50 text-green-600 border-green-200',
    yellow: 'bg-yellow-50 text-yellow-600 border-yellow-200',
    red: 'bg-red-50 text-red-600 border-red-200',
    purple: 'bg-purple-50 text-purple-600 border-purple-200',
  };

  return (
    <div className={`rounded-xl border p-5 ${colorMap[color] || colorMap.blue}`}>
      <div className="flex items-center justify-between">
        <div>
          <p className="text-sm font-medium opacity-75">{title}</p>
          {loading ? (
            <div className="mt-1 h-8 w-20 bg-current/20 rounded animate-pulse" />
          ) : (
            <p className="text-3xl font-bold mt-1">{value ?? '—'}</p>
          )}
        </div>
        {Icon && (
          <div className="p-3 rounded-lg bg-white/50">
            <Icon className="w-7 h-7" />
          </div>
        )}
      </div>
    </div>
  );
}
