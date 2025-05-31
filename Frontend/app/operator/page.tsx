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
                      <div className="bg-control-gradient border border-matrix-accent rounded-cyber-lg p-3 backdrop-blur-sm">
                        <h3 className="text-xs font-bold text-radar-primary font-mono tracking-wider mb-2 text-center">
                          SIMULATION CONTROL
                        </h3>
                        <div className="flex flex-col space-y-2">
                          <button
                            id="startButton"
                            className="w-full px-3 py-2 bg-button-gradient text-matrix-dark font-bold text-sm
                                       rounded-cyber border border-transparent font-mono tracking-wider
                                       transition-all duration-300 ease-out transform
                                       hover:bg-button-hover-gradient hover:scale-105 hover:shadow-cyber-lg
                                       active:scale-95 active:shadow-inset-cyber
                                       focus:outline-none focus:ring-2 focus:ring-radar-primary focus:ring-offset-2 focus:ring-offset-matrix-dark
                                       relative overflow-hidden group animate-glow"
                          >
                            <span className="relative z-10">START SIMULATION</span>
                            <div className="absolute inset-0 bg-gradient-to-r from-transparent via-white/10 to-transparent
                                            transform translate-x-[-100%] group-hover:animate-scan transition-transform duration-500"></div>
                          </button>

                          <div className="flex space-x-2">
                            <button
                              id="pauseButton"
                              className="flex-1 px-3 py-2 bg-warning-gradient text-matrix-dark font-bold text-xs
                                         rounded-cyber border border-transparent font-mono tracking-wider
                                         transition-all duration-300 ease-out transform
                                         hover:scale-105 hover:shadow-purple-lg
                                         active:scale-95 active:shadow-inset-cyber
                                         focus:outline-none focus:ring-2 focus:ring-neon-yellow focus:ring-offset-2 focus:ring-offset-matrix-dark
                                         relative overflow-hidden group"
                            >
                              <span className="relative z-10">PAUSE</span>
                              <div className="absolute inset-0 bg-gradient-to-r from-transparent via-white/10 to-transparent
                                              transform translate-x-[-100%] group-hover:animate-scan transition-transform duration-500"></div>
                            </button>

                            <button
                              id="resetButton"
                              className="flex-1 px-3 py-2 bg-danger-gradient text-white font-bold text-xs
                                         rounded-cyber border border-transparent font-mono tracking-wider
                                         transition-all duration-300 ease-out transform
                                         hover:scale-105 hover:shadow-purple-lg
                                         active:scale-95 active:shadow-inset-cyber
                                         focus:outline-none focus:ring-2 focus:ring-neon-pink focus:ring-offset-2 focus:ring-offset-matrix-dark
                                         relative overflow-hidden group"
                              >
                                <span className="relative z-10">RESET</span>
                                <div className="absolute inset-0 bg-gradient-to-r from-transparent via-white/10 to-transparent
                                                transform translate-x-[-100%] group-hover:animate-scan transition-transform duration-500"></div>
                              </button>
                          </div>
                        </div>
                      </div>
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
