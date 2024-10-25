"use client";
import React from 'react';
import { useDisplayRange } from '@/context/displayRangeContext';

const DisplayRangeSetting = () => {
  const { setDisplayRange } = useDisplayRange();

  const handleRangeChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    const newRange = Number(event.target.value);
    setDisplayRange((prevRange) => ({
      ...prevRange,
      range: newRange,
    }));
    console.log('Display range changed to:', newRange);
  };

  return (
    <div className="flex justify-between mb-5">
      <label htmlFor="displayRange" className="font-bold text-green-400 mr-2">表示範囲:</label>
      <input
        type="number"
        id="displayRange"
        defaultValue="200"
        min="10"
        max="4000"
        onChange={handleRangeChange}
        className="p-2 rounded border border-green-400 bg-gray-800 text-white text-center"
      />&nbsp;km
    </div>
  );
}

export default DisplayRangeSetting;