import { formatConflictPairLine, parseOtherCallsignFromPairId } from "./pairId";

describe("parseOtherCallsignFromPairId", () => {
  it("returns other when own is lexicographically first in pairId", () => {
    expect(parseOtherCallsignFromPairId("CF1-CF2", "CF1")).toBe("CF2");
  });

  it("returns other when own is lexicographically second", () => {
    expect(parseOtherCallsignFromPairId("CF1-CF2", "CF2")).toBe("CF1");
  });

  it("handles hyphenated own callsign (prefix match)", () => {
    expect(parseOtherCallsignFromPairId("A-B-ZZ", "A-B")).toBe("ZZ");
  });

  it("handles hyphenated own callsign (suffix match)", () => {
    expect(parseOtherCallsignFromPairId("A-B-ZZ", "ZZ")).toBe("A-B");
  });

  it("returns null when own is not in pairId", () => {
    expect(parseOtherCallsignFromPairId("CF1-CF2", "ZZ")).toBeNull();
  });
});

describe("formatConflictPairLine", () => {
  it("formats distances and TCPA", () => {
    expect(
      formatConflictPairLine({
        closestHorizontalDistance: 2.5,
        closestVerticalDistance: 400,
        riskLevel: 72,
        timeToClosest: 30,
      })
    ).toBe("H 2.5 NM · V 400 ft · R72 · TCPA 30s");
  });

  it("shows em dash for non-finite TCPA", () => {
    expect(
      formatConflictPairLine({
        closestHorizontalDistance: 0,
        closestVerticalDistance: 0,
        riskLevel: 0,
        timeToClosest: Number.POSITIVE_INFINITY,
      })
    ).toBe("H 0.0 NM · V 0 ft · R0 · TCPA —");
  });
});
