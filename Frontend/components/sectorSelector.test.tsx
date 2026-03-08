"use client";

import React from "react";
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import SectorSelector from "./sectorSelector";
import { CenterCoordinateProvider } from "@/context/centerCoordinateContext";
import { useCenterCoordinate } from "@/context/centerCoordinateContext";

function ConsumerWithDisplay() {
  const { centerCoordinate } = useCenterCoordinate();
  return (
    <div>
      <SectorSelector />
      <span data-testid="lat">{centerCoordinate.latitude}</span>
      <span data-testid="lon">{centerCoordinate.longitude}</span>
    </div>
  );
}

describe("SectorSelector", () => {
  it("updates center coordinate when sector is changed", async () => {
    const user = userEvent.setup();
    render(
      <CenterCoordinateProvider>
        <ConsumerWithDisplay />
      </CenterCoordinateProvider>
    );

    expect(screen.getByTestId("lat")).toHaveTextContent("34.482");
    expect(screen.getByTestId("lon")).toHaveTextContent("138.614");

    const select = screen.getByRole("combobox");
    await user.selectOptions(select, "T10");

    expect(select).toHaveValue("T10");
    expect(screen.getByTestId("lat")).toHaveTextContent("33.041");
    expect(screen.getByTestId("lon")).toHaveTextContent("139.456");
  });
});
