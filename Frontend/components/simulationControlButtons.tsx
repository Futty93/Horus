"use client";

import React, { useCallback } from "react";

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
  const onStart = useCallback(() => handleSimulationAction("start"), []);
  const onPause = useCallback(() => handleSimulationAction("pause"), []);

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
      </div>
    </div>
  );
};

export default SimulationControlButtons;
