"use client";
import React, { createContext, useContext, useState, ReactNode } from "react";

export interface DataBlockDisplaySetting {
  squawk: boolean;
  aircraftType: boolean;
  eta: boolean;
  /** 管制クリアランスメモ行（atcClearance vs 実測）。spec 20260326。 */
  atcClearanceMemo: boolean;
}

export interface DataBlockDisplaySettingContextType {
  dataBlockDisplaySetting: DataBlockDisplaySetting;
  setDataBlockDisplaySetting: React.Dispatch<
    React.SetStateAction<DataBlockDisplaySetting>
  >;
}

export const DataBlockDisplaySettingContext = createContext<
  DataBlockDisplaySettingContextType | undefined
>(undefined);

export const DataBlockDisplaySettingProvider: React.FC<{
  children: ReactNode;
}> = ({ children }) => {
  const [dataBlockDisplaySetting, setDataBlockDisplaySetting] =
    useState<DataBlockDisplaySetting>({
      squawk: false,
      aircraftType: true,
      eta: true,
      atcClearanceMemo: true,
    });

  return (
    <DataBlockDisplaySettingContext.Provider
      value={{ dataBlockDisplaySetting, setDataBlockDisplaySetting }}
    >
      {children}
    </DataBlockDisplaySettingContext.Provider>
  );
};

export const useDataBlockDisplaySetting = () => {
  const context = useContext(DataBlockDisplaySettingContext);
  if (!context) {
    throw new Error(
      "useDataBlockDisplaySetting must be used within a DataBlockDisplaySettingProvider"
    );
  }
  return context;
};
