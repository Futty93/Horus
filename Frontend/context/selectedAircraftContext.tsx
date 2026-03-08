"use client";

import React, {
  createContext,
  useMemo,
  useContext,
  useState,
  type ReactNode,
} from "react";

export interface InstructedVector {
  altitude: number;
  groundSpeed: number;
  heading: number;
}

export interface SelectedAircraftContextType {
  callsign: string | null;
  setCallsign: (callsign: string | null) => void;
  instructedVector: InstructedVector;
  setInstructedVector: React.Dispatch<React.SetStateAction<InstructedVector>>;
}

const DEFAULT_INSTRUCTED_VECTOR: InstructedVector = {
  altitude: 0,
  groundSpeed: 0,
  heading: 0,
};

export const SelectedAircraftContext = createContext<
  SelectedAircraftContextType | undefined
>(undefined);

export const SelectedAircraftProvider: React.FC<{ children: ReactNode }> = ({
  children,
}) => {
  const [callsign, setCallsign] = useState<string | null>(null);
  const [instructedVector, setInstructedVector] = useState<InstructedVector>(
    DEFAULT_INSTRUCTED_VECTOR
  );

  const value = useMemo(
    () => ({
      callsign,
      setCallsign,
      instructedVector,
      setInstructedVector,
    }),
    [callsign, instructedVector]
  );

  return (
    <SelectedAircraftContext.Provider value={value}>
      {children}
    </SelectedAircraftContext.Provider>
  );
};

export const useSelectedAircraft = () => {
  const context = useContext(SelectedAircraftContext);
  if (!context) {
    throw new Error(
      "useSelectedAircraft must be used within a SelectedAircraftProvider"
    );
  }
  return context;
};
