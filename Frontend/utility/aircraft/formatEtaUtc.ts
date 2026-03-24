/**
 * Formats API ETA (ISO 8601 instant, typically with Z) as HH:mm in UTC so the
 * data block matches backend semantics and is independent of browser timezone.
 */
export function formatEtaToUtcHhMm(eta: string): string {
  try {
    const trimmed = eta.trim();
    if (!trimmed) return "";
    const date = new Date(trimmed);
    if (Number.isNaN(date.getTime())) return "";
    const h = date.getUTCHours();
    const m = date.getUTCMinutes();
    return `${String(h).padStart(2, "0")}:${String(m).padStart(2, "0")}`;
  } catch {
    return "";
  }
}
