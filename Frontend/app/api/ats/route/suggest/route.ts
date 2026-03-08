import { NextRequest } from "next/server";
import { proxyToBackend } from "@/utility/api/backendProxy";

export const dynamic = "force-dynamic";

export async function GET(request: NextRequest) {
  const { searchParams } = new URL(request.url);
  const origin = searchParams.get("origin") ?? "";
  const destination = searchParams.get("destination") ?? "";
  const query = new URLSearchParams({ origin, destination }).toString();
  return proxyToBackend(`/ats/route/suggest?${query}`, {
    method: "GET",
  });
}
