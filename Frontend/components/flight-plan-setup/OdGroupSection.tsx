"use client";

import React, { useEffect, useState } from "react";
import { AtsRouteSearch } from "./AtsRouteSearch";
import { suggestRoute } from "@/utility/api/ats";
import type { ScenarioAircraft } from "@/utility/api/scenario";
import type { Route } from "@/utility/AtsRouteManager/RouteInterfaces/Route";

interface OdGroupSectionProps {
  origin: string;
  destination: string;
  aircraft: ScenarioAircraft[];
  selectedCallsign: string | null;
  onSelectAircraft: (a: ScenarioAircraft) => void;
  onSuggestStatus?: (status: string | null) => void;
  atsRoutes: { atsLowerRoutes: Route[]; rnavRoutes: Route[] };
  routeByOd: Map<
    string,
    { waypoints: string[]; cruiseAltitude: number; cruiseSpeed: number }
  >;
  onRouteChange: (
    key: string,
    route: { waypoints: string[]; cruiseAltitude: number; cruiseSpeed: number }
  ) => void;
}

export function OdGroupSection({
  origin,
  destination,
  aircraft,
  selectedCallsign,
  onSelectAircraft,
  onSuggestStatus,
  atsRoutes,
  routeByOd,
  onRouteChange,
}: OdGroupSectionProps) {
  const [expanded, setExpanded] = useState(true);
  const [suggesting, setSuggesting] = useState(false);
  const key = `${origin}→${destination}`;
  const current = routeByOd.get(key) ?? {
    waypoints: [],
    cruiseAltitude: 35000,
    cruiseSpeed: 450,
  };
  const [waypointsText, setWaypointsText] = useState(
    current.waypoints.join(", ")
  );
  const [cruiseAlt, setCruiseAlt] = useState(current.cruiseAltitude);
  const [cruiseSpd, setCruiseSpd] = useState(current.cruiseSpeed);

  useEffect(() => {
    const c = routeByOd.get(key) ?? {
      waypoints: [],
      cruiseAltitude: 35000,
      cruiseSpeed: 450,
    };
    setWaypointsText(c.waypoints.join(", "));
    setCruiseAlt(c.cruiseAltitude);
    setCruiseSpd(c.cruiseSpeed);
  }, [routeByOd, key]);

  const applyToGroup = () => {
    const wps = waypointsText
      .split(/[,\s]+/)
      .map((s) => s.trim().toUpperCase())
      .filter(Boolean);
    onRouteChange(key, {
      waypoints: wps,
      cruiseAltitude: cruiseAlt,
      cruiseSpeed: cruiseSpd,
    });
  };

  const handleAtsSelect = (waypoints: string[]) => {
    setWaypointsText(waypoints.join(", "));
  };

  const handleSuggestRoute = async () => {
    setSuggesting(true);
    onSuggestStatus?.(null);
    const result = await suggestRoute(origin, destination);
    setSuggesting(false);
    if ("waypoints" in result && result.waypoints.length > 0) {
      setWaypointsText(result.waypoints.join(", "));
      onRouteChange(key, {
        waypoints: result.waypoints,
        cruiseAltitude: cruiseAlt,
        cruiseSpeed: cruiseSpd,
      });
      onSuggestStatus?.(
        `Suggested and applied ${result.waypoints.length} waypoints`
      );
    } else {
      onSuggestStatus?.(
        "error" in result
          ? `Error: ${result.error}`
          : "No route found for this O/D"
      );
    }
  };

  return (
    <div className="border border-atc-border rounded-lg bg-atc-surface overflow-hidden">
      <button
        type="button"
        onClick={() => setExpanded(!expanded)}
        className="w-full px-4 py-2 flex items-center justify-between text-left
                   bg-atc-surface-elevated hover:bg-atc-surface"
      >
        <span className="font-mono text-sm text-atc-text">
          {origin} → {destination}
        </span>
        <span className="text-xs text-atc-text-muted">
          {aircraft.length} aircraft
        </span>
      </button>
      {expanded && (
        <div className="p-4 space-y-3 border-t border-atc-border">
          <p className="text-xs text-atc-text-muted flex flex-wrap gap-x-1 gap-y-0.5">
            {aircraft.map((a, i) => {
              const cs = a.flightPlan.callsign;
              const isSelected = selectedCallsign === cs;
              return (
                <React.Fragment key={cs}>
                  {i > 0 && ", "}
                  <button
                    type="button"
                    onClick={() => onSelectAircraft(a)}
                    className={`font-mono hover:underline ${
                      isSelected ? "text-atc-accent font-bold" : ""
                    }`}
                  >
                    {cs}
                  </button>
                </React.Fragment>
              );
            })}
          </p>
          <div className="space-y-3">
            <div className="flex flex-wrap gap-2 items-center">
              <button
                type="button"
                onClick={handleSuggestRoute}
                disabled={suggesting}
                className="px-3 py-1.5 text-xs font-bold bg-atc-accent text-white rounded
                           hover:opacity-90 disabled:opacity-50"
              >
                {suggesting ? "..." : "Suggest route"}
              </button>
              <span className="text-xs text-atc-text-muted font-medium">—</span>
              <span className="text-xs text-atc-text-muted">
                or choose from ATS routes:
              </span>
            </div>
            <AtsRouteSearch
              originIcao={origin}
              destinationIcao={destination}
              atsRoutes={atsRoutes}
              onSelect={handleAtsSelect}
            />
          </div>
          <div className="flex gap-2">
            <input
              type="text"
              value={waypointsText}
              onChange={(e) => setWaypointsText(e.target.value)}
              placeholder="KOITO, BOKJO, AOIKU"
              className="flex-1 px-3 py-1.5 text-sm bg-atc-bg border border-atc-border rounded
                         text-atc-text font-mono placeholder-atc-text-muted
                         focus:outline-none focus:border-atc-accent"
            />
            <input
              type="number"
              value={cruiseAlt}
              onChange={(e) => setCruiseAlt(Number(e.target.value))}
              className="w-20 px-2 py-1.5 text-sm bg-atc-bg border border-atc-border rounded
                         text-atc-text"
              title="Cruise altitude (ft)"
            />
            <input
              type="number"
              value={cruiseSpd}
              onChange={(e) => setCruiseSpd(Number(e.target.value))}
              className="w-20 px-2 py-1.5 text-sm bg-atc-bg border border-atc-border rounded
                         text-atc-text"
              title="Cruise speed (kts)"
            />
          </div>
          <button
            type="button"
            onClick={applyToGroup}
            className="px-3 py-1.5 text-xs font-bold bg-atc-accent text-white rounded
                       hover:opacity-90"
          >
            APPLY TO GROUP
          </button>
        </div>
      )}
    </div>
  );
}
