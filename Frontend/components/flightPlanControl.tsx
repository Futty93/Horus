"use client";

import React, { useState } from "react";
import { useSelectedAircraft } from "@/context/selectedAircraftContext";
import { directToFix, resumeNavigation } from "@/utility/api/flightPlan";

const FlightPlanControl: React.FC = () => {
  const { callsign } = useSelectedAircraft();
  const [fixName, setFixName] = useState("");
  const [resumeAfterDirect, setResumeAfterDirect] = useState(false);
  const [directResult, setDirectResult] = useState<
    "idle" | "success" | "error"
  >("idle");
  const [resumeResult, setResumeResult] = useState<
    "idle" | "success" | "error"
  >("idle");

  const handleDirectTo = async () => {
    if (!callsign || !fixName.trim()) return;
    setDirectResult("idle");
    const ok = await directToFix(callsign, fixName.trim(), resumeAfterDirect);
    setDirectResult(ok ? "success" : "error");
    if (ok) setTimeout(() => setDirectResult("idle"), 2000);
  };

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

      <div className="space-y-2">
        <label className="block text-xs text-atc-text-muted">
          Direct to Fix
        </label>
        <input
          type="text"
          value={fixName}
          onChange={(e) => setFixName(e.target.value.toUpperCase())}
          placeholder="FIX name (e.g. ABENO)"
          className="w-full px-3 py-2 bg-atc-surface-elevated border border-atc-border rounded
                     text-atc-text text-sm font-mono placeholder-atc-text-muted
                     focus:outline-none focus:border-atc-accent"
        />
        <label className="flex items-center gap-2 text-xs text-atc-text-muted cursor-pointer">
          <input
            type="checkbox"
            checked={resumeAfterDirect}
            onChange={(e) => setResumeAfterDirect(e.target.checked)}
          />
          Resume flight plan after reaching fix
        </label>
        <button
          onClick={handleDirectTo}
          disabled={!fixName.trim()}
          className="w-full px-4 py-2 bg-atc-accent text-white font-bold text-xs rounded
                     hover:opacity-90 disabled:opacity-50 disabled:cursor-not-allowed
                     transition-opacity duration-200"
        >
          DIRECT TO
        </button>
        {directResult === "success" && (
          <p className="text-xs text-atc-accent">Direct to applied</p>
        )}
        {directResult === "error" && (
          <p className="text-xs text-atc-danger">Failed to apply direct to</p>
        )}
      </div>

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
