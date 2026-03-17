import type { ScenarioJson } from "@/types/scenario";
import hanedaTemplateJson from "@/data/haneda-template.json";

export const hanedaTemplate: ScenarioJson = hanedaTemplateJson as ScenarioJson;

export function toScenarioJsonString(
  scenario: ScenarioJson,
  pretty = true
): string {
  return JSON.stringify(scenario, null, pretty ? 2 : 0);
}

export function getExportFilename(
  scenario: ScenarioJson,
  timestamp = Date.now()
): string {
  const base = `scenario-${scenario.scenarioName ?? "export"}-${timestamp}.json`;
  return base.replace(/\s+/g, "-");
}

export function exportScenario(scenario: ScenarioJson): void {
  const json = toScenarioJsonString(scenario);
  const blob = new Blob([json], { type: "application/json" });
  const url = URL.createObjectURL(blob);
  const a = document.createElement("a");
  a.href = url;
  a.download = getExportFilename(scenario);
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
  URL.revokeObjectURL(url);
}

function isScenarioLike(x: unknown): x is { aircraft: unknown[] } {
  return (
    !!x &&
    typeof x === "object" &&
    Array.isArray((x as Record<string, unknown>).aircraft)
  );
}

export function parseScenarioJson(json: string): ScenarioJson {
  const parsed = JSON.parse(json) as unknown;
  if (!isScenarioLike(parsed)) {
    throw new Error("Invalid scenario: missing aircraft array");
  }
  for (const a of parsed.aircraft) {
    const ac = a as Record<string, unknown>;
    const fp = ac?.flightPlan as Record<string, unknown> | undefined;
    if (!fp || typeof fp?.callsign !== "string" || !ac?.initialPosition) {
      throw new Error(
        "Invalid aircraft: each must have flightPlan and initialPosition"
      );
    }
  }
  return parsed as ScenarioJson;
}

export async function loadScenarioAndStart(
  scenario: ScenarioJson
): Promise<{ ok: boolean; message: string }> {
  try {
    const response = await fetch("/api/scenario/load", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(scenario),
    });
    if (!response.ok) {
      const text = await response.text();
      const parsed = parseJsonMessage(text);
      return {
        ok: false,
        message: parsed ?? (text || `HTTP ${response.status}`),
      };
    }
    return { ok: true, message: "Scenario loaded" };
  } catch (e) {
    return { ok: false, message: String(e) };
  }
}

function parseJsonMessage(text: string): string | null {
  if (!text?.trim()) return null;
  try {
    const obj = JSON.parse(text) as unknown;
    if (obj && typeof obj === "object" && "message" in obj) {
      const m = (obj as { message: unknown }).message;
      if (typeof m === "string") return m;
    }
  } catch {
    /* ignore */
  }
  return null;
}
