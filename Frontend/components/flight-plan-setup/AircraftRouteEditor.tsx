"use client";

import React, { useEffect, useMemo, useState } from "react";
import { AtsRouteSearch } from "./AtsRouteSearch";
import { RouteFixList } from "./RouteFixList";
import { suggestRoute } from "@/utility/api/ats";
import { appendFix } from "@/utility/flightPlanSetup/routeEdits";
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
  fixSources: { waypoints: Waypoint[]; radioNavAids: RadioNavigationAid[] };
  onApplyRoute: (
    route: AircraftRouteDef,
    applyOptions?: { callsign: string }
  ) => void;
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
  const [cruiseAlt, setCruiseAlt] = useState(
    () => aircraft?.flightPlan.cruiseAltitude ?? 31000
  );
  const [cruiseSpd, setCruiseSpd] = useState(
    () => aircraft?.flightPlan.cruiseSpeed ?? 450
  );
  const [fixSearch, setFixSearch] = useState("");

  const fixNameList = useMemo(() => {
    const names = new Map<string, string>();
    for (const waypoint of fixSources.waypoints) {
      names.set(waypoint.name.toUpperCase(), waypoint.name);
    }
    for (const aid of fixSources.radioNavAids) {
      names.set(aid.name.toUpperCase(), aid.name);
    }
    return Array.from(names.values()).sort((a, b) =>
      a.localeCompare(b, undefined, { sensitivity: "base" })
    );
  }, [fixSources.waypoints, fixSources.radioNavAids]);

  const fixSearchMatches = useMemo(() => {
    const query = fixSearch.trim().toUpperCase();
    if (!query || fixNameList.length === 0) return [];
    const prefix: string[] = [];
    const other: string[] = [];
    for (const name of fixNameList) {
      const upper = name.toUpperCase();
      if (upper.startsWith(query)) prefix.push(name);
      else if (upper.includes(query)) other.push(name);
    }
    return [...prefix, ...other].slice(0, 25);
  }, [fixNameList, fixSearch]);

  useEffect(() => {
    if (!aircraft) return;
    setCruiseAlt(aircraft.flightPlan.cruiseAltitude);
    setCruiseSpd(aircraft.flightPlan.cruiseSpeed);
    setFixSearch("");
  }, [aircraft]);

  const commit = (
    waypoints: string[],
    altitude = cruiseAlt,
    speed = cruiseSpd,
    applyOptions?: { callsign: string }
  ) => {
    onApplyRoute(
      {
        waypoints,
        cruiseAltitude: altitude,
        cruiseSpeed: speed,
      },
      applyOptions
    );
  };

  const appendFixFromCatalog = (rawName: string) => {
    if (!aircraft) return;
    const fp = aircraft.flightPlan;
    const current = fp.route.map((waypoint) => waypoint.fix);
    const result = appendFix(
      current,
      rawName,
      new Set([
        fp.departureAirport.toUpperCase(),
        fp.arrivalAirport.toUpperCase(),
      ])
    );
    if (!result.ok) {
      onSuggestStatus?.(
        result.reason === "duplicate"
          ? `${rawName.trim()} is already on this route`
          : "Enter a fix name"
      );
      return;
    }
    commit(result.waypoints);
    onSuggestStatus?.(
      `Added ${result.waypoints[result.waypoints.length - 1]} to ${fp.callsign}`
    );
  };

  const handleAtsSelect = (waypoints: string[]) => {
    commit(waypoints);
    onSuggestStatus?.(
      `Applied ATS route to ${aircraft?.flightPlan.callsign ?? "?"} (${waypoints.length} waypoints)`
    );
  };

  const handleSuggestRoute = async () => {
    if (!aircraft) return;
    const forCallsign = aircraft.flightPlan.callsign;
    const { departureAirport: origin, arrivalAirport: destination } =
      aircraft.flightPlan;
    setSuggesting(true);
    onSuggestStatus?.(null);
    const result = await suggestRoute(origin, destination);
    setSuggesting(false);
    if ("waypoints" in result && result.waypoints.length > 0) {
      commit(result.waypoints, cruiseAlt, cruiseSpd, { callsign: forCallsign });
      onSuggestStatus?.(
        `Suggested route for ${forCallsign} (${result.waypoints.length} waypoints)`
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
      <div className="text-sm text-[#6b6256]">
        Select an aircraft. The chart and this route follow that flight.
      </div>
    );
  }

  const fp = aircraft.flightPlan;
  const waypoints = fp.route.map((waypoint) => waypoint.fix);

  return (
    <div className="space-y-4">
      <div>
        <h2 className="text-xs font-semibold uppercase tracking-[0.16em] text-[#8a7d6b]">
          Route
        </h2>
        <p className="mt-1 font-[family-name:var(--font-geist-mono)] text-lg text-[#1c1915]">
          {fp.callsign}
          <span className="ml-2 text-sm text-[#6b6256]">
            {fp.departureAirport} → {fp.arrivalAirport}
          </span>
        </p>
        <p className="mt-1 text-sm text-[#6b6256]">
          Drag a row, or use Up and Down. Removing a fix updates the chart
          immediately.
        </p>
      </div>
      <div className="flex flex-wrap items-center gap-3">
        <button
          type="button"
          onClick={handleSuggestRoute}
          disabled={suggesting}
          className="rounded-full bg-[#1c1915] px-4 py-2 text-sm font-semibold text-[#f4efe4] hover:bg-[#3f3830] disabled:opacity-40"
        >
          {suggesting ? "Suggesting…" : "Suggest again"}
        </button>
        <span className="text-sm text-[#8a7d6b]">or pick an airway</span>
      </div>
      <AtsRouteSearch
        originIcao={fp.departureAirport}
        destinationIcao={fp.arrivalAirport}
        atsRoutes={atsRoutes}
        onSelect={handleAtsSelect}
      />
      <RouteFixList waypoints={waypoints} onChange={(next) => commit(next)} />
      <div className="space-y-1">
        <label className="block text-xs text-[#6b6256]">Add a fix</label>
        <input
          type="text"
          value={fixSearch}
          onChange={(event) => setFixSearch(event.target.value)}
          onKeyDown={(event) => {
            if (event.key !== "Enter" || fixSearchMatches.length === 0) {
              return;
            }
            event.preventDefault();
            appendFixFromCatalog(fixSearchMatches[0]);
            setFixSearch("");
          }}
          placeholder="KOI, then Enter"
          className="w-full rounded-xl border border-[#e4d8c4] bg-white px-3 py-2 font-[family-name:var(--font-geist-mono)] text-sm text-[#1c1915] outline-none focus:border-[#c2410c]"
          autoComplete="off"
        />
        {fixNameList.length === 0 ? (
          <p className="text-xs text-[#8a7d6b]">Fix list is still loading.</p>
        ) : fixSearch.trim() && fixSearchMatches.length === 0 ? (
          <p className="text-xs text-[#8a7d6b]">No matching fix names.</p>
        ) : fixSearchMatches.length > 0 ? (
          <ul
            className="max-h-40 overflow-y-auto rounded-xl border border-[#e4d8c4] bg-white font-[family-name:var(--font-geist-mono)] text-sm"
            role="listbox"
          >
            {fixSearchMatches.map((name) => (
              <li
                key={name}
                className="border-b border-[#f4efe4] last:border-b-0"
              >
                <button
                  type="button"
                  className="w-full px-3 py-2 text-left text-[#1c1915] hover:bg-[#f4efe4]"
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
      </div>
      <div className="grid grid-cols-2 gap-3">
        <label>
          <span className="mb-1 block text-xs text-[#6b6256]">
            Cruise altitude (ft)
          </span>
          <input
            type="number"
            value={cruiseAlt}
            onChange={(event) => {
              const altitude = Number(event.target.value);
              setCruiseAlt(altitude);
              if (Number.isFinite(altitude)) {
                commit(waypoints, altitude, cruiseSpd);
              }
            }}
            className="w-full rounded-xl border border-[#e4d8c4] bg-white px-3 py-2 text-sm text-[#1c1915] outline-none focus:border-[#c2410c]"
          />
        </label>
        <label>
          <span className="mb-1 block text-xs text-[#6b6256]">
            Cruise speed (kt)
          </span>
          <input
            type="number"
            value={cruiseSpd}
            onChange={(event) => {
              const speed = Number(event.target.value);
              setCruiseSpd(speed);
              if (Number.isFinite(speed)) {
                commit(waypoints, cruiseAlt, speed);
              }
            }}
            className="w-full rounded-xl border border-[#e4d8c4] bg-white px-3 py-2 text-sm text-[#1c1915] outline-none focus:border-[#c2410c]"
          />
        </label>
      </div>
    </div>
  );
}
