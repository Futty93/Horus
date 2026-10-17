"use client";

import React from "react";
import type { ScenarioAircraft } from "@/types/scenario";

interface RoutePreviewMapChromeProps {
  selectedAircraft: ScenarioAircraft | null;
  showRnavAirways: boolean;
  showAtsLower: boolean;
  onShowRnavAirways: (v: boolean) => void;
  onShowAtsLower: (v: boolean) => void;
}

export function RoutePreviewMapChrome({
  selectedAircraft,
  showRnavAirways,
  showAtsLower,
  onShowRnavAirways,
  onShowAtsLower,
}: RoutePreviewMapChromeProps) {
  const callsign = selectedAircraft?.flightPlan.callsign;
  return (
    <div className="shrink-0 space-y-2 border-b border-white/10 bg-[#1c1915]/80 px-5 py-4 text-[#f4efe4] backdrop-blur-sm">
      <div className="flex items-baseline justify-between gap-4">
        <h3 className="font-[family-name:var(--font-geist-sans)] text-lg font-semibold">
          Chart
          {callsign && (
            <span className="ml-2 font-[family-name:var(--font-geist-mono)] text-sm font-normal text-[#d6cbb8]">
              {callsign}
            </span>
          )}
        </h3>
        <div className="flex gap-4 text-xs text-[#d6cbb8]">
          <label className="inline-flex cursor-pointer items-center gap-1.5">
            <input
              type="checkbox"
              checked={showRnavAirways}
              onChange={(event) => onShowRnavAirways(event.target.checked)}
            />
            RNAV
          </label>
          <label className="inline-flex cursor-pointer items-center gap-1.5">
            <input
              type="checkbox"
              checked={showAtsLower}
              onChange={(event) => onShowAtsLower(event.target.checked)}
            />
            ATS lower
          </label>
        </div>
      </div>
      <p className="max-w-xl text-sm text-[#d6cbb8]">
        Click a fix to add it at the end. Click a green leg to insert the
        nearest fix. Drag the orange Now marker to move where the flight starts.
      </p>
    </div>
  );
}
