import { drawRangeRings } from "./routeRenderer";
import { GLOBAL_SETTINGS } from "../globals/settings";
import type { RangeRingsSetting } from "@/context/rangeRingsSettingContext";
import type { DisplayRange } from "@/context/displayRangeContext";
import { NM_TO_KM } from "@/utility/rangeRings/rangeRingsUtils";

function createMockContext() {
  return {
    save: jest.fn(),
    restore: jest.fn(),
    beginPath: jest.fn(),
    arc: jest.fn(),
    stroke: jest.fn(),
    fillText: jest.fn(),
    strokeStyle: "",
    fillStyle: "",
    globalAlpha: 1,
    lineWidth: 1,
    font: "",
  } as unknown as CanvasRenderingContext2D;
}

beforeEach(() => {
  GLOBAL_SETTINGS.canvasWidth = 1000;
  GLOBAL_SETTINGS.canvasHeight = 1000;
});

describe("drawRangeRings", () => {
  it("does not draw when enabled=false", () => {
    const ctx = createMockContext();
    const displayRange: DisplayRange = { range: 400 };
    const settings: RangeRingsSetting = { enabled: false, intervalNm: 10 };

    drawRangeRings(ctx, displayRange, settings);

    expect(ctx.arc).not.toHaveBeenCalled();
    expect(ctx.stroke).not.toHaveBeenCalled();
    expect(ctx.fillText).not.toHaveBeenCalled();
    expect(ctx.save).not.toHaveBeenCalled();
  });

  it("draws rings when enabled=true with default interval", () => {
    const ctx = createMockContext();
    const displayRange: DisplayRange = { range: 400 };
    const settings: RangeRingsSetting = { enabled: true, intervalNm: 10 };

    drawRangeRings(ctx, displayRange, settings);

    const maxRadiusNm = 200 / NM_TO_KM;
    const expectedRingCount = Math.floor(maxRadiusNm / 10);
    expect(ctx.arc).toHaveBeenCalledTimes(expectedRingCount);
    expect(ctx.stroke).toHaveBeenCalledTimes(expectedRingCount);
    expect(ctx.fillText).toHaveBeenCalledTimes(expectedRingCount);
    expect(ctx.save).toHaveBeenCalled();
    expect(ctx.restore).toHaveBeenCalled();
  });

  it("draws fewer rings with larger interval", () => {
    const ctx = createMockContext();
    const displayRange: DisplayRange = { range: 400 };
    const settings50: RangeRingsSetting = { enabled: true, intervalNm: 50 };

    drawRangeRings(ctx, displayRange, settings50);

    const maxRadiusNm = 200 / NM_TO_KM;
    const expectedRingCount50 = Math.floor(maxRadiusNm / 50);
    expect(ctx.arc).toHaveBeenCalledTimes(expectedRingCount50);
    expect(ctx.fillText).toHaveBeenCalledWith(
      "50 NM",
      expect.any(Number),
      expect.any(Number)
    );
  });

  it("draws more rings with smaller interval", () => {
    const ctx = createMockContext();
    const displayRange: DisplayRange = { range: 400 };
    const settings5: RangeRingsSetting = { enabled: true, intervalNm: 5 };

    drawRangeRings(ctx, displayRange, settings5);

    const maxRadiusNm = 200 / NM_TO_KM;
    const expectedRingCount5 = Math.floor(maxRadiusNm / 5);
    expect(ctx.arc).toHaveBeenCalledTimes(expectedRingCount5);
  });

  it("draws no rings when maxRadiusNm is less than intervalNm", () => {
    const ctx = createMockContext();
    const displayRange: DisplayRange = { range: 20 };
    const settings: RangeRingsSetting = { enabled: true, intervalNm: 10 };

    drawRangeRings(ctx, displayRange, settings);

    expect(ctx.arc).not.toHaveBeenCalled();
    expect(ctx.stroke).not.toHaveBeenCalled();
    expect(ctx.fillText).not.toHaveBeenCalled();
    expect(ctx.save).toHaveBeenCalled();
    expect(ctx.restore).toHaveBeenCalled();
  });

  it("restores canvas state via save/restore", () => {
    const ctx = createMockContext();
    const displayRange: DisplayRange = { range: 400 };
    const settings: RangeRingsSetting = { enabled: true, intervalNm: 50 };

    drawRangeRings(ctx, displayRange, settings);

    expect(ctx.save).toHaveBeenCalled();
    expect(ctx.restore).toHaveBeenCalled();
  });
});
