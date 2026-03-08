"use client";

import React, { useEffect } from "react";
import { render, screen } from "@testing-library/react";
import {
  SelectedAircraftProvider,
  useSelectedAircraft,
} from "@/context/selectedAircraftContext";
import SelectedCallsignDisplay from "./selectedCallsignDisplay";

function SetCallsignEffect({ callsign }: { callsign: string | null }) {
  const { setCallsign } = useSelectedAircraft();
  useEffect(() => {
    setCallsign(callsign);
  }, [callsign, setCallsign]);
  return null;
}

function renderWithProvider(
  ui: React.ReactElement,
  initialCallsign: string | null = null
) {
  return render(
    <SelectedAircraftProvider>
      <SetCallsignEffect callsign={initialCallsign} />
      {ui}
    </SelectedAircraftProvider>
  );
}

describe("SelectedCallsignDisplay", () => {
  it("displays callsign when provided (operator variant)", async () => {
    renderWithProvider(
      <SelectedCallsignDisplay variant="operator" />,
      "ANA456"
    );
    expect(await screen.findByText("ANA456")).toBeInTheDocument();
  });

  it("displays non-breaking space when callsign is null (operator variant)", () => {
    renderWithProvider(<SelectedCallsignDisplay variant="operator" />, null);
    const el = document.querySelector(".text-atc-text");
    expect(el).toBeInTheDocument();
    expect(el?.textContent).toBe("\u00a0");
  });

  it("displays callsign when provided (controller variant)", async () => {
    renderWithProvider(
      <SelectedCallsignDisplay variant="controller" />,
      "JAL789"
    );
    expect(await screen.findByText("JAL789")).toBeInTheDocument();
  });

  it("displays non-breaking space when callsign is null (controller variant)", () => {
    renderWithProvider(<SelectedCallsignDisplay variant="controller" />, null);
    const el = document.querySelector(".text-atc-text");
    expect(el).toBeInTheDocument();
    expect(el?.textContent).toBe("\u00a0");
  });
});
