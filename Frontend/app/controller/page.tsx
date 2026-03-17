import React from "react";
import RadarCanvas from "@/components/radarCanvas";
import { RouteInfoDisplaySettingProvider } from "@/context/routeInfoDisplaySettingContext";
import RouteInfoDisplaySetting from "@/components/routeInfoDisplaySetting";
import SectorSelector from "@/components/sectorSelector";
import { CenterCoordinateProvider } from "@/context/centerCoordinateContext";
import { DisplayRangeProvider } from "@/context/displayRangeContext";
import DisplayRangeSetting from "@/components/displayRangeSetting";
import { RangeRingsSettingProvider } from "@/context/rangeRingsSettingContext";
import RangeRingsSetting from "@/components/rangeRingsSetting";
import { SelectFixModeProvider } from "@/context/selectFixModeContext";
import { SelectedAircraftProvider } from "@/context/selectedAircraftContext";
// import SelectFixMode from "@/components/selectFixMode";
import SelectedCallsignDisplay from "@/components/selectedCallsignDisplay";
import FlightPlanDisplay from "@/components/flightPlanDisplay";
import InstructionMemo from "@/components/instructionMemo";
import { Metadata } from "next";

export const metadata: Metadata = {
  title: "Controller",
};

export default function ControllerPage() {
  return (
    <RouteInfoDisplaySettingProvider>
      <CenterCoordinateProvider>
        <DisplayRangeProvider>
          <RangeRingsSettingProvider>
            <SelectFixModeProvider>
              <SelectedAircraftProvider>
                <div className="flex h-screen w-full">
                  <div className="flex w-full">
                    <RadarCanvas />
                    <div
                      className="controlPanel bg-atc-bg border-l border-atc-border text-atc-text
                              p-4 flex flex-col justify-between min-w-80 max-w-80
                              h-full overflow-y-auto overflow-x-hidden
                              scrollbar-thin scrollbar-track-atc scrollbar-thumb-atc"
                    >
                      <div className="flex-1 space-y-4 min-h-0">
                        <SelectedCallsignDisplay variant="controller" />
                        <FlightPlanDisplay />
                        <InstructionMemo />
                      </div>
                      <div id="settingArea" className="mt-4 space-y-4">
                        <RouteInfoDisplaySetting />
                        <div className="flex flex-col space-y-3">
                          <SectorSelector />
                          <DisplayRangeSetting />
                          <RangeRingsSetting />
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </SelectedAircraftProvider>
            </SelectFixModeProvider>
          </RangeRingsSettingProvider>
        </DisplayRangeProvider>
      </CenterCoordinateProvider>
    </RouteInfoDisplaySettingProvider>
  );
}
