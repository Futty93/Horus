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

export async function POST(
  request: Request,
  { params }: { params: Promise<{ callsign: string }> }
) {
  const { callsign } = await params;
  const body = await request.text();
  return proxyToBackend(`/api/aircraft/${callsign}/flightplan`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body,
  });
}
