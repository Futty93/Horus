import { formatAltitudeTargetVsActualLabel } from "./altitudeDataBlockLabel";

describe("formatAltitudeTargetVsActualLabel", () => {
  it("shows single FL when display FL matches even if raw ft differs slightly", () => {
    expect(formatAltitudeTargetVsActualLabel(31000, 31050)).toBe("310");
    expect(formatAltitudeTargetVsActualLabel(31001, 31099)).toBe("310");
  });

  it("shows arrow when FL hundreds differ", () => {
    expect(formatAltitudeTargetVsActualLabel(32000, 31000)).toBe("320 ↑ 310");
    expect(formatAltitudeTargetVsActualLabel(30000, 31000)).toBe("300 ↓ 310");
  });

  it("shows single FL when target and actual ft are exactly equal", () => {
    expect(formatAltitudeTargetVsActualLabel(31000, 31000)).toBe("310");
  });
});
