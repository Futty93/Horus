import { proxyToBackend } from "@/utility/api/backendProxy";

export const dynamic = "force-dynamic";

export async function GET(request: Request) {
  const url = new URL(request.url);
  const airport = url.searchParams.get("airport");
  const path =
    airport && airport.length > 0
      ? `/ats/runways?airport=${encodeURIComponent(airport)}`
      : "/ats/runways";
  return proxyToBackend(path, {
    method: "GET",
    headers: { Accept: "application/json" },
  });
}
