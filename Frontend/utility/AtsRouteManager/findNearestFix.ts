import type { Waypoint } from "./RouteInterfaces/Waypoint";
import type { RadioNavigationAid } from "./RouteInterfaces/RadioNavigationAid";

export interface GeoCoordinate {
  latitude: number;
  longitude: number;
}

export function findNearestFixName(
  clickedGeo: GeoCoordinate,
  waypoints: readonly Waypoint[],
  radioNavAids: readonly RadioNavigationAid[]
): string {
  const allFixes = [...waypoints, ...radioNavAids];

  const nearest = allFixes.reduce(
    (acc, fix) => {
      const distance = Math.hypot(
        fix.latitude - clickedGeo.latitude,
        fix.longitude - clickedGeo.longitude
      );
      const isCloserOrPreferred =
        distance < acc.minDistance ||
        (distance === acc.minDistance &&
          "type" in fix &&
          fix.type === "radioNavigationAid");

      return isCloserOrPreferred
        ? { name: fix.name, minDistance: distance }
        : acc;
    },
    { name: "", minDistance: Number.MAX_VALUE }
  );

  return nearest.name;
}
