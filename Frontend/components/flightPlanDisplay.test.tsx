"use client";

import React, { useEffect } from "react";
import { render, screen, waitFor } from "@testing-library/react";
import FlightPlanDisplay from "./flightPlanDisplay";
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

describe("FlightPlanDisplay", () => {
  beforeEach(() => {
    jest.useFakeTimers();
  });

  afterEach(() => {
    jest.useRealTimers();
  });

  it("shows 'Select aircraft to view flight plan' when no callsign", () => {
    render(
      <SelectedAircraftProvider>
        <FlightPlanDisplay />
      </SelectedAircraftProvider>
    );

    expect(
      screen.getByText(/Select aircraft to view flight plan/i)
    ).toBeInTheDocument();
  });

  it.each([
    [
      "has plan",
      {
        callsign: "JAL123",
        navigationMode: "DIRECT",
        remainingWaypoints: ["ABENO", "OMOTE"],
        hasFlightPlan: true,
        departureAirport: "RJTT",
        arrivalAirport: "RJAA",
        currentWaypoint: "ABENO",
      },
      () => {
        expect(screen.getByText(/FLIGHT PLAN — DIRECT/i)).toBeInTheDocument();
        expect(screen.getByText(/RJTT → RJAA/)).toBeInTheDocument();
        expect(screen.getByText("Current:")).toBeInTheDocument();
        expect(screen.getAllByText("ABENO").length).toBeGreaterThan(0);
        expect(screen.getByText(/OMOTE/)).toBeInTheDocument();
      },
    ],
    [
      "no active plan",
      {
        callsign: "JAL123",
        navigationMode: "DIRECT",
        remainingWaypoints: [],
        hasFlightPlan: false,
      },
      () => {
        expect(screen.getByText(/No active flight plan/i)).toBeInTheDocument();
      },
    ],
  ] as const)("displays correctly when %s", async (_, mockStatus, assert) => {
    (flightPlanApi.fetchFlightPlan as jest.Mock).mockResolvedValue(mockStatus);

    render(
      <SelectedAircraftProvider>
        <StateSetter callsign="JAL123" />
        <FlightPlanDisplay />
      </SelectedAircraftProvider>
    );

    await waitFor(assert);
  });
});
