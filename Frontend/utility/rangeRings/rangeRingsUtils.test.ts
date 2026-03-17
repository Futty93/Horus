import { NM_TO_KM, nmToKm, nmToCanvasRadiusPx } from "./rangeRingsUtils";

describe("rangeRingsUtils", () => {
  describe("NM_TO_KM", () => {
    it("is 1.852 (international nautical mile)", () => {
      expect(NM_TO_KM).toBe(1.852);
    });
  });

  describe("nmToKm", () => {
    it("converts 1 NM to 1.852 km", () => {
      expect(nmToKm(1)).toBe(1.852);
    });

    it("converts 10 NM to 18.52 km", () => {
      expect(nmToKm(10)).toBe(18.52);
    });

    it("converts 0 NM to 0 km", () => {
      expect(nmToKm(0)).toBe(0);
    });
  });

  describe("nmToCanvasRadiusPx", () => {
    it("scales distance correctly: 10 NM at 400km range, 1000px canvas", () => {
      const radiusPx = nmToCanvasRadiusPx(10, 1000, 400);
      expect(radiusPx).toBeCloseTo(46.3, 1);
    });

    it("at half display range, radius equals half canvas width", () => {
      const rangeKm = 200;
      const canvasWidth = 1000;
      const halfRangeNm = rangeKm / 2 / NM_TO_KM;
      const radiusPx = nmToCanvasRadiusPx(halfRangeNm, canvasWidth, rangeKm);
      expect(radiusPx).toBeCloseTo(500, 0);
    });

    it("returns 0 for 0 NM", () => {
      expect(nmToCanvasRadiusPx(0, 1000, 400)).toBe(0);
    });
  });
});
