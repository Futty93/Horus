import type { Route } from "@/utility/AtsRouteManager/RouteInterfaces/Route";
import { toCanvas } from "@/utility/flightPlanSetup/routePreviewGeometry";

export function drawPublishedRoutes(
  ctx: CanvasRenderingContext2D,
  routes: Route[],
  color: string,
  alpha: number,
  center: { latitude: number; longitude: number },
  range: number,
  dispW: number,
  dispH: number
): void {
  if (routes.length === 0) return;
  ctx.save();
  ctx.strokeStyle = color;
  ctx.lineWidth = 1;
  ctx.globalAlpha = alpha;
  for (const route of routes) {
    const pts = route.points;
    if (pts.length < 2) continue;
    ctx.beginPath();
    const p0 = toCanvas(
      pts[0].latitude,
      pts[0].longitude,
      center,
      range,
      dispW,
      dispH
    );
    ctx.moveTo(p0.x, p0.y);
    for (let i = 1; i < pts.length; i++) {
      const pi = toCanvas(
        pts[i].latitude,
        pts[i].longitude,
        center,
        range,
        dispW,
        dispH
      );
      ctx.lineTo(pi.x, pi.y);
    }
    ctx.stroke();
  }
  ctx.restore();
}
