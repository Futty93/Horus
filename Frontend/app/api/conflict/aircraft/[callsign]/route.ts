import { proxyToBackend } from "@/utility/api/backendProxy";

export const dynamic = "force-dynamic";

export async function GET(
  request: Request,
  context: { params: { callsign: string } }
) {
  const raw = context.params.callsign;
  const callsign = encodeURIComponent(raw);
  return proxyToBackend(`/api/conflict/aircraft/${callsign}`, {
    method: "GET",
    headers: {
      Accept: request.headers.get("Accept") ?? "application/json",
    },
    cache: "no-store",
  });
}
