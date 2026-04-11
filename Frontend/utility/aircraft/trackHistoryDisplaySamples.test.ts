import type { PositionHistoryPoint } from "./aircraftClass";
import {
  getNewestHistoryRecordedAtMs,
  isTrackHistoryFreshForDraw,
  selectTrackDisplaySamples,
  TRACK_DISPLAY_DOT_COUNT,
  TRACK_DISPLAY_INTERVAL_SEC,
  TRACK_DISPLAY_OFFSETS_SEC,
} from "./trackHistoryDisplaySamples";

function pt(
  offsetSecFromNow: number,
  nowMs: number,
  lat = 35,
  lon = 139
): PositionHistoryPoint {
  return {
    latitude: lat,
    longitude: lon,
    altitude: 10000,
    recordedAt: new Date(nowMs - offsetSecFromNow * 1000).toISOString(),
  };
}

describe("selectTrackDisplaySamples", () => {
  it("picks one sample at or before each of 10/20/30s deadlines", () => {
    const now = Date.parse("2026-04-04T12:00:00.000Z");
    const history: PositionHistoryPoint[] = [
      pt(35, now),
      pt(22, now),
      pt(12, now),
      pt(11, now),
      pt(8, now),
      pt(3, now),
    ];

    const selected = selectTrackDisplaySamples(history, now);
    expect(selected).toHaveLength(TRACK_DISPLAY_DOT_COUNT);

    const agesSec = selected.map((p) =>
      Math.round((now - Date.parse(p.recordedAt)) / 1000)
    );
    expect(agesSec[0]).toBeGreaterThanOrEqual(10);
    expect(agesSec[0]).toBeLessThanOrEqual(12);
    expect(agesSec[1]).toBeGreaterThanOrEqual(20);
    expect(agesSec[1]).toBeLessThanOrEqual(22);
    expect(agesSec[2]).toBeGreaterThanOrEqual(30);
    expect(agesSec[2]).toBeLessThanOrEqual(35);
  });

  it("returns empty when history does not reach the first offset (10s)", () => {
    const now = Date.parse("2026-04-04T12:00:00.000Z");
    const history: PositionHistoryPoint[] = [pt(3, now), pt(1, now)];
    const selected = selectTrackDisplaySamples(history, now);
    expect(selected).toHaveLength(0);
  });

  it("deduplicates when the same sample is the best for multiple offsets", () => {
    const now = Date.parse("2026-04-04T12:00:00.000Z");
    const only = pt(100, now);
    const history: PositionHistoryPoint[] = [only, pt(5, now)];
    const selected = selectTrackDisplaySamples(history, now);
    expect(selected).toHaveLength(1);
    expect(selected[0]).toEqual(only);
  });

  it("derives offsets from dot count and interval", () => {
    expect(TRACK_DISPLAY_DOT_COUNT).toBe(3);
    expect(TRACK_DISPLAY_INTERVAL_SEC).toBe(10);
    expect([...TRACK_DISPLAY_OFFSETS_SEC]).toEqual([10, 20, 30]);
  });
});

describe("getNewestHistoryRecordedAtMs / isTrackHistoryFreshForDraw", () => {
  it("returns newest timestamp", () => {
    const now = Date.parse("2026-04-04T12:00:00.000Z");
    const history: PositionHistoryPoint[] = [pt(30, now), pt(5, now)];
    expect(getNewestHistoryRecordedAtMs(history)).toBe(now - 5_000);
  });

  it("isTrackHistoryFreshForDraw is false when newest sample exceeds max age", () => {
    const nowMs = 1_700_000_000_000;
    const history: PositionHistoryPoint[] = [
      {
        latitude: 35,
        longitude: 139,
        altitude: 10_000,
        recordedAt: new Date(nowMs - 20_000).toISOString(),
      },
    ];
    expect(isTrackHistoryFreshForDraw(history, nowMs, 10_000)).toBe(false);
  });

  it("isTrackHistoryFreshForDraw is false for empty history", () => {
    expect(isTrackHistoryFreshForDraw([], Date.now())).toBe(false);
  });
});
