import React from "react";
import RadarCanvas from "@/components/radarCanvas";
import { RouteInfoDisplaySettingProvider } from "@/context/routeInfoDisplaySettingContext";
import RouteInfoDisplaySetting from "@/components/routeInfoDisplaySetting";
import SectorSelector from "@/components/sectorSelector";
import { CenterCoordinateProvider } from "@/context/centerCoordinateContext";
import { DisplayRangeProvider } from "@/context/displayRangeContext";
import DisplayRangeSetting from "@/components/displayRangeSetting";
import { SelectFixModeProvider } from "@/context/selectFixModeContext";
import { SelectedAircraftProvider } from "@/context/selectedAircraftContext";
// import SelectFixMode from "@/components/selectFixMode";
import InputAircraftInfo from "@/components/inputInfoArea";
import SelectedCallsignDisplay from "@/components/selectedCallsignDisplay";
import FlightPlanDisplay from "@/components/flightPlanDisplay";
import FlightPlanControl from "@/components/flightPlanControl";
import SimulationControlButtons from "@/components/simulationControlButtons";
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
            <SelectedAircraftProvider>
              <div className="flex h-screen w-full">
                <div className="flex w-full">
                  <RadarCanvas />
                  <div className="controlPanel bg-zinc-900 text-white p-5 flex flex-col justify-between min-w-80">
                    <SelectedCallsignDisplay variant="controller" />
                    <InputAircraftInfo />
                  <FlightPlanDisplay />
                  <FlightPlanControl />
                  {/* <SelectFixMode /> */}
                  <div id="settingArea" className="mt-auto">
                    <SimulationControlButtons />
                    <RouteInfoDisplaySetting />
                    <div className="flex flex-col">
                      <SectorSelector />
                      <DisplayRangeSetting />
                    </div>
                  </div>
                </div>
              </div>
            </div>
            </SelectedAircraftProvider>
          </SelectFixModeProvider>
        </DisplayRangeProvider>
      </CenterCoordinateProvider>
    </RouteInfoDisplaySettingProvider>
  );
}
