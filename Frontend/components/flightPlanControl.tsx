"use client";

import React, { useState } from "react";
import { useSelectedAircraft } from "@/context/selectedAircraftContext";
import { directToFix, resumeNavigation } from "@/utility/api/flightPlan";

const FlightPlanControl: React.FC = () => {
  const { callsign } = useSelectedAircraft();
  const [fixName, setFixName] = useState("");
  const [resumeAfterDirect, setResumeAfterDirect] = useState(false);
  const [directResult, setDirectResult] = useState<"idle" | "success" | "error">("idle");
  const [resumeResult, setResumeResult] = useState<"idle" | "success" | "error">("idle");

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
      <div className="bg-control-gradient border border-matrix-accent rounded-cyber-lg p-4 backdrop-blur-sm mt-4">
        <p className="text-sm text-gray-500">Select aircraft to issue navigation commands</p>
      </div>
    );
  }

  return (
    <div className="bg-control-gradient border border-matrix-accent rounded-cyber-lg p-4 backdrop-blur-sm mt-4 space-y-4">
      <div className="text-xs font-semibold text-radar-primary mb-2">
        NAVIGATION COMMANDS
      </div>

      <div className="space-y-2">
        <label className="block text-xs text-gray-400">Direct to Fix</label>
        <input
          type="text"
          value={fixName}
          onChange={(e) => setFixName(e.target.value.toUpperCase())}
          placeholder="FIX name (e.g. ABENO)"
          className="w-full px-3 py-2 bg-matrix-dark border border-matrix-accent rounded-cyber
                     text-white text-sm font-mono placeholder-gray-500
                     focus:outline-none focus:border-radar-primary"
        />
        <label className="flex items-center gap-2 text-xs text-gray-400 cursor-pointer">
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
          className="w-full px-4 py-2 bg-button-gradient text-matrix-dark font-bold text-xs rounded-cyber
                     hover:opacity-90 disabled:opacity-50 disabled:cursor-not-allowed
                     transition-all duration-300"
        >
          DIRECT TO
        </button>
        {directResult === "success" && (
          <p className="text-xs text-radar-primary">Direct to applied</p>
        )}
        {directResult === "error" && (
          <p className="text-xs text-red-400">Failed to apply direct to</p>
        )}
      </div>

      <div className="border-t border-matrix-accent pt-3">
        <button
          onClick={handleResume}
          className="w-full px-4 py-2 bg-cyber-gradient text-radar-primary font-bold text-xs rounded-cyber
                     border border-radar-primary/50
                     hover:bg-radar-primary/10 transition-all duration-300"
        >
          RESUME OWN NAVIGATION
        </button>
        {resumeResult === "success" && (
          <p className="text-xs text-radar-primary mt-1">Resume applied</p>
        )}
        {resumeResult === "error" && (
          <p className="text-xs text-red-400 mt-1">Failed to resume</p>
        )}
      </div>
    </div>
  );
};

export default FlightPlanControl;
