import type { Waypoint } from "./RouteInterfaces/Waypoint";
import type { RadioNavigationAid } from "./RouteInterfaces/RadioNavigationAid";
import type { Coordinate } from "@/context/centerCoordinateContext";
import type { DisplayRange } from "@/context/displayRangeContext";
import CoordinateManager from "../coordinateManager/CoordinateManager";
import { findNearestFixName } from "./findNearestFix";

export function searchFixName(
  waypoints: Waypoint[],
  radioNavAids: RadioNavigationAid[],
  canvasCoordinate: { x: number; y: number },
  centerCoordinate: Coordinate,
  displayRange: DisplayRange
): string {
  const clickedGeo = CoordinateManager.calculateGeoCoordinates(
    canvasCoordinate.x,
    canvasCoordinate.y,
    centerCoordinate,
    displayRange
  );
  return findNearestFixName(clickedGeo, waypoints, radioNavAids);
}
