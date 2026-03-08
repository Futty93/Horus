import { proxyToBackend } from "@/utility/api/backendProxy";

export const dynamic = "force-dynamic";

export async function POST(
  _req: Request,
  { params }: { params: Promise<{ callsign: string }> }
) {
  const { callsign } = await params;
  return proxyToBackend(`/api/aircraft/${callsign}/resume-navigation`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
  });
}
