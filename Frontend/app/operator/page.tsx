import React from "react";
import RadarCanvas from "@/components/radarCanvas";
import { RouteInfoDisplaySettingProvider } from "@/context/routeInfoDisplaySettingContext";
import RouteInfoDisplaySetting from "@/components/routeInfoDisplaySetting";
import { CenterCoordinateProvider } from "@/context/centerCoordinateContext";
import { DisplayRangeProvider } from "@/context/displayRangeContext";
import { VelocityVectorLookaheadProvider } from "@/context/velocityVectorLookaheadContext";
import { RangeRingsSettingProvider } from "@/context/rangeRingsSettingContext";
import RadarViewSetting from "@/components/radarViewSetting";
import { DataBlockDisplaySettingProvider } from "@/context/dataBlockDisplaySettingContext";
import DataBlockDisplaySetting from "@/components/dataBlockDisplaySetting";
import ControlAircraft from "@/components/controlAircraft";
import { SelectFixModeProvider } from "@/context/selectFixModeContext";
import { SelectedAircraftProvider } from "@/context/selectedAircraftContext";
import SelectFixMode from "@/components/selectFixMode";
import SelectedCallsignDisplay from "@/components/selectedCallsignDisplay";
import FlightPlanControl from "@/components/flightPlanControl";
import SimulationControlButtons from "@/components/simulationControlButtons";
import ConflictSummaryStrip from "@/components/conflictSummaryStrip";
import SeparationViolationAlerts from "@/components/separationViolationAlerts";
import SelectedAircraftConflictsPanel from "@/components/selectedAircraftConflictsPanel";
import { Metadata } from "next";

export const metadata: Metadata = {
  title: "Operator",
};

export default function OperatorPage() {
  return (
    <RouteInfoDisplaySettingProvider>
      <CenterCoordinateProvider>
        <DisplayRangeProvider>
          <VelocityVectorLookaheadProvider>
            <RangeRingsSettingProvider>
              <DataBlockDisplaySettingProvider>
                <SelectFixModeProvider>
                  <SelectedAircraftProvider>
                    <div className="flex h-screen w-full overflow-hidden">
                      <div className="flex h-full min-h-0 w-full min-w-0 flex-1">
                        <div className="relative h-full min-h-0 min-w-0 flex-1">
                          <ConflictSummaryStrip />
                          <SeparationViolationAlerts />
                          <RadarCanvas />
                        </div>
                        <div
                          className="controlPanel bg-atc-bg border-l border-atc-border text-atc-text
                                  p-3 flex flex-col min-w-80 max-w-80
                                  h-full overflow-y-auto overflow-x-hidden
                                  scrollbar-thin scrollbar-track-atc scrollbar-thumb-atc"
                        >
                          <SelectedCallsignDisplay variant="operator" />
                          <SelectedAircraftConflictsPanel />

                          {/* Scrollable Content */}
                          <div className="flex-1 space-y-4 min-h-0">
                            <ControlAircraft />
                            <SelectFixMode />
                            <FlightPlanControl />

                            {/* Settings Area */}
                            <div className="space-y-3">
                              <RouteInfoDisplaySetting />
                              <RadarViewSetting />
                              <DataBlockDisplaySetting variant="operator" />

                              {/* Control Buttons */}
                              <SimulationControlButtons />
                            </div>
                          </div>
                        </div>
                      </div>
                    </div>
                  </SelectedAircraftProvider>
                </SelectFixModeProvider>
              </DataBlockDisplaySettingProvider>
            </RangeRingsSettingProvider>
          </VelocityVectorLookaheadProvider>
        </DisplayRangeProvider>
      </CenterCoordinateProvider>
    </RouteInfoDisplaySettingProvider>
  );
}
