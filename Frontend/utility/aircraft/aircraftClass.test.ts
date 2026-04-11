import { Aircraft, POSITION_HISTORY_MAX_POINTS } from "./aircraftClass";

describe("Aircraft", () => {
  const basePosition = { x: 100, y: 200, altitude: 35000 };
  const baseGeo = { latitude: 35.0, longitude: 139.0 };
  const baseVector = {
    heading: 90,
    groundSpeed: 250,
    verticalSpeed: 0,
  };
  const baseInstructedVector = {
    heading: 90,
    groundSpeed: 250,
    altitude: 35000,
  };

  it("updateAircraftLocationInfo does NOT update instructedVector", () => {
    const aircraft = new Aircraft(
      "JAL123",
      basePosition,
      baseGeo,
      baseVector,
      baseInstructedVector,
      null,
      "Commercial",
      "B738",
      "HND",
      "RJTT",
      "NRT",
      "RJAA",
      "12:00",
      50,
      50,
      0
    );

    const newAircraft = new Aircraft(
      "JAL123",
      { x: 150, y: 250, altitude: 36000 },
      { latitude: 35.1, longitude: 139.1 },
      { heading: 95, groundSpeed: 260, verticalSpeed: 5 },
      { heading: 180, groundSpeed: 300, altitude: 39000 },
      { heading: 200, groundSpeed: 240, altitude: 34000 },
      "Commercial",
      "B738",
      "HND",
      "RJTT",
      "NRT",
      "RJAA",
      "12:05",
      50,
      50,
      30
    );

    aircraft.updateAircraftLocationInfo(newAircraft);

    expect(aircraft.position).toEqual(newAircraft.position);
    expect(aircraft.vector).toEqual(newAircraft.vector);
    expect(aircraft.instructedVector).toEqual(baseInstructedVector);
    expect(aircraft.eta).toBe("12:05");
    expect(aircraft.riskLevel).toBe(30);
    expect(aircraft.atcClearance).toEqual({
      heading: 200,
      groundSpeed: 240,
      altitude: 34000,
    });
    expect(aircraft.positionHistory).toHaveLength(1);
    expect(aircraft.positionHistory[0]).toMatchObject({
      latitude: baseGeo.latitude,
      longitude: baseGeo.longitude,
      altitude: basePosition.altitude,
    });
    expect(aircraft.positionHistory[0].recordedAt).toEqual(expect.any(String));
  });

  it("caps position history at POSITION_HISTORY_MAX_POINTS", () => {
    const aircraft = new Aircraft(
      "JAL999",
      basePosition,
      baseGeo,
      baseVector,
      baseInstructedVector,
      null,
      "Commercial",
      "B738",
      "HND",
      "RJTT",
      "NRT",
      "RJAA",
      "12:00",
      50,
      50,
      0
    );

    for (let i = 0; i < POSITION_HISTORY_MAX_POINTS + 5; i += 1) {
      const next = new Aircraft(
        "JAL999",
        { x: 100 + i, y: 200, altitude: 35000 },
        { latitude: 35.0 + i * 0.001, longitude: 139.0 },
        baseVector,
        baseInstructedVector,
        null,
        "Commercial",
        "B738",
        "HND",
        "RJTT",
        "NRT",
        "RJAA",
        "12:00",
        50,
        50,
        0
      );
      aircraft.updateAircraftLocationInfo(next);
    }

    expect(aircraft.positionHistory.length).toBe(POSITION_HISTORY_MAX_POINTS);
  });
});
