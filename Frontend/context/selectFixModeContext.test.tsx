"use client";

import React from "react";
import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import {
  SelectFixModeProvider,
  useSelectFixMode,
} from "./selectFixModeContext";

function TestConsumer() {
  const {
    isSelectFixMode,
    setIsSelectFixMode,
    selectedFixName,
    setSelectedFixName,
  } = useSelectFixMode();
  return (
    <div>
      <span data-testid="selectFixMode">
        {isSelectFixMode.selectFixMode ? "true" : "false"}
      </span>
      <span data-testid="selectedFixName">{selectedFixName}</span>
      <button onClick={() => setIsSelectFixMode({ selectFixMode: true })}>
        Enter Fix Mode
      </button>
      <button onClick={() => setIsSelectFixMode({ selectFixMode: false })}>
        Exit Fix Mode
      </button>
      <button onClick={() => setSelectedFixName("KOITO")}>Set Fix KOITO</button>
      <button onClick={() => setSelectedFixName("No fixes selected")}>
        Reset Fix
      </button>
    </div>
  );
}

describe("SelectFixModeContext", () => {
  it("provides default values", () => {
    render(
      <SelectFixModeProvider>
        <TestConsumer />
      </SelectFixModeProvider>
    );

    expect(screen.getByTestId("selectFixMode")).toHaveTextContent("false");
    expect(screen.getByTestId("selectedFixName")).toHaveTextContent(
      "No fixes selected"
    );
  });

  it("updates selectFixMode when setIsSelectFixMode is called", async () => {
    const user = userEvent.setup();
    render(
      <SelectFixModeProvider>
        <TestConsumer />
      </SelectFixModeProvider>
    );

    await user.click(screen.getByRole("button", { name: "Enter Fix Mode" }));
    await waitFor(() =>
      expect(screen.getByTestId("selectFixMode")).toHaveTextContent("true")
    );

    await user.click(screen.getByRole("button", { name: "Exit Fix Mode" }));
    await waitFor(() =>
      expect(screen.getByTestId("selectFixMode")).toHaveTextContent("false")
    );
  });

  it("updates selectedFixName when setSelectedFixName is called", async () => {
    const user = userEvent.setup();
    render(
      <SelectFixModeProvider>
        <TestConsumer />
      </SelectFixModeProvider>
    );

    await user.click(screen.getByRole("button", { name: "Set Fix KOITO" }));
    await waitFor(() =>
      expect(screen.getByTestId("selectedFixName")).toHaveTextContent("KOITO")
    );

    await user.click(screen.getByRole("button", { name: "Reset Fix" }));
    await waitFor(() =>
      expect(screen.getByTestId("selectedFixName")).toHaveTextContent(
        "No fixes selected"
      )
    );
  });

  it("throws when useSelectFixMode is used outside Provider", () => {
    const consoleSpy = jest
      .spyOn(console, "error")
      .mockImplementation(() => {});

    expect(() => {
      render(<TestConsumer />);
    }).toThrow("useSelectFixMode must be used within a SelectFixModeProvider");

    consoleSpy.mockRestore();
  });
});
