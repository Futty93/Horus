import { Coordinate } from "@/context/centerCoordinateContext";
import { Aircraft } from "../aircraft/aircraftClass";
import { CoordinateManager } from "../coordinateManager/CoordinateManager";
import { DisplayRange } from "@/context/displayRangeContext";

export interface AircraftLocationDto {
  callsign: string;
  position: { latitude: number; longitude: number; altitude: number };
  vector: { heading: number; groundSpeed: number; verticalSpeed: number };
  /** Pilot target from backend; distinct from ATC clearance memo (see spec 20260326-instruction-memo-radar-label). */
  instructedVector: { heading: number; groundSpeed: number; altitude: number };
  /** Controller clearance memo; omitted or null if not recorded. */
  atcClearance?: {
    heading: number;
    groundSpeed: number;
    altitude: number;
  } | null;
  type: string;
  model: string;
  originIata: string;
  originIcao: string;
  destinationIata: string;
  destinationIcao: string;
  /** ISO 8601 instant (e.g. …Z). Data block shows UTC HH:mm. */
  eta: string;
  riskLevel: number;
}

export const fetchAircraftLocation = async (
  controllingAircrafts: Aircraft[],
  centerCoordinate: Coordinate,
  displayRange: DisplayRange,
  pathname: string
) => {
  let updatedControllingAircraft: Aircraft[] = [];
  try {
    const response = await fetch("/api/aircraft/location/all", {
      method: "GET",
      headers: {
        Accept: "application/json",
      },
      cache: "no-store",
    });

    if (response.ok) {
      const data: AircraftLocationDto[] = await response.json();
      const aircraftData = data.map((dto) =>
        mapDtoToAircraft(dto, centerCoordinate, displayRange)
      );
      updatedControllingAircraft = updateControllingAircrafts(
        aircraftData,
        controllingAircrafts,
        pathname
      );
    } else {
      console.error("Request failed with status:", response.status);
    }
  } catch (error) {
    console.error("Error occurred while fetching aircraft location:", error);
  }
  return updatedControllingAircraft;
};

function mapDtoToAircraft(
  dto: AircraftLocationDto,
  centerCoordinate: Coordinate,
  displayRange: DisplayRange
): Aircraft {
  const coordinateOnCanvas = CoordinateManager.calculateCanvasCoordinates(
    { latitude: dto.position.latitude, longitude: dto.position.longitude },
    centerCoordinate,
    displayRange
  );

  const atc =
    dto.atcClearance != null
      ? {
          heading: dto.atcClearance.heading,
          groundSpeed: dto.atcClearance.groundSpeed,
          altitude: dto.atcClearance.altitude,
        }
      : null;

  return new Aircraft(
    dto.callsign,
    {
      x: coordinateOnCanvas.x,
      y: coordinateOnCanvas.y,
      altitude: dto.position.altitude,
    },
    {
      latitude: dto.position.latitude,
      longitude: dto.position.longitude,
    },
    {
      heading: dto.vector.heading,
      groundSpeed: dto.vector.groundSpeed,
      verticalSpeed: dto.vector.verticalSpeed,
    },
    {
      heading: dto.instructedVector.heading,
      groundSpeed: dto.instructedVector.groundSpeed,
      altitude: dto.instructedVector.altitude,
    },
    atc,
    dto.type,
    dto.model,
    dto.originIata ?? "",
    dto.originIcao ?? "",
    dto.destinationIata ?? "",
    dto.destinationIcao ?? "",
    dto.eta ?? "",
    50,
    50,
    dto.riskLevel ?? 0
  );
}

function updateControllingAircrafts(
  apiResponse: Aircraft[],
  controllingAircrafts: Aircraft[],
  pathname: string
): Aircraft[] {
  const newAircraftMap = new Map<string, Aircraft>();

  apiResponse.forEach((aircraft) => {
    newAircraftMap.set(aircraft.callsign, aircraft);
  });

  controllingAircrafts = controllingAircrafts.filter((airplane) => {
    const newAircraft = newAircraftMap.get(airplane.callsign);
    if (newAircraft) {
      if (pathname === "/operator") {
        airplane.updateAircraftInfo(newAircraft);
      } else {
        airplane.updateAircraftLocationInfo(newAircraft);
      }
      newAircraftMap.delete(airplane.callsign);
      return true;
    } else {
      return false;
    }
  });

  newAircraftMap.forEach((newAircraft) => {
    const newAirplane = new Aircraft(
      newAircraft.callsign,
      newAircraft.position,
      newAircraft.geoPosition,
      newAircraft.vector,
      newAircraft.instructedVector,
      newAircraft.atcClearance,
      newAircraft.type,
      newAircraft.model,
      newAircraft.originIata,
      newAircraft.originIcao,
      newAircraft.destinationIata,
      newAircraft.destinationIcao,
      newAircraft.eta,
      50,
      50,
      newAircraft.riskLevel
    );
    controllingAircrafts.push(newAirplane);
  });

  return controllingAircrafts;
}
