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
    drawPoint(ctx, point, center, range, coordinateManager);
  });
  [...radioNavAids].forEach((point) => {
    drawPoint(ctx, point, center, range, coordinateManager);
  });

  // Draw ATS and RNAV routes
  [...atsRoutes].forEach((route) => {
    drawRoute(ctx, route, center, range, coordinateManager);
  });
  [...rnavRoutes].forEach((route) => {
    drawRoute(ctx, route, center, range, coordinateManager);
  });
}

function drawPoint(ctx: CanvasRenderingContext2D, point: Waypoint | RadioNavigationAid, center: { latitude: number, longitude: number }, range: number, coordinateManager: CoordinateManager) {
  const { x, y } = coordinateManager.calculateCanvasCoordinates(center.latitude, center.longitude, range, point.latitude, point.longitude);

  ctx.beginPath();
  if (point['type'] !== 'waypoint') {  // Checking if it's a RadioNavigationAid
    ctx.arc(x, y, 5, 0, 2 * Math.PI);
    ctx.strokeStyle = '#f00';  // Color for radio navigation aids
  } else {
    ctx.moveTo(x - 5, y);
    ctx.lineTo(x + 5, y);
    ctx.moveTo(x, y - 5);
    ctx.lineTo(x, y + 5);
    ctx.strokeStyle = '#666';  // Color for waypoints
  }
  ctx.stroke();

  // Display name to the top-right of the point
  ctx.font = '10px Arial';
  ctx.fillStyle = '#666';  // Text color
  ctx.fillText(point.name, x + 7, y - 7);
}

function drawRoute(ctx: CanvasRenderingContext2D, route: Route, center: { latitude: number, longitude: number }, range: number, coordinateManager: CoordinateManager) {
  const points = route.points;

  // Draw lines between consecutive points
  for (let i = 0; i < points.length - 1; i++) {
    drawLineBetweenPoints(ctx, points[i], points[i + 1], center, range, coordinateManager);
  }
}

function drawLineBetweenPoints(ctx: CanvasRenderingContext2D, point1: RoutePoint, point2: RoutePoint, center: { latitude: number, longitude: number }, range: number, coordinateManager: CoordinateManager) {
  const point1Coordinate = coordinateManager.calculateCanvasCoordinates(center.latitude, center.longitude, range, point1.latitude, point1.longitude);
  const point2Coordinate = coordinateManager.calculateCanvasCoordinates(center.latitude, center.longitude, range, point2.latitude, point2.longitude);

  ctx.beginPath();
  ctx.moveTo(point1Coordinate.x, point1Coordinate.y);
  ctx.lineTo(point2Coordinate.x, point2Coordinate.y);
  ctx.strokeStyle = '#666';  // Line color
  ctx.stroke();
}