"use client";

import React, { useEffect, useState } from "react";
import { useSelectedAircraft } from "@/context/selectedAircraftContext";
import {
  fetchFlightPlan,
  type FlightPlanStatus,
} from "@/utility/api/flightPlan";

const FlightPlanDisplay: React.FC = () => {
  const { callsign } = useSelectedAircraft();
  const [status, setStatus] = useState<FlightPlanStatus | null>(null);
  const [loading, setLoading] = useState(false);

  const refresh = React.useCallback(() => {
    if (!callsign) return;
    fetchFlightPlan(callsign).then(setStatus);
  }, [callsign]);

  useEffect(() => {
    if (!callsign) {
      setStatus(null);
      return;
    }
    setStatus(null);
    let cancelled = false;
    setLoading(true);
    fetchFlightPlan(callsign)
      .then((data) => {
        if (!cancelled) setStatus(data);
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, [callsign]);

  useEffect(() => {
    if (!callsign) return;
    const interval = setInterval(refresh, 5000);
    return () => clearInterval(interval);
  }, [callsign, refresh]);

  if (!callsign) {
    return (
      <div className="bg-atc-surface border border-atc-border rounded-lg p-4 mt-4">
        <p className="text-sm text-atc-text-muted">
          Select aircraft to view flight plan
        </p>
      </div>
    );
  }

  if (loading || !status) {
    return (
      <div className="bg-atc-surface border border-atc-border rounded-lg p-4 mt-4">
        <p className="text-sm text-atc-text-muted">
          {loading ? "Loading..." : "No flight plan"}
        </p>
      </div>
    );
  }

  const waypoints = status.remainingWaypoints ?? [];
  const hasPlan = status.hasFlightPlan !== false && waypoints.length > 0;

  return (
    <div className="bg-atc-surface border border-atc-border rounded-lg p-4 mt-4">
      <div className="text-xs font-semibold text-atc-text mb-2">
        FLIGHT PLAN — {status.navigationMode}
      </div>
      {hasPlan ? (
        <>
          {(status.departureAirport || status.arrivalAirport) && (
            <p className="text-xs text-atc-text-muted mb-2">
              {status.departureAirport} → {status.arrivalAirport}
            </p>
          )}
          {status.currentWaypoint && (
            <p className="text-xs text-atc-accent mb-1">
              Current:{" "}
              <span className="font-mono">{status.currentWaypoint}</span>
            </p>
          )}
          {waypoints.length > 0 && (
            <div className="text-xs font-mono text-atc-text space-y-0.5 max-h-24 overflow-y-auto">
              {waypoints.map((wp, i) => (
                <div key={`${wp}-${i}`} className="flex items-center gap-2">
                  <span className="text-atc-text-muted">{i + 1}.</span>
                  <span>{wp}</span>
                </div>
              ))}
            </div>
          )}
        </>
      ) : (
        <p className="text-xs text-atc-text-muted">No active flight plan</p>
      )}
    </div>
  );
};

export default FlightPlanDisplay;
