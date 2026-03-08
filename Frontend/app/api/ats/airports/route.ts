import { proxyToBackend } from "@/utility/api/backendProxy";

export const dynamic = "force-dynamic";

export async function GET() {
  return proxyToBackend("/ats/airports", {
    method: "GET",
    headers: { Accept: "application/json" },
  });
}
