"use client";

import React, { useCallback, useEffect, useState } from "react";
import type { ScenarioAircraft, InitialPositionDto } from "@/types/scenario";

interface InitialPositionEditorProps {
  aircraft: ScenarioAircraft | null;
  onUpdate: (callsign: string, pos: Partial<InitialPositionDto>) => void;
}

function parseNum(v: string, fallback: number): number {
  const n = Number(v);
  return Number.isFinite(n) ? n : fallback;
}

export function InitialPositionEditor({
  aircraft,
  onUpdate,
}: InitialPositionEditorProps) {
  const [lat, setLat] = useState("");
  const [lon, setLon] = useState("");
  const [altitude, setAltitude] = useState("");
  const [heading, setHeading] = useState("");
  const [groundSpeed, setGroundSpeed] = useState("");
  const [verticalSpeed, setVerticalSpeed] = useState("");

  useEffect(() => {
    if (!aircraft) return;
    const p = aircraft.initialPosition;
    setLat(p.latitude.toString());
    setLon(p.longitude.toString());
    setAltitude(p.altitude.toString());
    setHeading(p.heading.toString());
    setGroundSpeed(p.groundSpeed.toString());
    setVerticalSpeed(p.verticalSpeed.toString());
  }, [aircraft]);

  const applyField = useCallback(
    (field: keyof InitialPositionDto, value: string) => {
      if (!aircraft) return;
      const fallback = aircraft.initialPosition[field];
      const num = parseNum(value, fallback as number);
      onUpdate(aircraft.flightPlan.callsign, { [field]: num });
    },
    [aircraft, onUpdate]
  );

  if (!aircraft) return null;

  return (
    <div className="mt-4 border border-atc-border rounded-lg p-4 bg-atc-surface">
      <h3 className="font-mono text-sm font-bold text-atc-text mb-2">
        Initial Position — {aircraft.flightPlan.callsign}
      </h3>
      <div className="grid grid-cols-2 gap-2 text-sm">
        <label>
          <span className="block text-atc-text-muted text-xs mb-1">Lat</span>
          <input
            type="number"
            value={lat}
            onChange={(e) => setLat(e.target.value)}
            onBlur={(e) => applyField("latitude", e.target.value)}
            step="any"
            className="w-full px-2 py-1.5 bg-atc-bg border border-atc-border rounded text-atc-text
                       focus:outline-none focus:border-atc-accent"
          />
        </label>
        <label>
          <span className="block text-atc-text-muted text-xs mb-1">Lon</span>
          <input
            type="number"
            value={lon}
            onChange={(e) => setLon(e.target.value)}
            onBlur={(e) => applyField("longitude", e.target.value)}
            step="any"
            className="w-full px-2 py-1.5 bg-atc-bg border border-atc-border rounded text-atc-text
                       focus:outline-none focus:border-atc-accent"
          />
        </label>
        <label>
          <span className="block text-atc-text-muted text-xs mb-1">
            Alt (ft)
          </span>
          <input
            type="number"
            value={altitude}
            onChange={(e) => setAltitude(e.target.value)}
            onBlur={(e) => applyField("altitude", e.target.value)}
            className="w-full px-2 py-1.5 bg-atc-bg border border-atc-border rounded text-atc-text
                       focus:outline-none focus:border-atc-accent"
          />
        </label>
        <label>
          <span className="block text-atc-text-muted text-xs mb-1">Hdg</span>
          <input
            type="number"
            value={heading}
            onChange={(e) => setHeading(e.target.value)}
            onBlur={(e) => applyField("heading", e.target.value)}
            min={0}
            max={359}
            className="w-full px-2 py-1.5 bg-atc-bg border border-atc-border rounded text-atc-text
                       focus:outline-none focus:border-atc-accent"
          />
        </label>
        <label>
          <span className="block text-atc-text-muted text-xs mb-1">
            GS (kts)
          </span>
          <input
            type="number"
            value={groundSpeed}
            onChange={(e) => setGroundSpeed(e.target.value)}
            onBlur={(e) => applyField("groundSpeed", e.target.value)}
            className="w-full px-2 py-1.5 bg-atc-bg border border-atc-border rounded text-atc-text
                       focus:outline-none focus:border-atc-accent"
          />
        </label>
        <label>
          <span className="block text-atc-text-muted text-xs mb-1">VS</span>
          <input
            type="number"
            value={verticalSpeed}
            onChange={(e) => setVerticalSpeed(e.target.value)}
            onBlur={(e) => applyField("verticalSpeed", e.target.value)}
            className="w-full px-2 py-1.5 bg-atc-bg border border-atc-border rounded text-atc-text
                       focus:outline-none focus:border-atc-accent"
          />
        </label>
      </div>
    </div>
  );
}
