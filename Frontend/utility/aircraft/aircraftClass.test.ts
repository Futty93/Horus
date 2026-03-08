import { Aircraft } from "./aircraftClass";

describe("Aircraft", () => {
  const basePosition = { x: 100, y: 200, altitude: 35000 };
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

  it("constructs with provided values", () => {
    const aircraft = new Aircraft(
      "JAL123",
      basePosition,
      baseVector,
      baseInstructedVector,
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

    expect(aircraft.callsign).toBe("JAL123");
    expect(aircraft.position).toEqual(basePosition);
    expect(aircraft.vector).toEqual(baseVector);
    expect(aircraft.instructedVector).toEqual(baseInstructedVector);
    expect(aircraft.label).toEqual({ x: 50, y: 50 });
  });

  it("updateAircraftInfo updates position, vector, instructedVector, eta, riskLevel", () => {
    const aircraft = new Aircraft(
      "JAL123",
      basePosition,
      baseVector,
      baseInstructedVector,
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
      { heading: 95, groundSpeed: 260, verticalSpeed: 5 },
      { heading: 95, groundSpeed: 260, altitude: 36000 },
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

    aircraft.updateAircraftInfo(newAircraft);

    expect(aircraft.position).toEqual(newAircraft.position);
    expect(aircraft.vector).toEqual(newAircraft.vector);
    expect(aircraft.instructedVector).toEqual(newAircraft.instructedVector);
    expect(aircraft.eta).toBe("12:05");
    expect(aircraft.riskLevel).toBe(30);
  });

  it("updateAircraftLocationInfo does NOT update instructedVector", () => {
    const aircraft = new Aircraft(
      "JAL123",
      basePosition,
      baseVector,
      baseInstructedVector,
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
      { heading: 95, groundSpeed: 260, verticalSpeed: 5 },
      { heading: 180, groundSpeed: 300, altitude: 39000 },
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
  });
});
