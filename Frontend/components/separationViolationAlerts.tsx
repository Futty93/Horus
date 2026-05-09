"use client";

import React, { useCallback, useEffect, useRef, useState } from "react";
import { GLOBAL_CONSTANTS } from "@/utility/globals/constants";
import {
  fetchConflictAll,
  type RiskAssessmentDto,
} from "@/utility/api/conflict";
import { formatConflictPairLine } from "@/utility/conflict/pairId";
import { findNewIdsInSet } from "@/utility/conflict/violationDiff";

const COOLDOWN_MS = 30_000;
const BANNER_AUTO_HIDE_MS = 10_000;

function mergePairIds(
  violations: Array<{ pairId: string }> | null,
  critical: Array<{ pairId: string }> | null
): Set<string> {
  const s = new Set<string>();
  for (const v of violations ?? []) {
    s.add(v.pairId);
  }
  for (const c of critical ?? []) {
    s.add(c.pairId);
  }
  return s;
}

function pickAlertForPair(
  pairId: string,
  violations: Array<RiskAssessmentDto & { pairId: string }> | null,
  critical: Array<RiskAssessmentDto & { pairId: string }> | null
): (RiskAssessmentDto & { pairId: string }) | null {
  return (
    violations?.find((x) => x.pairId === pairId) ??
    critical?.find((x) => x.pairId === pairId) ??
    null
  );
}

const SeparationViolationAlerts: React.FC = () => {
  const [banner, setBanner] = useState<{
    text: string;
    variant: "violation" | "critical" | "sep";
  } | null>(null);
  const hideTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  const prevMergedPairIdsRef = useRef<Set<string> | null>(null);
  const prevSepCountRef = useRef<number | null>(null);
  const baselineDoneRef = useRef(false);
  const cooldownUntilRef = useRef<Map<string, number>>(new Map());

  const showBanner = useCallback(
    (text: string, variant: "violation" | "critical" | "sep") => {
      if (hideTimerRef.current) {
        clearTimeout(hideTimerRef.current);
      }
      setBanner({ text, variant });
      hideTimerRef.current = setTimeout(() => {
        setBanner(null);
        hideTimerRef.current = null;
      }, BANNER_AUTO_HIDE_MS);
    },
    []
  );

  useEffect(() => {
    let cancelled = false;

    const tick = async () => {
      const allConflicts = await fetchConflictAll();
      if (cancelled) return;
      const entries = Object.entries(allConflicts ?? {});
      const violations = entries
        .filter(([, risk]) => risk.conflictPredicted)
        .map(([pairId, risk]) => ({ pairId, ...risk }));
      const critical = entries
        .filter(([, risk]) => risk.alertLevel === "RED_CONFLICT")
        .map(([pairId, risk]) => ({ pairId, ...risk }));
      const sepCount = violations.length;

      const now = Date.now();
      const merged = mergePairIds(violations, critical);

      if (!baselineDoneRef.current) {
        prevMergedPairIdsRef.current = merged;
        prevSepCountRef.current = sepCount;
        baselineDoneRef.current = true;
        return;
      }

      const prev = prevMergedPairIdsRef.current ?? new Set();
      const newIds = findNewIdsInSet(prev, merged);
      const violationIdSet = new Set((violations ?? []).map((v) => v.pairId));
      const orderedNew = [
        ...newIds.filter((id) => violationIdSet.has(id)),
        ...newIds.filter((id) => !violationIdSet.has(id)),
      ];

      const tryNotifyPair = (
        pairId: string,
        source: "violation" | "critical"
      ): boolean => {
        const until = cooldownUntilRef.current.get(pairId) ?? 0;
        if (now < until) {
          return false;
        }
        const alert = pickAlertForPair(pairId, violations, critical);
        cooldownUntilRef.current.set(pairId, now + COOLDOWN_MS);
        if (alert) {
          showBanner(
            `STCA ${source}: ${pairId} — ${formatConflictPairLine(alert)}`,
            source
          );
        } else {
          showBanner(`STCA ${source}: new pair ${pairId}`, source);
        }
        return true;
      };

      let notified = false;
      for (const id of orderedNew) {
        const inV = violationIdSet.has(id);
        if (tryNotifyPair(id, inV ? "violation" : "critical")) {
          notified = true;
          break;
        }
      }

      const prevSep = prevSepCountRef.current ?? 0;
      const sepIncreased = sepCount > prevSep;

      if (!notified && sepIncreased) {
        const sepKey = "__sep_count__";
        const until = cooldownUntilRef.current.get(sepKey) ?? 0;
        if (now >= until) {
          cooldownUntilRef.current.set(sepKey, now + COOLDOWN_MS);
          showBanner(
            `Separation violation count increased (Sep ${sepCount})`,
            "sep"
          );
        }
      }

      prevMergedPairIdsRef.current = merged;
      prevSepCountRef.current = sepCount;
    };

    void tick();
    const id = setInterval(
      () => void tick(),
      GLOBAL_CONSTANTS.LOCATION_UPDATE_INTERVAL
    );
    return () => {
      cancelled = true;
      clearInterval(id);
      if (hideTimerRef.current) {
        clearTimeout(hideTimerRef.current);
      }
    };
  }, [showBanner]);

  if (!banner) {
    return null;
  }

  const borderClass =
    banner.variant === "critical" ? "border-atc-danger" : "border-atc-warning";

  return (
    <div
      role="alert"
      aria-live="assertive"
      className={`pointer-events-none absolute bottom-4 left-1/2 z-20 max-w-[min(100%-2rem,36rem)] -translate-x-1/2 rounded border px-4 py-2 text-center text-sm font-medium shadow-lg backdrop-blur-sm ${borderClass} bg-atc-surface/95 text-atc-text`}
    >
      {banner.text}
    </div>
  );
};

export default SeparationViolationAlerts;
