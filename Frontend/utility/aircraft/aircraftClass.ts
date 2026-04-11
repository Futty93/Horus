export type AtcClearanceVector = {
  heading: number;
  groundSpeed: number;
  altitude: number;
};

/** Past geographic samples for track dots; canvas coords are derived at draw time. */
export type PositionHistoryPoint = {
  latitude: number;
  longitude: number;
  altitude: number;
  /** ISO 8601 instant when this sample was recorded on the client. */
  recordedAt: string;
};

/** Enough for 90s track marks at ~1 Hz polls plus margin. */
export const POSITION_HISTORY_MAX_POINTS = 120;

export class Aircraft {
  callsign: string;
  position: { x: number; y: number; altitude: number };
  /** WGS-84 from last API sample; kept in sync with `position` for history / replay. */
  geoPosition: { latitude: number; longitude: number };
  vector: { heading: number; groundSpeed: number; verticalSpeed: number };
  /**
   * Pilot-applied target from Operator (POST /control). Not the controller’s clearance memo;
   * see spec/20260326-instruction-memo-radar-label/spec.md.
   */
  instructedVector: { heading: number; groundSpeed: number; altitude: number };
  /** Controller-recorded clearance memo from POST .../atc-clearance; null if none. */
  atcClearance: AtcClearanceVector | null;
  type: string; // 航空機カテゴリ（Commercial, Military, Helicopter）
  model: string; // 航空機機種（B738, F-35A, UH-60J等）
  originIata: string;
  originIcao: string;
  destinationIata: string;
  destinationIcao: string;
  /** ISO 8601 instant from API; data block shows UTC HH:mm (see spec 20260318-data-block-display-items). */
  eta: string;
  label: { x: number; y: number };
  riskLevel: number; // 危険度（0-100）
  positionHistory: PositionHistoryPoint[];

  constructor(
    callsign: string,
    position: { x: number; y: number; altitude: number },
    geoPosition: { latitude: number; longitude: number },
    vector: { heading: number; groundSpeed: number; verticalSpeed: number },
    instructedVector: {
      heading: number;
      groundSpeed: number;
      altitude: number;
    },
    atcClearance: AtcClearanceVector | null = null,
    type: string,
    model: string,
    originIata: string,
    originIcao: string,
    destinationIata: string,
    destinationIcao: string,
    eta: string,
    labelX: number = 50,
    labelY: number = 50,
    riskLevel: number = 0
  ) {
    this.callsign = callsign;
    this.position = position;
    this.geoPosition = geoPosition;
    this.vector = vector;
    this.instructedVector = instructedVector;
    this.atcClearance = atcClearance;
    this.type = type;
    this.model = model;
    this.originIata = originIata;
    this.originIcao = originIcao;
    this.destinationIata = destinationIata;
    this.destinationIcao = destinationIcao;
    this.eta = eta;
    this.label = { x: labelX, y: labelY };
    this.riskLevel = riskLevel;
    this.positionHistory = [];
  }

  private recordPastGeoSample(): void {
    this.positionHistory.push({
      latitude: this.geoPosition.latitude,
      longitude: this.geoPosition.longitude,
      altitude: this.position.altitude,
      recordedAt: new Date().toISOString(),
    });
    while (this.positionHistory.length > POSITION_HISTORY_MAX_POINTS) {
      this.positionHistory.shift();
    }
  }

  public updateAircraftInfo(newAircraft: Aircraft) {
    this.recordPastGeoSample();
    this.position = newAircraft.position;
    this.geoPosition = newAircraft.geoPosition;
    this.vector = newAircraft.vector;
    this.instructedVector = newAircraft.instructedVector;
    this.atcClearance = newAircraft.atcClearance;
    this.eta = newAircraft.eta;
    this.riskLevel = newAircraft.riskLevel;
  }

  public updateAircraftLocationInfo(newAircraft: Aircraft) {
    this.recordPastGeoSample();
    this.position = newAircraft.position;
    this.geoPosition = newAircraft.geoPosition;
    this.vector = newAircraft.vector;
    this.atcClearance = newAircraft.atcClearance;
    this.eta = newAircraft.eta;
    this.riskLevel = newAircraft.riskLevel;
  }
}
