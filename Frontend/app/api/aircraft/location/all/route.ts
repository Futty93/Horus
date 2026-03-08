import { proxyToBackend } from "@/utility/api/backendProxy";

export const dynamic = "force-dynamic";

export async function GET(request: Request) {
  return proxyToBackend("/aircraft/location/all", {
    method: "GET",
    headers: {
      Accept: request.headers.get("Accept") ?? "application/json",
    },
    cache: "no-store",
  });
}
