"use client";
import React from 'react';

const InputAircraftInfo = () => {
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
  };


  return (
    <div className="flex flex-col">
      {['Altitude', 'Speed', 'Heading'].map((label) => (
        <div className="group mb-5 transition-transform duration-300 ease" key={label}>
          <div className="text-green-400 font-bold mb-1 group-focus-within:scale-105 transition-transform duration-300 ease">{label}</div>
          <input
            type="number"
            id={label.toLowerCase()}
            placeholder="0"
            className="group/item w-4/5 p-2 rounded-md border border-green-400 bg-gray-800 text-white mb-4 transition-all duration-300 ease focus:outline-none focus:border-green-400 focus:ring focus:ring-green-400/70 focus:shadow-lg-no-offset focus:shadow-green-400/70"
          />
        </div>
      ))}
      <input
        type="button"
        value="Confirm"
        onClick={controlAircraft}
        id='confirmButton'
        className="bg-green-400 text-gray-900 border-none px-4 py-2 text-lg font-bold cursor-pointer transition-all duration-300 ease-in-out rounded-md hover:bg-green-500 hover:shadow-lg-no-offset hover:shadow-green-400/70"
      />
    </div>
  );
};

export default InputAircraftInfo;