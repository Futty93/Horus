import {
  computeBounds,
  DEFAULT_CENTER,
  DEFAULT_RANGE,
  nearestCatalogFix,
  pickHitTarget,
  previewCanvasToGeo,
  toCanvas,
  type RoutePreviewPickTarget,
} from "./routePreviewGeometry";

describe("computeBounds", () => {
  it("returns defaults for empty points", () => {
    expect(computeBounds([])).toEqual({
      center: DEFAULT_CENTER,
      range: DEFAULT_RANGE,
    });
  });

  it("centers on min/max and rounds range", () => {
    const r = computeBounds([
      { latitude: 35, longitude: 140 },
      { latitude: 36, longitude: 141 },
    ]);
    expect(r.center.latitude).toBeCloseTo(35.5, 5);
    expect(r.center.longitude).toBeCloseTo(140.5, 5);
    expect(r.range).toBeGreaterThan(50);
  });
});

describe("nearestCatalogFix", () => {
  it("returns closest within maxKm", () => {
    const m = new Map<string, { latitude: number; longitude: number }>();
    m.set("AAA", { latitude: 35, longitude: 140 });
    m.set("BBB", { latitude: 35.5, longitude: 140 });
    const near = nearestCatalogFix(35.01, 140, m, 50);
    expect(near?.name).toBe("AAA");
  });

  it("returns null when all fixes are too far", () => {
    const m = new Map([["X", { latitude: 0, longitude: 0 }]]);
    expect(nearestCatalogFix(35, 140, m, 1)).toBeNull();
  });
});

describe("pickHitTarget", () => {
  const targets: RoutePreviewPickTarget[] = [{ x: 100, y: 100, label: "FOO" }];

  it("hits marker circle", () => {
    expect(pickHitTarget(100, 100, targets)).toBe("FOO");
  });

  it("returns null when far", () => {
    expect(pickHitTarget(200, 200, targets)).toBeNull();
  });
});

describe("toCanvas and previewCanvasToGeo round-trip (identity region)", () => {
  it("maps center to canvas center for square logical canvas", () => {
    const center = { latitude: 35, longitude: 140 };
    const range = 400;
    const w = 800;
    const h = 800;
    const p = toCanvas(center.latitude, center.longitude, center, range, w, h);
    expect(p.x).toBeCloseTo(w / 2, 0);
    expect(p.y).toBeCloseTo(h / 2, 0);
    const g = previewCanvasToGeo(p.x, p.y, center, range, w, h);
    expect(g.latitude).toBeCloseTo(center.latitude, 5);
    expect(g.longitude).toBeCloseTo(center.longitude, 5);
  });
});
