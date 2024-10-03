import { Waypoint } from "./RouteInterfaces/Waypoint";
import { Route } from "./RouteInterfaces/Route";
import { RoutePoint } from "./RouteInterfaces/RoutePoint";
import { RadioNavigationAid } from "./RouteInterfaces/RadioNavigationAid";
import { CoordinateManager } from "../CoordinateManager";

export function renderMap(
  waypoints: Waypoint[],
  radioNavAids: RadioNavigationAid[],
  atsRoutes: Route[],
  rnavRoutes: Route[],
  center: { latitude: number, longitude: number },
  range: number,
  ctx: CanvasRenderingContext2D,
  canvasWidth: number,
  canvasHeight: number
) {
  const coordinateManager = new CoordinateManager(canvasWidth, canvasHeight);

  // Draw all waypoints and radio navigation aids in one loop
  [...waypoints].forEach((point) => {
    drawPoint(ctx, point, center, range, coordinateManager, '#00f');
  });
  [...radioNavAids].forEach((point) => {
    drawPoint(ctx, point, center, range, coordinateManager, "#f00");
  });

  // Draw ATS and RNAV routes
  [...atsRoutes].forEach((route) => {
    drawRoute(ctx, route, center, range, coordinateManager, '#0ff');
  });
  [...rnavRoutes].forEach((route) => {
    drawRoute(ctx, route, center, range, coordinateManager, '#ff0');
  });
}

function drawPoint(ctx: CanvasRenderingContext2D, point: Waypoint | RadioNavigationAid, center: { latitude: number, longitude: number }, range: number, coordinateManager: CoordinateManager, color) {
  const { x, y } = coordinateManager.calculateCanvasCoordinates(center.latitude, center.longitude, range, point.latitude, point.longitude);

  const markerSize = 5;

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

  // Display name to the top-right of the point
  ctx.font = '10px Arial';
  ctx.fillStyle = color;  // Text color
  ctx.fillText(point.name, x + 7, y - 7);
}

function drawRoute(ctx: CanvasRenderingContext2D, route: Route, center: { latitude: number, longitude: number }, range: number, coordinateManager: CoordinateManager, color: string) {
  const points = route.points;

  // Draw lines between consecutive points
  for (let i = 0; i < points.length - 1; i++) {
    drawLineBetweenPoints(ctx, points[i], points[i + 1], center, range, coordinateManager, color);
  }
}

function drawLineBetweenPoints(ctx: CanvasRenderingContext2D, point1: RoutePoint, point2: RoutePoint, center: { latitude: number, longitude: number }, range: number, coordinateManager: CoordinateManager, color: string) {
  const point1Coordinate = coordinateManager.calculateCanvasCoordinates(center.latitude, center.longitude, range, point1.latitude, point1.longitude);
  const point2Coordinate = coordinateManager.calculateCanvasCoordinates(center.latitude, center.longitude, range, point2.latitude, point2.longitude);

  ctx.beginPath();
  ctx.moveTo(point1Coordinate.x, point1Coordinate.y);
  ctx.lineTo(point2Coordinate.x, point2Coordinate.y);
  ctx.strokeStyle = color;  // Line color
  ctx.stroke();
}