"use client";
import React, { createContext, useContext, useState, ReactNode } from "react";

export interface RangeRingsSetting {
  enabled: boolean;
  intervalNm: number;
}

export interface RangeRingsSettingContextType {
  rangeRingsSetting: RangeRingsSetting;
  setRangeRingsSetting: React.Dispatch<React.SetStateAction<RangeRingsSetting>>;
}

export const RangeRingsSettingContext = createContext<
  RangeRingsSettingContextType | undefined
>(undefined);

export const RANGE_RINGS_INTERVAL_OPTIONS = [5, 10, 20, 50] as const;

export const RangeRingsSettingProvider: React.FC<{ children: ReactNode }> = ({
  children,
}) => {
  const [rangeRingsSetting, setRangeRingsSetting] = useState<RangeRingsSetting>(
    {
      enabled: true,
      intervalNm: 10,
    }
  );

  return (
    <RangeRingsSettingContext.Provider
      value={{ rangeRingsSetting, setRangeRingsSetting }}
    >
      {children}
    </RangeRingsSettingContext.Provider>
  );
};

export const useRangeRingsSetting = () => {
  const context = useContext(RangeRingsSettingContext);
  if (!context) {
    throw new Error(
      "useRangeRingsSetting must be used within a RangeRingsSettingProvider"
    );
  }
  return context;
};
