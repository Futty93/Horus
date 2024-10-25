import { Waypoint } from "./RouteInterfaces/Waypoint";
import { Route } from "./RouteInterfaces/Route";
import { RoutePoint } from "./RouteInterfaces/RoutePoint";
import { RadioNavigationAid } from "./RouteInterfaces/RadioNavigationAid";
import { CoordinateManager } from "../coordinateManager/CoordinateManager";
import { GLOBAL_SETTINGS } from "../globals/settings";
import { GLOBAL_CONSTANTS } from "../globals/constants";

import { DisplaySettings } from '@/context/routeInfoDisplaySettingContext'; // コンテキストをインポート
import { Coordinate } from "@/context/centerCoordinateContext";
import { DisplayRange } from "@/context/displayRangeContext";

export function renderMap(
  waypoints: Waypoint[],
  radioNavAids: RadioNavigationAid[],
  atsRoutes: Route[],
  rnavRoutes: Route[],
  ctx: CanvasRenderingContext2D,
  displaySettings: DisplaySettings = { // デフォルト値を設定
    waypointName: false,
    waypointPoint: true,
    radioNavigationAidsName: false,
    radioNavigationAidsPoint: true,
    atsLowerRoute: false,
    rnavRoute: true,
  },
  centerCoordinate: Coordinate,
  displayRange: DisplayRange
) {
  // Draw ATS and RNAV routes
  if (displaySettings.atsLowerRoute) { // GLOBAL_SETTINGSではなくdisplaySettingsを使用
    [...atsRoutes].forEach((route) => {
      drawRoute(ctx, route, '#0ff', centerCoordinate, displayRange);
    });
  }
  if (displaySettings.rnavRoute) { // GLOBAL_SETTINGSではなくdisplaySettingsを使用
    [...rnavRoutes].forEach((route) => {
      drawRoute(ctx, route, '#376', centerCoordinate, displayRange);
    });
  }

  // Draw all waypoints and radio navigation aids in one loop
  [...waypoints].forEach((point) => {
    drawPoint(ctx, point, '#376', displaySettings.waypointName, displaySettings.waypointPoint, centerCoordinate, displayRange); // GLOBAL_SETTINGSではなくdisplaySettingsを使用
  });
  [...radioNavAids].forEach((point) => {
    drawPoint(ctx, point, "#376", displaySettings.radioNavigationAidsName, displaySettings.radioNavigationAidsPoint, centerCoordinate, displayRange); // GLOBAL_SETTINGSではなくdisplaySettingsを使用
  });
}

function drawPoint(ctx: CanvasRenderingContext2D, point: Waypoint | RadioNavigationAid, color: string, isDisplayingName: boolean, isDisplayingPoint: boolean, centerCoordinate: Coordinate, displayRange: DisplayRange) {
  const { x, y } = CoordinateManager.calculateCanvasCoordinates(point, centerCoordinate, displayRange); // GLOBAL_SETTINGSではなくcenterCoordinateを使用

  const markerSize = 3;

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
      ctx.globalAlpha = 0.7;  // Set opacity for waypoints
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

function drawRoute(ctx: CanvasRenderingContext2D, route: Route, color: string, centerCoordinate: Coordinate, displayRange: DisplayRange) {
  const points = route.points;

  // Draw lines between consecutive points
  for (let i = 0; i < points.length - 1; i++) {
    drawLineBetweenPoints(ctx, points[i], points[i + 1], color, centerCoordinate, displayRange);
  }
}

function drawLineBetweenPoints(ctx: CanvasRenderingContext2D, point1: RoutePoint, point2: RoutePoint, color: string, centerCoordinate: Coordinate, displayRange: DisplayRange) {
  const point1Coordinate = CoordinateManager.calculateCanvasCoordinates(point1, centerCoordinate, displayRange);
  const point2Coordinate = CoordinateManager.calculateCanvasCoordinates(point2, centerCoordinate, displayRange);

  ctx.beginPath();
  ctx.moveTo(point1Coordinate.x, point1Coordinate.y);
  ctx.lineTo(point2Coordinate.x, point2Coordinate.y);
  ctx.globalAlpha = 0.7;  // Set opacity for waypoints
  ctx.strokeStyle = color;  // Line color
  ctx.stroke();


  // Draw black circle under the marker
  ctx.beginPath();
  ctx.arc(point1Coordinate.x, point1Coordinate.y, 7, 0, 2 * Math.PI);
  ctx.fillStyle = 'black';
  ctx.fill();
  ctx.beginPath();
  ctx.arc(point2Coordinate.x, point2Coordinate.y, 7, 0, 2 * Math.PI);
  ctx.fillStyle = 'black';
  ctx.fill();
}