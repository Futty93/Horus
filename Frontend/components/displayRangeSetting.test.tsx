"use client";

import React from "react";
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import DisplayRangeSetting from "./displayRangeSetting";
import { DisplayRangeProvider } from "@/context/displayRangeContext";

describe("DisplayRangeSetting", () => {
  it("renders with default range 400km", () => {
    render(
      <DisplayRangeProvider>
        <DisplayRangeSetting />
      </DisplayRangeProvider>
    );

    expect(screen.getByLabelText(/表示範囲/i)).toBeInTheDocument();
    expect(screen.getByText(/Current: 400km/)).toBeInTheDocument();
  });

  it("updates displayed range when input changes", async () => {
    const user = userEvent.setup();
    render(
      <DisplayRangeProvider>
        <DisplayRangeSetting />
      </DisplayRangeProvider>
    );

    const input = screen.getByRole("spinbutton");
    await user.clear(input);
    await user.type(input, "200");

    expect(screen.getByText(/Current: 200km/)).toBeInTheDocument();
  });
});
