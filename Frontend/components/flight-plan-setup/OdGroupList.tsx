import { OdGroupSection } from "./OdGroupSection";
import type { ScenarioAircraft } from "@/utility/api/scenario";
import type { Route } from "@/utility/AtsRouteManager/RouteInterfaces/Route";

type RouteDef = {
  waypoints: string[];
  cruiseAltitude: number;
  cruiseSpeed: number;
};

interface OdGroupListProps {
  odPairs: {
    origin: string;
    destination: string;
    aircraft: ScenarioAircraft[];
  }[];
  selectedCallsign: string | null;
  onSelectAircraft: (a: ScenarioAircraft) => void;
  onSuggestStatus?: (status: string | null) => void;
  atsRoutes: { atsLowerRoutes: Route[]; rnavRoutes: Route[] };
  routeByOd: Map<string, RouteDef>;
  onRouteChange: (key: string, route: RouteDef) => void;
}

export function OdGroupList({
  odPairs,
  selectedCallsign,
  onSelectAircraft,
  onSuggestStatus,
  atsRoutes,
  routeByOd,
  onRouteChange,
}: OdGroupListProps) {
  return (
    <section>
      <h2 className="font-mono text-sm font-bold mb-3">
        Grouped by Origin → Destination
      </h2>
      <div className="space-y-3">
        {odPairs.map((pair) => (
          <OdGroupSection
            key={`${pair.origin}→${pair.destination}`}
            origin={pair.origin}
            destination={pair.destination}
            aircraft={pair.aircraft}
            selectedCallsign={selectedCallsign}
            onSelectAircraft={onSelectAircraft}
            onSuggestStatus={onSuggestStatus}
            atsRoutes={atsRoutes}
            routeByOd={routeByOd}
            onRouteChange={onRouteChange}
          />
        ))}
      </div>
    </section>
  );
}
