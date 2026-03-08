"use client";

import React, { useEffect } from "react";
import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import InputAircraftInfo from "./inputInfoArea";
import {
  SelectedAircraftProvider,
  useSelectedAircraft,
} from "@/context/selectedAircraftContext";
import * as controlAircraftApi from "@/utility/api/controlAircraft";

jest.mock("@/utility/api/controlAircraft");

function HandlerRegistrar({
  onRegister,
}: {
  onRegister: (
    callsign: string,
    vec: { altitude: number; groundSpeed: number; heading: number }
  ) => void;
}) {
  const { registerApplyInstructedVectorHandler } = useSelectedAircraft();
  useEffect(() => {
    const unregister = registerApplyInstructedVectorHandler(onRegister);
    return unregister;
  }, [registerApplyInstructedVectorHandler, onRegister]);
  return null;
}

function StateSetter() {
  const { setCallsign, setInstructedVector } = useSelectedAircraft();
  useEffect(() => {
    setCallsign("JAL123");
    setInstructedVector({ altitude: 35000, groundSpeed: 250, heading: 90 });
  }, [setCallsign, setInstructedVector]);
  return null;
}

describe("InputAircraftInfo - instructed vector immediate update", () => {
  it("calls applyInstructedVectorToRadar when Confirm succeeds", async () => {
    const applySpy = jest.fn();
    jest.spyOn(controlAircraftApi, "controlAircraft").mockResolvedValue(true);

    render(
      <SelectedAircraftProvider>
        <HandlerRegistrar onRegister={applySpy} />
        <StateSetter />
        <InputAircraftInfo />
      </SelectedAircraftProvider>
    );

    const user = userEvent.setup();
    await user.click(screen.getByRole("button", { name: "Confirm" }));

    await waitFor(() => {
      expect(applySpy).toHaveBeenCalledTimes(1);
      expect(applySpy).toHaveBeenCalledWith("JAL123", {
        altitude: 35000,
        groundSpeed: 250,
        heading: 90,
      });
    });
  });

  it("does not call applyInstructedVectorToRadar when API fails", async () => {
    const applySpy = jest.fn();
    jest.spyOn(controlAircraftApi, "controlAircraft").mockResolvedValue(false);

    render(
      <SelectedAircraftProvider>
        <HandlerRegistrar onRegister={applySpy} />
        <StateSetter />
        <InputAircraftInfo />
      </SelectedAircraftProvider>
    );

    const user = userEvent.setup();
    await user.click(screen.getByRole("button", { name: "Confirm" }));

    await waitFor(() => {
      expect(controlAircraftApi.controlAircraft).toHaveBeenCalled();
    });
    expect(applySpy).not.toHaveBeenCalled();
  });
});
