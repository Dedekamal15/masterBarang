import api from './axiosClient';

export async function fetchStats() {
  return api.get('/stats');
}

export async function fetchHealth() {
  return api.get('/health');
}
