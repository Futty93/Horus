"use client";
import React, { createContext, useContext, useState, ReactNode } from 'react';

export interface Coordinate {
  latitude: number;
  longitude: number;
}

export interface CenterCoordinateContextType {
  centerCoordinate: Coordinate;
  setCenterCoordinate: React.Dispatch<React.SetStateAction<Coordinate>>;
}

export const CenterCoordinateContext = createContext<CenterCoordinateContextType | undefined>(undefined);

export const CenterCoordinateProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [centerCoordinate, setCenterCoordinate] = useState<Coordinate>({
    latitude: 34.482,
    longitude: 138.614,
  });

  return (
    <CenterCoordinateContext.Provider value={{ centerCoordinate, setCenterCoordinate }}>
      {children}
    </CenterCoordinateContext.Provider>
  );
}

export const useCenterCoordinate = () => {
  const context = useContext(CenterCoordinateContext);
  if (!context) {
    throw new Error('useCenterCoordinate must be used within a CenterCoordinateProvider');
  }
  return context;
};