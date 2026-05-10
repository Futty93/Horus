export function FlightPlanSetupHeader() {
  return (
    <header>
      <h1 className="font-mono text-xl font-bold tracking-wider">
        FLIGHT PLAN SETUP
      </h1>
      <p className="text-sm text-atc-text-muted mt-1">
        Select an aircraft to edit its cruise route; use bulk O/D only when
        every flight on that pair should share one path.
      </p>
    </header>
  );
}
