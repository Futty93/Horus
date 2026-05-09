"use client";

import React, { useCallback, useEffect, useRef, useState } from "react";
import { GLOBAL_CONSTANTS } from "@/utility/globals/constants";
import {
  fetchConflictAll,
  type ConflictAlertDto,
} from "@/utility/api/conflict";
import { formatConflictPairLine } from "@/utility/conflict/pairId";
import { findNewIdsInSet } from "@/utility/conflict/violationDiff";

const COOLDOWN_MS = 30_000;
const BANNER_AUTO_HIDE_MS = 10_000;

function toPairKey(alert: { callsignA: string; callsignB: string }): string {
  return `${alert.callsignA}::${alert.callsignB}`;
}

function mergePairIds(
  violations: ConflictAlertDto[] | null,
  critical: ConflictAlertDto[] | null
): Set<string> {
  const s = new Set<string>();
  for (const v of violations ?? []) {
    s.add(toPairKey(v));
  }
  for (const c of critical ?? []) {
    s.add(toPairKey(c));
  }
  return s;
}

function pickAlertForPair(
  pairKey: string,
  violations: ConflictAlertDto[] | null,
  critical: ConflictAlertDto[] | null
): ConflictAlertDto | null {
  return (
    violations?.find((x) => toPairKey(x) === pairKey) ??
    critical?.find((x) => toPairKey(x) === pairKey) ??
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
      if (allConflicts == null) {
        return;
      }
      const rows = allConflicts;
      const violations = rows.filter((risk) => risk.conflictPredicted);
      const critical = rows.filter(
        (risk) => risk.alertLevel === "RED_CONFLICT"
      );
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
      const violationIdSet = new Set(
        (violations ?? []).map((v) => toPairKey(v))
      );
      const orderedNew = [
        ...newIds.filter((id) => violationIdSet.has(id)),
        ...newIds.filter((id) => !violationIdSet.has(id)),
      ];

      const tryNotifyPair = (
        pairKey: string,
        source: "violation" | "critical"
      ): boolean => {
        const until = cooldownUntilRef.current.get(pairKey) ?? 0;
        if (now < until) {
          return false;
        }
        const alert = pickAlertForPair(pairKey, violations, critical);
        cooldownUntilRef.current.set(pairKey, now + COOLDOWN_MS);
        if (alert) {
          showBanner(
            `STCA ${source}: ${alert.callsignA}-${alert.callsignB} — ${formatConflictPairLine(alert)}`,
            source
          );
        } else {
          showBanner(`STCA ${source}: new pair ${pairKey}`, source);
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
