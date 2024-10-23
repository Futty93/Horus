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
  setRouteInfoDisplaySetting: React.Dispatch<React.SetStateAction<RouteInfoDisplaySettingContextType>>;
}

const RouteInfoDisplaySettingContext = createContext<RouteInfoDisplaySettingContextType | undefined>(undefined);

export const RouteInfoDisplaySettingProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [routeInfoDisplaySetting, setRouteInfoDisplaySetting] = useState<RouteInfoDisplaySettingContextType>({
    isDisplaying: {
      waypointName: false,
      waypointPoint: true,
      radioNavigationAidsName: false,
      radioNavigationAidsPoint: true,
      atsLowerRoute: false,
      rnavRoute: true,
    },
    setRouteInfoDisplaySetting: () => {}, 
  });

  return (
    <RouteInfoDisplaySettingContext.Provider value={{ ...routeInfoDisplaySetting, setRouteInfoDisplaySetting }}>
      {children}
    </RouteInfoDisplaySettingContext.Provider>
  );
}

export const useRouteInfoDisplaySetting = () => {
  const context = useContext(RouteInfoDisplaySettingContext);
  if (!context) {
    throw new Error('useHousingDetail must be used within a RouteInfoDisplaySettingProvider');
  }
  return context;
};
