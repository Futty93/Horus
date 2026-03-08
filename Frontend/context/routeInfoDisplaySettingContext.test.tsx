"use client";

import React from "react";
import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import {
  RouteInfoDisplaySettingProvider,
  useRouteInfoDisplaySetting,
} from "./routeInfoDisplaySettingContext";

function TestConsumer() {
  const { isDisplaying, setRouteInfoDisplaySetting } =
    useRouteInfoDisplaySetting();
  return (
    <div>
      <span data-testid="waypointName">
        {String(isDisplaying.waypointName)}
      </span>
      <span data-testid="atsLowerRoute">
        {String(isDisplaying.atsLowerRoute)}
      </span>
      <button
        onClick={() =>
          setRouteInfoDisplaySetting((prev) => ({
            ...prev,
            waypointName: !prev.waypointName,
          }))
        }
      >
        Toggle Waypoint Name
      </button>
      <button
        onClick={() =>
          setRouteInfoDisplaySetting((prev) => ({
            ...prev,
            atsLowerRoute: true,
          }))
        }
      >
        Enable ATS Lower
      </button>
    </div>
  );
}

describe("RouteInfoDisplaySettingContext", () => {
  it("provides default display settings", () => {
    render(
      <RouteInfoDisplaySettingProvider>
        <TestConsumer />
      </RouteInfoDisplaySettingProvider>
    );

    expect(screen.getByTestId("waypointName")).toHaveTextContent("false");
    expect(screen.getByTestId("atsLowerRoute")).toHaveTextContent("false");
  });

  it("updates settings when setRouteInfoDisplaySetting is called", async () => {
    const user = userEvent.setup();
    render(
      <RouteInfoDisplaySettingProvider>
        <TestConsumer />
      </RouteInfoDisplaySettingProvider>
    );

    await user.click(
      screen.getByRole("button", { name: "Toggle Waypoint Name" })
    );
    await waitFor(() =>
      expect(screen.getByTestId("waypointName")).toHaveTextContent("true")
    );

    await user.click(screen.getByRole("button", { name: "Enable ATS Lower" }));
    await waitFor(() =>
      expect(screen.getByTestId("atsLowerRoute")).toHaveTextContent("true")
    );
  });

  it("throws when used outside Provider", () => {
    const consoleSpy = jest
      .spyOn(console, "error")
      .mockImplementation(() => {});
    expect(() => render(<TestConsumer />)).toThrow(
      "useRouteInfoDisplaySetting must be used within a RouteInfoDisplaySettingProvider"
    );
    consoleSpy.mockRestore();
  });
});
