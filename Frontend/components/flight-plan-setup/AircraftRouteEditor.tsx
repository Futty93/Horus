"use client";

import React, { useEffect, useMemo, useState } from "react";
import { AtsRouteSearch } from "./AtsRouteSearch";
import { suggestRoute } from "@/utility/api/ats";
import type { ScenarioAircraft } from "@/types/scenario";
import type { Route } from "@/utility/AtsRouteManager/RouteInterfaces/Route";
import type { Waypoint } from "@/utility/AtsRouteManager/RouteInterfaces/Waypoint";
import type { RadioNavigationAid } from "@/utility/AtsRouteManager/RouteInterfaces/RadioNavigationAid";

export type AircraftRouteDef = {
  waypoints: string[];
  cruiseAltitude: number;
  cruiseSpeed: number;
};

interface AircraftRouteEditorProps {
  aircraft: ScenarioAircraft | null;
  atsRoutes: { atsLowerRoutes: Route[]; rnavRoutes: Route[] };
  /** Waypoints + navaids from loaded ATS data (names for search / add). */
  fixSources: { waypoints: Waypoint[]; radioNavAids: RadioNavigationAid[] };
  onApplyRoute: (route: AircraftRouteDef) => void;
  onSuggestStatus?: (status: string | null) => void;
}

export function AircraftRouteEditor({
  aircraft,
  atsRoutes,
  fixSources,
  onApplyRoute,
  onSuggestStatus,
}: AircraftRouteEditorProps) {
  const [suggesting, setSuggesting] = useState(false);
  const [waypointsText, setWaypointsText] = useState("");
  const [cruiseAlt, setCruiseAlt] = useState(35000);
  const [cruiseSpd, setCruiseSpd] = useState(450);
  const [fixSearch, setFixSearch] = useState("");

  const fixNameList = useMemo(() => {
    const m = new Map<string, string>();
    for (const w of fixSources.waypoints) {
      m.set(w.name.toUpperCase(), w.name);
    }
    for (const r of fixSources.radioNavAids) {
      m.set(r.name.toUpperCase(), r.name);
    }
    return Array.from(m.values()).sort((a, b) =>
      a.localeCompare(b, undefined, { sensitivity: "base" })
    );
  }, [fixSources.waypoints, fixSources.radioNavAids]);

  const fixSearchMatches = useMemo(() => {
    const q = fixSearch.trim().toUpperCase();
    if (!q || fixNameList.length === 0) return [];
    const prefix: string[] = [];
    const other: string[] = [];
    for (const name of fixNameList) {
      const u = name.toUpperCase();
      if (u.startsWith(q)) prefix.push(name);
      else if (u.includes(q)) other.push(name);
    }
    return [...prefix, ...other].slice(0, 25);
  }, [fixNameList, fixSearch]);

  useEffect(() => {
    if (!aircraft) return;
    const fp = aircraft.flightPlan;
    setWaypointsText(fp.route.map((w) => w.fix).join(", "));
    setCruiseAlt(fp.cruiseAltitude);
    setCruiseSpd(fp.cruiseSpeed);
    setFixSearch("");
  }, [aircraft]);

  const pushRoute = (waypoints: string[]) => {
    onApplyRoute({
      waypoints,
      cruiseAltitude: cruiseAlt,
      cruiseSpeed: cruiseSpd,
    });
  };

  const appendFixFromCatalog = (rawName: string) => {
    if (!aircraft) return;
    const name = rawName.trim().toUpperCase();
    if (!name) return;
    const parts = waypointsText
      .split(/[,\s]+/)
      .map((s) => s.trim().toUpperCase())
      .filter(Boolean);
    const nextParts = parts.includes(name) ? parts : [...parts, name];
    setWaypointsText(nextParts.join(", "));
    pushRoute(nextParts);
    onSuggestStatus?.(`Added ${name} to ${aircraft.flightPlan.callsign}`);
  };

  const applyFromText = () => {
    const wps = waypointsText
      .split(/[,\s]+/)
      .map((s) => s.trim().toUpperCase())
      .filter(Boolean);
    pushRoute(wps);
  };

  const handleAtsSelect = (waypoints: string[]) => {
    setWaypointsText(waypoints.join(", "));
    pushRoute(waypoints);
    onSuggestStatus?.(
      `Applied ATS route to ${aircraft?.flightPlan.callsign ?? "?"} (${waypoints.length} waypoints)`
    );
  };

  const handleSuggestRoute = async () => {
    if (!aircraft) return;
    const { departureAirport: origin, arrivalAirport: destination } =
      aircraft.flightPlan;
    setSuggesting(true);
    onSuggestStatus?.(null);
    const result = await suggestRoute(origin, destination);
    setSuggesting(false);
    if ("waypoints" in result && result.waypoints.length > 0) {
      setWaypointsText(result.waypoints.join(", "));
      pushRoute(result.waypoints);
      onSuggestStatus?.(
        `Suggested route for ${aircraft.flightPlan.callsign} (${result.waypoints.length} waypoints)`
      );
    } else {
      onSuggestStatus?.(
        "error" in result
          ? `Error: ${result.error}`
          : "No route found for this O/D"
      );
    }
  };

  if (!aircraft) {
    return (
      <div className="border border-atc-border rounded-lg p-4 bg-atc-surface text-atc-text-muted text-sm">
        Select an aircraft in the table below. Its cruise route, preview map,
        and initial position (right panel) all follow that selection.
      </div>
    );
  }

  const fp = aircraft.flightPlan;

  return (
    <div className="border border-atc-border rounded-lg bg-atc-surface overflow-hidden">
      <div className="px-4 py-3 border-b border-atc-border bg-atc-surface-elevated">
        <h2 className="font-mono text-sm font-bold text-atc-text">
          Route — <span className="text-atc-accent">{fp.callsign}</span>
          <span className="font-normal text-atc-text-muted ml-2">
            {fp.departureAirport} → {fp.arrivalAirport}
          </span>
        </h2>
        <p className="text-xs text-atc-text-muted mt-1">
          Changes here apply only to this aircraft. Use “Bulk by O/D” below to
          copy one path to every flight with the same airports.
        </p>
      </div>
      <div className="p-4 space-y-3">
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
            originIcao={fp.departureAirport}
            destinationIcao={fp.arrivalAirport}
            atsRoutes={atsRoutes}
            onSelect={handleAtsSelect}
          />
        </div>
        <div className="space-y-1">
          <label className="block text-xs text-atc-text-muted font-medium">
            Add fix from database
          </label>
          <input
            type="text"
            value={fixSearch}
            onChange={(e) => setFixSearch(e.target.value)}
            onKeyDown={(e) => {
              if (e.key !== "Enter" || fixSearchMatches.length === 0) return;
              e.preventDefault();
              appendFixFromCatalog(fixSearchMatches[0]);
              setFixSearch("");
            }}
            placeholder="Partial name (e.g. KOI) — Enter adds first match"
            className="w-full px-3 py-1.5 text-sm bg-atc-bg border border-atc-border rounded
                       text-atc-text font-mono placeholder-atc-text-muted
                       focus:outline-none focus:border-atc-accent"
            autoComplete="off"
          />
          {fixNameList.length === 0 ? (
            <p className="text-xs text-atc-text-muted">
              No fix list loaded yet (check ATS data / network).
            </p>
          ) : fixSearch.trim() && fixSearchMatches.length === 0 ? (
            <p className="text-xs text-atc-text-muted">
              No matching fix names.
            </p>
          ) : fixSearchMatches.length > 0 ? (
            <ul
              className="max-h-40 overflow-y-auto border border-atc-border rounded-md bg-atc-bg text-sm font-mono"
              role="listbox"
            >
              {fixSearchMatches.map((name) => (
                <li
                  key={name}
                  className="border-b border-atc-border last:border-b-0"
                >
                  <button
                    type="button"
                    className="w-full text-left px-2 py-1.5 text-atc-text hover:bg-atc-surface-elevated"
                    onClick={() => {
                      appendFixFromCatalog(name);
                      setFixSearch("");
                    }}
                  >
                    {name}
                  </button>
                </li>
              ))}
            </ul>
          ) : null}
          <p className="text-xs text-atc-text-muted">
            Appends to the end of the route (no duplicate). Uses loaded
            waypoints / navaids only.
          </p>
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
          onClick={applyFromText}
          className="px-3 py-1.5 text-xs font-bold bg-atc-surface border border-atc-border rounded
                     text-atc-text hover:border-atc-accent"
        >
          Apply waypoint text & cruise to this aircraft
        </button>
        <p className="text-xs text-atc-text-muted">
          Suggest and ATS selections apply immediately. Use the button above if
          you only edited the text field.
        </p>
      </div>
    </div>
  );
}
