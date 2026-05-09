export const SIMULATION_SPEED_PRESETS = [0.25, 0.5, 1, 2, 4, 10] as const;

export interface SimulationSpeed {
  speedMultiplier: number;
  tickIntervalWallMs: number;
}

export async function fetchSimulationSpeed(): Promise<SimulationSpeed | null> {
  try {
    const response = await fetch("/api/simulation/speed", {
      method: "GET",
      headers: { Accept: "application/json" },
    });
    if (!response.ok) return null;
    return (await response.json()) as SimulationSpeed;
  } catch (error) {
    console.error("Error fetching simulation speed:", error);
    return null;
  }
}

export async function setSimulationSpeed(
  speedMultiplier: number
): Promise<SimulationSpeed | null> {
  try {
    const response = await fetch("/api/simulation/speed", {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ speedMultiplier }),
    });
    if (!response.ok) {
      console.error("Failed to set simulation speed:", response.status);
      return null;
    }
    return fetchSimulationSpeed();
  } catch (error) {
    console.error("Error setting simulation speed:", error);
    return null;
  }
}

export function isSameSpeedMultiplier(a: number, b: number): boolean {
  return Math.abs(a - b) < 1e-6;
}
