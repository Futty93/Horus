import type { ScenarioJson, ScenarioAircraft } from "@/types/scenario";
import {
  parseScenarioJson,
  toScenarioJsonString,
  getExportFilename,
  hanedaTemplate,
} from "./scenario";

const validAircraft: ScenarioAircraft = {
  flightPlan: {
    callsign: "JAL101",
    aircraftType: "B777",
    departureAirport: "RJTT",
    arrivalAirport: "RJAA",
    cruiseAltitude: 35000,
    cruiseSpeed: 450,
    route: [{ fix: "KOITO", action: "CONTINUE" }, { fix: "BOKJO" }],
  },
  initialPosition: {
    latitude: 35.48,
    longitude: 139.61,
    altitude: 31000,
    heading: 90,
    groundSpeed: 450,
    verticalSpeed: 0,
  },
};

describe("parseScenarioJson", () => {
  it.each<[string, string, ScenarioJson]>([
    [
      "valid single aircraft",
      JSON.stringify({ aircraft: [validAircraft] }),
      { aircraft: [validAircraft] },
    ],
    [
      "valid with metadata",
      JSON.stringify({
        scenarioName: "Test",
        description: "Desc",
        createdAt: "2026-03-08",
        aircraft: [validAircraft],
      }),
      {
        scenarioName: "Test",
        description: "Desc",
        createdAt: "2026-03-08",
        aircraft: [validAircraft],
      },
    ],
    [
      "valid empty aircraft array",
      JSON.stringify({ aircraft: [] }),
      { aircraft: [] },
    ],
    [
      "valid multiple aircraft",
      JSON.stringify({
        aircraft: [
          validAircraft,
          {
            ...validAircraft,
            flightPlan: { ...validAircraft.flightPlan, callsign: "ANA102" },
          },
        ],
      }),
      {
        aircraft: [
          validAircraft,
          {
            ...validAircraft,
            flightPlan: { ...validAircraft.flightPlan, callsign: "ANA102" },
          },
        ],
      },
    ],
  ])("parses %s", (_, json, expected) => {
    const result = parseScenarioJson(json);
    expect(result).toEqual(expected);
  });

  it.each<[string, string, string]>([
    [
      "missing aircraft array",
      "{}",
      "Invalid scenario: missing aircraft array",
    ],
    [
      "aircraft is null",
      '{"aircraft":null}',
      "Invalid scenario: missing aircraft array",
    ],
    [
      "aircraft is string",
      '{"aircraft":"x"}',
      "Invalid scenario: missing aircraft array",
    ],
    [
      "missing flightPlan",
      JSON.stringify({
        aircraft: [{ initialPosition: validAircraft.initialPosition }],
      }),
      "Invalid aircraft: each must have flightPlan and initialPosition",
    ],
    [
      "missing initialPosition",
      JSON.stringify({
        aircraft: [{ flightPlan: validAircraft.flightPlan }],
      }),
      "Invalid aircraft: each must have flightPlan and initialPosition",
    ],
    [
      "flightPlan.callsign is number",
      JSON.stringify({
        aircraft: [
          {
            flightPlan: { ...validAircraft.flightPlan, callsign: 123 },
            initialPosition: validAircraft.initialPosition,
          },
        ],
      }),
      "Invalid aircraft: each must have flightPlan and initialPosition",
    ],
    ["invalid JSON syntax", "{ invalid }", "JSON"],
  ])("throws for %s", (_, json, expectedMessage) => {
    expect(() => parseScenarioJson(json)).toThrow(expectedMessage);
  });
});

describe("toScenarioJsonString", () => {
  it.each<[ScenarioJson, boolean]>([
    [{ aircraft: [] }, true],
    [{ aircraft: [] }, false],
    [{ scenarioName: "X", aircraft: [validAircraft] }, true],
  ])("serializes scenario (pretty=%s)", (scenario, pretty) => {
    const result = toScenarioJsonString(scenario, pretty);
    expect(JSON.parse(result)).toEqual(scenario);
  });

  it("produces pretty-printed JSON when pretty=true", () => {
    const result = toScenarioJsonString({ aircraft: [] }, true);
    expect(result).toContain("\n");
  });

  it("produces minified JSON when pretty=false", () => {
    const result = toScenarioJsonString({ aircraft: [] }, false);
    expect(result).toBe('{"aircraft":[]}');
  });

  it("round-trips through parseScenarioJson", () => {
    const scenario: ScenarioJson = {
      scenarioName: "RoundTrip",
      aircraft: [validAircraft],
    };
    const json = toScenarioJsonString(scenario);
    const parsed = parseScenarioJson(json);
    expect(parsed).toEqual(scenario);
  });
});

describe("hanedaTemplate", () => {
  it("is valid ScenarioJson with 28 aircraft", () => {
    expect(hanedaTemplate.aircraft).toHaveLength(28);
    expect(hanedaTemplate.scenarioName).toBe("Haneda Samples (T09)");
    for (const ac of hanedaTemplate.aircraft) {
      expect(ac.flightPlan).toBeDefined();
      expect(ac.flightPlan.callsign).toBeDefined();
      expect(ac.initialPosition).toBeDefined();
      expect(typeof ac.initialPosition.latitude).toBe("number");
      expect(typeof ac.initialPosition.longitude).toBe("number");
    }
  });

  it("round-trips through parseScenarioJson", () => {
    const json = toScenarioJsonString(hanedaTemplate);
    const parsed = parseScenarioJson(json);
    expect(parsed.aircraft).toHaveLength(hanedaTemplate.aircraft.length);
    expect(parsed.scenarioName).toBe(hanedaTemplate.scenarioName);
    expect(parsed.createdAt).toBe(hanedaTemplate.createdAt);
  });
});

describe("getExportFilename", () => {
  it.each<[ScenarioJson, number | undefined, RegExp]>([
    [{ aircraft: [] }, 1234567890, /^scenario-export-1234567890\.json$/],
    [
      { scenarioName: "Tokyo", aircraft: [] },
      undefined,
      /^scenario-Tokyo-\d+\.json$/,
    ],
    [
      { scenarioName: "Space In Name", aircraft: [] },
      1,
      /^scenario-Space-In-Name-1\.json$/,
    ],
  ])(
    "generates filename for scenario with name %s",
    (scenario, ts, pattern) => {
      const name = getExportFilename(scenario, ts);
      expect(name).toMatch(pattern);
    }
  );
});
