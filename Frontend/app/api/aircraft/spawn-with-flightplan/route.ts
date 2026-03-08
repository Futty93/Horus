import { proxyToBackend } from "@/utility/api/backendProxy";

export const dynamic = "force-dynamic";

export async function POST(request: Request) {
  const body = await request.text();
  return proxyToBackend("/api/aircraft/spawn-with-flightplan", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body,
  });
}
