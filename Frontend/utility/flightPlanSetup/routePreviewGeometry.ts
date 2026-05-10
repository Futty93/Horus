import type { Waypoint } from "@/utility/AtsRouteManager/RouteInterfaces/Waypoint";
import type { RadioNavigationAid } from "@/utility/AtsRouteManager/RouteInterfaces/RadioNavigationAid";
import { CoordinateManager } from "@/utility/coordinateManager/CoordinateManager";
import { GLOBAL_SETTINGS } from "@/utility/globals/settings";
import { GLOBAL_CONSTANTS } from "@/utility/globals/constants";

export const DEFAULT_CENTER = { latitude: 34.5, longitude: 138.5 };
export const DEFAULT_RANGE = 500;
const KM_PER_DEG_LAT = 111;
const PADDING_FACTOR = 1.4;

export const MARKER_HIT_PX = 18;
export const SEGMENT_HIT_PX = 12;
export const NEAREST_FIX_MAX_KM = 150;
const LABEL_CHAR_PX = 7.5;
const LABEL_PAD_X = 8;
const LABEL_OFF_Y = 10;

export type RoutePreviewPickTarget = {
  x: number;
  y: number;
  label: string;
};

export function buildFixLookup(
  waypoints: Waypoint[],
  radioNavAids: RadioNavigationAid[],
  airportPositions?: Map<string, { latitude: number; longitude: number }>
): Map<string, { latitude: number; longitude: number }> {
  const m = new Map<string, { latitude: number; longitude: number }>();
  for (const w of waypoints) {
    m.set(w.name.toUpperCase(), {
      latitude: w.latitude,
      longitude: w.longitude,
    });
  }
  for (const r of radioNavAids) {
    m.set(r.name.toUpperCase(), {
      latitude: r.latitude,
      longitude: r.longitude,
    });
  }
  if (airportPositions) {
    for (const [icao, pos] of Array.from(airportPositions)) {
      m.set(icao.toUpperCase(), pos);
    }
  }
  return m;
}

export function computeBounds(
  points: {
    latitude: number;
    longitude: number;
  }[]
): {
  center: { latitude: number; longitude: number };
  range: number;
} {
  if (points.length === 0) {
    return { center: DEFAULT_CENTER, range: DEFAULT_RANGE };
  }
  let minLat = points[0].latitude;
  let maxLat = points[0].latitude;
  let minLon = points[0].longitude;
  let maxLon = points[0].longitude;
  for (const p of points) {
    minLat = Math.min(minLat, p.latitude);
    maxLat = Math.max(maxLat, p.latitude);
    minLon = Math.min(minLon, p.longitude);
    maxLon = Math.max(maxLon, p.longitude);
  }
  const centerLat = (minLat + maxLat) / 2;
  const centerLon = (minLon + maxLon) / 2;
  const latSpanKm = (maxLat - minLat) * KM_PER_DEG_LAT;
  const lonSpanKm =
    (maxLon - minLon) * KM_PER_DEG_LAT * Math.cos((centerLat * Math.PI) / 180);
  const rangeKm = Math.max(latSpanKm, lonSpanKm, 50) * PADDING_FACTOR;
  return {
    center: { latitude: centerLat, longitude: centerLon },
    range: Math.round(rangeKm),
  };
}

export function toCanvas(
  lat: number,
  lon: number,
  center: { latitude: number; longitude: number },
  range: number,
  width: number,
  height: number
): { x: number; y: number } {
  const result = CoordinateManager.calculateCanvasCoordinates(
    { latitude: lat, longitude: lon },
    center,
    { range }
  );
  const scaleX = width / GLOBAL_SETTINGS.canvasWidth;
  const scaleY = height / GLOBAL_SETTINGS.canvasHeight;
  return {
    x: result.x * scaleX,
    y: result.y * scaleY,
  };
}

export function pickHitTarget(
  px: number,
  py: number,
  targets: RoutePreviewPickTarget[]
): string | null {
  for (let i = targets.length - 1; i >= 0; i--) {
    const p = targets[i];
    const dx = px - p.x;
    const dy = py - p.y;
    if (dx * dx + dy * dy <= MARKER_HIT_PX * MARKER_HIT_PX) {
      return p.label;
    }
    const lx = p.x + LABEL_PAD_X;
    const ly = p.y - LABEL_OFF_Y;
    const lw = Math.max(36, p.label.length * LABEL_CHAR_PX);
    const lh = 14;
    if (px >= lx - 2 && px <= lx + lw && py >= ly - 2 && py <= ly + lh + 2) {
      return p.label;
    }
  }
  return null;
}

export function distanceToSegment(
  px: number,
  py: number,
  ax: number,
  ay: number,
  bx: number,
  by: number
): number {
  const dx = bx - ax;
  const dy = by - ay;
  const len2 = dx * dx + dy * dy;
  if (len2 < 1e-6) return Math.hypot(px - ax, py - ay);
  let t = ((px - ax) * dx + (py - ay) * dy) / len2;
  t = Math.max(0, Math.min(1, t));
  const qx = ax + t * dx;
  const qy = ay + t * dy;
  return Math.hypot(px - qx, py - qy);
}

export function previewCanvasToGeo(
  canvasX: number,
  canvasY: number,
  center: { latitude: number; longitude: number },
  range: number,
  dispW: number,
  dispH: number
): { latitude: number; longitude: number } {
  const logicalX = canvasX * (GLOBAL_SETTINGS.canvasWidth / dispW);
  const logicalY = canvasY * (GLOBAL_SETTINGS.canvasHeight / dispH);
  return CoordinateManager.calculateGeoCoordinates(logicalX, logicalY, center, {
    range,
  });
}

export function geoDistanceKm(
  lat1: number,
  lon1: number,
  lat2: number,
  lon2: number
): number {
  const R = GLOBAL_CONSTANTS.EARTH_RADIUS_KM;
  const toR = (deg: number) => (deg * Math.PI) / 180;
  const dLat = toR(lat2 - lat1);
  const dLon = toR(lon2 - lon1);
  const a =
    Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.cos(toR(lat1)) *
      Math.cos(toR(lat2)) *
      Math.sin(dLon / 2) *
      Math.sin(dLon / 2);
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  return R * c;
}

export function nearestCatalogFix(
  lat: number,
  lon: number,
  lookup: Map<string, { latitude: number; longitude: number }>,
  maxKm: number
): { name: string; distKm: number } | null {
  let best: { name: string; distKm: number } | null = null;
  for (const [name, pos] of Array.from(lookup.entries())) {
    const d = geoDistanceKm(lat, lon, pos.latitude, pos.longitude);
    if (d <= maxKm && (!best || d < best.distKm)) {
      best = { name, distKm: d };
    }
  }
  return best;
}
