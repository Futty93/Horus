import { proxyToBackend } from "@/utility/api/backendProxy";

export const dynamic = "force-dynamic";

export async function GET(request: Request) {
  const url = new URL(request.url);
  const level = url.searchParams.get("level")?.trim() || "WHITE_CONFLICT";
  const qs = new URLSearchParams({ level });
  return proxyToBackend(`/api/conflict/filtered?${qs.toString()}`, {
    method: "GET",
    headers: {
      Accept: request.headers.get("Accept") ?? "application/json",
    },
    cache: "no-store",
  });
}
