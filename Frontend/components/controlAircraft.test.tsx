"use client";

import React, { useEffect } from "react";
import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import ControlAircraft from "./controlAircraft";
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
    setCallsign("ANA456");
    setInstructedVector({ altitude: 39000, groundSpeed: 300, heading: 270 });
  }, [setCallsign, setInstructedVector]);
  return null;
}

describe("ControlAircraft - instructed vector immediate update", () => {
  it("calls applyInstructedVectorToRadar when EXECUTE COMMAND succeeds", async () => {
    const applySpy = jest.fn();
    jest.spyOn(controlAircraftApi, "controlAircraft").mockResolvedValue(true);

    render(
      <SelectedAircraftProvider>
        <HandlerRegistrar onRegister={applySpy} />
        <StateSetter />
        <ControlAircraft />
      </SelectedAircraftProvider>
    );

    const user = userEvent.setup();
    await user.click(screen.getByRole("button", { name: /EXECUTE COMMAND/i }));

    await waitFor(() => {
      expect(applySpy).toHaveBeenCalledTimes(1);
      expect(applySpy).toHaveBeenCalledWith("ANA456", {
        altitude: 39000,
        groundSpeed: 300,
        heading: 270,
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
        <ControlAircraft />
      </SelectedAircraftProvider>
    );

    const user = userEvent.setup();
    await user.click(screen.getByRole("button", { name: /EXECUTE COMMAND/i }));

    await waitFor(() => {
      expect(controlAircraftApi.controlAircraft).toHaveBeenCalled();
    });
    expect(applySpy).not.toHaveBeenCalled();
  });
});
