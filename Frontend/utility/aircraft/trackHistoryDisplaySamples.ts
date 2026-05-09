import type { PositionHistoryPoint } from "./aircraftClass";

/** Number of history dots drawn on the radar (nearest at INTERVAL_SEC, then 2×,3×, …). */
export const TRACK_DISPLAY_DOT_COUNT = 3;

/** Seconds between successive displayed dots; first dot is at this many seconds before "now". */
export const TRACK_DISPLAY_INTERVAL_SEC = 10;

/** Offsets in seconds before "now", derived from dot count and interval (e.g. [10, 20, 30]). */
export const TRACK_DISPLAY_OFFSETS_SEC: readonly number[] = Object.freeze(
  Array.from(
    { length: TRACK_DISPLAY_DOT_COUNT },
    (_, i) => (i + 1) * TRACK_DISPLAY_INTERVAL_SEC
  )
);

/** Canvas radius in px for each history dot (aircraft marker is 5). */
export const TRACK_DISPLAY_DOT_RADIUS_PX = 2;

/**
 * When the newest history sample is older than this, skip drawing trail dots so
 * frozen positions do not leave persistent marks after location polling stops.
 */
export const TRACK_HISTORY_DRAW_MAX_STALE_MS = 10_000;

function recordedAtMs(p: PositionHistoryPoint): number | null {
  const t = Date.parse(p.recordedAt);
  return Number.isNaN(t) ? null : t;
}

/** Latest `recordedAt` in history, or null if empty / unparseable. */
export function getNewestHistoryRecordedAtMs(
  history: PositionHistoryPoint[]
): number | null {
  let best: number | null = null;
  for (const p of history) {
    const t = recordedAtMs(p);
    if (t === null) continue;
    if (best === null || t > best) best = t;
  }
  return best;
}

export function isTrackHistoryFreshForDraw(
  history: PositionHistoryPoint[],
  nowMs: number,
  maxStaleMs: number = TRACK_HISTORY_DRAW_MAX_STALE_MS
): boolean {
  if (history.length === 0) return false;
  const newest = getNewestHistoryRecordedAtMs(history);
  if (newest === null) return false;
  return nowMs - newest <= maxStaleMs;
}

/** Backend presets; avoids divide-by-zero and matches SimulationTiming. */
const MIN_SIM_SPEED_MULTIPLIER = 0.25;

/**
 * For each offset (simulation-time seconds before "now"), choose the newest sample with
 * recordedAt <= (now - offsetSimSec * 1000 / speedMultiplier). History timestamps are
 * wall-clock client receive times; scaling aligns dot spacing with sim time when speed ≠ 1.
 */
export function selectTrackDisplaySamples(
  history: PositionHistoryPoint[],
  nowMs: number,
  simulationSpeedMultiplier = 1
): PositionHistoryPoint[] {
  const speed = Math.max(simulationSpeedMultiplier, MIN_SIM_SPEED_MULTIPLIER);
  const seen = new Set<string>();
  const out: PositionHistoryPoint[] = [];

  for (const sec of TRACK_DISPLAY_OFFSETS_SEC) {
    const wallMsAgo = (sec * 1000) / speed;
    const deadline = nowMs - wallMsAgo;
    let best: PositionHistoryPoint | null = null;
    let bestT = -Infinity;

    for (const p of history) {
      const t = recordedAtMs(p);
      if (t === null || t > deadline) continue;
      if (t > bestT) {
        bestT = t;
        best = p;
      }
    }

    if (best == null) continue;
    const key = `${best.recordedAt}|${best.latitude}|${best.longitude}|${best.altitude}`;
    if (seen.has(key)) continue;
    seen.add(key);
    out.push(best);
  }

  return out;
}
