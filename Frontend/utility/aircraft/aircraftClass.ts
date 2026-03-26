export class Aircraft {
  callsign: string;
  position: { x: number; y: number; altitude: number };
  vector: { heading: number; groundSpeed: number; verticalSpeed: number };
  /**
   * Pilot-applied target from Operator (POST /control). Not the controller’s clearance memo;
   * see spec/20260326-instruction-memo-radar-label/spec.md.
   */
  instructedVector: { heading: number; groundSpeed: number; altitude: number };
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

  constructor(
    callsign: string,
    position: { x: number; y: number; altitude: number },
    vector: { heading: number; groundSpeed: number; verticalSpeed: number },
    instructedVector: {
      heading: number;
      groundSpeed: number;
      altitude: number;
    },
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
    this.vector = vector;
    this.instructedVector = instructedVector;
    this.type = type;
    this.model = model;
    this.originIata = originIata;
    this.originIcao = originIcao;
    this.destinationIata = destinationIata;
    this.destinationIcao = destinationIcao;
    this.eta = eta;
    this.label = { x: labelX, y: labelY }; // Default label position at (50, 50)
    this.riskLevel = riskLevel;
  }

  public updateAircraftInfo(newAircraft: Aircraft) {
    this.position = newAircraft.position;
    this.vector = newAircraft.vector;
    this.instructedVector = newAircraft.instructedVector;
    this.eta = newAircraft.eta;
    this.riskLevel = newAircraft.riskLevel;
  }

  public updateAircraftLocationInfo(newAircraft: Aircraft) {
    this.position = newAircraft.position;
    this.vector = newAircraft.vector;
    this.eta = newAircraft.eta;
    this.riskLevel = newAircraft.riskLevel;
  }
}
