import React from "react";
import RadarCanvas from "@/components/radarCanvas";
import { RouteInfoDisplaySettingProvider } from "@/context/routeInfoDisplaySettingContext";
import RouteInfoDisplaySetting from "@/components/routeInfoDisplaySetting";
import SectorSelector from "@/components/sectorSelector";
import { CenterCoordinateProvider } from "@/context/centerCoordinateContext";
import { DisplayRangeProvider } from "@/context/displayRangeContext";
import DisplayRangeSetting from "@/components/displayRangeSetting";
import ControlAircraft from "@/components/controlAircraft";
import { SelectFixModeProvider } from "@/context/selectFixModeContext";
import SelectFixMode from "@/components/selectFixMode";
import SimulationControlButtons from "@/components/simulationControlButtons";
import { Metadata } from "next";

export const metadata: Metadata = {
  title: "Operator",
};

export default function OperatorPage() {
  return (
    <RouteInfoDisplaySettingProvider>
      <CenterCoordinateProvider>
        <DisplayRangeProvider>
          <SelectFixModeProvider>
            <div className="flex h-screen w-full overflow-hidden">
              <div className="flex w-full h-full">
                <RadarCanvas />
                <div className="controlPanel bg-cyber-gradient border-l border-matrix-accent
                                text-white p-3 flex flex-col min-w-80 max-w-80
                                h-full overflow-y-auto overflow-x-hidden
                                scrollbar-thin scrollbar-track-matrix-dark scrollbar-thumb-radar-primary">
                  {/* Fixed Header - Callsign Display */}
                  <div className="flex-shrink-0 mb-4">
                    <div id="callsignDisplay" className="text-center">
                      <div className="bg-control-gradient border border-matrix-accent rounded-cyber p-3 backdrop-blur-sm">
                        <p id="callsign" className="text-xl font-bold text-radar-primary font-mono tracking-wider">&nbsp;</p>
                        <div className="mt-1 h-0.5 bg-gradient-to-r from-transparent via-radar-primary to-transparent opacity-50"></div>
                      </div>
                    </div>
                  </div>

                  {/* Scrollable Content */}
                  <div className="flex-1 space-y-4 min-h-0">
                    <ControlAircraft />
                    <SelectFixMode />

                    {/* Settings Area */}
                    <div className="space-y-3">
                      <RouteInfoDisplaySetting />
                      <SectorSelector />
                      <DisplayRangeSetting />

                      {/* Control Buttons */}
                      <SimulationControlButtons />
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
