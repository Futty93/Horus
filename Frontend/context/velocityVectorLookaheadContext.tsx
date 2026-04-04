"use client";

import React, {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useState,
  type ReactNode,
} from "react";

export const VELOCITY_VECTOR_LOOKAHEAD_MIN_MINUTES = 0.5;
export const VELOCITY_VECTOR_LOOKAHEAD_MAX_MINUTES = 10;
export const VELOCITY_VECTOR_LOOKAHEAD_STEP_MINUTES = 0.5;
export const VELOCITY_VECTOR_LOOKAHEAD_DEFAULT_MINUTES = 1;

const STORAGE_KEY = "horus.velocityVectorLookaheadMinutes";

export function clampVelocityVectorLookaheadMinutes(value: number): number {
  const step = VELOCITY_VECTOR_LOOKAHEAD_STEP_MINUTES;
  const min = VELOCITY_VECTOR_LOOKAHEAD_MIN_MINUTES;
  const max = VELOCITY_VECTOR_LOOKAHEAD_MAX_MINUTES;
  const rounded = Math.round(value / step) * step;
  return Math.min(max, Math.max(min, rounded));
}

function readStoredDurationMinutes(): number {
  if (typeof window === "undefined") {
    return VELOCITY_VECTOR_LOOKAHEAD_DEFAULT_MINUTES;
  }
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (raw == null) return VELOCITY_VECTOR_LOOKAHEAD_DEFAULT_MINUTES;
    const parsed = Number(raw);
    if (!Number.isFinite(parsed)) {
      return VELOCITY_VECTOR_LOOKAHEAD_DEFAULT_MINUTES;
    }
    return clampVelocityVectorLookaheadMinutes(parsed);
  } catch {
    return VELOCITY_VECTOR_LOOKAHEAD_DEFAULT_MINUTES;
  }
}

export interface VelocityVectorLookaheadContextType {
  durationMinutes: number;
  setDurationMinutes: (value: number) => void;
}

export const VelocityVectorLookaheadContext = createContext<
  VelocityVectorLookaheadContextType | undefined
>(undefined);

export const VelocityVectorLookaheadProvider: React.FC<{
  children: ReactNode;
}> = ({ children }) => {
  const [durationMinutes, setDurationMinutesState] = useState(
    VELOCITY_VECTOR_LOOKAHEAD_DEFAULT_MINUTES
  );

  useEffect(() => {
    setDurationMinutesState(readStoredDurationMinutes());
  }, []);

  const setDurationMinutes = useCallback((value: number) => {
    const next = clampVelocityVectorLookaheadMinutes(value);
    setDurationMinutesState(next);
    try {
      localStorage.setItem(STORAGE_KEY, String(next));
    } catch {
      /* quota / private mode */
    }
  }, []);

  return (
    <VelocityVectorLookaheadContext.Provider
      value={{ durationMinutes, setDurationMinutes }}
    >
      {children}
    </VelocityVectorLookaheadContext.Provider>
  );
};

export const useVelocityVectorLookahead = () => {
  const ctx = useContext(VelocityVectorLookaheadContext);
  if (!ctx) {
    throw new Error(
      "useVelocityVectorLookahead must be used within a VelocityVectorLookaheadProvider"
    );
  }
  return ctx;
};
