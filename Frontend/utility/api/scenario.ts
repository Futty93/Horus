export interface FlightPlanWaypointDto {
  fix: string;
  altitude?: number;
  constraint?: string;
  speed?: number;
  action?: string;
}

export interface FlightPlanDto {
  callsign: string;
  aircraftType?: string;
  departureAirport: string;
  arrivalAirport: string;
  cruiseAltitude: number;
  cruiseSpeed: number;
  route: FlightPlanWaypointDto[];
}

export interface InitialPositionDto {
  latitude: number;
  longitude: number;
  altitude: number;
  heading: number;
  groundSpeed: number;
  verticalSpeed: number;
}

export interface ScenarioAircraft {
  flightPlan: FlightPlanDto;
  initialPosition: InitialPositionDto;
  /** Relative seconds from simulation start. null/0 = immediate spawn. Reserved for future delayed spawn. */
  spawnTime?: number;
}

export interface ScenarioJson {
  scenarioName?: string;
  description?: string;
  createdAt?: string;
  aircraft: ScenarioAircraft[];
}

const HANEDA_TEMPLATE_RAW: string[][] = [
  [
    "JAL101",
    "34.482",
    "136.614",
    "31000",
    "450",
    "0",
    "90",
    "B777",
    "RJBB",
    "RJAA",
  ],
  [
    "ANA102",
    "34.482",
    "140.614",
    "31000",
    "460",
    "0",
    "270",
    "B787",
    "RJAA",
    "RJBB",
  ],
  [
    "SKY103",
    "34.532",
    "136.814",
    "31000",
    "440",
    "0",
    "85",
    "B737",
    "RJOO",
    "RJTT",
  ],
  [
    "JAL104",
    "34.432",
    "140.414",
    "31000",
    "470",
    "0",
    "275",
    "B777",
    "RJTT",
    "RJOO",
  ],
  [
    "ANA201",
    "32.482",
    "138.614",
    "32000",
    "420",
    "0",
    "0",
    "A320",
    "RJFK",
    "RJSM",
  ],
  [
    "SKY202",
    "36.482",
    "138.614",
    "32000",
    "430",
    "0",
    "180",
    "B738",
    "RJSM",
    "RJFK",
  ],
  [
    "JAL203",
    "32.682",
    "138.414",
    "32000",
    "410",
    "0",
    "10",
    "B787",
    "RJOM",
    "RJSA",
  ],
  [
    "ANA204",
    "36.282",
    "138.814",
    "32000",
    "440",
    "0",
    "190",
    "A321",
    "RJSA",
    "RJOM",
  ],
  [
    "SKY301",
    "33.482",
    "137.614",
    "30000",
    "380",
    "0",
    "45",
    "B737",
    "RJFF",
    "RJAA",
  ],
  [
    "JAL302",
    "35.482",
    "139.614",
    "30000",
    "390",
    "0",
    "225",
    "B777",
    "RJAA",
    "RJFF",
  ],
  [
    "ANA303",
    "33.482",
    "139.614",
    "30000",
    "400",
    "0",
    "315",
    "A320",
    "RJGG",
    "RJOO",
  ],
  [
    "SKY304",
    "35.482",
    "137.614",
    "30000",
    "410",
    "0",
    "135",
    "B738",
    "RJOO",
    "RJGG",
  ],
  [
    "JAL305",
    "34.982",
    "138.114",
    "30000",
    "420",
    "0",
    "135",
    "B787",
    "RJFM",
    "RJTT",
  ],
  [
    "ANA306",
    "33.982",
    "139.114",
    "30000",
    "430",
    "0",
    "315",
    "A321",
    "RJTT",
    "RJFM",
  ],
  [
    "SKY401",
    "34.482",
    "137.114",
    "33000",
    "380",
    "0",
    "90",
    "B737",
    "RJOA",
    "RJAA",
  ],
  [
    "JAL402",
    "34.482",
    "136.614",
    "33000",
    "480",
    "0",
    "90",
    "B777",
    "RJFF",
    "RJAA",
  ],
  [
    "ANA403",
    "34.232",
    "137.314",
    "33000",
    "390",
    "0",
    "75",
    "A320",
    "RJFK",
    "RJGG",
  ],
  [
    "SKY404",
    "34.132",
    "136.814",
    "33000",
    "470",
    "0",
    "75",
    "B738",
    "RJOM",
    "RJGG",
  ],
  [
    "JAL501",
    "34.782",
    "138.314",
    "29000",
    "420",
    "1000",
    "180",
    "B777",
    "RJSM",
    "RJFF",
  ],
  [
    "ANA502",
    "34.182",
    "138.914",
    "34000",
    "430",
    "-1000",
    "0",
    "B787",
    "RJFF",
    "RJSM",
  ],
  [
    "SKY503",
    "34.682",
    "138.814",
    "31000",
    "410",
    "800",
    "200",
    "B737",
    "RJBE",
    "ROAH",
  ],
  [
    "JAL504",
    "34.282",
    "138.414",
    "35000",
    "440",
    "-800",
    "20",
    "B777",
    "ROAH",
    "RJBE",
  ],
  [
    "ANA601",
    "33.282",
    "137.414",
    "28000",
    "450",
    "0",
    "60",
    "A320",
    "RJEC",
    "RORA",
  ],
  [
    "SKY602",
    "35.682",
    "139.814",
    "28000",
    "460",
    "0",
    "240",
    "B738",
    "RORA",
    "RJEC",
  ],
  [
    "JAL603",
    "33.282",
    "139.814",
    "28000",
    "440",
    "0",
    "300",
    "B787",
    "ROMD",
    "RJSN",
  ],
  [
    "ANA604",
    "35.682",
    "137.414",
    "28000",
    "470",
    "0",
    "120",
    "A321",
    "RJSN",
    "ROMD",
  ],
  [
    "SKY605",
    "34.082",
    "137.814",
    "28000",
    "430",
    "0",
    "45",
    "B737",
    "RJGG",
    "RJTT",
  ],
  [
    "JAL606",
    "34.882",
    "139.414",
    "28000",
    "420",
    "0",
    "225",
    "B777",
    "RJTT",
    "RJGG",
  ],
];

export function getHanedaTemplate(): ScenarioJson {
  const aircraft: ScenarioAircraft[] = HANEDA_TEMPLATE_RAW.map((row) => {
    const [callsign, lat, lon, alt, gs, vs, hdg, type, dep, arr] = row;
    return {
      flightPlan: {
        callsign,
        aircraftType: type,
        departureAirport: dep,
        arrivalAirport: arr,
        cruiseAltitude: parseInt(alt, 10),
        cruiseSpeed: parseInt(gs, 10),
        route: [],
      },
      initialPosition: {
        latitude: parseFloat(lat),
        longitude: parseFloat(lon),
        altitude: parseInt(alt, 10),
        heading: parseInt(hdg, 10),
        groundSpeed: parseInt(gs, 10),
        verticalSpeed: parseInt(vs, 10),
      },
    };
  });
  return {
    scenarioName: "Haneda Samples (T09)",
    description: "T09 sector conflict test scenario",
    createdAt: new Date().toISOString().slice(0, 10),
    aircraft,
  };
}

export function exportScenario(scenario: ScenarioJson): void {
  const json = JSON.stringify(scenario, null, 2);
  const blob = new Blob([json], { type: "application/json" });
  const url = URL.createObjectURL(blob);
  const a = document.createElement("a");
  a.href = url;
  a.download =
    `scenario-${scenario.scenarioName ?? "export"}-${Date.now()}.json`.replace(
      /\s+/g,
      "-"
    );
  a.click();
  URL.revokeObjectURL(url);
}

function isScenarioLike(x: unknown): x is { aircraft: unknown[] } {
  return (
    !!x &&
    typeof x === "object" &&
    Array.isArray((x as Record<string, unknown>).aircraft)
  );
}

export function parseScenarioJson(json: string): ScenarioJson {
  const parsed = JSON.parse(json) as unknown;
  if (!isScenarioLike(parsed)) {
    throw new Error("Invalid scenario: missing aircraft array");
  }
  for (const a of parsed.aircraft) {
    const ac = a as Record<string, unknown>;
    const fp = ac?.flightPlan as Record<string, unknown> | undefined;
    if (!fp || typeof fp?.callsign !== "string" || !ac?.initialPosition) {
      throw new Error(
        "Invalid aircraft: each must have flightPlan and initialPosition"
      );
    }
  }
  return parsed as ScenarioJson;
}

export async function loadScenarioAndStart(
  scenario: ScenarioJson
): Promise<{ ok: boolean; message: string }> {
  try {
    const response = await fetch("/api/scenario/load", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(scenario),
    });
    if (!response.ok) {
      const text = await response.text();
      return { ok: false, message: text || `HTTP ${response.status}` };
    }
    return { ok: true, message: "Scenario loaded" };
  } catch (e) {
    return { ok: false, message: String(e) };
  }
}
