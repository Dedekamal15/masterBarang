import { NavLink } from 'react-router-dom';
import {
  LayoutDashboard,
  Package,
  ArrowLeftRight,
  Download,
  X,
  Warehouse,
} from 'lucide-react';

const menuItems = [
  { to: '/', icon: LayoutDashboard, label: 'Dashboard' },
  { to: '/assets', icon: Package, label: 'Barang' },
  { to: '/transactions', icon: ArrowLeftRight, label: 'Transaksi' },
  { to: '/export', icon: Download, label: 'Export' },
];

export default function Sidebar({ open, onClose }) {
  return (
    <>
      {/* Overlay mobile */}
      {open && (
        <div
          className="fixed inset-0 bg-black/30 z-40 lg:hidden"
          onClick={onClose}
        />
      )}

      <aside
        className={`
          fixed top-0 left-0 z-50 h-full w-64 bg-white border-r border-gray-200
          transform transition-transform duration-200 ease-in-out
          ${open ? 'translate-x-0' : '-translate-x-full'}
          lg:translate-x-0 lg:static lg:z-auto
        `}
      >
        {/* Logo */}
        <div className="flex items-center justify-between px-6 py-5 border-b border-gray-200">
          <NavLink to="/" className="flex items-center gap-3">
            <div className="p-2 bg-blue-600 rounded-lg">
              <Warehouse className="w-5 h-5 text-white" />
            </div>
            <div>
              <h1 className="font-bold text-lg text-gray-900">AssetTrack</h1>
              <p className="text-xs text-gray-500 -mt-0.5">Manajemen Barang</p>
            </div>
          </NavLink>
          <button onClick={onClose} className="lg:hidden p-1 hover:bg-gray-100 rounded">
            <X className="w-5 h-5 text-gray-500" />
          </button>
        </div>

        {/* Menu */}
        <nav className="px-3 py-4 space-y-1">
          {menuItems.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.to === '/'}
              onClick={onClose}
              className={({ isActive }) =>
                `flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition ${
                  isActive
                    ? 'bg-blue-50 text-blue-700'
                    : 'text-gray-600 hover:bg-gray-100 hover:text-gray-900'
                }`
              }
            >
              <item.icon className="w-5 h-5" />
              {item.label}
            </NavLink>
          ))}
        </nav>
      </aside>
    </>
  );
}
