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
    <ul className="space-y-2">
      {aircraft.map((aircraftItem) => {
        const fp = aircraftItem.flightPlan;
        const selected = selectedCallsign === fp.callsign;
        const route =
          fp.route.length > 0
            ? fp.route.map((waypoint) => waypoint.fix).join(" · ")
            : "Route not set";
        return (
          <li key={fp.callsign}>
            <div
              className={`flex items-start gap-3 rounded-2xl px-3 py-3 ${
                selected
                  ? "bg-[#1c1915] text-[#f4efe4]"
                  : "bg-white text-[#1c1915]"
              }`}
            >
              <button
                type="button"
                onClick={() => onSelectAircraft(aircraftItem)}
                className="min-w-0 flex-1 text-left"
              >
                <span className="block font-[family-name:var(--font-geist-mono)] text-sm">
                  {fp.callsign}
                </span>
                <span
                  className={`mt-0.5 block text-xs ${
                    selected ? "text-[#d6cbb8]" : "text-[#6b6256]"
                  }`}
                >
                  {fp.departureAirport} → {fp.arrivalAirport}
                </span>
                <span
                  className={`mt-1 block truncate font-[family-name:var(--font-geist-mono)] text-[11px] ${
                    selected ? "text-[#f4efe4]/70" : "text-[#8a7d6b]"
                  }`}
                >
                  {route}
                </span>
              </button>
              {onDeleteAircraft && (
                <button
                  type="button"
                  onClick={() => onDeleteAircraft(fp.callsign)}
                  className={`text-xs ${
                    selected
                      ? "text-[#f4efe4]/70 hover:text-white"
                      : "text-[#c2410c] hover:underline"
                  }`}
                >
                  Delete
                </button>
              )}
            </div>
          </li>
        );
      })}
    </ul>
  );
}
