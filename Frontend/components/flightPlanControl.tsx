"use client";

import React, { useState } from "react";
import { useSelectedAircraft } from "@/context/selectedAircraftContext";
import { resumeNavigation } from "@/utility/api/flightPlan";
import {
  goAround,
  handOffToTower,
  joinFinalApproach,
} from "@/utility/api/finalApproach";

const FlightPlanControl: React.FC = () => {
  const { callsign } = useSelectedAircraft();
  const [resumeResult, setResumeResult] = useState<
    "idle" | "success" | "error"
  >("idle");
  const [airportIcao, setAirportIcao] = useState("RJTT");
  const [runwayDesignator, setRunwayDesignator] = useState("16R");
  const [finalResult, setFinalResult] = useState<"idle" | "success" | "error">(
    "idle"
  );
  const [climbAltitudeFt, setClimbAltitudeFt] = useState("3000");
  const [goAroundResult, setGoAroundResult] = useState<
    "idle" | "success" | "error"
  >("idle");
  const [goAroundMessage, setGoAroundMessage] = useState("");
  const [towerResult, setTowerResult] = useState<"idle" | "success" | "error">(
    "idle"
  );
  const [towerMessage, setTowerMessage] = useState("");

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

      <div className="border-t border-atc-border pt-3 space-y-2">
        <div className="text-xs font-semibold text-atc-text-muted">
          FINAL APPROACH (extension intercept)
        </div>
        <div className="flex gap-2">
          <input
            className="flex-1 px-2 py-1 text-xs font-mono bg-atc-surface-elevated border border-atc-border rounded"
            value={airportIcao}
            onChange={(e) => setAirportIcao(e.target.value.toUpperCase())}
            placeholder="ICAO"
          />
          <input
            className="w-20 px-2 py-1 text-xs font-mono bg-atc-surface-elevated border border-atc-border rounded"
            value={runwayDesignator}
            onChange={(e) => setRunwayDesignator(e.target.value.toUpperCase())}
            placeholder="RWY"
          />
        </div>
        <button
          type="button"
          onClick={async () => {
            if (!callsign) return;
            setFinalResult("idle");
            const ok = await joinFinalApproach(
              callsign,
              airportIcao,
              runwayDesignator
            );
            setFinalResult(ok ? "success" : "error");
            if (ok) setTimeout(() => setFinalResult("idle"), 2000);
          }}
          className="w-full px-4 py-2 bg-atc-surface-elevated text-atc-text font-bold text-xs rounded border border-atc-accent hover:bg-atc-accent hover:text-white"
        >
          JOIN FINAL
        </button>
        {finalResult === "success" && (
          <p className="text-xs text-atc-accent">
            Final approach intercept set
          </p>
        )}
        {finalResult === "error" && (
          <p className="text-xs text-atc-danger">Failed to join final</p>
        )}
        <div className="text-xs font-semibold text-atc-text-muted pt-2">
          GO AROUND (runway heading climb)
        </div>
        <input
          className="w-full px-2 py-1 text-xs font-mono bg-atc-surface-elevated border border-atc-border rounded"
          value={climbAltitudeFt}
          onChange={(e) => setClimbAltitudeFt(e.target.value)}
          inputMode="numeric"
          placeholder="Climb altitude ft"
        />
        <button
          type="button"
          onClick={async () => {
            if (!callsign) return;
            const altitude = Number(climbAltitudeFt);
            if (!Number.isFinite(altitude)) {
              setGoAroundResult("error");
              setGoAroundMessage("Enter a climb altitude in feet");
              return;
            }
            setGoAroundResult("idle");
            const result = await goAround(
              callsign,
              airportIcao,
              runwayDesignator,
              altitude
            );
            setGoAroundResult(result.ok ? "success" : "error");
            setGoAroundMessage(result.message ?? "");
            if (result.ok) setTimeout(() => setGoAroundResult("idle"), 2000);
          }}
          className="w-full px-4 py-2 bg-atc-surface-elevated text-atc-text font-bold text-xs rounded border border-atc-danger hover:bg-atc-danger hover:text-white"
        >
          GO AROUND
        </button>
        {goAroundResult === "success" && (
          <p className="text-xs text-atc-accent">Go-around set</p>
        )}
        {goAroundResult === "error" && (
          <p className="text-xs text-atc-danger">
            {goAroundMessage || "Failed to go around"}
          </p>
        )}
        <div className="text-xs font-semibold text-atc-text-muted pt-2">
          TOWER (established on final, within 10 NM)
        </div>
        <button
          type="button"
          onClick={async () => {
            if (!callsign) return;
            setTowerResult("idle");
            const result = await handOffToTower(
              callsign,
              airportIcao,
              runwayDesignator
            );
            setTowerResult(result.ok ? "success" : "error");
            setTowerMessage(result.message ?? "");
            if (result.ok) setTimeout(() => setTowerResult("idle"), 2000);
          }}
          className="w-full px-4 py-2 bg-atc-surface-elevated text-atc-text font-bold text-xs rounded border border-atc-accent hover:bg-atc-accent hover:text-white"
        >
          HAND OFF TO TOWER
        </button>
        {towerResult === "success" && (
          <p className="text-xs text-atc-accent">Handed off to the tower</p>
        )}
        {towerResult === "error" && (
          <p className="text-xs text-atc-danger">
            {towerMessage || "Failed to hand off to the tower"}
          </p>
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
