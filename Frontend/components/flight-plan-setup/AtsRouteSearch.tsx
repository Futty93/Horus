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
        <label className="block text-xs text-[#6b6256]">
          <span className="block mb-1">Filter routes</span>
          <input
            type="text"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder="Route name or description…"
            className="w-full rounded-xl border border-[#e4d8c4] bg-white px-3 py-2 text-sm text-[#1c1915] outline-none focus:border-[#c2410c]"
          />
        </label>
        <p className="text-xs text-[#8a7d6b]">
          No ATS routes match. Enter waypoints manually (e.g. KOITO, BOKJO,
          AOIKU for T09).
        </p>
      </div>
    );
  }

  return (
    <div className="space-y-2">
      <label className="block text-xs text-[#6b6256]">
        <span className="block mb-1">Filter routes</span>
        <input
          type="text"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          placeholder="Route name or description…"
          className="w-full rounded-xl border border-[#e4d8c4] bg-white px-3 py-2 text-sm text-[#1c1915] outline-none focus:border-[#c2410c]"
        />
      </label>
      <p className="text-xs text-[#8a7d6b]">
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
              className="max-w-full rounded-full bg-white px-3 py-1 text-left text-xs text-[#1c1915] hover:bg-[#1c1915] hover:text-[#f4efe4]"
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
