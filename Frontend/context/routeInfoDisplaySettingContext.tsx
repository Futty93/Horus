"use client";
import React, { createContext, useContext, useState, ReactNode } from 'react';

export interface DisplaySettings {
  waypointName: boolean;
  waypointPoint: boolean;
  radioNavigationAidsName: boolean;
  radioNavigationAidsPoint: boolean;
  atsLowerRoute: boolean;
  rnavRoute: boolean;
}

export interface RouteInfoDisplaySettingContextType {
  isDisplaying: DisplaySettings;
  setRouteInfoDisplaySetting: React.Dispatch<React.SetStateAction<DisplaySettings>>;
}

const RouteInfoDisplaySettingContext = createContext<RouteInfoDisplaySettingContextType | undefined>(undefined);

export const RouteInfoDisplaySettingProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [isDisplaying, setRouteInfoDisplaySetting] = useState<DisplaySettings>({
    waypointName: false,
    waypointPoint: true,
    radioNavigationAidsName: false,
    radioNavigationAidsPoint: true,
    atsLowerRoute: false,
    rnavRoute: true,
  });

  return (
    <RouteInfoDisplaySettingContext.Provider value={{ isDisplaying, setRouteInfoDisplaySetting }}>
      {children}
    </RouteInfoDisplaySettingContext.Provider>
  );
}

export const useRouteInfoDisplaySetting = () => {
  const context = useContext(RouteInfoDisplaySettingContext);
  if (!context) {
    throw new Error('useRouteInfoDisplaySetting must be used within a RouteInfoDisplaySettingProvider');
  }
  return context;
};