import { proxyToBackend } from "@/utility/api/backendProxy";

export const dynamic = "force-dynamic";

export async function POST(
  request: Request,
  { params }: { params: Promise<{ callsign: string }> }
) {
  const { callsign } = await params;
  const body = await request.text();
  return proxyToBackend(
    `/api/aircraft/${encodeURIComponent(callsign)}/atc-clearance`,
    {
      method: "POST",
      headers: {
        "Content-Type":
          request.headers.get("Content-Type") ?? "application/json",
      },
      body: body || undefined,
    }
  );
}
