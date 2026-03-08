"use client";

import React from "react";
import { useSelectedAircraft } from "@/context/selectedAircraftContext";

type Variant = "operator" | "controller";

interface SelectedCallsignDisplayProps {
  variant?: Variant;
}

const SelectedCallsignDisplay: React.FC<SelectedCallsignDisplayProps> = ({
  variant = "operator",
}) => {
  const { callsign } = useSelectedAircraft();

  if (variant === "controller") {
    return (
      <div className="text-center mb-5">
        <p className="text-2xl font-bold text-atc-text font-mono tracking-wider">
          {callsign ?? "\u00a0"}
        </p>
      </div>
    );
  }

  return (
    <div className="flex-shrink-0 mb-4">
      <div className="text-center">
        <div className="bg-atc-surface border border-atc-border rounded p-3">
          <p className="text-xl font-bold text-atc-text font-mono tracking-wider">
            {callsign ?? "\u00a0"}
          </p>
          <div className="mt-1 h-px bg-atc-border" />
        </div>
      </div>
    </div>
  );
};

export default SelectedCallsignDisplay;
