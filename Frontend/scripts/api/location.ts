import { Aircraft } from "../aircraft/aircraftClass";
import { CoordinateManager } from "../coordinateManager/CoordinateManager";

export const fetchAircraftLocation = async (controllingAircrafts: Aircraft[]) => {
  let updatedControllingAircraft: Aircraft[] = [];
  try {
    const response = await fetch(
      "http://localhost:8080/aircraft/location/all",
      {
        method: "GET",
        headers: {
          "accept": "*/*", // Assuming the server sends a custom format
        },
      },
    );

    if (response.ok) {
      const textData = await response.text(); // Fetches text data
      // console.log("Raw Aircraft Locations:", textData);

      // Example: Parsing custom format (e.g., CommercialAircraft{callsign=...})
      const aircraftData = parseAircraftData(textData);
      if (aircraftData) {
        updatedControllingAircraft =  updateControllingAircrafts(aircraftData, controllingAircrafts); // Call the function to update controlledAirplanes
        
      } else {
        console.error("Failed to parse aircraft data");
      }
    } else {
      console.error("Request failed with status:", response.status);
    }
  } catch (error) {
    console.error("Error occurred while fetching aircraft location:", error);
  }
  return updatedControllingAircraft;
};

// Function to parse custom format into an array of objects
const parseAircraftData = (data: string): Aircraft[] | null => {
  // Implement parsing logic based on the actual format of your data
  // This is a placeholder example; you'll need to adjust it according to the actual format
  try {
    // Example parsing logic (assuming data is in some custom text format)
    const aircraftStrings = data.split("\n").filter((line) =>
      line.startsWith("CommercialAircraft")
    );
    return aircraftStrings.map((aircraftString) => {
      // Parse each aircraftString into an Aircraft object
      // Example: Implement a function to parse the string into a valid Aircraft object
      return parseAircraftString(aircraftString);
    });
  } catch (error) {
    console.error("Error parsing aircraft data:", error);
    return null;
  }
};

const parseAircraftString = (aircraftString: string): Aircraft => {
  const aircraftRegex =
    /callsign=(.*?), position=\{latitude=(.*?), longitude=(.*?), altitude=(.*?)\}, vector=\{heading=(.*?), groundSpeed=(.*?), verticalSpeed=(.*?)\}, instructedVector=\{heading=(.*?), groundSpeed=(.*?), altitude=(.*?)\}, type=(.*?), originIata=(.*?), originIcao=(.*?), destinationIata=(.*?), destinationIcao=(.*?), eta=(.*?)\}/;
  const matches = aircraftString.match(aircraftRegex);
  if (matches) {
    const [
      _,
      callsign,
      lat,
      lon,
      altitude,
      heading,
      groundSpeed,
      verticalSpeed,
      instructedHeading,
      instructedGroundSpeed,
      instructedAltitude,
      type,
      originIata,
      originIcao,
      destinationIata,
      destinationIcao,
      eta,
    ] = matches;

    // Convert latitude and longitude into canvas coordinates using radarGame utility
    const coordinateOnCanvas = CoordinateManager.calculateCanvasCoordinates(
        parseFloat(lat),
        parseFloat(lon),
      );

    return new Aircraft(
      callsign,
      {
        x: coordinateOnCanvas.x,
        y: coordinateOnCanvas.y,
        altitude: parseFloat(altitude),
      }, // position
      {
        heading: parseFloat(heading),
        groundSpeed: parseFloat(groundSpeed),
        verticalSpeed: parseFloat(verticalSpeed),
      }, // vector
      {
        heading: parseFloat(instructedHeading),
        groundSpeed: parseFloat(instructedGroundSpeed),
        altitude: parseFloat(instructedAltitude),
      }, // instructedVector
      type,
      originIata,
      originIcao,
      destinationIata,
      destinationIcao,
      eta,
    );
  }

  throw new Error("Failed to parse aircraft string: " + aircraftString);
};

// Function to update controlledAirplanes based on API data (from earlier code)
function updateControllingAircrafts(apiResponse: Aircraft[], controllingAircrafts: Aircraft[]): Aircraft[] {
  const newAircraftMap = new Map<string, Aircraft>();

  apiResponse.forEach((aircraft) => {
    newAircraftMap.set(aircraft.callsign, aircraft);
  });

  controllingAircrafts = controllingAircrafts.filter(
    (airplane) => {
      const newAircraft = newAircraftMap.get(airplane.callsign);
      if (newAircraft) {
        airplane.updateAircraftInfo(newAircraft);
        newAircraftMap.delete(airplane.callsign);
        return true;
      } else {
        return false;
      }
    },
  );

  newAircraftMap.forEach((newAircraft) => {
    const newAirplane = new Aircraft(
      newAircraft.callsign,
      newAircraft.position,
      newAircraft.vector,
      newAircraft.instructedVector,
      newAircraft.type,
      newAircraft.originIata,
      newAircraft.originIcao,
      newAircraft.destinationIata,
      newAircraft.destinationIcao,
      newAircraft.eta,
    );
    controllingAircrafts.push(newAirplane);
  });

  return controllingAircrafts;
}