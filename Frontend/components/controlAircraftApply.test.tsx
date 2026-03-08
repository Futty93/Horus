"use client";

import React, { useEffect } from "react";
import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import InputAircraftInfo from "./inputInfoArea";
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

function StateSetter({
  callsign,
  instructedVector,
}: {
  callsign: string;
  instructedVector: { altitude: number; groundSpeed: number; heading: number };
}) {
  const { setCallsign, setInstructedVector } = useSelectedAircraft();
  useEffect(() => {
    setCallsign(callsign);
    setInstructedVector(instructedVector);
  }, [callsign, instructedVector, setCallsign, setInstructedVector]);
  return null;
}

describe("applyInstructedVectorToRadar on control API success", () => {
  it.each([
    [
      "InputAircraftInfo",
      InputAircraftInfo,
      /CONFIRM/i,
      "JAL123",
      { altitude: 35000, groundSpeed: 250, heading: 90 },
    ],
    [
      "ControlAircraft",
      ControlAircraft,
      /EXECUTE COMMAND/i,
      "ANA456",
      { altitude: 39000, groundSpeed: 300, heading: 270 },
    ],
  ] as const)(
    "%s calls applyInstructedVectorToRadar when API succeeds",
    async (_, Component, buttonMatcher, callsign, instructedVector) => {
      const applySpy = jest.fn();
      jest.spyOn(controlAircraftApi, "controlAircraft").mockResolvedValue(true);

      render(
        <SelectedAircraftProvider>
          <HandlerRegistrar onRegister={applySpy} />
          <StateSetter
            callsign={callsign}
            instructedVector={instructedVector}
          />
          <Component />
        </SelectedAircraftProvider>
      );

      const user = userEvent.setup();
      await user.click(
        screen.getByRole("button", {
          name:
            typeof buttonMatcher === "string" ? buttonMatcher : buttonMatcher,
        })
      );

      await waitFor(() => {
        expect(applySpy).toHaveBeenCalledWith(callsign, instructedVector);
      });
    }
  );

  it.each([
    ["InputAircraftInfo", InputAircraftInfo, /CONFIRM/i],
    ["ControlAircraft", ControlAircraft, /EXECUTE COMMAND/i],
  ] as const)(
    "%s does not call applyInstructedVectorToRadar when API fails",
    async (_, Component, buttonMatcher) => {
      const applySpy = jest.fn();
      jest
        .spyOn(controlAircraftApi, "controlAircraft")
        .mockResolvedValue(false);

      render(
        <SelectedAircraftProvider>
          <HandlerRegistrar onRegister={applySpy} />
          <StateSetter
            callsign="JAL123"
            instructedVector={{
              altitude: 35000,
              groundSpeed: 250,
              heading: 90,
            }}
          />
          <Component />
        </SelectedAircraftProvider>
      );

      const user = userEvent.setup();
      await user.click(
        screen.getByRole("button", {
          name:
            typeof buttonMatcher === "string" ? buttonMatcher : buttonMatcher,
        })
      );

      await waitFor(() => {
        expect(controlAircraftApi.controlAircraft).toHaveBeenCalled();
      });
      expect(applySpy).not.toHaveBeenCalled();
    }
  );
});
