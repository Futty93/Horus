"use client";

import React, { useCallback, useState } from "react";
import { OdGroupSection } from "./OdGroupSection";
import type { ScenarioAircraft } from "@/types/scenario";
import type { Route } from "@/utility/AtsRouteManager/RouteInterfaces/Route";

type RouteDef = {
  waypoints: string[];
  cruiseAltitude: number;
  cruiseSpeed: number;
};

function flightPlanRouteSignature(a: ScenarioAircraft): string {
  const fp = a.flightPlan;
  return `${fp.route.map((w) => w.fix).join(",")}|${fp.cruiseAltitude}|${fp.cruiseSpeed}`;
}

function routesDivergeInGroup(aircraft: ScenarioAircraft[]): boolean {
  if (aircraft.length <= 1) return false;
  const s0 = flightPlanRouteSignature(aircraft[0]);
  return aircraft.some((ac) => flightPlanRouteSignature(ac) !== s0);
}

function odKey(origin: string, destination: string): string {
  return `${origin}→${destination}`;
}

interface OdGroupListProps {
  odPairs: {
    origin: string;
    destination: string;
    aircraft: ScenarioAircraft[];
  }[];
  selectedCallsign: string | null;
  onSelectAircraft: (a: ScenarioAircraft) => void;
  onSuggestStatus?: (status: string | null) => void;
  atsRoutes: { atsLowerRoutes: Route[]; rnavRoutes: Route[] };
  routeByOd: Map<string, RouteDef>;
  onRouteChange: (key: string, route: RouteDef) => void;
}

export function OdGroupList({
  odPairs,
  selectedCallsign,
  onSelectAircraft,
  onSuggestStatus,
  atsRoutes,
  routeByOd,
  onRouteChange,
}: OdGroupListProps) {
  const [expandedByKey, setExpandedByKey] = useState<
    Record<string, boolean | undefined>
  >({});

  const expandAll = useCallback(() => {
    const next: Record<string, boolean | undefined> = {};
    for (const p of odPairs) {
      next[odKey(p.origin, p.destination)] = true;
    }
    setExpandedByKey(next);
  }, [odPairs]);

  const collapseAll = useCallback(() => {
    setExpandedByKey({});
  }, []);

  return (
    <section>
      <div className="flex flex-wrap items-center justify-between gap-2 mb-3">
        <h2 className="text-xs font-semibold uppercase tracking-[0.16em] text-[#8a7d6b]">
          Same airports, one path
        </h2>
        <div className="flex flex-wrap gap-2">
          <button
            type="button"
            onClick={expandAll}
            className="text-xs text-[#6b6256] underline-offset-4 hover:underline"
          >
            Expand all
          </button>
          <button
            type="button"
            onClick={collapseAll}
            className="text-xs text-[#6b6256] underline-offset-4 hover:underline"
          >
            Collapse all
          </button>
        </div>
      </div>
      <p className="mb-3 text-sm text-[#6b6256]">
        Overwrites every flight that shares this origin and destination. Each
        flight’s own route is edited above.
      </p>
      <div className="space-y-3">
        {odPairs.map((pair) => {
          const key = odKey(pair.origin, pair.destination);
          const expanded = expandedByKey[key] === true;
          return (
            <OdGroupSection
              key={key}
              origin={pair.origin}
              destination={pair.destination}
              aircraft={pair.aircraft}
              routesDiverge={routesDivergeInGroup(pair.aircraft)}
              expanded={expanded}
              onToggleExpanded={() =>
                setExpandedByKey((m) => ({ ...m, [key]: !expanded }))
              }
              selectedCallsign={selectedCallsign}
              onSelectAircraft={onSelectAircraft}
              onSuggestStatus={onSuggestStatus}
              atsRoutes={atsRoutes}
              routeByOd={routeByOd}
              onRouteChange={onRouteChange}
            />
          );
        })}
      </div>
    </section>
  );
}
