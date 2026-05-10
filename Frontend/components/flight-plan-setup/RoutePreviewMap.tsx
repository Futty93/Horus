"use client";

import React, { useCallback, useEffect, useRef, useState } from "react";
import type { ScenarioAircraft } from "@/types/scenario";
import type { Waypoint } from "@/utility/AtsRouteManager/RouteInterfaces/Waypoint";
import type { RadioNavigationAid } from "@/utility/AtsRouteManager/RouteInterfaces/RadioNavigationAid";
import type { Route } from "@/utility/AtsRouteManager/RouteInterfaces/Route";
import { GLOBAL_CONSTANTS } from "@/utility/globals/constants";
import type { JapanOutline } from "@/utility/AtsRouteManager/atsRoutesLoader";
import { drawPublishedRoutes } from "@/utility/flightPlanSetup/routePreviewCanvasDraw";
import {
  buildFixLookup,
  computeBounds,
  DEFAULT_CENTER,
  DEFAULT_RANGE,
  distanceToSegment,
  nearestCatalogFix,
  NEAREST_FIX_MAX_KM,
  pickHitTarget,
  previewCanvasToGeo,
  SEGMENT_HIT_PX,
  toCanvas,
  type RoutePreviewPickTarget,
} from "@/utility/flightPlanSetup/routePreviewGeometry";
import type { RoutePreviewPickPayload } from "./routePreviewTypes";
import { RoutePreviewMapChrome } from "./RoutePreviewMapChrome";
import { useObservedSize } from "./useObservedSize";

const RNAV_AIRWAY_COLOR = "#376";
const ATS_LOWER_AIRWAY_COLOR = "#0ff";
const PUBLISHED_ROUTE_ALPHA = 0.28;

interface RoutePreviewMapProps {
  selectedAircraft: ScenarioAircraft | null;
  waypoints: Waypoint[];
  radioNavAids: RadioNavigationAid[];
  airportPositions?: Map<string, { latitude: number; longitude: number }>;
  japanOutline?: JapanOutline;
  rnavRoutes?: Route[];
  atsLowerRoutes?: Route[];
  className?: string;
  onPickRoute?: (payload: RoutePreviewPickPayload) => void;
  onPickHint?: (message: string) => void;
  onInitialPositionGeoChange?: (latitude: number, longitude: number) => void;
}

const HEADING_ARROW_LENGTH = 18;
const NOW_DRAG_HIT_PX = 14;

type SegmentPick = {
  ax: number;
  ay: number;
  bx: number;
  by: number;
  insertIndex: number;
};

export function RoutePreviewMap({
  selectedAircraft,
  waypoints,
  radioNavAids,
  airportPositions,
  japanOutline = [],
  rnavRoutes = [],
  atsLowerRoutes = [],
  className = "",
  onPickRoute,
  onPickHint,
  onInitialPositionGeoChange,
}: RoutePreviewMapProps) {
  const containerRef = useRef<HTMLDivElement>(null);
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const nowPosRef = useRef<{ x: number; y: number } | null>(null);
  const draggingNowRef = useRef(false);
  const dragMovedRef = useRef(false);
  const suppressRouteClickRef = useRef(false);
  const nowPointerActiveRef = useRef(false);
  const onGeoChangeRef = useRef(onInitialPositionGeoChange);
  onGeoChangeRef.current = onInitialPositionGeoChange;
  const pickTargetsRef = useRef<RoutePreviewPickTarget[]>([]);
  const segmentPickRef = useRef<SegmentPick[]>([]);
  const pickContextRef = useRef<{
    center: { latitude: number; longitude: number };
    range: number;
    dispW: number;
    dispH: number;
  } | null>(null);
  const fixLookupRef = useRef<
    Map<string, { latitude: number; longitude: number }>
  >(new Map());

  const [showRnavAirways, setShowRnavAirways] = useState(false);
  const [showAtsLower, setShowAtsLower] = useState(false);
  const [draggingNowMarker, setDraggingNowMarker] = useState(false);

  const { w: dw, h: dh } = useObservedSize(containerRef);

  useEffect(() => {
    nowPosRef.current = null;
    pickTargetsRef.current = [];
    const fixLookup = buildFixLookup(waypoints, radioNavAids, airportPositions);
    fixLookupRef.current = fixLookup;
    const canvas = canvasRef.current;
    if (!canvas) return;

    const ctx = canvas.getContext("2d");
    if (!ctx) return;

    const dpr =
      typeof window !== "undefined"
        ? Math.min(window.devicePixelRatio || 1, 2)
        : 1;
    const w = dw;
    const h = dh;
    if (w < 2 || h < 2) return;
    canvas.style.width = `${w}px`;
    canvas.style.height = `${h}px`;
    canvas.width = Math.max(1, Math.floor(w * dpr));
    canvas.height = Math.max(1, Math.floor(h * dpr));
    ctx.setTransform(dpr, 0, 0, dpr, 0, 0);

    ctx.fillStyle = "#0a0a0f";
    ctx.fillRect(0, 0, w, h);

    const drawJapanOutline = (
      c: { latitude: number; longitude: number },
      r: number
    ) => {
      ctx.strokeStyle = "#30363d";
      ctx.lineWidth = 1;
      ctx.globalAlpha = 0.6;
      for (const ring of japanOutline) {
        const pts = ring.map(([la, lo]) => toCanvas(la, lo, c, r, w, h));
        ctx.beginPath();
        ctx.moveTo(pts[0].x, pts[0].y);
        for (let i = 1; i < pts.length; i++) ctx.lineTo(pts[i].x, pts[i].y);
        ctx.closePath();
        ctx.stroke();
      }
      ctx.globalAlpha = 1;
    };

    if (!selectedAircraft) {
      pickContextRef.current = null;
      segmentPickRef.current = [];
      drawJapanOutline(DEFAULT_CENTER, DEFAULT_RANGE);
      ctx.fillStyle = "#6b7280";
      ctx.font = "14px monospace";
      ctx.textAlign = "center";
      ctx.fillText("Select an aircraft to preview route", w / 2, h / 2 - 8);
      ctx.font = "11px monospace";
      ctx.fillText(
        "(table row or callsign in an expanded O/D group)",
        w / 2,
        h / 2 + 10
      );
      return;
    }

    const fp = selectedAircraft.flightPlan;
    const routeFixes = [
      fp.departureAirport,
      ...fp.route.map((x) => x.fix),
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

    pickContextRef.current = { center, range, dispW: w, dispH: h };

    const points: { x: number; y: number; label: string }[] = [];
    for (const c of coordsWithLabels) {
      const xy = toCanvas(c.lat, c.lon, center, range, w, h);
      points.push({ x: xy.x, y: xy.y, label: c.label });
    }

    drawJapanOutline(center, range);

    if (showAtsLower) {
      drawPublishedRoutes(
        ctx,
        atsLowerRoutes,
        ATS_LOWER_AIRWAY_COLOR,
        PUBLISHED_ROUTE_ALPHA,
        center,
        range,
        w,
        h
      );
    }
    if (showRnavAirways) {
      drawPublishedRoutes(
        ctx,
        rnavRoutes,
        RNAV_AIRWAY_COLOR,
        PUBLISHED_ROUTE_ALPHA,
        center,
        range,
        w,
        h
      );
    }

    if (points.length < 2) {
      pickTargetsRef.current = points.map((p) => ({
        x: p.x,
        y: p.y,
        label: p.label,
      }));
      ctx.fillStyle = "#6b7280";
      ctx.font = "12px monospace";
      ctx.textAlign = "center";
      const centerY = h / 2;
      ctx.fillText(`${fp.callsign}: Waypoints not found`, w / 2, centerY - 8);
      if (missing.length > 0) {
        ctx.fillText(`Missing: ${missing.join(", ")}`, w / 2, centerY + 8);
      }
      const pos = toCanvas(latitude, longitude, center, range, w, h);
      const hdg = selectedAircraft.initialPosition.heading;
      const rad = ((90 - hdg) * Math.PI) / 180;
      ctx.strokeStyle = "#f59e0b";
      ctx.lineWidth = 2;
      ctx.beginPath();
      ctx.moveTo(pos.x, pos.y);
      ctx.lineTo(
        pos.x + Math.cos(rad) * HEADING_ARROW_LENGTH,
        pos.y - Math.sin(rad) * HEADING_ARROW_LENGTH
      );
      ctx.stroke();
      ctx.fillStyle = "#f59e0b";
      ctx.beginPath();
      ctx.arc(pos.x, pos.y, 5, 0, 2 * Math.PI);
      ctx.fill();
      nowPosRef.current = { x: pos.x, y: pos.y };
      segmentPickRef.current = [];
      return;
    }

    pickTargetsRef.current = points.map((p) => ({
      x: p.x,
      y: p.y,
      label: p.label,
    }));

    segmentPickRef.current = [];
    for (let i = 0; i < points.length - 1; i++) {
      segmentPickRef.current.push({
        ax: points[i].x,
        ay: points[i].y,
        bx: points[i + 1].x,
        by: points[i + 1].y,
        insertIndex: i,
      });
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

    const pos = toCanvas(latitude, longitude, center, range, w, h);
    const hdg = selectedAircraft.initialPosition.heading;
    const hdgRad = ((90 - hdg) * Math.PI) / 180;
    ctx.strokeStyle = "#f59e0b";
    ctx.lineWidth = 2;
    ctx.beginPath();
    ctx.moveTo(pos.x, pos.y);
    ctx.lineTo(
      pos.x + Math.cos(hdgRad) * HEADING_ARROW_LENGTH,
      pos.y - Math.sin(hdgRad) * HEADING_ARROW_LENGTH
    );
    ctx.stroke();
    ctx.fillStyle = "#f59e0b";
    ctx.beginPath();
    ctx.arc(pos.x, pos.y, 5, 0, 2 * Math.PI);
    ctx.fill();
    ctx.fillStyle = "#e5e7eb";
    ctx.textAlign = "left";
    ctx.fillText("Now", pos.x + 8, pos.y + 4);
    nowPosRef.current = { x: pos.x, y: pos.y };
  }, [
    selectedAircraft,
    waypoints,
    radioNavAids,
    airportPositions,
    japanOutline,
    dw,
    dh,
    showRnavAirways,
    showAtsLower,
    rnavRoutes,
    atsLowerRoutes,
  ]);

  const canvasCssOffset = useCallback(
    (
      e:
        | React.PointerEvent<HTMLCanvasElement>
        | React.MouseEvent<HTMLCanvasElement>
    ) => {
      const canvas = canvasRef.current;
      if (!canvas) return null;
      const rect = canvas.getBoundingClientRect();
      return { x: e.clientX - rect.left, y: e.clientY - rect.top };
    },
    []
  );

  const handleCanvasPointerDown = useCallback(
    (e: React.PointerEvent<HTMLCanvasElement>) => {
      if (!selectedAircraft || !onInitialPositionGeoChange || e.button !== 0) {
        return;
      }
      const p = nowPosRef.current;
      const off = canvasCssOffset(e);
      if (!p || !off) return;
      const dx = off.x - p.x;
      const dy = off.y - p.y;
      if (dx * dx + dy * dy > NOW_DRAG_HIT_PX * NOW_DRAG_HIT_PX) return;
      e.currentTarget.setPointerCapture(e.pointerId);
      draggingNowRef.current = true;
      dragMovedRef.current = false;
      nowPointerActiveRef.current = true;
      setDraggingNowMarker(true);
    },
    [selectedAircraft, onInitialPositionGeoChange, canvasCssOffset]
  );

  const handleCanvasPointerMove = useCallback(
    (e: React.PointerEvent<HTMLCanvasElement>) => {
      if (!draggingNowRef.current) return;
      const cb = onGeoChangeRef.current;
      if (!cb) return;
      const off = canvasCssOffset(e);
      const pctx = pickContextRef.current;
      if (!off || !pctx) return;
      dragMovedRef.current = true;
      const geo = previewCanvasToGeo(
        off.x,
        off.y,
        pctx.center,
        pctx.range,
        pctx.dispW,
        pctx.dispH
      );
      cb(geo.latitude, geo.longitude);
    },
    [canvasCssOffset]
  );

  const handleCanvasPointerUp = useCallback(
    (e: React.PointerEvent<HTMLCanvasElement>) => {
      if (draggingNowRef.current) {
        draggingNowRef.current = false;
        setDraggingNowMarker(false);
        try {
          e.currentTarget.releasePointerCapture(e.pointerId);
        } catch {
          /* already released */
        }
        if (dragMovedRef.current) {
          suppressRouteClickRef.current = true;
        }
      }
      if (nowPointerActiveRef.current) {
        nowPointerActiveRef.current = false;
        suppressRouteClickRef.current = true;
      }
    },
    []
  );

  const handleCanvasClick = (e: React.MouseEvent<HTMLCanvasElement>) => {
    if (suppressRouteClickRef.current) {
      suppressRouteClickRef.current = false;
      return;
    }
    if (!onPickRoute || !selectedAircraft) return;
    const canvas = canvasRef.current;
    if (!canvas) return;
    const rect = canvas.getBoundingClientRect();
    const x = e.clientX - rect.left;
    const y = e.clientY - rect.top;
    const markerLabel = pickHitTarget(x, y, pickTargetsRef.current);
    if (markerLabel) {
      onPickRoute({ kind: "append", fixName: markerLabel });
      return;
    }

    const pctx = pickContextRef.current;
    if (!pctx) return;

    const geo = previewCanvasToGeo(
      x,
      y,
      pctx.center,
      pctx.range,
      pctx.dispW,
      pctx.dispH
    );
    const nearest = nearestCatalogFix(
      geo.latitude,
      geo.longitude,
      fixLookupRef.current,
      NEAREST_FIX_MAX_KM
    );
    if (!nearest) {
      onPickHint?.(
        `No waypoint/NAVAID/airport within ${NEAREST_FIX_MAX_KM} km of that point`
      );
      return;
    }

    let bestSeg: { insertIndex: number; dist: number } | null = null;
    for (const seg of segmentPickRef.current) {
      const d = distanceToSegment(x, y, seg.ax, seg.ay, seg.bx, seg.by);
      if (d <= SEGMENT_HIT_PX && (!bestSeg || d < bestSeg.dist)) {
        bestSeg = { insertIndex: seg.insertIndex, dist: d };
      }
    }

    if (bestSeg) {
      onPickRoute({
        kind: "insert",
        fixName: nearest.name,
        insertIndex: bestSeg.insertIndex,
      });
    } else {
      onPickRoute({ kind: "append", fixName: nearest.name });
    }
  };

  const interactive =
    Boolean(onPickRoute && selectedAircraft) ||
    Boolean(onInitialPositionGeoChange && selectedAircraft);

  return (
    <div
      className={`border border-atc-border rounded-lg overflow-hidden bg-atc-bg flex flex-col min-h-0 h-full ${className}`}
    >
      <RoutePreviewMapChrome
        selectedAircraft={selectedAircraft}
        showRnavAirways={showRnavAirways}
        showAtsLower={showAtsLower}
        onShowRnavAirways={setShowRnavAirways}
        onShowAtsLower={setShowAtsLower}
      />
      <div
        ref={containerRef}
        className="relative flex-1 min-h-[200px] w-full min-w-0 overflow-hidden"
      >
        <canvas
          ref={canvasRef}
          className={`block w-full h-full select-none touch-none ${
            draggingNowMarker
              ? "cursor-grabbing"
              : interactive
                ? "cursor-pointer"
                : ""
          }`}
          onClick={handleCanvasClick}
          onPointerDown={handleCanvasPointerDown}
          onPointerMove={handleCanvasPointerMove}
          onPointerUp={handleCanvasPointerUp}
          onPointerCancel={handleCanvasPointerUp}
        />
      </div>
      {selectedAircraft && (
        <div className="px-3 py-2 text-xs text-atc-text-muted border-t border-atc-border space-y-1 shrink-0">
          <div>
            <span className="text-atc-accent">●</span> Origin ·{" "}
            <span className="text-atc-danger">●</span> Dest ·{" "}
            <span className="text-amber-500">▲</span> Current pos (arrow =
            heading)
          </div>
          {onPickRoute && (
            <p>
              Click a fix (dot or label) to append. Click a green leg to insert
              the nearest DB fix at that position. Elsewhere appends the nearest
              fix (within {NEAREST_FIX_MAX_KM} km). Already-used fixes are
              skipped.
            </p>
          )}
          {onInitialPositionGeoChange && (
            <p>
              Drag the <span className="text-amber-500">▲ Now</span> marker to
              change initial latitude / longitude (also updates the form below).
            </p>
          )}
        </div>
      )}
    </div>
  );
}
