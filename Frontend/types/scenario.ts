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
