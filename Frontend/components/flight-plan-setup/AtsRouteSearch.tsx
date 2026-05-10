"use client";

import React, { useMemo, useState } from "react";
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
  const [query, setQuery] = useState("");
  const origin = originIcao.toUpperCase();
  const dest = destinationIcao.toUpperCase();
  const q = query.trim().toUpperCase();

  const matches = useMemo(() => {
    const raw = [...atsRoutes.atsLowerRoutes, ...atsRoutes.rnavRoutes].filter(
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
    const filtered = q
      ? raw.filter((r) => {
          const n = r.name.toUpperCase();
          const d = (r.description ?? "").toUpperCase();
          return n.includes(q) || d.includes(q);
        })
      : raw;
    return [...filtered].sort((a, b) =>
      a.name.localeCompare(b.name, undefined, { sensitivity: "base" })
    );
  }, [atsRoutes.atsLowerRoutes, atsRoutes.rnavRoutes, origin, dest, q]);

  const toWaypoints = (r: Route) => r.points.map((p) => p.name);

  if (matches.length === 0) {
    return (
      <div className="space-y-2">
        <label className="block text-xs text-atc-text-muted">
          <span className="block mb-1">Filter routes</span>
          <input
            type="text"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder="Route name or description…"
            className="w-full px-2 py-1 text-sm bg-atc-bg border border-atc-border rounded
                       text-atc-text focus:outline-none focus:border-atc-accent"
          />
        </label>
        <p className="text-xs text-atc-text-muted">
          No ATS routes match. Enter waypoints manually (e.g. KOITO, BOKJO,
          AOIKU for T09).
        </p>
      </div>
    );
  }

  return (
    <div className="space-y-2">
      <label className="block text-xs text-atc-text-muted">
        <span className="block mb-1">Filter routes</span>
        <input
          type="text"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          placeholder="Route name or description…"
          className="w-full px-2 py-1 text-sm bg-atc-bg border border-atc-border rounded
                     text-atc-text focus:outline-none focus:border-atc-accent"
        />
      </label>
      <p className="text-xs text-atc-text-muted">
        Select a route ({matches.length} shown):
      </p>
      <div className="flex flex-wrap gap-2 max-h-48 overflow-y-auto pr-1">
        {matches.map((r, idx) => {
          const wps = toWaypoints(r);
          return (
            <button
              key={`${r.name}-${idx}`}
              type="button"
              onClick={() => onSelect(wps)}
              className="px-2 py-1 text-xs bg-atc-surface-elevated border border-atc-border rounded
                         hover:border-atc-accent text-atc-text text-left max-w-full"
            >
              {r.name}: {wps.slice(0, 3).join(" → ")}
              {wps.length > 3 ? " …" : ""}
            </button>
          );
        })}
      </div>
    </div>
  );
}
