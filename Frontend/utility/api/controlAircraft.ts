import type { InstructedVector } from "@/context/selectedAircraftContext";

const serverIp = process.env.NEXT_PUBLIC_SERVER_IP;
const serverPort = process.env.NEXT_PUBLIC_SERVER_PORT;

export interface ControlAircraftDto {
  instructedAltitude: number;
  instructedGroundSpeed: number;
  instructedHeading: number;
}

function toDto(vector: InstructedVector): ControlAircraftDto {
  return {
    instructedAltitude: vector.altitude,
    instructedGroundSpeed: vector.groundSpeed,
    instructedHeading: vector.heading,
  };
}

export async function controlAircraft(
  callsign: string,
  instructedVector: InstructedVector
): Promise<void> {
  if (!callsign || callsign.length < 2) {
    console.error("No aircraft selected");
    return;
  }

  const dto = toDto(instructedVector);

  try {
    const response = await fetch(
      `http://${serverIp}:${serverPort}/api/aircraft/control/${callsign}`,
      {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(dto),
      }
    );

    if (response.ok) {
      console.log(`Aircraft ${callsign} controlled successfully.`);
    } else {
      console.error(
        `Failed to control aircraft ${callsign}. Status:`,
        response.status
      );
    }
  } catch (error) {
    console.error("Error occurred while controlling aircraft:", error);
  }
}
