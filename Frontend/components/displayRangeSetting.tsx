"use client";
import React from "react";
import { useDisplayRange } from "@/context/displayRangeContext";

const DisplayRangeSetting = () => {
  const { displayRange, setDisplayRange } = useDisplayRange();

  const handleRangeChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    const newRange = Number(event.target.value);
    setDisplayRange((prevRange) => ({
      ...prevRange,
      range: newRange,
    }));
    console.log("Display range changed to:", newRange);
  };

  return (
    <div className="bg-atc-surface border border-atc-border rounded-lg p-3 mb-3">
      <div className="flex items-center justify-between">
        <label
          htmlFor="displayRange"
          className="font-bold text-atc-text font-mono tracking-wider text-xs"
        >
          表示範囲:
        </label>
        <div className="flex items-center space-x-2">
          <input
            type="number"
            id="displayRange"
            defaultValue={`${displayRange.range}`}
            min="10"
            max="4000"
            onChange={handleRangeChange}
            className="w-16 px-2 py-1 bg-atc-surface-elevated border border-atc-border rounded
                       text-atc-text text-center font-mono text-xs
                       focus:outline-none focus:border-atc-accent
                       hover:border-atc-text-muted
                       [appearance:textfield] [&::-webkit-outer-spin-button]:appearance-none [&::-webkit-inner-spin-button]:appearance-none"
          />
          <span className="text-atc-text-muted font-mono text-xs tracking-wider">
            km
          </span>
        </div>
      </div>

      <div className="mt-2 flex justify-between text-xs font-mono text-atc-text-muted">
        <span>10km</span>
        <span className="text-atc-text">Current: {displayRange.range}km</span>
        <span>4000km</span>
      </div>

      <div className="mt-1 w-full bg-atc-surface-elevated rounded-full h-1">
        <div
          className="bg-atc-accent h-1 rounded-full transition-all duration-200"
          style={{
            width: `${((displayRange.range - 10) / (4000 - 10)) * 100}%`,
          }}
        />
      </div>
    </div>
  );
};

export default DisplayRangeSetting;
