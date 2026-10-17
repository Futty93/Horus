interface FlightPlanSetupHeaderProps {
  count: number;
}

export function FlightPlanSetupHeader({ count }: FlightPlanSetupHeaderProps) {
  return (
    <header className="mb-3">
      <p className="text-[11px] font-semibold uppercase tracking-[0.22em] text-[#8a7d6b]">
        Horus
      </p>
      <h1 className="mt-1 font-[family-name:var(--font-geist-sans)] text-3xl font-semibold tracking-tight text-[#1c1915]">
        Plan the flight
      </h1>
      <p className="mt-1 text-sm text-[#6b6256]">
        {count === 0
          ? "Name the flight and pick two airports. The chart does the rest."
          : `${count} aircraft on this plan.`}
      </p>
    </header>
  );
}
