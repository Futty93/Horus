import { formatEtaToUtcHhMm } from "./formatEtaUtc";

describe("formatEtaToUtcHhMm", () => {
  it("formats Zulu ISO as UTC HH:mm", () => {
    expect(formatEtaToUtcHhMm("2024-09-11T12:55:00Z")).toBe("12:55");
  });

  it("uses UTC for offset timestamps (same instant)", () => {
    expect(formatEtaToUtcHhMm("2024-09-11T15:55:00+03:00")).toBe("12:55");
  });

  it("pads hours and minutes", () => {
    expect(formatEtaToUtcHhMm("2024-01-01T03:05:00Z")).toBe("03:05");
    expect(formatEtaToUtcHhMm("2024-01-01T00:00:00Z")).toBe("00:00");
  });

  it("returns empty for blank or invalid", () => {
    expect(formatEtaToUtcHhMm("")).toBe("");
    expect(formatEtaToUtcHhMm("   ")).toBe("");
    expect(formatEtaToUtcHhMm("not-a-date")).toBe("");
  });
});
