import { Waypoint } from "./RouteInterfaces/Waypoint";
import { Route } from "./RouteInterfaces/Route";
import { RoutePoint } from "./RouteInterfaces/RoutePoint";
import { RadioNavigationAid } from "./RouteInterfaces/RadioNavigationAid";
import { CoordinateManager } from "../coordinateManager/CoordinateManager";
import { GLOBAL_SETTINGS } from "../globals/settings";
import { GLOBAL_CONSTANTS } from "../globals/constants";

export function renderMap(
  waypoints: Waypoint[],
  radioNavAids: RadioNavigationAid[],
  atsRoutes: Route[],
  rnavRoutes: Route[],
  ctx: CanvasRenderingContext2D,
) {
  // Draw ATS and RNAV routes
  if (GLOBAL_SETTINGS.isDisplaying.atsLowerRoute) {
    [...atsRoutes].forEach((route) => {
      drawRoute(ctx, route, '#0ff');
    });
  }
  if (GLOBAL_SETTINGS.isDisplaying.rnavRoute) {
    [...rnavRoutes].forEach((route) => {
      drawRoute(ctx, route, '#ff0');
    });
  }


  // Draw all waypoints and radio navigation aids in one loop
  [...waypoints].forEach((point) => {
    drawPoint(ctx, point, '#0f0', GLOBAL_SETTINGS.isDisplaying.waypointName, GLOBAL_SETTINGS.isDisplaying.waypointPoint);
  });
  [...radioNavAids].forEach((point) => {
    drawPoint(ctx, point, "#f8b", GLOBAL_SETTINGS.isDisplaying.radioNavigationAidsName, GLOBAL_SETTINGS.isDisplaying.radioNavigationAidsPoint);
  });
}

function drawPoint(ctx: CanvasRenderingContext2D, point: Waypoint | RadioNavigationAid, color, isDisplayingName: boolean, isDisplayingPoint: boolean) {
  const { x, y } = CoordinateManager.calculateCanvasCoordinates(point.latitude, point.longitude);

  const markerSize = 5;

  if (isDisplayingPoint) {
    ctx.beginPath();
    if (point['type'] !== 'waypoint') {  // Checking if it's a RadioNavigationAid
      ctx.arc(x, y, markerSize, 0, 2 * Math.PI);
      ctx.strokeStyle = color;  // Color for radio navigation aids
    } else {
      ctx.moveTo(x - markerSize, y);
      ctx.lineTo(x + markerSize, y);
      ctx.moveTo(x, y - markerSize);
      ctx.lineTo(x, y + markerSize);
      ctx.strokeStyle = color;  // Color for waypoints
    }
    ctx.stroke();
  }

  if (isDisplayingName) {
    ctx.font = GLOBAL_CONSTANTS.FONT_STYLE_IN_CANVAS;
    ctx.fillStyle = color;  // Text color
    ctx.fillText(point.name, x + 7, y - 7);
  }
}

function drawRoute(ctx: CanvasRenderingContext2D, route: Route, color: string) {
  const points = route.points;

  // Draw lines between consecutive points
  for (let i = 0; i < points.length - 1; i++) {
    drawLineBetweenPoints(ctx, points[i], points[i + 1], color);
  }
}

function drawLineBetweenPoints(ctx: CanvasRenderingContext2D, point1: RoutePoint, point2: RoutePoint, color: string) {
  const point1Coordinate = CoordinateManager.calculateCanvasCoordinates(point1.latitude, point1.longitude);
  const point2Coordinate = CoordinateManager.calculateCanvasCoordinates(point2.latitude, point2.longitude);

  ctx.beginPath();
  ctx.moveTo(point1Coordinate.x, point1Coordinate.y);
  ctx.lineTo(point2Coordinate.x, point2Coordinate.y);
  ctx.strokeStyle = color;  // Line color
  ctx.stroke();
}