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

export async function goAround(
  callsign: string,
  airportIcao: string,
  runwayDesignator: string,
  climbAltitudeFt: number
): Promise<{ ok: boolean; message?: string }> {
  if (!callsign) return { ok: false, message: "No aircraft selected" };
  try {
    const response = await fetch(`/api/aircraft/${callsign}/go-around`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ airportIcao, runwayDesignator, climbAltitudeFt }),
    });
    if (response.ok) return { ok: true };
    const body = (await response.json().catch(() => null)) as {
      message?: string;
      detail?: string;
    } | null;
    return {
      ok: false,
      message: body?.message ?? body?.detail ?? "Failed to go around",
    };
  } catch {
    return { ok: false, message: "Failed to go around" };
  }
}
