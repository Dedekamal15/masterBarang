import { BrowserRouter, Routes, Route } from 'react-router-dom';
import Layout from './components/layout/Layout';
import Dashboard from './pages/Dashboard';
import AssetsList from './pages/AssetsList';
import AssetDetail from './pages/AssetDetail';
import TransactionsList from './pages/TransactionsList';
import TransactionDetail from './pages/TransactionDetail';
import ExportPage from './pages/ExportPage';
import NotFound from './pages/NotFound';

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route element={<Layout />}>
          <Route path="/" element={<Dashboard />} />
          <Route path="/assets" element={<AssetsList />} />
          <Route path="/assets/:id" element={<AssetDetail />} />
          <Route path="/transactions" element={<TransactionsList />} />
          <Route path="/transactions/:id" element={<TransactionDetail />} />
          <Route path="/export" element={<ExportPage />} />
          <Route path="*" element={<NotFound />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}
