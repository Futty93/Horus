import type { RoutePreviewPickTarget } from "@/utility/flightPlanSetup/routePreviewGeometry";

export type { RoutePreviewPickTarget };

export type RoutePreviewPickPayload =
  | { kind: "append"; fixName: string }
  | { kind: "insert"; fixName: string; insertIndex: number };
