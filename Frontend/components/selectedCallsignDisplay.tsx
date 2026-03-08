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
        <p className="text-2xl font-bold text-green-400 font-mono tracking-wider">
          {callsign ?? "\u00a0"}
        </p>
      </div>
    );
  }

  return (
    <div className="flex-shrink-0 mb-4">
      <div className="text-center">
        <div className="bg-control-gradient border border-matrix-accent rounded-cyber p-3 backdrop-blur-sm">
          <p className="text-xl font-bold text-radar-primary font-mono tracking-wider">
            {callsign ?? "\u00a0"}
          </p>
          <div className="mt-1 h-0.5 bg-gradient-to-r from-transparent via-radar-primary to-transparent opacity-50"></div>
        </div>
      </div>
    </div>
  );
};

export default SelectedCallsignDisplay;
