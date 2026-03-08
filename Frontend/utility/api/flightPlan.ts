const serverIp = process.env.NEXT_PUBLIC_SERVER_IP;
const serverPort = process.env.NEXT_PUBLIC_SERVER_PORT;

const baseUrl = () => `http://${serverIp}:${serverPort}`;

export interface FlightPlanStatus {
  callsign: string;
  navigationMode: string;
  currentWaypointIndex?: number;
  currentWaypoint?: string | null;
  remainingWaypoints?: string[];
  departureAirport?: string;
  arrivalAirport?: string;
  hasFlightPlan?: boolean;
}

export async function fetchFlightPlan(callsign: string): Promise<FlightPlanStatus | null> {
  try {
    const response = await fetch(`${baseUrl()}/api/aircraft/${callsign}/flightplan`, {
      method: "GET",
      headers: { Accept: "application/json" },
    });
    if (!response.ok) return null;
    return (await response.json()) as FlightPlanStatus;
  } catch (error) {
    console.error("Error fetching flight plan:", error);
    return null;
  }
}

export async function directToFix(
  callsign: string,
  fixName: string,
  resumeFlightPlan: boolean
): Promise<boolean> {
  try {
    const response = await fetch(`${baseUrl()}/api/aircraft/${callsign}/direct-to`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ fixName, resumeFlightPlan }),
    });
    return response.ok;
  } catch (error) {
    console.error("Error directing to fix:", error);
    return false;
  }
}

export async function resumeNavigation(callsign: string): Promise<boolean> {
  try {
    const response = await fetch(`${baseUrl()}/api/aircraft/${callsign}/resume-navigation`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
    });
    return response.ok;
  } catch (error) {
    console.error("Error resuming navigation:", error);
    return false;
  }
}
