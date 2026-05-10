import type { ScenarioAircraft } from "@/types/scenario";

export type ScenarioRouteDef = {
  waypoints: string[];
  cruiseAltitude: number;
  cruiseSpeed: number;
};

export function groupByOd(aircraft: ScenarioAircraft[]): {
  origin: string;
  destination: string;
  aircraft: ScenarioAircraft[];
}[] {
  const byKey = new Map<string, ScenarioAircraft[]>();
  for (const a of aircraft) {
    const key = `${a.flightPlan.departureAirport}→${a.flightPlan.arrivalAirport}`;
    const list = byKey.get(key) ?? [];
    list.push(a);
    byKey.set(key, list);
  }
  return Array.from(byKey.entries()).map(([key, ac]) => {
    const [origin, destination] = key.split("→");
    return { origin, destination, aircraft: ac };
  });
}

export function deriveRouteByOd(
  aircraft: ScenarioAircraft[]
): Map<string, ScenarioRouteDef> {
  const m = new Map<string, ScenarioRouteDef>();
  for (const a of aircraft) {
    const key = `${a.flightPlan.departureAirport}→${a.flightPlan.arrivalAirport}`;
    if (m.has(key)) continue;
    m.set(key, {
      waypoints: a.flightPlan.route.map((wp) => wp.fix),
      cruiseAltitude: a.flightPlan.cruiseAltitude,
      cruiseSpeed: a.flightPlan.cruiseSpeed,
    });
  }
  return m;
}

export function validateScenarioForStart(
  aircraft: ScenarioAircraft[]
): string[] {
  const issues: string[] = [];
  const callsigns = aircraft.map((a) => a.flightPlan.callsign);
  const seen = new Set<string>();
  const dups = new Set<string>();
  for (const c of callsigns) {
    if (seen.has(c)) dups.add(c);
    seen.add(c);
  }
  if (dups.size > 0) {
    issues.push(`Duplicate callsigns: ${Array.from(dups).sort().join(", ")}`);
  }
  const emptyRoute = aircraft.filter((a) => a.flightPlan.route.length === 0);
  if (emptyRoute.length > 0) {
    issues.push(
      `Empty route: ${emptyRoute.map((a) => a.flightPlan.callsign).join(", ")}`
    );
  }
  return issues;
}
