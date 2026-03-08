"use client";

import React from "react";
import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import {
  SelectedAircraftProvider,
  useSelectedAircraft,
} from "./selectedAircraftContext";

function TestConsumer() {
  const { callsign, setCallsign, instructedVector, setInstructedVector } =
    useSelectedAircraft();
  return (
    <div>
      <span data-testid="callsign">{callsign ?? "null"}</span>
      <span data-testid="altitude">{instructedVector.altitude}</span>
      <span data-testid="groundSpeed">{instructedVector.groundSpeed}</span>
      <span data-testid="heading">{instructedVector.heading}</span>
      <button onClick={() => setCallsign("JAL123")}>Set Callsign</button>
      <button onClick={() => setCallsign(null)}>Clear Callsign</button>
      <button
        onClick={() =>
          setInstructedVector({
            altitude: 35000,
            groundSpeed: 250,
            heading: 90,
          })
        }
      >
        Set Vector
      </button>
    </div>
  );
}

describe("SelectedAircraftContext", () => {
  it("provides default values", () => {
    render(
      <SelectedAircraftProvider>
        <TestConsumer />
      </SelectedAircraftProvider>
    );

    expect(screen.getByTestId("callsign")).toHaveTextContent("null");
    expect(screen.getByTestId("altitude")).toHaveTextContent("0");
    expect(screen.getByTestId("groundSpeed")).toHaveTextContent("0");
    expect(screen.getByTestId("heading")).toHaveTextContent("0");
  });

  it("updates callsign when setCallsign is called", async () => {
    const user = userEvent.setup();
    render(
      <SelectedAircraftProvider>
        <TestConsumer />
      </SelectedAircraftProvider>
    );

    await user.click(screen.getByRole("button", { name: "Set Callsign" }));
    await waitFor(() =>
      expect(screen.getByTestId("callsign")).toHaveTextContent("JAL123")
    );

    await user.click(screen.getByRole("button", { name: "Clear Callsign" }));
    await waitFor(() =>
      expect(screen.getByTestId("callsign")).toHaveTextContent("null")
    );
  });

  it("updates instructedVector when setInstructedVector is called", async () => {
    const user = userEvent.setup();
    render(
      <SelectedAircraftProvider>
        <TestConsumer />
      </SelectedAircraftProvider>
    );

    await user.click(screen.getByRole("button", { name: "Set Vector" }));
    await waitFor(() => {
      expect(screen.getByTestId("altitude")).toHaveTextContent("35000");
      expect(screen.getByTestId("groundSpeed")).toHaveTextContent("250");
      expect(screen.getByTestId("heading")).toHaveTextContent("90");
    });
  });

  it("throws when useSelectedAircraft is used outside Provider", () => {
    const consoleSpy = jest
      .spyOn(console, "error")
      .mockImplementation(() => {});

    expect(() => {
      render(<TestConsumer />);
    }).toThrow(
      "useSelectedAircraft must be used within a SelectedAircraftProvider"
    );

    consoleSpy.mockRestore();
  });
});
