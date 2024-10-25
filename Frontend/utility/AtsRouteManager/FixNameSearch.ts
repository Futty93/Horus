import { Waypoint } from "./RouteInterfaces/Waypoint";
import { RadioNavigationAid } from "./RouteInterfaces/RadioNavigationAid";
import { Coordinate } from "@/context/centerCoordinateContext";
import { DisplayRange } from "@/context/displayRangeContext";
import CoordinateManager from "../coordinateManager/CoordinateManager";

// export function searchFixName(
//   waypoints: Waypoint[],
//   radioNavAids: RadioNavigationAid[],
//   canvasCoordinate : {x: number, y: number},
//   centerCoordinate: Coordinate,
//   displayRange: DisplayRange
// ): string {
//   const clickedGeoCoordinate = CoordinateManager.calculateGeoCoordinates(canvasCoordinate.x, canvasCoordinate.y, centerCoordinate, displayRange);
//   let minDistance = Number.MAX_VALUE;
//   let nearestFixName = '';

//   for (const waypoint of waypoints) {
//     const distance = (waypoint.latitude - clickedGeoCoordinate.latitude)^2 + (waypoint.longitude - clickedGeoCoordinate.longitude)^2;
//     if (distance < minDistance) {
//       minDistance = distance;
//       nearestFixName = waypoint.name;
//     }
//   }
//   for (const radioNavAid of radioNavAids) {
//     const distance = (radioNavAid.latitude - clickedGeoCoordinate.latitude)^2 + (radioNavAid.longitude - clickedGeoCoordinate.longitude)^2;
//     if (distance < minDistance) {
//       minDistance = distance;
//       nearestFixName = radioNavAid.name;
//     }
//   }

//   return nearestFixName;
// }

export function searchFixName(
  waypoints: Waypoint[],
  radioNavAids: RadioNavigationAid[],
  canvasCoordinate: { x: number, y: number },
  centerCoordinate: Coordinate,
  displayRange: DisplayRange
): string {
  const clickedGeoCoordinate = CoordinateManager.calculateGeoCoordinates(
    canvasCoordinate.x,
    canvasCoordinate.y,
    centerCoordinate,
    displayRange
  );

  // Combine waypoints and radioNavAids into one array
  const allFixes = [...waypoints, ...radioNavAids];

  // Find the nearest fix by reducing the array
  const nearestFix = allFixes.reduce(
    (nearest, fix) => {
      const distance = Math.hypot(
        fix.latitude - clickedGeoCoordinate.latitude,
        fix.longitude - clickedGeoCoordinate.longitude
      );
  
      // Prioritize radioNavAid if distances are equal
      const isCloserOrPreferred = 
        distance < nearest.minDistance || 
        (distance === nearest.minDistance && 'type' in fix && fix.type === 'radioNavigationAid');
  
      return isCloserOrPreferred
        ? { name: fix.name, minDistance: distance }
        : nearest;
    },
    { name: "", minDistance: Number.MAX_VALUE }
  );

  return nearestFix.name;
}