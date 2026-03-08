"use client";
import { useSelectFixMode } from "@/context/selectFixModeContext";
import { useSelectedAircraft } from "@/context/selectedAircraftContext";
import { directToFix } from "@/utility/api/flightPlan";
import React from "react";

const SelectFixMode = () => {
  const {
    isSelectFixMode,
    setIsSelectFixMode,
    selectedFixName,
    setSelectedFixName,
  } = useSelectFixMode();
  const { callsign } = useSelectedAircraft();

  return (
    <div className="bg-atc-surface border border-atc-border rounded-lg p-4 mt-4">
      <div className="text-center mb-4">
        <p className="text-lg font-bold text-atc-text font-mono tracking-wider">
          {selectedFixName}
        </p>
        <div className="mt-1 h-px bg-atc-border" />
      </div>

      {!isSelectFixMode.selectFixMode ? (
        <button
          className="w-full px-4 py-3 bg-atc-surface-elevated text-atc-text font-bold text-sm
                     rounded-lg border border-atc-accent
                     transition-colors duration-200
                     hover:bg-atc-accent hover:text-white
                     focus:outline-none focus:ring-2 focus:ring-atc-accent focus:ring-offset-2 focus:ring-offset-atc-bg"
          onClick={() => {
            if (!callsign || callsign.length < 2) {
              setSelectedFixName("Select aircraft first");
              return;
            }
            setSelectedFixName("No fixes selected");
            setIsSelectFixMode({ selectFixMode: true });
          }}
        >
          <span className="font-mono tracking-wider">DIRECT TO FIX</span>
        </button>
      ) : (
        <div className="flex flex-col space-y-3">
          <button
            className="w-full px-4 py-3 bg-atc-accent text-white font-bold text-sm
                       rounded-lg border border-transparent
                       transition-colors duration-200
                       hover:bg-atc-accent-hover
                       focus:outline-none focus:ring-2 focus:ring-atc-accent focus:ring-offset-2 focus:ring-offset-atc-bg"
            onClick={async () => {
              if (!callsign || callsign.length < 2) return;
              if (!selectedFixName || selectedFixName === "No fixes selected") {
                return;
              }
              try {
                const ok = await directToFix(callsign, selectedFixName, false);
                if (ok) {
                  console.log(`Aircraft ${callsign} controlled successfully.`);
                } else {
                  console.error(
                    `Failed to control aircraft ${callsign}. Status:`
                  );
                }
              } catch (error) {
                console.error(
                  "Error occurred while controlling aircraft:",
                  error
                );
              }

              setSelectedFixName("No fixes selected");
              setIsSelectFixMode({ selectFixMode: false });
            }}
          >
            <span className="font-mono tracking-wider">✓ CONFIRM</span>
          </button>

          <button
            className="w-full px-4 py-3 bg-atc-danger text-white font-bold text-sm
                       rounded-lg border border-transparent
                       transition-opacity duration-200
                       hover:opacity-90
                       focus:outline-none focus:ring-2 focus:ring-atc-danger focus:ring-offset-2 focus:ring-offset-atc-bg"
            onClick={() => {
              setSelectedFixName("No fixes selected");
              setIsSelectFixMode({ selectFixMode: false });
            }}
          >
            <span className="font-mono tracking-wider">✕ CANCEL</span>
          </button>
        </div>
      )}
    </div>
  );
};

export default SelectFixMode;
