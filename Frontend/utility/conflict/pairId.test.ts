import { formatConflictPairLine, getOtherCallsignFromPair } from "./pairId";

describe("getOtherCallsignFromPair", () => {
  it("returns opposite callsign from structured pair", () => {
    expect(
      getOtherCallsignFromPair(
        { callsignA: "ANA601", callsignB: "SKY605" },
        "ANA601"
      )
    ).toBe("SKY605");
    expect(
      getOtherCallsignFromPair(
        { callsignA: "ANA601", callsignB: "SKY605" },
        "SKY605"
      )
    ).toBe("ANA601");
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
