export class Aircraft {
  callsign: string;
  position: { x: number; y: number; altitude: number };
  vector: { heading: number; groundSpeed: number; verticalSpeed: number };
  instructedVector: { heading: number; groundSpeed: number; altitude: number };
  type: string;
  originIata: string;
  originIcao: string;
  destinationIata: string;
  destinationIcao: string;
  eta: string; // You may want to change this to a Date object if needed
  label: { x: number; y: number };

  constructor(
    callsign: string,
    position: { x: number; y: number; altitude: number },
    vector: { heading: number; groundSpeed: number; verticalSpeed: number },
    instructedVector: { heading: number; groundSpeed: number; altitude: number },
    type: string,
    originIata: string,
    originIcao: string,
    destinationIata: string,
    destinationIcao: string,
    eta: string,
    labelX: number = 50,
    labelY: number = 50
  ) {
    this.callsign = callsign;
    this.position = position;
    this.vector = vector;
    this.instructedVector = instructedVector;
    this.type = type;
    this.originIata = originIata;
    this.originIcao = originIcao;
    this.destinationIata = destinationIata;
    this.destinationIcao = destinationIcao;
    this.eta = eta;
    this.label = { x: labelX, y: labelY }; // Default label position at (50, 50)
  }

  public updateAircraftInfo(newAircraft: Aircraft) {
    this.position = newAircraft.position;
    this.vector = newAircraft.vector;
    this.instructedVector = newAircraft.instructedVector;
    this.eta = newAircraft.eta;
  }

  public updateAircraftLocationInfo(newAircraft: Aircraft) {
    this.position = newAircraft.position;
    this.vector = newAircraft.vector;
    this.eta = newAircraft.eta;
  }
}