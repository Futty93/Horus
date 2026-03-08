"use client";

import React from "react";
import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { DisplayRangeProvider, useDisplayRange } from "./displayRangeContext";

function TestConsumer() {
  const { displayRange, setDisplayRange } = useDisplayRange();
  return (
    <div>
      <span data-testid="range">{displayRange.range}</span>
      <button onClick={() => setDisplayRange({ range: 200 })}>Set 200</button>
      <button
        onClick={() => setDisplayRange((prev) => ({ ...prev, range: 800 }))}
      >
        Set 800
      </button>
    </div>
  );
}

describe("DisplayRangeContext", () => {
  it("provides default range of 400", () => {
    render(
      <DisplayRangeProvider>
        <TestConsumer />
      </DisplayRangeProvider>
    );

    expect(screen.getByTestId("range")).toHaveTextContent("400");
  });

  it("updates range when setDisplayRange is called", async () => {
    const user = userEvent.setup();
    render(
      <DisplayRangeProvider>
        <TestConsumer />
      </DisplayRangeProvider>
    );

    await user.click(screen.getByRole("button", { name: "Set 200" }));
    await waitFor(() =>
      expect(screen.getByTestId("range")).toHaveTextContent("200")
    );

    await user.click(screen.getByRole("button", { name: "Set 800" }));
    await waitFor(() =>
      expect(screen.getByTestId("range")).toHaveTextContent("800")
    );
  });

  it("throws when used outside Provider", () => {
    const consoleSpy = jest
      .spyOn(console, "error")
      .mockImplementation(() => {});
    expect(() => render(<TestConsumer />)).toThrow(
      "useDisplayRange must be used within a DisplayRangeProvider"
    );
    consoleSpy.mockRestore();
  });
});
