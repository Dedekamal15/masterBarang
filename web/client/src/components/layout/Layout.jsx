import { useState } from 'react';
import { Outlet, useLocation } from 'react-router-dom';
import Sidebar from './Sidebar';
import Navbar from './Navbar';

const pageTitles = {
  '/': 'Dashboard',
  '/assets': 'Daftar Barang',
  '/transactions': 'Daftar Transaksi',
  '/export': 'Export Data',
};

export default function Layout() {
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const location = useLocation();
  const title = pageTitles[location.pathname] || 'AssetTrack';

  // Cari title untuk route parameterized seperti /assets/:id
  const derivedTitle = (() => {
    if (title !== 'AssetTrack') return title;
    if (location.pathname.startsWith('/assets/') && location.pathname !== '/assets') return 'Detail Barang';
    if (location.pathname.startsWith('/transactions/') && location.pathname !== '/transactions') return 'Detail Transaksi';
    return 'AssetTrack';
  })();

  return (
    <div className="flex h-screen overflow-hidden">
      <Sidebar open={sidebarOpen} onClose={() => setSidebarOpen(false)} />
      <div className="flex-1 flex flex-col overflow-hidden">
        <Navbar title={derivedTitle} onMenuClick={() => setSidebarOpen(true)} />
        <main className="flex-1 overflow-y-auto p-4 lg:p-6 bg-gray-50">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
