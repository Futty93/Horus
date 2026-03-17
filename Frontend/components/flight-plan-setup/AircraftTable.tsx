import type { ScenarioAircraft } from "@/types/scenario";

interface AircraftTableProps {
  aircraft: ScenarioAircraft[];
  selectedCallsign: string | null;
  onSelectAircraft: (a: ScenarioAircraft) => void;
  onDeleteAircraft?: (callsign: string) => void;
}

export function AircraftTable({
  aircraft,
  selectedCallsign,
  onSelectAircraft,
  onDeleteAircraft,
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
            {onDeleteAircraft && (
              <th className="px-2 py-2 text-right w-12">—</th>
            )}
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
                {onDeleteAircraft && (
                  <td
                    className="px-2 py-2 text-right"
                    onClick={(e) => e.stopPropagation()}
                  >
                    <button
                      type="button"
                      onClick={() => onDeleteAircraft(fp.callsign)}
                      className="px-2 py-1 text-xs font-bold text-atc-danger hover:bg-atc-danger/20 rounded
                                 focus:outline-none focus:ring-1 focus:ring-atc-danger"
                      title="Delete aircraft"
                    >
                      Delete
                    </button>
                  </td>
                )}
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
}
