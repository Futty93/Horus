"use client";
import React from 'react';
import { useDisplayRange } from '@/context/displayRangeContext';

const DisplayRangeSetting = () => {
  const { displayRange, setDisplayRange } = useDisplayRange();

  const handleRangeChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    const newRange = Number(event.target.value);
    setDisplayRange((prevRange) => ({
      ...prevRange,
      range: newRange,
    }));
    console.log('Display range changed to:', newRange);
  };

  return (
    <div className="bg-control-gradient border border-matrix-accent rounded-cyber-lg p-3 backdrop-blur-sm mb-3">
      <div className="flex items-center justify-between">
        <label htmlFor="displayRange"
               className="font-bold text-radar-primary font-mono tracking-wider text-xs">
          表示範囲:
        </label>
        <div className="flex items-center space-x-2">
          <div className="relative">
            <input
              type="number"
              id="displayRange"
              defaultValue={`${displayRange.range}`}
              min="10"
              max="4000"
              onChange={handleRangeChange}
              className="w-16 px-2 py-1 bg-matrix-dark border border-matrix-accent rounded-cyber
                         text-white text-center font-mono text-xs
                         transition-all duration-300 ease-out
                         focus:outline-none focus:border-radar-primary focus:shadow-cyber focus:scale-105
                         hover:border-radar-secondary hover:shadow-neon
                         [appearance:textfield] [&::-webkit-outer-spin-button]:appearance-none [&::-webkit-inner-spin-button]:appearance-none"
            />

            {/* Glow effect */}
            <div className="absolute inset-0 rounded-cyber opacity-0 focus-within:opacity-100
                            transition-opacity duration-300 pointer-events-none
                            bg-gradient-to-r from-transparent via-radar-primary/5 to-transparent"></div>
          </div>

          <span className="text-radar-secondary font-mono text-xs tracking-wider">km</span>
        </div>
      </div>

      {/* Range indicator */}
      <div className="mt-2 flex justify-between text-xs font-mono text-gray-400">
        <span>10km</span>
        <span className="text-radar-primary">Current: {displayRange.range}km</span>
        <span>4000km</span>
      </div>

      {/* Visual range bar */}
      <div className="mt-1 w-full bg-matrix-dark rounded-full h-1">
        <div
          className="bg-button-gradient h-1 rounded-full transition-all duration-300 ease-out shadow-cyber"
          style={{ width: `${((displayRange.range - 10) / (4000 - 10)) * 100}%` }}
        ></div>
      </div>
    </div>
  );
}

export default DisplayRangeSetting;
