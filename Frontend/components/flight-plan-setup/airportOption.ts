export type AirportOption = {
  icaoCode: string;
  name?: string;
  iataCode?: string;
  latitude: number;
  longitude: number;
};

export function airportLabel(airport: AirportOption): string {
  const name = airport.name?.trim();
  const iata = airport.iataCode?.trim();
  if (name && iata) return `${airport.icaoCode}  ${name} (${iata})`;
  if (name) return `${airport.icaoCode}  ${name}`;
  return airport.icaoCode;
}

export function filterAirports(
  airports: readonly AirportOption[],
  query: string
): AirportOption[] {
  const q = query.trim().toUpperCase();
  if (!q) return airports.slice(0, 8);
  const scored = airports.filter((airport) => {
    const haystack = [airport.icaoCode, airport.iataCode, airport.name]
      .filter(Boolean)
      .join(" ")
      .toUpperCase();
    return haystack.includes(q);
  });
  scored.sort((a, b) => {
    const aPrefix = a.icaoCode.startsWith(q) || a.iataCode?.startsWith(q);
    const bPrefix = b.icaoCode.startsWith(q) || b.iataCode?.startsWith(q);
    if (aPrefix !== bPrefix) return aPrefix ? -1 : 1;
    return a.icaoCode.localeCompare(b.icaoCode);
  });
  return scored.slice(0, 8);
}
