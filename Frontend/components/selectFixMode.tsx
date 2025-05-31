"use client";
import { useSelectFixMode } from "@/context/selectFixModeContext";
import React from "react";

const serverIp = process.env.NEXT_PUBLIC_SERVER_IP;
const serverPort = process.env.NEXT_PUBLIC_SERVER_PORT;

const SelectFixMode = () => {
  const { isSelectFixMode, setIsSelectFixMode } = useSelectFixMode();

  return (
    <div className="bg-control-gradient border border-matrix-accent rounded-cyber-lg p-4 backdrop-blur-sm mt-4">
      <div className="text-center mb-4">
        <div className="relative inline-block">
          <p id="selectedFixName"
             className="text-lg font-bold text-radar-primary font-mono tracking-wider
                        transition-all duration-300 hover:text-neon-blue hover:drop-shadow-lg">
            No fixes selected
          </p>
          <div className="absolute -bottom-1 left-0 w-full h-0.5 bg-gradient-to-r from-transparent via-radar-primary to-transparent
                          opacity-50 animate-pulse-slow"></div>
        </div>
      </div>

      {!isSelectFixMode.selectFixMode ? (
        <button
          className="w-full px-4 py-3 bg-cyber-gradient text-white font-bold text-sm
                     rounded-cyber-lg border border-cyber-600
                     transition-all duration-300 ease-out transform
                     hover:bg-button-gradient hover:scale-105 hover:shadow-neon-lg hover:border-transparent
                     active:scale-95 active:shadow-inset-neon
                     focus:outline-none focus:ring-2 focus:ring-cyber-500 focus:ring-offset-2 focus:ring-offset-matrix-dark
                     relative overflow-hidden group"
          onClick={() => {
            console.log("Direct to Fix 押されたよ");
            const callsignElement = document.getElementById("callsign") as HTMLParagraphElement;
            const selectedFixNameElement = document.getElementById("selectedFixName") as HTMLParagraphElement;
            if (callsignElement.innerText.length <= 1) {
              console.error("Callsign is empty");
              selectedFixNameElement.innerText = "Select aircraft first";
              return;
            }
            setIsSelectFixMode({ selectFixMode: true });
          }}
        >
          <span className="relative z-10 font-mono tracking-wider">DIRECT TO FIX</span>
          <div className="absolute inset-0 bg-gradient-to-r from-transparent via-white/10 to-transparent
                          transform translate-x-[-100%] group-hover:animate-scan transition-transform duration-500"></div>
        </button>
      ) : (
        <div className="flex flex-col space-y-3">
          <button
            className="w-full px-4 py-3 bg-button-gradient text-matrix-dark font-bold text-sm
                       rounded-cyber-lg border border-transparent
                       transition-all duration-300 ease-out transform
                       hover:bg-button-hover-gradient hover:scale-105 hover:shadow-cyber-lg
                       active:scale-95 active:shadow-inset-cyber
                       focus:outline-none focus:ring-2 focus:ring-radar-primary focus:ring-offset-2 focus:ring-offset-matrix-dark
                       relative overflow-hidden group animate-glow"
            onClick={async () => {
              console.log("Confirm 押されたよ");
              const callsignElement = document.getElementById("callsign") as HTMLParagraphElement;
              const selectedFixNameElement = document.getElementById("selectedFixName") as HTMLParagraphElement;

              if (callsignElement.innerText.length <= 1) {
                console.error("Callsign is empty");
              } else if (callsignElement) {
                const callsign = callsignElement.innerText;
                console.log("Callsign:", callsign);
              } else {
                console.error("Callsign element not found");
              }

              const callsign = callsignElement.innerText;
              const selectedFixName = selectedFixNameElement.innerText;
              try {
                const response = await fetch(
                  `http://${serverIp}:${serverPort}/api/aircraft/control/${callsign}/direct/${selectedFixName}`,
                  {
                    method: "POST",
                    headers: {
                      "Content-Type": "application/json",
                    },
                  }
                );

                if (response.ok) {
                  console.log(`Aircraft ${callsign} controlled successfully.`);
                } else {
                  console.error(`Failed to control aircraft ${callsign}. Status:`, response.status);
                }
              } catch (error) {
                console.error("Error occurred while controlling aircraft:", error);
              }

              selectedFixNameElement.innerText = "No fixes selected";
              setIsSelectFixMode({ selectFixMode: false });
            }}
          >
            <span className="relative z-10 font-mono tracking-wider">✓ CONFIRM</span>
            <div className="absolute inset-0 bg-gradient-to-r from-transparent via-white/10 to-transparent
                            transform translate-x-[-100%] group-hover:animate-scan transition-transform duration-500"></div>
          </button>

          <button
            className="w-full px-4 py-3 bg-danger-gradient text-white font-bold text-sm
                       rounded-cyber-lg border border-transparent
                       transition-all duration-300 ease-out transform
                       hover:scale-105 hover:shadow-purple-lg
                       active:scale-95 active:shadow-inset-cyber
                       focus:outline-none focus:ring-2 focus:ring-neon-pink focus:ring-offset-2 focus:ring-offset-matrix-dark
                       relative overflow-hidden group"
            onClick={() => {
              console.log("Cancel 押されたよ");
              const selectedFixNameElement = document.getElementById("selectedFixName") as HTMLParagraphElement;

              selectedFixNameElement.innerText = "No fixes selected";
              setIsSelectFixMode({ selectFixMode: false });
            }}
          >
            <span className="relative z-10 font-mono tracking-wider">✕ CANCEL</span>
            <div className="absolute inset-0 bg-gradient-to-r from-transparent via-white/10 to-transparent
                            transform translate-x-[-100%] group-hover:animate-scan transition-transform duration-500"></div>
          </button>
        </div>
      )}
    </div>
  );
};

export default SelectFixMode;
