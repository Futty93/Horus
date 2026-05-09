"use client";

import React, { useEffect } from "react";
import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import FlightPlanControl from "./flightPlanControl";
import {
  SelectedAircraftProvider,
  useSelectedAircraft,
} from "@/context/selectedAircraftContext";
import * as flightPlanApi from "@/utility/api/flightPlan";

jest.mock("@/utility/api/flightPlan");

function StateSetter({ callsign }: { callsign: string | null }) {
  const { setCallsign } = useSelectedAircraft();
  useEffect(() => {
    setCallsign(callsign);
  }, [callsign, setCallsign]);
  return null;
}

describe("FlightPlanControl", () => {
  it("shows 'Select aircraft' when no callsign", () => {
    render(
      <SelectedAircraftProvider>
        <FlightPlanControl />
      </SelectedAircraftProvider>
    );

    expect(
      screen.getByText(/Select aircraft to issue navigation commands/i)
    ).toBeInTheDocument();
  });

  it("renders guidance and RESUME button when callsign set", () => {
    render(
      <SelectedAircraftProvider>
        <StateSetter callsign="JAL123" />
        <FlightPlanControl />
      </SelectedAircraftProvider>
    );

    expect(
      screen.getByText(/panel to select a fix and choose direct\/hold/i)
    ).toBeInTheDocument();
    expect(
      screen.getByRole("button", { name: /RESUME OWN NAVIGATION/i })
    ).toBeInTheDocument();
  });

  it("shows success message when resumeNavigation succeeds", async () => {
    (flightPlanApi.resumeNavigation as jest.Mock).mockResolvedValue(true);
    const user = userEvent.setup();

    render(
      <SelectedAircraftProvider>
        <StateSetter callsign="JAL123" />
        <FlightPlanControl />
      </SelectedAircraftProvider>
    );

    await user.click(
      screen.getByRole("button", { name: /RESUME OWN NAVIGATION/i })
    );

    await waitFor(() => {
      expect(screen.getByText(/Resume applied/i)).toBeInTheDocument();
    });
  });
});
