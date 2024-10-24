"use client";
import React from "react";
import Image from "next/image";
import RadarCanvas from "@/component/radarCanvas";
import { RouteInfoDisplaySettingProvider } from "@/context/routeInfoDisplaySettingContext";
import RouteInfoDisplaySetting from "@/component/routeInfoDisplaySetting";
import SectorSelector from "@/component/sectorSelector";
import { CenterCoordinateProvider } from "@/context/centerCoordinateContext";
import { DisplayRangeProvider } from "@/context/displayRangeContext";
import DisplayRangeSetting from "@/component/displayRangeSetting";
import ControlAircraft from "@/component/controlAircraft";

export default function OperatorPage() {
  return (
    <RouteInfoDisplaySettingProvider>
      <CenterCoordinateProvider>
        <DisplayRangeProvider>
          <div className="flex h-screen w-full">
            <div className="flex w-full">
              <RadarCanvas />
              <div className="controlPanel bg-zinc-900 text-white p-5 flex flex-col justify-between min-w-80">
                <div id="callsignDisplay" className="text-center mb-5">
                  <p id="callsign" className="text-2xl font-bold text-green-400">&nbsp;</p>
                </div>
                <ControlAircraft />
                <div id="settingArea" className="mt-auto">
                  <RouteInfoDisplaySetting />
                  <div className="flex flex-col">
                    <SectorSelector />
                    <DisplayRangeSetting />
                    <div className="flex justify-between">
                      <input type="button" value="Start" id="startButton" className="bg-green-400 text-gray-800 font-bold p-2 rounded transition duration-300 cursor-pointer hover:bg-green-500 hover:shadow-lg-no-offset hover:shadow-green-400/70" />
                      <input type="button" value="Pause" id="pauseButton" className="bg-yellow-400 text-gray-800 font-bold p-2 rounded transition duration-300 cursor-pointer hover:bg-yellow-500 hover:shadow-lg-no-offset hover:shadow-yellow-400/70" />
                      <input type="button" value="Reset" id="resetButton" className="bg-red-500 text-gray-800 font-bold p-2 rounded transition duration-300 cursor-pointer hover:bg-red-600 hover:shadow-lg-no-offset hover:shadow-red-400/70" />
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </DisplayRangeProvider>
      </CenterCoordinateProvider>
    </RouteInfoDisplaySettingProvider>
  );
}
