"use client";

import React, { useEffect } from "react";
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

  it("invokes registered handler when applyInstructedVectorToRadar is called", async () => {
    const handler = jest.fn();
    function RegistrarAndTrigger() {
      const {
        registerApplyInstructedVectorHandler,
        applyInstructedVectorToRadar,
        setCallsign,
        setInstructedVector,
      } = useSelectedAircraft();

      useEffect(() => {
        const unregister = registerApplyInstructedVectorHandler(handler);
        return unregister;
      }, [registerApplyInstructedVectorHandler]);

      return (
        <div>
          <button
            onClick={() => {
              setCallsign("ANA456");
              setInstructedVector({
                altitude: 37000,
                groundSpeed: 280,
                heading: 180,
              });
            }}
          >
            Set State
          </button>
          <button
            onClick={() =>
              applyInstructedVectorToRadar("ANA456", {
                altitude: 37000,
                groundSpeed: 280,
                heading: 180,
              })
            }
          >
            Apply
          </button>
        </div>
      );
    }

    const user = userEvent.setup();
    render(
      <SelectedAircraftProvider>
        <RegistrarAndTrigger />
      </SelectedAircraftProvider>
    );

    await user.click(screen.getByRole("button", { name: "Apply" }));
    await waitFor(() => {
      expect(handler).toHaveBeenCalledTimes(1);
      expect(handler).toHaveBeenCalledWith("ANA456", {
        altitude: 37000,
        groundSpeed: 280,
        heading: 180,
      });
    });
  });

  it("does not invoke handler after unregister", async () => {
    const handler = jest.fn();
    function RegistrarAndUnregister() {
      const {
        registerApplyInstructedVectorHandler,
        applyInstructedVectorToRadar,
      } = useSelectedAircraft();

      useEffect(() => {
        const unregister = registerApplyInstructedVectorHandler(handler);
        unregister();
      }, [registerApplyInstructedVectorHandler]);

      return (
        <button
          onClick={() =>
            applyInstructedVectorToRadar("XXX", {
              altitude: 0,
              groundSpeed: 0,
              heading: 0,
            })
          }
        >
          Apply
        </button>
      );
    }

    const user = userEvent.setup();
    render(
      <SelectedAircraftProvider>
        <RegistrarAndUnregister />
      </SelectedAircraftProvider>
    );

    await user.click(screen.getByRole("button", { name: "Apply" }));
    await waitFor(() => expect(handler).not.toHaveBeenCalled());
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
