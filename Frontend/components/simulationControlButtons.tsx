"use client";

import React, { useCallback, useEffect, useState } from "react";
import {
  SIMULATION_SPEED_PRESETS,
  fetchSimulationSpeed,
  isSameSpeedMultiplier,
  setSimulationSpeed,
  type SimulationSpeed,
} from "@/utility/api/simulation";

async function handleSimulationAction(
  action: "start" | "pause"
): Promise<void> {
  try {
    const response = await fetch(`/api/simulation/${action}`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
    });
    if (response.ok) {
      console.log(`${action === "start" ? "Start" : "Pause"} successful`);
    } else {
      console.error(`Failed to ${action} simulation. Status:`, response.status);
    }
  } catch (error) {
    console.error(
      `Error occurred while trying to ${action} simulation:`,
      error
    );
  }
}

const SimulationControlButtons: React.FC = () => {
  const [speed, setSpeed] = useState<SimulationSpeed | null>(null);
  const [speedLoading, setSpeedLoading] = useState(true);
  const [speedApplying, setSpeedApplying] = useState(false);

  const refreshSpeed = useCallback(async () => {
    setSpeedLoading(true);
    const next = await fetchSimulationSpeed();
    setSpeed(next);
    setSpeedLoading(false);
    if (!next) {
      console.error("Could not load simulation speed");
    }
  }, []);

  useEffect(() => {
    void refreshSpeed();
  }, [refreshSpeed]);

  const onStart = useCallback(() => handleSimulationAction("start"), []);
  const onPause = useCallback(() => handleSimulationAction("pause"), []);

  const onSelectPreset = useCallback(async (multiplier: number) => {
    setSpeedApplying(true);
    const next = await setSimulationSpeed(multiplier);
    setSpeedApplying(false);
    if (next) {
      setSpeed(next);
    } else {
      console.error("Could not set simulation speed");
    }
  }, []);

  const mult = speed?.speedMultiplier;
  const tickMs = speed?.tickIntervalWallMs;

  return (
    <div className="bg-atc-surface border border-atc-border rounded-lg p-3">
      <h3 className="text-xs font-bold text-atc-text font-mono tracking-wider mb-2 text-center">
        SIMULATION CONTROL
      </h3>
      <div className="flex flex-col space-y-2">
        <button
          type="button"
          onClick={onStart}
          className="w-full px-3 py-2 bg-atc-accent text-white font-bold text-sm
                     rounded border border-transparent font-mono tracking-wider
                     transition-colors duration-200
                     hover:bg-atc-accent-hover
                     focus:outline-none focus:ring-2 focus:ring-atc-accent focus:ring-offset-2 focus:ring-offset-atc-bg"
        >
          START SIMULATION
        </button>

        <div className="flex space-x-2">
          <button
            type="button"
            onClick={onPause}
            className="flex-1 px-3 py-2 bg-atc-warning text-white font-bold text-xs
                       rounded border border-transparent font-mono tracking-wider
                       transition-opacity duration-200
                       hover:opacity-90
                       focus:outline-none focus:ring-2 focus:ring-atc-warning focus:ring-offset-2 focus:ring-offset-atc-bg"
          >
            PAUSE
          </button>

          <button
            type="button"
            id="resetButton"
            className="flex-1 px-3 py-2 bg-atc-danger text-white font-bold text-xs
                       rounded border border-transparent font-mono tracking-wider
                       transition-opacity duration-200
                       hover:opacity-90
                       focus:outline-none focus:ring-2 focus:ring-atc-danger focus:ring-offset-2 focus:ring-offset-atc-bg"
          >
            RESET
          </button>
        </div>

        <div className="pt-1 border-t border-atc-border">
          <div className="text-[10px] font-mono text-atc-text/80 tracking-wider mb-1.5 text-center">
            TIME SCALE
          </div>
          <div className="text-[10px] font-mono text-atc-text/70 text-center mb-2 space-x-2">
            <span>
              {speedLoading ? "…" : mult !== undefined ? `${mult}×` : "—"}
            </span>
            <span className="text-atc-border">|</span>
            <span>
              {speedLoading
                ? "…"
                : tickMs !== undefined
                  ? `${tickMs} ms/tick`
                  : "—"}
            </span>
          </div>
          <div className="grid grid-cols-3 gap-1.5">
            {SIMULATION_SPEED_PRESETS.map((preset) => {
              const active =
                mult !== undefined && isSameSpeedMultiplier(mult, preset);
              return (
                <button
                  key={preset}
                  type="button"
                  disabled={speedApplying}
                  onClick={() => void onSelectPreset(preset)}
                  className={`px-1.5 py-1.5 text-[10px] font-mono font-bold tracking-wider rounded border transition-colors duration-150
                    focus:outline-none focus:ring-2 focus:ring-atc-accent focus:ring-offset-1 focus:ring-offset-atc-bg
                    disabled:opacity-50 disabled:cursor-not-allowed
                    ${
                      active
                        ? "bg-atc-accent/25 text-atc-text border-atc-accent"
                        : "bg-atc-bg text-atc-text/90 border-atc-border hover:border-atc-accent/60"
                    }`}
                >
                  {`${preset}×`}
                </button>
              );
            })}
          </div>
        </div>
      </div>
    </div>
  );
};

export default SimulationControlButtons;
