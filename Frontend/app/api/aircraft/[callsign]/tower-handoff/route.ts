import { proxyToBackend } from "@/utility/api/backendProxy";

export const dynamic = "force-dynamic";

export async function POST(
  request: Request,
  context: { params: Promise<{ callsign: string }> }
) {
  const { callsign } = await context.params;
  const body = await request.text();
  return proxyToBackend(
    `/api/aircraft/${encodeURIComponent(callsign)}/tower-handoff`,
    {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body,
    }
  );
}
