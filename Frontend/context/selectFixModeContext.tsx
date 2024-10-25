"use client";
import React, { createContext, useContext, useState, ReactNode } from 'react';

export interface isSelectFixMode {
  selectFixMode: boolean;
}

export interface SelectFixModeContextType {
  isSelectFixMode: isSelectFixMode;
  setIsSelectFixMode: React.Dispatch<React.SetStateAction<isSelectFixMode>>;
}

export const SelectFixModeContext = createContext<SelectFixModeContextType | undefined>(undefined);

export const SelectFixModeProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [isSelectFixMode, setIsSelectFixMode] = useState<isSelectFixMode>({
    selectFixMode: false,
  });

  return (
    <SelectFixModeContext.Provider value={{ isSelectFixMode, setIsSelectFixMode }}>
      {children}
    </SelectFixModeContext.Provider>
  );
}

export const useSelectFixMode = () => {
  const context = useContext(SelectFixModeContext);
  if (!context) {
    throw new Error('useSelectFixMode must be used within a SelectFixModeProvider');
  }
  return context;
};