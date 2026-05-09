import { proxyToBackend } from "@/utility/api/backendProxy";

export async function GET() {
  return proxyToBackend("/simulation/speed", { method: "GET" });
}

export async function PUT(request: Request) {
  const body = await request.text();
  return proxyToBackend("/simulation/speed", {
    method: "PUT",
    headers: {
      "Content-Type": request.headers.get("Content-Type") ?? "application/json",
    },
    body: body || undefined,
  });
}
