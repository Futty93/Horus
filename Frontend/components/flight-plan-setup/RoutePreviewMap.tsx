"use client";

import React, { useEffect, useRef } from "react";
import type { ScenarioAircraft } from "@/types/scenario";
import type { Waypoint } from "@/utility/AtsRouteManager/RouteInterfaces/Waypoint";
import type { RadioNavigationAid } from "@/utility/AtsRouteManager/RouteInterfaces/RadioNavigationAid";
import { CoordinateManager } from "@/utility/coordinateManager/CoordinateManager";
import { GLOBAL_SETTINGS } from "@/utility/globals/settings";
import { GLOBAL_CONSTANTS } from "@/utility/globals/constants";

interface RoutePreviewMapProps {
  selectedAircraft: ScenarioAircraft | null;
  waypoints: Waypoint[];
  radioNavAids: RadioNavigationAid[];
  airportPositions?: Map<string, { latitude: number; longitude: number }>;
}

const PREVIEW_SIZE = 420;
const DEFAULT_CENTER = { latitude: 34.5, longitude: 138.5 };
const DEFAULT_RANGE = 500;
const KM_PER_DEG_LAT = 111;
const PADDING_FACTOR = 1.4;

function buildFixLookup(
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

function computeBounds(points: { latitude: number; longitude: number }[]): {
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

function toCanvas(
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

export function RoutePreviewMap({
  selectedAircraft,
  waypoints,
  radioNavAids,
  airportPositions,
}: RoutePreviewMapProps) {
  const canvasRef = useRef<HTMLCanvasElement>(null);

  useEffect(() => {
    const fixLookup = buildFixLookup(waypoints, radioNavAids, airportPositions);
    const canvas = canvasRef.current;
    if (!canvas) return;

    const ctx = canvas.getContext("2d");
    if (!ctx) return;

    ctx.fillStyle = "#0a0a0f";
    ctx.fillRect(0, 0, PREVIEW_SIZE, PREVIEW_SIZE);

    if (!selectedAircraft) {
      ctx.fillStyle = "#6b7280";
      ctx.font = "14px monospace";
      ctx.textAlign = "center";
      ctx.fillText(
        "Select an aircraft to preview route",
        PREVIEW_SIZE / 2,
        PREVIEW_SIZE / 2
      );
      return;
    }

    const fp = selectedAircraft.flightPlan;
    const routeFixes = [
      fp.departureAirport,
      ...fp.route.map((w) => w.fix),
      fp.arrivalAirport,
    ];

    const coordsWithLabels: { lat: number; lon: number; label: string }[] = [];
    const missing: string[] = [];
    for (const fix of routeFixes) {
      const coord = fixLookup.get(fix.toUpperCase());
      if (!coord) {
        missing.push(fix);
        continue;
      }
      coordsWithLabels.push({
        lat: coord.latitude,
        lon: coord.longitude,
        label: fix,
      });
    }
    const { latitude, longitude } = selectedAircraft.initialPosition;
    const allPointsForBounds = [
      ...coordsWithLabels.map((c) => ({ latitude: c.lat, longitude: c.lon })),
      { latitude, longitude },
    ];
    const { center, range } =
      allPointsForBounds.length > 0
        ? computeBounds(allPointsForBounds)
        : { center: DEFAULT_CENTER, range: DEFAULT_RANGE };

    const points: { x: number; y: number; label: string }[] = [];
    for (const c of coordsWithLabels) {
      const { x, y } = toCanvas(
        c.lat,
        c.lon,
        center,
        range,
        PREVIEW_SIZE,
        PREVIEW_SIZE
      );
      points.push({ x, y, label: c.label });
    }

    if (points.length < 2) {
      ctx.fillStyle = "#6b7280";
      ctx.font = "12px monospace";
      ctx.textAlign = "center";
      const centerY = PREVIEW_SIZE / 2;
      ctx.fillText(
        `${fp.callsign}: Waypoints not found`,
        PREVIEW_SIZE / 2,
        centerY - 8
      );
      if (missing.length > 0) {
        ctx.fillText(
          `Missing: ${missing.join(", ")}`,
          PREVIEW_SIZE / 2,
          centerY + 8
        );
      }
      return;
    }

    ctx.strokeStyle = "#22c55e";
    ctx.lineWidth = 2;
    ctx.globalAlpha = 0.9;
    ctx.beginPath();
    ctx.moveTo(points[0].x, points[0].y);
    for (let i = 1; i < points.length; i++) {
      ctx.lineTo(points[i].x, points[i].y);
    }
    ctx.stroke();
    ctx.globalAlpha = 1;

    ctx.font = GLOBAL_CONSTANTS.FONT_STYLE_IN_CANVAS;
    points.forEach((p, i) => {
      ctx.fillStyle = "#0a0a0f";
      ctx.beginPath();
      ctx.arc(p.x, p.y, 6, 0, 2 * Math.PI);
      ctx.fill();
      ctx.strokeStyle =
        i === 0 ? "#3b82f6" : i === points.length - 1 ? "#ef4444" : "#22c55e";
      ctx.lineWidth = 2;
      ctx.stroke();
      ctx.fillStyle = "#e5e7eb";
      ctx.textAlign = "left";
      ctx.fillText(p.label, p.x + 8, p.y + 4);
    });

    const pos = toCanvas(
      latitude,
      longitude,
      center,
      range,
      PREVIEW_SIZE,
      PREVIEW_SIZE
    );
    ctx.fillStyle = "#f59e0b";
    ctx.beginPath();
    ctx.arc(pos.x, pos.y, 5, 0, 2 * Math.PI);
    ctx.fill();
    ctx.fillStyle = "#e5e7eb";
    ctx.textAlign = "left";
    ctx.fillText("Now", pos.x + 8, pos.y + 4);
  }, [selectedAircraft, waypoints, radioNavAids, airportPositions]);

  return (
    <div className="border border-atc-border rounded-lg overflow-hidden bg-atc-bg">
      <div className="px-3 py-2 border-b border-atc-border bg-atc-surface-elevated">
        <h3 className="font-mono text-sm font-bold text-atc-text">
          Route Preview
          {selectedAircraft && (
            <span className="ml-2 font-normal text-atc-text-muted">
              {selectedAircraft.flightPlan.callsign}
            </span>
          )}
        </h3>
      </div>
      <canvas
        ref={canvasRef}
        width={PREVIEW_SIZE}
        height={PREVIEW_SIZE}
        className="block w-full"
      />
      {selectedAircraft && (
        <div className="px-3 py-2 text-xs text-atc-text-muted border-t border-atc-border">
          <span className="text-atc-accent">●</span> Origin ·{" "}
          <span className="text-atc-danger">●</span> Dest ·{" "}
          <span className="text-amber-500">▲</span> Current pos
        </div>
      )}
    </div>
  );
}
