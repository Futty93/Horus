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
    <div className="bg-control-gradient border border-matrix-accent rounded-cyber-lg p-3 backdrop-blur-sm">
      <h3 className="text-xs font-bold text-radar-primary font-mono tracking-wider mb-2 text-center">
        SIMULATION CONTROL
      </h3>
      <div className="flex flex-col space-y-2">
        <button
          type="button"
          onClick={onStart}
          className="w-full px-3 py-2 bg-button-gradient text-matrix-dark font-bold text-sm
                     rounded-cyber border border-transparent font-mono tracking-wider
                     transition-all duration-300 ease-out transform
                     hover:bg-button-hover-gradient hover:scale-105 hover:shadow-cyber-lg
                     active:scale-95 active:shadow-inset-cyber
                     focus:outline-none focus:ring-2 focus:ring-radar-primary focus:ring-offset-2 focus:ring-offset-matrix-dark
                     relative overflow-hidden group animate-glow"
        >
          <span className="relative z-10">START SIMULATION</span>
          <div
            className="absolute inset-0 bg-gradient-to-r from-transparent via-white/10 to-transparent
                        transform translate-x-[-100%] group-hover:animate-scan transition-transform duration-500"
          />
        </button>

        <div className="flex space-x-2">
          <button
            type="button"
            onClick={onPause}
            className="flex-1 px-3 py-2 bg-warning-gradient text-matrix-dark font-bold text-xs
                       rounded-cyber border border-transparent font-mono tracking-wider
                       transition-all duration-300 ease-out transform
                       hover:scale-105 hover:shadow-purple-lg
                       active:scale-95 active:shadow-inset-cyber
                       focus:outline-none focus:ring-2 focus:ring-neon-yellow focus:ring-offset-2 focus:ring-offset-matrix-dark
                       relative overflow-hidden group"
          >
            <span className="relative z-10">PAUSE</span>
            <div
              className="absolute inset-0 bg-gradient-to-r from-transparent via-white/10 to-transparent
                          transform translate-x-[-100%] group-hover:animate-scan transition-transform duration-500"
            />
          </button>

          <button
            type="button"
            id="resetButton"
            className="flex-1 px-3 py-2 bg-danger-gradient text-white font-bold text-xs
                       rounded-cyber border border-transparent font-mono tracking-wider
                       transition-all duration-300 ease-out transform
                       hover:scale-105 hover:shadow-purple-lg
                       active:scale-95 active:shadow-inset-cyber
                       focus:outline-none focus:ring-2 focus:ring-neon-pink focus:ring-offset-2 focus:ring-offset-matrix-dark
                       relative overflow-hidden group"
          >
            <span className="relative z-10">RESET</span>
            <div
              className="absolute inset-0 bg-gradient-to-r from-transparent via-white/10 to-transparent
                          transform translate-x-[-100%] group-hover:animate-scan transition-transform duration-500"
            />
          </button>
        </div>
      </div>
    </div>
  );
};

export default SimulationControlButtons;
