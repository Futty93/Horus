"use client";

import React, { useState } from "react";
import { moveItem } from "@/utility/flightPlanSetup/routeEdits";

interface RouteFixListProps {
  waypoints: readonly string[];
  onChange: (waypoints: string[]) => void;
}

export function RouteFixList({ waypoints, onChange }: RouteFixListProps) {
  const [dragFrom, setDragFrom] = useState<number | null>(null);

  if (waypoints.length === 0) {
    return (
      <p className="rounded-2xl border border-dashed border-[#d9cdb8] px-3 py-4 text-sm text-[#8a7d6b]">
        No fixes between the airports yet.
      </p>
    );
  }

  return (
    <ol className="space-y-1">
      {waypoints.map((fix, index) => (
        <li
          key={`${fix}-${index}`}
          draggable
          onDragStart={() => setDragFrom(index)}
          onDragOver={(event) => event.preventDefault()}
          onDrop={() => {
            if (dragFrom == null) return;
            onChange(moveItem(waypoints, dragFrom, index));
            setDragFrom(null);
          }}
          className="flex items-center gap-2 rounded-2xl bg-white px-3 py-2 shadow-[0_1px_0_rgba(28,25,21,0.06)]"
        >
          <span className="w-5 text-xs tabular-nums text-[#8a7d6b]">
            {index + 1}
          </span>
          <span className="flex-1 font-[family-name:var(--font-geist-mono)] text-sm text-[#1c1915]">
            {fix}
          </span>
          <button
            type="button"
            aria-label={`Move ${fix} earlier`}
            disabled={index === 0}
            onClick={() => onChange(moveItem(waypoints, index, index - 1))}
            className="text-xs text-[#8a7d6b] hover:text-[#1c1915] disabled:opacity-30"
          >
            Up
          </button>
          <button
            type="button"
            aria-label={`Move ${fix} later`}
            disabled={index === waypoints.length - 1}
            onClick={() => onChange(moveItem(waypoints, index, index + 1))}
            className="text-xs text-[#8a7d6b] hover:text-[#1c1915] disabled:opacity-30"
          >
            Down
          </button>
          <button
            type="button"
            aria-label={`Remove ${fix}`}
            onClick={() =>
              onChange(waypoints.filter((_, itemIndex) => itemIndex !== index))
            }
            className="text-xs text-[#c2410c] hover:underline"
          >
            Remove
          </button>
        </li>
      ))}
    </ol>
  );
}
