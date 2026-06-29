import axios from 'axios';

const baseURL = '/api/v1/export';

export function downloadAssetsCSV(filters = {}) {
  const params = new URLSearchParams();
  if (filters.status) params.set('status', filters.status);
  if (filters.category) params.set('category', filters.category);
  if (filters.search) params.set('search', filters.search);

  const url = `${baseURL}/assets/csv?${params.toString()}`;
  triggerDownload(url, 'assets.csv');
}

export function downloadTransactionsCSV(filters = {}) {
  const params = new URLSearchParams();
  if (filters.tx_type) params.set('tx_type', filters.tx_type);
  if (filters.from_ms) params.set('from_ms', filters.from_ms);
  if (filters.to_ms) params.set('to_ms', filters.to_ms);

  const url = `${baseURL}/transactions/csv?${params.toString()}`;
  triggerDownload(url, 'transactions.csv');
}

function triggerDownload(url, filename) {
  // Buat anchor element untuk trigger download
  const a = document.createElement('a');
  a.href = url;
  a.download = filename;
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
}
