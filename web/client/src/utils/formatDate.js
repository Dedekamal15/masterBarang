import { format, fromUnixTime } from 'date-fns';
import { id } from 'date-fns/locale/id';

/**
 * Konversi epoch milliseconds ke string tanggal Indonesia
 */
export function formatEpoch(ms, fmt = 'dd MMM yyyy HH:mm') {
  if (!ms) return '-';
  try {
    return format(fromUnixTime(ms / 1000), fmt, { locale: id });
  } catch {
    return String(ms);
  }
}

/**
 * Konversi epoch ke tanggal (format pendek)
 */
export function formatDate(ms) {
  return formatEpoch(ms, 'dd MMM yyyy');
}

/**
 * Konversi epoch ke waktu
 */
export function formatTime(ms) {
  return formatEpoch(ms, 'HH:mm');
}
