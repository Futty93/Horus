"use client";

import React, { useState, useEffect, useCallback } from "react";
import { useSelectedAircraft } from "@/context/selectedAircraftContext";
import { postAtcClearance } from "@/utility/api/atcClearance";
import type { AircraftLocationDto } from "@/utility/api/location";

interface MemoEntry {
  altitude: number;
  groundSpeed: number;
  heading: number;
}

function memoEntryFromLocation(loc: AircraftLocationDto): MemoEntry {
  return {
    altitude: loc.position.altitude,
    groundSpeed: loc.vector.groundSpeed,
    heading: loc.vector.heading,
  };
}

async function fetchSelectedAircraftLocation(
  callsign: string
): Promise<AircraftLocationDto | null> {
  try {
    const response = await fetch("/api/aircraft/location/all", {
      method: "GET",
      headers: { Accept: "application/json" },
      cache: "no-store",
    });
    if (!response.ok) return null;
    const data: AircraftLocationDto[] = await response.json();
    return data.find((a) => a.callsign === callsign) ?? null;
  } catch {
    return null;
  }
}

const InstructionMemo = () => {
  const { callsign } = useSelectedAircraft();
  const [altitude, setAltitude] = useState(0);
  const [groundSpeed, setGroundSpeed] = useState(0);
  const [heading, setHeading] = useState(0);
  const [memo, setMemo] = useState<MemoEntry | null>(null);
  const [currentState, setCurrentState] = useState<MemoEntry | null>(null);
  const [recording, setRecording] = useState(false);
  const [recordError, setRecordError] = useState<string | null>(null);

  const refreshCurrentDisplay = useCallback(async () => {
    if (!callsign || callsign.length < 2) {
      setCurrentState(null);
      return;
    }
    const loc = await fetchSelectedAircraftLocation(callsign);
    if (loc) {
      setCurrentState(memoEntryFromLocation(loc));
    } else {
      setCurrentState(null);
    }
  }, [callsign]);

  useEffect(() => {
    if (!callsign || callsign.length < 2) {
      setAltitude(0);
      setGroundSpeed(0);
      setHeading(0);
      setCurrentState(null);
      return;
    }
    let cancelled = false;
    void (async () => {
      const loc = await fetchSelectedAircraftLocation(callsign);
      if (cancelled || !loc) return;
      const entry = memoEntryFromLocation(loc);
      setCurrentState(entry);
      setAltitude(Math.round(entry.altitude));
      setGroundSpeed(Math.round(entry.groundSpeed));
      setHeading(Math.round(entry.heading));
    })();
    return () => {
      cancelled = true;
    };
  }, [callsign]);

  useEffect(() => {
    if (!callsign || callsign.length < 2) return;
    const interval = setInterval(() => {
      void refreshCurrentDisplay();
    }, 3000);
    return () => clearInterval(interval);
  }, [callsign, refreshCurrentDisplay]);

  const handleFillFromCurrent = () => {
    if (currentState) {
      setAltitude(currentState.altitude);
      setGroundSpeed(currentState.groundSpeed);
      setHeading(currentState.heading);
    }
  };

  const handleRecord = async () => {
    if (!callsign || callsign.length < 2) return;
    setRecordError(null);
    setRecording(true);
    try {
      const ok = await postAtcClearance(callsign, {
        instructedAltitude: Math.round(altitude),
        instructedGroundSpeed: Math.round(groundSpeed),
        instructedHeading: Math.min(360, Math.max(0, Math.round(heading))),
      });
      if (!ok) {
        setRecordError("Failed to save clearance to server.");
        return;
      }
      setMemo({ altitude, groundSpeed, heading });
    } finally {
      setRecording(false);
    }
  };

  const fields = [
    {
      key: "altitude" as const,
      label: "Altitude",
      unit: "ft",
      value: altitude,
      setter: setAltitude,
    },
    {
      key: "groundSpeed" as const,
      label: "Speed",
      unit: "kts",
      value: groundSpeed,
      setter: setGroundSpeed,
    },
    {
      key: "heading" as const,
      label: "Heading",
      unit: "°",
      value: heading,
      setter: setHeading,
    },
  ] as const;

  if (!callsign || callsign.length < 2) {
    return (
      <div className="bg-atc-surface border border-atc-border rounded-lg p-4">
        <h3 className="text-sm font-bold text-atc-text font-mono tracking-wider mb-3">
          INSTRUCTION MEMO
        </h3>
        <p className="text-sm text-atc-text-muted">
          Select aircraft to record your instruction memo
        </p>
      </div>
    );
  }

  return (
    <div className="bg-atc-surface border border-atc-border rounded-lg p-4">
      <h3 className="text-sm font-bold text-atc-text font-mono tracking-wider mb-3">
        INSTRUCTION MEMO
      </h3>
      <p className="text-xs text-atc-text-muted mb-3">
        RECORD saves the values as server-side ATC clearance (shared on all
        radar clients). Same JSON shape as pilot control; semantics are
        controller clearance memo only. Leave a field at 0 to skip recording
        that axis (radar memo only shows differences for recorded axes; use 360°
        for north).
      </p>

      {currentState && (
        <div className="mb-4 p-3 bg-atc-surface-elevated border border-atc-border rounded">
          <div className="text-xs font-semibold text-atc-text-muted mb-2">
            Current state
          </div>
          <div className="text-sm font-mono text-atc-text">
            {currentState.altitude}ft / {currentState.groundSpeed}kts /{" "}
            {currentState.heading}°
          </div>
          <button
            type="button"
            onClick={handleFillFromCurrent}
            className="mt-2 text-xs text-atc-accent hover:text-atc-accent-hover transition-colors"
          >
            Refresh inputs from current
          </button>
        </div>
      )}

      <div className="space-y-4">
        {fields.map(({ key, label, unit, value, setter }) => (
          <div key={key} className="group relative">
            <label
              htmlFor={`memo-${key}`}
              className="block text-xs font-semibold text-atc-text-muted mb-1"
            >
              {label}
            </label>
            <input
              type="number"
              id={`memo-${key}`}
              placeholder="0"
              value={value || ""}
              onChange={(e) => setter(parseInt(e.target.value, 10) || 0)}
              className="w-full px-3 py-2 bg-atc-surface-elevated border border-atc-border rounded
                         text-atc-text text-sm font-mono placeholder-atc-text-muted
                         focus:outline-none focus:border-atc-accent
                         hover:border-atc-text-muted"
            />
            <span className="absolute right-2 top-1/2 transform -translate-y-1/2 text-xs text-atc-text-muted font-mono">
              {unit}
            </span>
          </div>
        ))}

        <button
          type="button"
          onClick={() => void handleRecord()}
          disabled={recording}
          className="w-full mt-4 px-4 py-3 bg-atc-surface-elevated text-atc-text font-bold text-sm
                     rounded-lg border border-atc-accent
                     transition-colors duration-200
                     hover:bg-atc-accent hover:text-white
                     focus:outline-none focus:ring-2 focus:ring-atc-accent focus:ring-offset-2 focus:ring-offset-atc-bg
                     disabled:opacity-50 disabled:pointer-events-none"
        >
          {recording ? "RECORDING…" : "RECORD (記録)"}
        </button>

        {recordError && (
          <p className="mt-2 text-xs text-red-400 font-mono" role="alert">
            {recordError}
          </p>
        )}

        {memo && (
          <div className="mt-4 p-3 bg-atc-surface-elevated/80 border border-atc-border rounded text-xs font-mono text-atc-text-muted">
            <div className="font-semibold text-atc-text mb-1">
              Recorded memo:
            </div>
            <div>
              {memo.altitude}ft / {memo.groundSpeed}kts / {memo.heading}°
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default InstructionMemo;
