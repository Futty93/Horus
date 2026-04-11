import { Waypoint } from "./RouteInterfaces/Waypoint";
import { Route } from "./RouteInterfaces/Route";
import { RadioNavigationAid } from "./RouteInterfaces/RadioNavigationAid";

interface RawWaypoint {
  name: string;
  latitude: string | number;
  longitude: string | number;
  type: string;
}

interface RawRoutePoint {
  name: string;
  latitude: string | number;
  longitude: string | number;
  type: string;
}

interface RawRoute {
  name: string;
  description: string;
  points: RawRoutePoint[];
}

interface RawRadioNavigationAid {
  name: string;
  id: string;
  type: string;
  latitude: string | number;
  longitude: string | number;
  frequency: string;
}

export type JapanOutline = [number, number][][];

async function loadAtsRoutes(): Promise<{
  waypoints: Waypoint[];
  radioNavigationAids: RadioNavigationAid[];
  atsLowerRoutes: Route[];
  rnavRoutes: Route[];
  japanOutline: JapanOutline;
}> {
  try {
    const response = await fetch("/api/ats/route/all");

    // レスポンスのステータスコードをチェック
    if (!response.ok) {
      console.error(`Failed to load ATS routes. Status: ${response.status}`);
      throw new Error(`API request failed with status ${response.status}`);
    }

    const data = (await response.json()) as {
      waypoints: RawWaypoint[];
      radioNavigationAids: RawRadioNavigationAid[];
      atsLowerRoutes: RawRoute[];
      rnavRoutes: RawRoute[];
      japanOutline?: JapanOutline;
    };

    if (
      !data.waypoints ||
      !data.radioNavigationAids ||
      !data.atsLowerRoutes ||
      !data.rnavRoutes
    ) {
      console.error("Invalid data structure:", data);
      throw new Error("Invalid data structure");
    }

    const japanOutline = data.japanOutline ?? [];

    const toWaypoint = (wp: RawWaypoint): Waypoint => ({
      name: wp.name,
      latitude: parseFloat(String(wp.latitude)),
      longitude: parseFloat(String(wp.longitude)),
      type: wp.type,
    });

    const toRadioNavAid = (aid: RawRadioNavigationAid): RadioNavigationAid => ({
      name: aid.name,
      id: aid.id,
      type: aid.type,
      latitude:
        typeof aid.latitude === "number"
          ? aid.latitude
          : parseFloat(aid.latitude),
      longitude:
        typeof aid.longitude === "number"
          ? aid.longitude
          : parseFloat(aid.longitude),
      frequency: aid.frequency,
    });

    const toRoutePoint = (p: RawRoutePoint) => ({
      name: p.name,
      latitude:
        typeof p.latitude === "number" ? p.latitude : parseFloat(p.latitude),
      longitude:
        typeof p.longitude === "number" ? p.longitude : parseFloat(p.longitude),
      type: p.type,
    });

    const waypoints = data.waypoints.map(toWaypoint);
    const radioNavigationAids = data.radioNavigationAids.map(toRadioNavAid);
    const atsLowerRoutes: Route[] = data.atsLowerRoutes.map((route) => ({
      name: route.name,
      description: route.description,
      points: route.points.map(toRoutePoint),
    }));
    const rnavRoutes: Route[] = data.rnavRoutes.map((route) => ({
      name: route.name,
      description: route.description,
      points: route.points.map(toRoutePoint),
    }));

    return {
      waypoints,
      radioNavigationAids,
      atsLowerRoutes,
      rnavRoutes,
      japanOutline,
    };
  } catch (error) {
    console.error("Failed to load ATS routes:", error);
    throw error;
  }
}

export default loadAtsRoutes;
