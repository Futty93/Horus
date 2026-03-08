import { proxyToBackend } from "@/utility/api/backendProxy";

export const dynamic = "force-dynamic";

const VALID_ACTIONS = ["start", "pause"] as const;

export async function POST(
  request: Request,
  { params }: { params: Promise<{ action: string }> }
) {
  const { action } = await params;
  if (!VALID_ACTIONS.includes(action as (typeof VALID_ACTIONS)[number])) {
    return new Response(JSON.stringify({ error: "Invalid action" }), {
      status: 400,
      headers: { "Content-Type": "application/json" },
    });
  }
  const body = await request.text();
  return proxyToBackend(`/simulation/${action}`, {
    method: "POST",
    headers: {
      "Content-Type": request.headers.get("Content-Type") ?? "application/json",
    },
    body: body || undefined,
  });
}
