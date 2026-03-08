"use client";
import React from "react";
import { useSelectedAircraft } from "@/context/selectedAircraftContext";

const serverIp = process.env.NEXT_PUBLIC_SERVER_IP;
const serverPort = process.env.NEXT_PUBLIC_SERVER_PORT;

const InputAircraftInfo = () => {
  const { callsign, instructedVector, setInstructedVector } =
    useSelectedAircraft();

  const controlAircraft = async () => {
    if (!callsign || callsign.length < 2) {
      console.error("No aircraft selected");
      return;
    }

    const controlAircraftDto = {
      instructedAltitude: instructedVector.altitude,
      instructedGroundSpeed: instructedVector.groundSpeed,
      instructedHeading: instructedVector.heading,
    };

    try {
      const response = await fetch(
        `http://${serverIp}:${serverPort}/api/aircraft/control/${callsign}`,
        {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(controlAircraftDto),
        },
      );

      if (response.ok) {
        console.log(`Aircraft ${callsign} controlled successfully.`);
      } else {
        console.error(
          `Failed to control aircraft ${callsign}. Status:`,
          response.status,
        );
      }
    } catch (error) {
      console.error("Error occurred while controlling aircraft:", error);
    }
  };

  const fields = [
    { key: "altitude" as const, label: "Altitude" },
    { key: "groundSpeed" as const, label: "Speed" },
    { key: "heading" as const, label: "Heading" },
  ] as const;

  return (
    <div className="flex flex-col">
      {fields.map(({ key, label }) => (
        <div
          className="group mb-5 transition-transform duration-300 ease"
          key={key}
        >
          <div className="text-green-400 font-bold mb-1 group-focus-within:scale-105 transition-transform duration-300 ease">
            {label}
          </div>
          <input
            type="number"
            id={key}
            placeholder="0"
            value={instructedVector[key]}
            onChange={(e) =>
              setInstructedVector((prev) => ({
                ...prev,
                [key]: parseInt(e.target.value, 10) || 0,
              }))
            }
            className="group/item w-4/5 p-2 rounded-md border border-green-400 bg-gray-800 text-white mb-4 transition-all duration-300 ease focus:outline-none focus:border-green-400 focus:ring focus:ring-green-400/70 focus:shadow-lg-no-offset focus:shadow-green-400/70"
          />
        </div>
      ))}
      <button
        type="button"
        onClick={controlAircraft}
        className="bg-green-400 text-gray-900 border-none px-4 py-2 text-lg font-bold cursor-pointer transition-all duration-300 ease-in-out rounded-md hover:bg-green-500 hover:shadow-lg-no-offset hover:shadow-green-400/70"
      >
        Confirm
      </button>
    </div>
  );
};

export default InputAircraftInfo;
