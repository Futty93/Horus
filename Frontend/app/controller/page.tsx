import React from "react";
import RadarCanvas from "@/components/radarCanvas";
import { RouteInfoDisplaySettingProvider } from "@/context/routeInfoDisplaySettingContext";
import RouteInfoDisplaySetting from "@/components/routeInfoDisplaySetting";
import SectorSelector from "@/components/sectorSelector";
import { CenterCoordinateProvider } from "@/context/centerCoordinateContext";
import { DisplayRangeProvider } from "@/context/displayRangeContext";
import DisplayRangeSetting from "@/components/displayRangeSetting";
import { SelectFixModeProvider } from "@/context/selectFixModeContext";
// import SelectFixMode from "@/components/selectFixMode";
import InputAircraftInfo from "@/components/inputInfoArea";
import { Metadata } from "next";

export const metadata: Metadata = {
  title: "Controller",
};

export default function ControllerPage() {

  return (
    <RouteInfoDisplaySettingProvider>
      <CenterCoordinateProvider>
        <DisplayRangeProvider>
          <SelectFixModeProvider>
            <div className="flex h-screen w-full">
              <div className="flex w-full">
                <RadarCanvas />
                <div className="controlPanel bg-zinc-900 text-white p-5 flex flex-col justify-between min-w-80">
                  <div id="callsignDisplay" className="text-center mb-5">
                    <p id="callsign" className="text-2xl font-bold text-green-400">&nbsp;</p>
                  </div>
                  <InputAircraftInfo />
                  {/* <SelectFixMode /> */}
                  <div id="settingArea" className="mt-auto">
                    <RouteInfoDisplaySetting />
                    <div className="flex flex-col">
                      <SectorSelector />
                      <DisplayRangeSetting />
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </SelectFixModeProvider>
        </DisplayRangeProvider>
      </CenterCoordinateProvider>
    </RouteInfoDisplaySettingProvider>
  );
}
