"use client";
import React from "react";
import { useSelectedAircraft } from "@/context/selectedAircraftContext";
import { controlAircraft as sendControlAircraft } from "@/utility/api/controlAircraft";

const ControlAircraft = () => {
  const {
    callsign,
    instructedVector,
    setInstructedVector,
    applyInstructedVectorToRadar,
  } = useSelectedAircraft();

  const handleExecute = async () => {
    const ok = await sendControlAircraft(callsign ?? "", instructedVector);
    if (ok && callsign) {
      applyInstructedVectorToRadar(callsign, instructedVector);
    }
  };

  const fields = [
    { key: "altitude" as const, label: "Altitude", unit: "ft" },
    { key: "groundSpeed" as const, label: "Speed", unit: "kts" },
    { key: "heading" as const, label: "Heading", unit: "°" },
  ] as const;

  return (
    <div className="bg-control-gradient border border-matrix-accent rounded-cyber-lg p-4 backdrop-blur-sm">
      <div className="space-y-4">
        {fields.map(({ key, label, unit }) => (
          <div key={key} className="group relative">
            <label
              htmlFor={key}
              className="block text-xs font-semibold text-radar-primary mb-1 transition-all duration-300
                         group-focus-within:text-neon-blue group-focus-within:drop-shadow-lg"
            >
              {label}
            </label>
            <div className="relative">
              <input
                type="number"
                id={key}
                placeholder="0"
                value={instructedVector[key]}
                onChange={(e) =>
                  setInstructedVector((prev) => ({
                    ...prev,
                    [key]: parseInt(e.target.value, 10) || 0,
                  }))
                }
                className="w-full px-3 py-2 bg-matrix-dark border border-matrix-accent rounded-cyber
                           text-white text-sm font-mono placeholder-gray-500
                           transition-all duration-300 ease-out
                           focus:outline-none focus:border-radar-primary focus:shadow-cyber focus:scale-[1.02]
                           hover:border-radar-secondary hover:shadow-neon
                           backdrop-blur-sm"
              />
              <span className="absolute right-2 top-1/2 transform -translate-y-1/2 text-xs text-gray-400 font-mono">
                {unit}
              </span>
              <div
                className="absolute inset-0 rounded-cyber opacity-0 group-focus-within:opacity-100
                              transition-opacity duration-300 pointer-events-none
                              bg-gradient-to-r from-transparent via-radar-primary/5 to-transparent"
              ></div>
            </div>
          </div>
        ))}

        <button
          onClick={handleExecute}
          className="w-full mt-4 px-4 py-3 bg-button-gradient text-matrix-dark font-bold text-sm
                     rounded-cyber-lg border border-transparent
                     transition-all duration-300 ease-out transform
                     hover:bg-button-hover-gradient hover:scale-105 hover:shadow-cyber-lg
                     active:scale-95 active:shadow-inset-cyber
                     focus:outline-none focus:ring-2 focus:ring-radar-primary focus:ring-offset-2 focus:ring-offset-matrix-dark
                     animate-glow"
        >
          <span className="relative z-10">EXECUTE COMMAND</span>
          <div
            className="absolute inset-0 rounded-cyber-lg bg-gradient-to-r from-transparent via-white/10 to-transparent
                          transform translate-x-[-100%] hover:animate-scan transition-transform duration-500"
          ></div>
        </button>
      </div>
    </div>
  );
};

export default ControlAircraft;
