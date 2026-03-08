"use client";

import React from "react";
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import SimulationControlButtons from "./simulationControlButtons";

const originalFetch = global.fetch;

beforeEach(() => {
  global.fetch = jest.fn();
});

afterEach(() => {
  global.fetch = originalFetch;
});

describe("SimulationControlButtons", () => {
  it.each([
    ["START SIMULATION", "/simulation/start"],
    ["PAUSE", "/simulation/pause"],
  ] as const)(
    "calls fetch with %s URL when button clicked",
    async (label, path) => {
      (global.fetch as jest.Mock).mockResolvedValue({ ok: true });
      const user = userEvent.setup();

      render(<SimulationControlButtons />);
      await user.click(
        screen.getByRole("button", { name: new RegExp(label, "i") })
      );

      expect(global.fetch).toHaveBeenCalledWith(
        expect.stringContaining(path),
        expect.objectContaining({ method: "POST" })
      );
    }
  );
});
