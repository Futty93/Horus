"use client";
import React from "react";
import Image from "next/image";
import RadarCanvas from "@/component/radarCanvas";
import { RouteInfoDisplaySettingProvider } from "@/context/routeInfoDisplaySettingContext";
import RouteInfoDisplaySetting from "@/component/routeInfoDisplaySetting";
import SectorSelector from "@/component/sectorSelector";
import { CenterCoordinateProvider } from "@/context/centerCoordinateContext";

export default function OperatorPage() {
  return (
    <RouteInfoDisplaySettingProvider>
      <CenterCoordinateProvider>
      <div className="flex h-screen w-full">
        <div className="flex w-full">
            <RadarCanvas />
          <div className="controlPanel bg-zinc-900 text-white p-5 flex flex-col justify-between min-w-80">
            <div id="callsignDisplay" className="text-center mb-5">
              <p id="callsign" className="text-2xl font-bold text-green-400">&nbsp;</p>
            </div>
            <div className="flex flex-col">
              {['Altitude', 'Speed', 'Heading'].map((label) => (
                <div className="mb-5" key={label}>
                  <div className="font-bold text-green-400">{label}</div>
                  <input
                    type="number"
                    id={label.toLowerCase()}
                    placeholder="0"
                    className="w-4/5 p-2 rounded border border-green-400 bg-gray-900 text-white focus:outline-none focus:border-green-400 focus:ring focus:ring-green-400 transition duration-300"
                  />
                </div>
              ))}
              <input type="button" value="Confirm" id="confirmButton" className="bg-green-400 text-gray-800 font-bold p-2 rounded transition duration-300 cursor-pointer hover:bg-green-500" />
            </div>
            <div id="settingArea" className="mt-auto">
              <RouteInfoDisplaySetting />
              <div className="flex flex-col">
                <SectorSelector onSectorChange={(coordinates) => console.log("Selected sector coordinates:", coordinates)} />
                <div className="flex justify-between mb-5">
                  <label htmlFor="displayRange" className="font-bold text-green-400 mr-2">表示範囲:</label>
                  <input
                    type="number"
                    id="displayRange"
                    defaultValue="200"
                    min="10"
                    max="4000"
                    className="p-2 rounded border border-green-400 bg-gray-900 text-white text-center"
                  />&nbsp;km
                </div>
                <div className="flex justify-between">
                  <input type="button" value="Start" id="startButton" className="bg-green-400 text-gray-800 font-bold p-2 rounded transition duration-300 cursor-pointer hover:bg-green-500" />
                  <input type="button" value="Pause" id="pauseButton" className="bg-yellow-500 text-gray-800 font-bold p-2 rounded transition duration-300 cursor-pointer hover:bg-yellow-600" />
                  <input type="button" value="Reset" id="resetButton" className="bg-red-500 text-gray-800 font-bold p-2 rounded transition duration-300 cursor-pointer hover:bg-red-600" />
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      </CenterCoordinateProvider>
    </RouteInfoDisplaySettingProvider>
  );
}
