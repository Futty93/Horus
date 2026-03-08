import { proxyToBackend } from "@/utility/api/backendProxy";

export const dynamic = "force-dynamic";

export async function POST() {
  return proxyToBackend("/api/aircraft/create-haneda-samples", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
  });
}
