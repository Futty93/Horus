import type { ScenarioAircraft } from "@/utility/api/scenario";

interface AircraftTableProps {
  aircraft: ScenarioAircraft[];
  selectedCallsign: string | null;
  onSelectAircraft: (a: ScenarioAircraft) => void;
}

export function AircraftTable({
  aircraft,
  selectedCallsign,
  onSelectAircraft,
}: AircraftTableProps) {
  return (
    <div className="border border-atc-border rounded-lg overflow-x-auto">
      <table className="w-full text-sm">
        <thead>
          <tr className="bg-atc-surface-elevated">
            <th className="px-3 py-2 text-left font-mono">Callsign</th>
            <th className="px-3 py-2 text-left">Origin</th>
            <th className="px-3 py-2 text-left">Dest</th>
            <th className="px-3 py-2 text-left">Route</th>
            <th className="px-3 py-2 text-left">Alt</th>
            <th className="px-3 py-2 text-left">Spd</th>
          </tr>
        </thead>
        <tbody>
          {aircraft.map((a) => {
            const fp = a.flightPlan;
            const routeStr = fp.route.length
              ? fp.route.map((wp) => wp.fix).join(" → ")
              : "—";
            const isSelected = selectedCallsign === fp.callsign;
            return (
              <tr
                key={fp.callsign}
                className={`border-t border-atc-border cursor-pointer transition-colors ${
                  isSelected
                    ? "bg-atc-accent/20"
                    : "hover:bg-atc-surface-elevated"
                }`}
                onClick={() => onSelectAircraft(a)}
              >
                <td className="px-3 py-2 font-mono">{fp.callsign}</td>
                <td className="px-3 py-2">{fp.departureAirport}</td>
                <td className="px-3 py-2">{fp.arrivalAirport}</td>
                <td className="px-3 py-2 font-mono text-atc-text-muted">
                  {routeStr}
                </td>
                <td className="px-3 py-2">{fp.cruiseAltitude}</td>
                <td className="px-3 py-2">{fp.cruiseSpeed}</td>
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
}
