"use client";

import React from "react";
import type { Route } from "@/utility/AtsRouteManager/RouteInterfaces/Route";

interface AtsRouteSearchProps {
  originIcao: string;
  destinationIcao: string;
  atsRoutes: { atsLowerRoutes: Route[]; rnavRoutes: Route[] };
  onSelect: (waypoints: string[]) => void;
}

export function AtsRouteSearch({
  originIcao,
  destinationIcao,
  atsRoutes,
  onSelect,
}: AtsRouteSearchProps) {
  const origin = originIcao.toUpperCase();
  const dest = destinationIcao.toUpperCase();

  const matches = [...atsRoutes.atsLowerRoutes, ...atsRoutes.rnavRoutes].filter(
    (r) => {
      const n = r.name.toUpperCase();
      const d = (r.description ?? "").toUpperCase();
      return (
        n.includes(origin) ||
        n.includes(dest) ||
        d.includes(origin) ||
        d.includes(dest)
      );
    }
  );

  const toWaypoints = (r: Route) => r.points.map((p) => p.name);

  if (matches.length === 0) {
    return (
      <p className="text-xs text-atc-text-muted">
        No ATS routes match. Enter waypoints manually (e.g. KOITO, BOKJO, AOIKU
        for T09).
      </p>
    );
  }

  return (
    <div className="space-y-2">
      <p className="text-xs text-atc-text-muted">Select a route:</p>
      <div className="flex flex-wrap gap-2">
        {matches.slice(0, 5).map((r) => (
          <button
            key={r.name}
            type="button"
            onClick={() => onSelect(toWaypoints(r))}
            className="px-2 py-1 text-xs bg-atc-surface-elevated border border-atc-border rounded
                       hover:border-atc-accent text-atc-text"
          >
            {r.name}: {toWaypoints(r).slice(0, 3).join(" → ")}
            {toWaypoints(r).length > 3 ? " …" : ""}
          </button>
        ))}
      </div>
    </div>
  );
}
