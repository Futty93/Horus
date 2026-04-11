import {
  ATC_ALTITUDE_TOLERANCE_FT,
  ATC_GROUND_SPEED_TOLERANCE_KT,
  ATC_HEADING_TOLERANCE_DEG,
  formatAtcClearanceMemoLine,
  smallestHeadingDifferenceDeg,
} from "./atcClearanceMemoLine";

describe("smallestHeadingDifferenceDeg", () => {
  it("returns small diff for nearby headings", () => {
    expect(smallestHeadingDifferenceDeg(270, 272)).toBe(2);
  });

  it("wraps across 0/360", () => {
    expect(smallestHeadingDifferenceDeg(359, 1)).toBe(2);
  });
});

describe("formatAtcClearanceMemoLine", () => {
  const actual = { altitudeFt: 35000, heading: 270, groundSpeed: 250 };

  it("returns null when no clearance", () => {
    expect(formatAtcClearanceMemoLine(null, actual)).toBeNull();
    expect(formatAtcClearanceMemoLine(undefined, actual)).toBeNull();
  });

  it("returns null when all axes within tolerance", () => {
    const c = {
      altitude: 35000 + ATC_ALTITUDE_TOLERANCE_FT - 1,
      heading: 270 + ATC_HEADING_TOLERANCE_DEG - 0.5,
      groundSpeed: 250 + ATC_GROUND_SPEED_TOLERANCE_KT - 1,
    };
    expect(formatAtcClearanceMemoLine(c, actual)).toBeNull();
  });

  it("includes FL pair when altitude out of tolerance", () => {
    const line = formatAtcClearanceMemoLine(
      { altitude: 36000, heading: 270, groundSpeed: 250 },
      actual
    );
    expect(line).toContain("360/350");
  });

  it("includes heading pair when heading out of tolerance", () => {
    const line = formatAtcClearanceMemoLine(
      { altitude: 35000, heading: 200, groundSpeed: 250 },
      actual
    );
    expect(line).toContain("200/270");
  });

  it("includes speed pair when speed out of tolerance", () => {
    const line = formatAtcClearanceMemoLine(
      { altitude: 35000, heading: 270, groundSpeed: 220 },
      actual
    );
    expect(line).toContain("220/250");
  });

  it("does not show heading or speed pairs when clearance was left at 0 (unset)", () => {
    const line = formatAtcClearanceMemoLine(
      { altitude: 36000, heading: 0, groundSpeed: 0 },
      { altitudeFt: 35000, heading: 233, groundSpeed: 440 }
    );
    expect(line).toBe("360/350");
    expect(line).not.toMatch(/233/);
    expect(line).not.toMatch(/440/);
  });

  it("omits altitude when omitAltitude is true (shown on primary row instead)", () => {
    const line = formatAtcClearanceMemoLine(
      { altitude: 36000, heading: 200, groundSpeed: 250 },
      { altitudeFt: 35000, heading: 270, groundSpeed: 250 },
      { omitAltitude: true }
    );
    expect(line).toContain("200/270");
    expect(line).not.toContain("360/350");
  });
});
