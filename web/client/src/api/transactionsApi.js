import api from './axiosClient';

export async function fetchTransactions({ tx_type, from_ms, to_ms, page = 1, limit = 20 } = {}) {
  const params = { page, limit };
  if (tx_type) params.tx_type = tx_type;
  if (from_ms) params.from_ms = from_ms;
  if (to_ms) params.to_ms = to_ms;
  return api.get('/transactions', { params });
}

export async function fetchTransactionById(id) {
  return api.get(`/transactions/${id}`);
}
