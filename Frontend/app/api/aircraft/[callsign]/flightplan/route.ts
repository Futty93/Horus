import { proxyToBackend } from "@/utility/api/backendProxy";

export const dynamic = "force-dynamic";

export async function GET(
  _req: Request,
  { params }: { params: Promise<{ callsign: string }> }
) {
  const { callsign } = await params;
  return proxyToBackend(`/api/aircraft/${callsign}/flightplan`, {
    method: "GET",
    headers: { Accept: "application/json" },
  });
}
