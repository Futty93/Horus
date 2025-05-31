"use client";
import React from 'react';

const serverIp = process.env.NEXT_PUBLIC_SERVER_IP;
const serverPort = process.env.NEXT_PUBLIC_SERVER_PORT;

const ControlAircraft = () => {
  let callsign: string = "";

  const controlAircraft = async () => {
    const callsignElement = document.getElementById("callsign") as HTMLParagraphElement;
    const inputAltitude = document.getElementById("altitude") as HTMLInputElement;
    const inputSpeed = document.getElementById("speed") as HTMLInputElement;
    const inputHeading = document.getElementById("heading") as HTMLInputElement;
    if (callsignElement.innerText.length >= 2) {
      callsign = callsignElement.innerText;
    } else {
      console.error("Callsign element not found");
      return;
    }
    if (!inputAltitude || !inputSpeed || !inputHeading) {
      console.error("Input elements not found");
      return;
    }
    const instructedAltitude = parseInt(inputAltitude.value);
    const instructedGroundSpeed = parseInt(inputSpeed.value);
    const instructedHeading = parseInt(inputHeading.value);

    const controlAircraftDto = {
      instructedAltitude,
      instructedGroundSpeed,
      instructedHeading,
    };

    try {
      const response = await fetch(
        `http://${serverIp}:${serverPort}/api/aircraft/control/${callsign}`,
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify(controlAircraftDto),
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
  };

  return (
    <div className="bg-control-gradient border border-matrix-accent rounded-cyber-lg p-4 backdrop-blur-sm">
      <div className="space-y-4">
        {[
          { label: 'Altitude', id: 'altitude', unit: 'ft' },
          { label: 'Speed', id: 'speed', unit: 'kts' },
          { label: 'Heading', id: 'heading', unit: '°' }
        ].map(({ label, id, unit }) => (
          <div key={label} className="group relative">
            <label
              htmlFor={id}
              className="block text-xs font-semibold text-radar-primary mb-1 transition-all duration-300
                         group-focus-within:text-neon-blue group-focus-within:drop-shadow-lg"
            >
              {label}
            </label>
            <div className="relative">
              <input
                type="number"
                id={id}
                placeholder="0"
                className="w-full px-3 py-2 bg-matrix-dark border border-matrix-accent rounded-cyber
                           text-white text-sm font-mono placeholder-gray-500
                           transition-all duration-300 ease-out
                           focus:outline-none focus:border-radar-primary focus:shadow-cyber focus:scale-[1.02]
                           hover:border-radar-secondary hover:shadow-neon
                           backdrop-blur-sm"
              />
              <span className="absolute right-2 top-1/2 transform -translate-y-1/2 text-xs text-gray-400 font-mono">
                {unit}
              </span>
              <div className="absolute inset-0 rounded-cyber opacity-0 group-focus-within:opacity-100
                              transition-opacity duration-300 pointer-events-none
                              bg-gradient-to-r from-transparent via-radar-primary/5 to-transparent"></div>
            </div>
          </div>
        ))}

        <button
          onClick={controlAircraft}
          className="w-full mt-4 px-4 py-3 bg-button-gradient text-matrix-dark font-bold text-sm
                     rounded-cyber-lg border border-transparent
                     transition-all duration-300 ease-out transform
                     hover:bg-button-hover-gradient hover:scale-105 hover:shadow-cyber-lg
                     active:scale-95 active:shadow-inset-cyber
                     focus:outline-none focus:ring-2 focus:ring-radar-primary focus:ring-offset-2 focus:ring-offset-matrix-dark
                     animate-glow"
        >
          <span className="relative z-10">EXECUTE COMMAND</span>
          <div className="absolute inset-0 rounded-cyber-lg bg-gradient-to-r from-transparent via-white/10 to-transparent
                          transform translate-x-[-100%] hover:animate-scan transition-transform duration-500"></div>
        </button>
      </div>
    </div>
  );
};

export default ControlAircraft;
