import { Coordinate } from "@/context/centerCoordinateContext";
import { Aircraft } from "../aircraft/aircraftClass";
import { CoordinateManager } from "../coordinateManager/CoordinateManager";
import { GLOBAL_SETTINGS } from "../globals/settings";
import { DisplayRange } from "@/context/displayRangeContext";

const serverIp = process.env.NEXT_PUBLIC_SERVER_IP;
const serverPort = process.env.NEXT_PUBLIC_SERVER_PORT;

export const fetchAircraftLocation = async (controllingAircrafts: Aircraft[], centerCoordinate: Coordinate, displayRange: DisplayRange, pathname: string) => {
  let updatedControllingAircraft: Aircraft[] = [];
  try {
    const response = await fetch(
      `http://${serverIp}:${serverPort}/aircraft/location/all`,
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

      const aircraftData = parseAircraftData(textData, centerCoordinate, displayRange); // Parse the text data
      if (aircraftData) {
        updatedControllingAircraft =  updateControllingAircrafts(aircraftData, controllingAircrafts, pathname); // Call the function to update controlledAirplanes
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
const parseAircraftData = (data: string, centerCoordinate: Coordinate, displayRange: DisplayRange): Aircraft[] | null => {
  // Implement parsing logic based on the actual format of your data
  // This is a placeholder example; you'll need to adjust it according to the actual format
  try {
    // Example parsing logic (assuming data is in some custom text format)
    const aircraftStrings = data.split("\n").filter((line) =>
      line.startsWith("Aircraft{") && line.trim() !== ""
    );
    return aircraftStrings.map((aircraftString) => {
      // Parse each aircraftString into an Aircraft object
      // Example: Implement a function to parse the string into a valid Aircraft object
      return parseAircraftString(aircraftString, centerCoordinate, displayRange);
    });
  } catch (error) {
    console.error("Error parsing aircraft data:", error);
    return null;
  }
};

const parseAircraftString = (aircraftString: string, centerCoordinate: Coordinate, displayRange: DisplayRange): Aircraft => {
  // 基本フィールドを抽出（全航空機タイプ共通）
  const basicRegex = /callsign=(.*?), position=\{latitude=(.*?), longitude=(.*?), altitude=(.*?)\}, vector=\{heading=(.*?), groundSpeed=(.*?), verticalSpeed=(.*?)\}, instructedVector=\{heading=(.*?), groundSpeed=(.*?), altitude=(.*?)\}, type=(.*?), model=(.*?)(?:,|, riskLevel=)/;

  const basicMatches = aircraftString.match(basicRegex);

  if (!basicMatches) {
    throw new Error("Failed to parse basic aircraft data: " + aircraftString);
  }

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
    model,
  ] = basicMatches;

  // 航空機タイプ別の追加フィールドを抽出
  let originIata = "", originIcao = "", destinationIata = "", destinationIcao = "", eta = "";

  if (type === "COMMERCIAL_PASSENGER" || type === "COMMERCIAL_CARGO") {
    // 商用航空機の場合、出発地・到着地・ETA情報を抽出
    const commercialRegex = /originIata=(.*?), originIcao=(.*?), destinationIata=(.*?), destinationIcao=(.*?), eta=(.*?)(?:, riskLevel=|$)/;
    const commercialMatches = aircraftString.match(commercialRegex);
    if (commercialMatches) {
      [, originIata, originIcao, destinationIata, destinationIcao, eta] = commercialMatches;
    }
  }

  // riskLevel を抽出
  const riskRegex = /riskLevel=([\d.]+)/;
  const riskMatch = aircraftString.match(riskRegex);
  const riskLevel = riskMatch ? parseFloat(riskMatch[1]) : 0;

  // Convert latitude and longitude into canvas coordinates using radarGame utility
  const coordinateOnCanvas = CoordinateManager.calculateCanvasCoordinates(
    { latitude: parseFloat(lat), longitude: parseFloat(lon) },
    centerCoordinate,
    displayRange
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
    model,
    originIata,
    originIcao,
    destinationIata,
    destinationIcao,
    eta,
    50, // labelX
    50, // labelY
    riskLevel // riskLevel
  );
};

// Function to update controlledAirplanes based on API data (from earlier code)
function updateControllingAircrafts(apiResponse: Aircraft[], controllingAircrafts: Aircraft[], pathname: string): Aircraft[] {
  const newAircraftMap = new Map<string, Aircraft>();

  apiResponse.forEach((aircraft) => {
    newAircraftMap.set(aircraft.callsign, aircraft);
  });

  controllingAircrafts = controllingAircrafts.filter(
    (airplane) => {
      const newAircraft = newAircraftMap.get(airplane.callsign);
      if (newAircraft) {
        if (pathname === "/operator") {
          airplane.updateAircraftInfo(newAircraft);
        } else {
          airplane.updateAircraftLocationInfo(newAircraft);
        }
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
      newAircraft.model,
      newAircraft.originIata,
      newAircraft.originIcao,
      newAircraft.destinationIata,
      newAircraft.destinationIcao,
      newAircraft.eta,
      50, // labelX
      50, // labelY
      newAircraft.riskLevel // riskLevel
    );
    controllingAircrafts.push(newAirplane);
  });

  return controllingAircrafts;
}
