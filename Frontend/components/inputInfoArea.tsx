"use client";
import React from "react";
import { useSelectedAircraft } from "@/context/selectedAircraftContext";
import { controlAircraft as sendControlAircraft } from "@/utility/api/controlAircraft";

const InputAircraftInfo = () => {
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
    <div className="bg-atc-surface border border-atc-border rounded-lg p-4">
      <h3 className="text-sm font-bold text-atc-text font-mono tracking-wider mb-3">
        INSTRUCTION
      </h3>
      <div className="space-y-4">
        {fields.map(({ key, label, unit }) => (
          <div key={key} className="group relative">
            <label
              htmlFor={key}
              className="block text-xs font-semibold text-atc-text-muted mb-1"
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
                className="w-full px-3 py-2 bg-atc-surface-elevated border border-atc-border rounded
                           text-atc-text text-sm font-mono placeholder-atc-text-muted
                           focus:outline-none focus:border-atc-accent
                           hover:border-atc-text-muted"
              />
              <span className="absolute right-2 top-1/2 transform -translate-y-1/2 text-xs text-atc-text-muted font-mono">
                {unit}
              </span>
            </div>
          </div>
        ))}

        <button
          type="button"
          onClick={handleExecute}
          className="w-full mt-4 px-4 py-3 bg-atc-accent text-white font-bold text-sm
                     rounded-lg border border-transparent
                     transition-colors duration-200
                     hover:bg-atc-accent-hover
                     focus:outline-none focus:ring-2 focus:ring-atc-accent focus:ring-offset-2 focus:ring-offset-atc-bg"
        >
          CONFIRM
        </button>
      </div>
    </div>
  );
};

export default InputAircraftInfo;
