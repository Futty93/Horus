export async function suggestRoute(
  origin: string,
  destination: string
): Promise<{ waypoints: string[] } | { error: string }> {
  const o = origin.trim();
  const d = destination.trim();
  const params = new URLSearchParams({ origin: o, destination: d });
  const response = await fetch(`/api/ats/route/suggest?${params}`, {
    method: "GET",
    headers: { Accept: "application/json" },
  });
  const data = (await response.json()) as
    | { waypoints: string[] }
    | { error: string };

  if (!response.ok) {
    return {
      error: "error" in data ? data.error : `HTTP ${response.status}`,
    };
  }
  if (!("waypoints" in data) || !Array.isArray(data.waypoints)) {
    return { error: "Invalid response" };
  }
  const waypoints = data.waypoints;
  const reason =
    "reason" in data ? (data as { reason?: string }).reason : undefined;
  return reason ? { waypoints, reason } : { waypoints };
}
