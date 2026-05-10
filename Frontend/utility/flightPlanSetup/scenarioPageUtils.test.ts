import type { ScenarioAircraft } from "@/types/scenario";
import {
  deriveRouteByOd,
  groupByOd,
  validateScenarioForStart,
} from "./scenarioPageUtils";

function ac(
  callsign: string,
  dep: string,
  arr: string,
  route: string[]
): ScenarioAircraft {
  return {
    flightPlan: {
      callsign,
      departureAirport: dep,
      arrivalAirport: arr,
      route: route.map((fix) => ({ fix, action: "CONTINUE" as const })),
      cruiseAltitude: 350,
      cruiseSpeed: 280,
    },
    initialPosition: {
      latitude: 35,
      longitude: 140,
      altitude: 10000,
      heading: 90,
      groundSpeed: 250,
      verticalSpeed: 0,
    },
  };
}

describe("groupByOd", () => {
  it("groups by departure and arrival", () => {
    const list = [
      ac("A", "RJTT", "RJFK", ["X"]),
      ac("B", "RJTT", "RJFK", ["Y"]),
      ac("C", "RJBB", "RJFK", ["Z"]),
    ];
    const g = groupByOd(list);
    expect(g).toHaveLength(2);
    const ttFj = g.find((p) => p.origin === "RJTT");
    expect(ttFj?.aircraft).toHaveLength(2);
  });
});

describe("deriveRouteByOd", () => {
  it("keeps first aircraft route per O/D key", () => {
    const m = deriveRouteByOd([
      ac("A", "RJTT", "RJFK", ["P", "Q"]),
      ac("B", "RJTT", "RJFK", ["R"]),
    ]);
    expect(m.get("RJTT→RJFK")?.waypoints).toEqual(["P", "Q"]);
  });
});

describe("validateScenarioForStart", () => {
  it("flags duplicate callsigns", () => {
    const issues = validateScenarioForStart([
      ac("SAME", "RJTT", "RJFK", []),
      ac("SAME", "RJBB", "RJFK", []),
    ]);
    expect(issues.some((s) => s.includes("Duplicate"))).toBe(true);
  });

  it("flags empty routes", () => {
    const issues = validateScenarioForStart([ac("A", "RJTT", "RJFK", [])]);
    expect(issues.some((s) => s.includes("Empty route"))).toBe(true);
  });

  it("returns empty when valid", () => {
    expect(
      validateScenarioForStart([ac("A", "RJTT", "RJFK", ["X"])]).length
    ).toBe(0);
  });
});
