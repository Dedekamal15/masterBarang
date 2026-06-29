import api from './axiosClient';

export async function fetchAssets({ status, category, search, page = 1, limit = 20 } = {}) {
  const params = { page, limit };
  if (status) params.status = status;
  if (category) params.category = category;
  if (search) params.search = search;
  return api.get('/assets', { params });
}

export async function fetchAssetById(id) {
  return api.get(`/assets/${id}`);
}

export async function fetchAssetTransactions(id, { tx_type, from_ms, to_ms, page = 1, limit = 20 } = {}) {
  const params = { page, limit };
  if (tx_type) params.tx_type = tx_type;
  if (from_ms) params.from_ms = from_ms;
  if (to_ms) params.to_ms = to_ms;
  return api.get(`/assets/${id}/transactions`, { params });
}
