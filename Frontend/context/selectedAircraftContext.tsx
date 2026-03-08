"use client";

import React, { createContext, useContext, useState, type ReactNode } from "react";

export interface SelectedAircraftContextType {
  callsign: string | null;
  setCallsign: (callsign: string | null) => void;
}

export const SelectedAircraftContext =
  createContext<SelectedAircraftContextType | undefined>(undefined);

export const SelectedAircraftProvider: React.FC<{ children: ReactNode }> = ({
  children,
}) => {
  const [callsign, setCallsign] = useState<string | null>(null);

  return (
    <SelectedAircraftContext.Provider value={{ callsign, setCallsign }}>
      {children}
    </SelectedAircraftContext.Provider>
  );
};

export const useSelectedAircraft = () => {
  const context = useContext(SelectedAircraftContext);
  if (!context) {
    throw new Error(
      "useSelectedAircraft must be used within a SelectedAircraftProvider",
    );
  }
  return context;
};
