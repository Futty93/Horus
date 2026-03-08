import type { InstructedVector } from "@/context/selectedAircraftContext";

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
): Promise<boolean> {
  if (!callsign || callsign.length < 2) {
    console.error("No aircraft selected");
    return false;
  }

  const dto = toDto(instructedVector);

  try {
    const response = await fetch(`/api/aircraft/control/${callsign}`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(dto),
    });

    if (response.ok) {
      console.log(`Aircraft ${callsign} controlled successfully.`);
      return true;
    }
    console.error(
      `Failed to control aircraft ${callsign}. Status:`,
      response.status
    );
    return false;
  } catch (error) {
    console.error("Error occurred while controlling aircraft:", error);
    return false;
  }
}
