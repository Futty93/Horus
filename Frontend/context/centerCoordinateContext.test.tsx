"use client";

import React from "react";
import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import {
  CenterCoordinateProvider,
  useCenterCoordinate,
} from "./centerCoordinateContext";

function TestConsumer() {
  const { centerCoordinate, setCenterCoordinate } = useCenterCoordinate();
  return (
    <div>
      <span data-testid="lat">{centerCoordinate.latitude}</span>
      <span data-testid="lon">{centerCoordinate.longitude}</span>
      <button
        onClick={() =>
          setCenterCoordinate({ latitude: 35.0, longitude: 140.0 })
        }
      >
        Set Tokyo
      </button>
    </div>
  );
}

describe("CenterCoordinateContext", () => {
  it("provides default coordinates", () => {
    render(
      <CenterCoordinateProvider>
        <TestConsumer />
      </CenterCoordinateProvider>
    );

    expect(screen.getByTestId("lat")).toHaveTextContent("34.482");
    expect(screen.getByTestId("lon")).toHaveTextContent("138.614");
  });

  it("updates coordinates when setCenterCoordinate is called", async () => {
    const user = userEvent.setup();
    render(
      <CenterCoordinateProvider>
        <TestConsumer />
      </CenterCoordinateProvider>
    );

    await user.click(screen.getByRole("button", { name: "Set Tokyo" }));
    await waitFor(() => {
      expect(screen.getByTestId("lat")).toHaveTextContent("35");
      expect(screen.getByTestId("lon")).toHaveTextContent("140");
    });
  });

  it("throws when used outside Provider", () => {
    const consoleSpy = jest
      .spyOn(console, "error")
      .mockImplementation(() => {});
    expect(() => render(<TestConsumer />)).toThrow(
      "useCenterCoordinate must be used within a CenterCoordinateProvider"
    );
    consoleSpy.mockRestore();
  });
});
