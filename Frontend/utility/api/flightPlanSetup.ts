export interface AircraftForSetup {
  callsign: string;
  originIcao: string;
  destinationIcao: string;
  originIata: string;
  destinationIata: string;
  position: { latitude: number; longitude: number; altitude: number };
  vector: { heading: number; groundSpeed: number; verticalSpeed: number };
  type: string;
}

export interface FlightPlanWaypointInput {
  fix: string;
  altitude?: number;
  constraint?: string;
  speed?: number;
  action?: string;
}

export interface FlightPlanInput {
  callsign: string;
  aircraftType?: string;
  departureAirport: string;
  arrivalAirport: string;
  cruiseAltitude: number;
  cruiseSpeed: number;
  route: FlightPlanWaypointInput[];
}

export interface OdPair {
  origin: string;
  destination: string;
  aircraft: AircraftForSetup[];
}

export async function fetchAircraftList(): Promise<AircraftForSetup[]> {
  const response = await fetch("/api/aircraft/location/all", {
    method: "GET",
    headers: { Accept: "application/json" },
    cache: "no-store",
  });
  if (!response.ok) return [];
  const data = (await response.json()) as Array<{
    callsign: string;
    position: { latitude: number; longitude: number; altitude: number };
    vector: { heading: number; groundSpeed: number; verticalSpeed: number };
    type?: string;
    originIata?: string;
    originIcao?: string;
    destinationIata?: string;
    destinationIcao?: string;
  }>;
  return data.map((d) => ({
    callsign: d.callsign,
    originIcao: d.originIcao ?? "",
    destinationIcao: d.destinationIcao ?? "",
    originIata: d.originIata ?? "",
    destinationIata: d.destinationIata ?? "",
    position: d.position,
    vector: d.vector,
    type: d.type ?? "B738",
  }));
}

export async function createHanedaSamples(): Promise<{
  ok: boolean;
  message: string;
}> {
  try {
    const response = await fetch("/api/aircraft/create-haneda-samples", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
    });
    if (!response.ok) {
      const text = await response.text();
      return { ok: false, message: text || `HTTP ${response.status}` };
    }
    const body = (await response.json()) as string[];
    return { ok: true, message: body?.join(", ") ?? "Created" };
  } catch (e) {
    return { ok: false, message: String(e) };
  }
}

export async function assignFlightPlan(
  callsign: string,
  plan: FlightPlanInput
): Promise<{ ok: boolean; message: string }> {
  try {
    const body = {
      callsign: plan.callsign,
      aircraftType: plan.aircraftType ?? "B738",
      departureAirport: plan.departureAirport,
      arrivalAirport: plan.arrivalAirport,
      cruiseAltitude: plan.cruiseAltitude,
      cruiseSpeed: plan.cruiseSpeed,
      route: plan.route.map((wp) => ({
        fix: wp.fix,
        altitude: wp.altitude ?? null,
        constraint: wp.constraint ?? null,
        speed: wp.speed ?? null,
        action: wp.action ?? "CONTINUE",
      })),
    };
    const response = await fetch(`/api/aircraft/${callsign}/flightplan`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(body),
    });
    if (!response.ok) {
      const text = await response.text();
      return { ok: false, message: text || `HTTP ${response.status}` };
    }
    return { ok: true, message: "Assigned" };
  } catch (e) {
    return { ok: false, message: String(e) };
  }
}

export function groupByOd(aircraft: AircraftForSetup[]): OdPair[] {
  const map = new Map<string, AircraftForSetup[]>();
  for (const a of aircraft) {
    const key = `${a.originIcao}→${a.destinationIcao}`;
    const list = map.get(key) ?? [];
    list.push(a);
    map.set(key, list);
  }
  return Array.from(map.entries()).map(([key, list]) => {
    const [origin, destination] = key.split("→");
    return { origin, destination, aircraft: list };
  });
}
