import { Waypoint} from "./RouteInterfaces/Waypoint";
import { Route } from "./RouteInterfaces/Route";
import { RoutePoint } from "./RouteInterfaces/RoutePoint";
import { RadioNavigationAid } from "./RouteInterfaces/RadioNavigationAid";

// データを取得し、パースする関数
async function loadAtsRoutes(): Promise<{
  waypoints: Waypoint[];
  radioNavigationAids: RadioNavigationAid[];
  atsLowerRoutes: Route[];
  rnavRoutes: Route[];
}> {
  try {
    const response = await fetch('http://localhost:8080/ats/route/all');  // エンドポイントにリクエストを送信
    const data = await response.json();

    // データをインターフェースにマッピング
    const waypoints: Waypoint[] = data.waypoints.map((wp: any) => ({
      name: wp.name,
      latitude: parseFloat(wp.latitude),
      longitude: parseFloat(wp.longitude),
      type: wp.type,
    }));

    const radioNavigationAids: RadioNavigationAid[] = data.radioNavigationAids.map((aid: any) => ({
      name: aid.name,
      id: aid.id,
      type: aid.type,
      latitude: aid.latitude,
      longitude: aid.longitude,
      frequency: aid.frequency,
    }));

    const atsLowerRoutes: Route[] = data.atsLowerRoutes.map((route: any) => ({
      name: route.name,
      description: route.description,
      points: route.points.map((point: any) => ({
        name: point.name,
        latitude: point.latitude,
        longitude: point.longitude,
        type: point.type,
      })),
    }));

    const rnavRoutes: Route[] = data.rnavRoutes.map((route: any) => ({
      name: route.name,
      description: route.description,
      points: route.points.map((point: any) => ({
        name: point.name,
        latitude: point.latitude,
        longitude: point.longitude,
        type: point.type,
      })),
    }));

    return { waypoints, radioNavigationAids, atsLowerRoutes, rnavRoutes };
  } catch (error) {
    console.error('Failed to load ATS routes:', error);
    throw error;
  }
}

export default loadAtsRoutes;