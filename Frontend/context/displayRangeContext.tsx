"use client";
import React, { createContext, useContext, useState, ReactNode } from 'react';

export interface DisplayRange {
  range: number;
}

export interface DisplayRangeContextType {
  displayRange: DisplayRange;
  setDisplayRange: React.Dispatch<React.SetStateAction<DisplayRange>>;
}

export const DisplayRangeContext = createContext<DisplayRangeContextType | undefined>(undefined);

export const DisplayRangeProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  // 修正: DisplayRange のみを useState で管理し、setDisplayRange を直接提供する
  const [displayRange, setDisplayRange] = useState<DisplayRange>({
    range: 400,  // 初期値
  });

  return (
    <DisplayRangeContext.Provider value={{ displayRange, setDisplayRange }}>
      {children}
    </DisplayRangeContext.Provider>
  );
}

export const useDisplayRange = () => {
  const context = useContext(DisplayRangeContext);
  if (!context) {
    throw new Error('useDisplayRange must be used within a DisplayRangeProvider');
  }
  return context;
};