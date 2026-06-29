import { Menu, Activity } from 'lucide-react';
import { useState, useEffect } from 'react';
import { fetchHealth } from '../../api/statsApi';

export default function Navbar({ onMenuClick, title }) {
  const [dbStatus, setDbStatus] = useState('checking');

  useEffect(() => {
    let mounted = true;
    const check = async () => {
      try {
        const res = await fetchHealth();
        if (mounted) setDbStatus(res.db === 'connected' ? 'connected' : 'disconnected');
      } catch {
        if (mounted) setDbStatus('disconnected');
      }
    };
    check();
    const interval = setInterval(check, 30000);
    return () => {
      mounted = false;
      clearInterval(interval);
    };
  }, []);

  const statusColor = {
    connected: 'bg-green-500',
    disconnected: 'bg-red-500',
    checking: 'bg-yellow-500',
  };

  return (
    <header className="sticky top-0 z-30 bg-white border-b border-gray-200 px-4 lg:px-6 py-3">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-3">
          <button onClick={onMenuClick} className="lg:hidden p-1.5 hover:bg-gray-100 rounded-lg">
            <Menu className="w-5 h-5 text-gray-600" />
          </button>
          <h2 className="text-lg font-semibold text-gray-900">{title}</h2>
        </div>

        <div className="flex items-center gap-3">
          <div className="flex items-center gap-1.5 text-xs text-gray-500">
            <Activity className="w-3.5 h-3.5" />
            <span>DB</span>
            <span className={`w-2 h-2 rounded-full ${statusColor[dbStatus]}`} />
          </div>
        </div>
      </div>
    </header>
  );
}
