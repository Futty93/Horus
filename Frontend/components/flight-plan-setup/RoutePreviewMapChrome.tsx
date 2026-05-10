"use client";

import React from "react";
import type { ScenarioAircraft } from "@/types/scenario";

const RNAV_AIRWAY_COLOR = "#376";
const ATS_LOWER_AIRWAY_COLOR = "#0ff";

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
  return (
    <div className="px-3 py-2 border-b border-atc-border bg-atc-surface-elevated shrink-0 space-y-2">
      <h3 className="font-mono text-sm font-bold text-atc-text">
        Route Preview
        {selectedAircraft && (
          <span className="ml-2 font-normal text-atc-text-muted">
            {selectedAircraft.flightPlan.callsign}
          </span>
        )}
      </h3>
      <div className="flex flex-wrap gap-3 text-xs text-atc-text-muted">
        <label className="inline-flex items-center gap-1.5 cursor-pointer select-none">
          <input
            type="checkbox"
            className="rounded border-atc-border"
            checked={showRnavAirways}
            onChange={(e) => onShowRnavAirways(e.target.checked)}
          />
          <span style={{ color: RNAV_AIRWAY_COLOR }}>RNAV</span> published
        </label>
        <label className="inline-flex items-center gap-1.5 cursor-pointer select-none">
          <input
            type="checkbox"
            className="rounded border-atc-border"
            checked={showAtsLower}
            onChange={(e) => onShowAtsLower(e.target.checked)}
          />
          <span style={{ color: ATS_LOWER_AIRWAY_COLOR }}>ATS Lower</span>{" "}
          published
        </label>
      </div>
    </div>
  );
}
