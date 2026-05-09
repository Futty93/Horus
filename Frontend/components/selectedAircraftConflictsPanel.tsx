"use client";

import React, { useEffect, useState } from "react";
import { useSelectedAircraft } from "@/context/selectedAircraftContext";
import { GLOBAL_CONSTANTS } from "@/utility/globals/constants";
import {
  fetchAircraftConflicts,
  type ConflictAlertDto,
} from "@/utility/api/conflict";
import {
  formatConflictPairLine,
  getOtherCallsignFromPair,
} from "@/utility/conflict/pairId";

const SelectedAircraftConflictsPanel: React.FC = () => {
  const { callsign } = useSelectedAircraft();
  const [rows, setRows] = useState<ConflictAlertDto[]>([]);

  useEffect(() => {
    if (!callsign) {
      setRows([]);
      return;
    }
    let cancelled = false;
    const tick = async () => {
      const data = await fetchAircraftConflicts(callsign);
      if (!cancelled) {
        setRows(data ?? []);
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
  }, [callsign]);

  if (!callsign) {
    return null;
  }

  return (
    <div className="space-y-1 rounded border border-atc-border bg-atc-surface/50 p-2 text-xs">
      <div className="font-medium text-atc-text-muted">
        STCA pairs (selected)
      </div>
      {rows.length === 0 ? (
        <p className="text-atc-text-muted">
          No pair assessments for this aircraft.
        </p>
      ) : (
        <ul className="space-y-1.5 text-atc-text">
          {rows.map((r) => {
            const label = getOtherCallsignFromPair(r, callsign);
            return (
              <li
                key={`${r.callsignA}-${r.callsignB}`}
                className="leading-snug"
              >
                <span className="font-medium text-atc-warning">{label}</span>
                <span className="text-atc-text-muted"> · </span>
                <span>{formatConflictPairLine(r)}</span>
              </li>
            );
          })}
        </ul>
      )}
    </div>
  );
};

export default SelectedAircraftConflictsPanel;
