"use client";

import React, { useEffect, useState } from "react";
import { GLOBAL_CONSTANTS } from "@/utility/globals/constants";
import {
  fetchConflictStatistics,
  type ConflictStatisticsDto,
} from "@/utility/api/conflict";

const ConflictSummaryStrip: React.FC = () => {
  const [stats, setStats] = useState<ConflictStatisticsDto | null>(null);

  useEffect(() => {
    let cancelled = false;
    const tick = async () => {
      const next = await fetchConflictStatistics();
      if (!cancelled) {
        setStats(next);
      }
    };
    void tick();
    const id = setInterval(
      () => void tick(),
      GLOBAL_CONSTANTS.LOCATION_UPDATE_INTERVAL
    );
    return () => {
      cancelled = true;
      clearInterval(id);
    };
  }, []);

  if (stats == null) {
    return null;
  }

  return (
    <div
      className="pointer-events-none absolute left-3 top-3 z-10 max-w-[min(100%-1.5rem,24rem)] rounded border border-atc-border bg-atc-surface/90 px-3 py-1.5 text-xs text-atc-text shadow-sm backdrop-blur-sm"
      aria-live="polite"
    >
      <span className="text-atc-text-muted">STCA</span>{" "}
      <span className="font-medium text-atc-danger">
        R {stats.redConflictCount}
      </span>
      <span className="text-atc-text-muted"> · </span>
      <span className="font-medium text-atc-warning">
        W {stats.whiteConflictCount}
      </span>
      <span className="text-atc-text-muted"> · </span>
      <span>Sep {stats.separationViolationCount}</span>
      <span className="text-atc-text-muted"> · </span>
      <span className="text-atc-text-muted">pairs {stats.totalConflicts}</span>
    </div>
  );
};

export default ConflictSummaryStrip;
