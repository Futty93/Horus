"use client";

import React from "react";
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import RouteInfoDisplaySetting from "./routeInfoDisplaySetting";
import { RouteInfoDisplaySettingProvider } from "@/context/routeInfoDisplaySettingContext";

describe("RouteInfoDisplaySetting", () => {
  it("renders ROUTE DISPLAY SETTINGS heading", () => {
    render(
      <RouteInfoDisplaySettingProvider>
        <RouteInfoDisplaySetting />
      </RouteInfoDisplaySettingProvider>
    );

    expect(
      screen.getByRole("heading", { name: /ROUTE DISPLAY SETTINGS/i })
    ).toBeInTheDocument();
  });

  it("toggles ATS Lower Route checkbox", async () => {
    const user = userEvent.setup();
    render(
      <RouteInfoDisplaySettingProvider>
        <RouteInfoDisplaySetting />
      </RouteInfoDisplaySettingProvider>
    );

    const atsCheckbox = screen.getByRole("checkbox", {
      name: /ATS Lower Route/i,
    });
    expect(atsCheckbox).not.toBeChecked();

    await user.click(atsCheckbox);
    expect(atsCheckbox).toBeChecked();

    await user.click(atsCheckbox);
    expect(atsCheckbox).not.toBeChecked();
  });
});
