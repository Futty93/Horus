import type { ControlAircraftDto } from "@/utility/api/controlAircraft";

export async function postAtcClearance(
  callsign: string,
  dto: ControlAircraftDto
): Promise<boolean> {
  if (!callsign || callsign.length < 2) {
    return false;
  }
  const path = `/api/aircraft/${encodeURIComponent(callsign)}/atc-clearance`;
  try {
    const response = await fetch(path, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(dto),
    });
    return response.ok;
  } catch {
    return false;
  }
}
