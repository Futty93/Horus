import { controlAircraft } from "./controlAircraft";
import type { InstructedVector } from "@/context/selectedAircraftContext";

const originalFetch = global.fetch;

beforeEach(() => {
  global.fetch = jest.fn();
});

afterEach(() => {
  global.fetch = originalFetch;
});

describe("controlAircraft", () => {
  const instructedVector: InstructedVector = {
    altitude: 35000,
    groundSpeed: 250,
    heading: 90,
  };

  it.each([
    ["empty string", ""],
    ["too short", "A"],
  ])("returns false when callsign is %s", async (_, callsign) => {
    const result = await controlAircraft(callsign, instructedVector);
    expect(result).toBe(false);
    expect(global.fetch).not.toHaveBeenCalled();
  });

  it("returns true when fetch succeeds", async () => {
    (global.fetch as jest.Mock).mockResolvedValue({ ok: true });

    const result = await controlAircraft("JAL123", instructedVector);

    expect(result).toBe(true);
    expect(global.fetch).toHaveBeenCalledWith(
      expect.stringContaining("/api/aircraft/control/JAL123"),
      expect.objectContaining({
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          instructedAltitude: 35000,
          instructedGroundSpeed: 250,
          instructedHeading: 90,
        }),
      })
    );
  });

  it.each([
    [
      "returns not ok",
      () => (global.fetch as jest.Mock).mockResolvedValue({ ok: false }),
    ],
    [
      "throws",
      () =>
        (global.fetch as jest.Mock).mockRejectedValue(
          new Error("Network error")
        ),
    ],
  ])("returns false when fetch %s", async (_, setupFetch) => {
    setupFetch();

    const result = await controlAircraft("JAL123", instructedVector);

    expect(result).toBe(false);
  });
});
