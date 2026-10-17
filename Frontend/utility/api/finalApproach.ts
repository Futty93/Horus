export async function joinFinalApproach(
  callsign: string,
  airportIcao: string,
  runwayDesignator: string
): Promise<boolean> {
  if (!callsign) return false;
  try {
    const response = await fetch(`/api/aircraft/${callsign}/final-approach`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ airportIcao, runwayDesignator }),
    });
    return response.ok;
  } catch {
    return false;
  }
}
