"use client";

import React, { useCallback, useEffect, useState } from "react";
import type { ScenarioAircraft, InitialPositionDto } from "@/types/scenario";
import { positionToFormStrings } from "@/utility/flightPlanSetup/initialPositionForm";

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

  /**
   * Not an external system sync: keep local string buffers aligned with props when
   * the same aircraft is updated elsewhere (e.g. map drag). key={callsign} alone
   * does not cover per-field updates for a fixed callsign.
   */
  useEffect(() => {
    if (!aircraft) return;
    const s = positionToFormStrings(aircraft.initialPosition);
    setLat(s.lat);
    setLon(s.lon);
    setAltitude(s.altitude);
    setHeading(s.heading);
    setGroundSpeed(s.groundSpeed);
    setVerticalSpeed(s.verticalSpeed);
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

  const fieldClass =
    "w-full rounded-xl border border-[#e4d8c4] bg-white px-3 py-2 text-sm text-[#1c1915] outline-none focus:border-[#c2410c]";

  return (
    <div>
      <h3 className="text-xs font-semibold uppercase tracking-[0.16em] text-[#8a7d6b]">
        Start state
      </h3>
      <p className="mt-1 mb-3 text-sm text-[#6b6256]">
        Drag Now on the chart to move {aircraft.flightPlan.callsign}. These
        numbers stay in sync.
      </p>
      <div className="grid grid-cols-2 gap-3 text-sm">
        <label>
          <span className="mb-1 block text-xs text-[#6b6256]">Lat</span>
          <input
            type="number"
            value={lat}
            onChange={(e) => setLat(e.target.value)}
            onBlur={(e) => applyField("latitude", e.target.value)}
            step="any"
            className={fieldClass}
          />
        </label>
        <label>
          <span className="mb-1 block text-xs text-[#6b6256]">Lon</span>
          <input
            type="number"
            value={lon}
            onChange={(e) => setLon(e.target.value)}
            onBlur={(e) => applyField("longitude", e.target.value)}
            step="any"
            className={fieldClass}
          />
        </label>
        <label>
          <span className="mb-1 block text-xs text-[#6b6256]">Alt (ft)</span>
          <input
            type="number"
            value={altitude}
            onChange={(e) => setAltitude(e.target.value)}
            onBlur={(e) => applyField("altitude", e.target.value)}
            className={fieldClass}
          />
        </label>
        <label>
          <span className="mb-1 block text-xs text-[#6b6256]">Hdg</span>
          <input
            type="number"
            value={heading}
            onChange={(e) => setHeading(e.target.value)}
            onBlur={(e) => applyField("heading", e.target.value)}
            min={0}
            max={359}
            className={fieldClass}
          />
        </label>
        <label>
          <span className="mb-1 block text-xs text-[#6b6256]">GS (kt)</span>
          <input
            type="number"
            value={groundSpeed}
            onChange={(e) => setGroundSpeed(e.target.value)}
            onBlur={(e) => applyField("groundSpeed", e.target.value)}
            className={fieldClass}
          />
        </label>
        <label>
          <span className="mb-1 block text-xs text-[#6b6256]">VS</span>
          <input
            type="number"
            value={verticalSpeed}
            onChange={(e) => setVerticalSpeed(e.target.value)}
            onBlur={(e) => applyField("verticalSpeed", e.target.value)}
            className={fieldClass}
          />
        </label>
      </div>
    </div>
  );
}
