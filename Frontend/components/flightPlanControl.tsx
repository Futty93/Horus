"use client";

import React, { useState } from "react";
import { useSelectedAircraft } from "@/context/selectedAircraftContext";
import { resumeNavigation } from "@/utility/api/flightPlan";

const FlightPlanControl: React.FC = () => {
  const { callsign } = useSelectedAircraft();
  const [resumeResult, setResumeResult] = useState<
    "idle" | "success" | "error"
  >("idle");

  const handleResume = async () => {
    if (!callsign) return;
    setResumeResult("idle");
    const ok = await resumeNavigation(callsign);
    setResumeResult(ok ? "success" : "error");
    if (ok) setTimeout(() => setResumeResult("idle"), 2000);
  };

  if (!callsign) {
    return (
      <div className="bg-atc-surface border border-atc-border rounded-lg p-4 mt-4">
        <p className="text-sm text-atc-text-muted">
          Select aircraft to issue navigation commands
        </p>
      </div>
    );
  }

  return (
    <div className="bg-atc-surface border border-atc-border rounded-lg p-4 mt-4 space-y-4">
      <div className="text-xs font-semibold text-atc-text mb-2">
        NAVIGATION COMMANDS
      </div>

      <p className="text-xs text-atc-text-muted">
        Use <span className="font-semibold">DIRECT TO FIX</span> panel to select
        a fix and choose Direct/Hold.
      </p>

      <div className="border-t border-atc-border pt-3">
        <button
          onClick={handleResume}
          className="w-full px-4 py-2 bg-atc-surface-elevated text-atc-text font-bold text-xs rounded
                     border border-atc-accent
                     hover:bg-atc-accent hover:text-white transition-colors duration-200"
        >
          RESUME OWN NAVIGATION
        </button>
        {resumeResult === "success" && (
          <p className="text-xs text-atc-accent mt-1">Resume applied</p>
        )}
        {resumeResult === "error" && (
          <p className="text-xs text-atc-danger mt-1">Failed to resume</p>
        )}
      </div>
    </div>
  );
};

export default FlightPlanControl;
