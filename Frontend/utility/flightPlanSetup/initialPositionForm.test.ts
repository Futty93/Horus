import { positionToFormStrings } from "./initialPositionForm";

describe("positionToFormStrings", () => {
  it("stringifies all fields", () => {
    const s = positionToFormStrings({
      latitude: 35.5,
      longitude: 139.1,
      altitude: 320,
      heading: 270,
      groundSpeed: 250,
      verticalSpeed: -500,
    });
    expect(s.lat).toBe("35.5");
    expect(s.lon).toBe("139.1");
    expect(s.verticalSpeed).toBe("-500");
  });
});
