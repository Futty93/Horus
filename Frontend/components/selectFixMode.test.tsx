"use client";

import React, { useEffect } from "react";
import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import SelectFixMode from "./selectFixMode";
import {
  SelectFixModeProvider,
  useSelectFixMode,
} from "@/context/selectFixModeContext";
import {
  SelectedAircraftProvider,
  useSelectedAircraft,
} from "@/context/selectedAircraftContext";

function StateSetter({ callsign }: { callsign: string | null }) {
  const { setCallsign } = useSelectedAircraft();
  useEffect(() => {
    setCallsign(callsign);
  }, [callsign, setCallsign]);
  return null;
}

function FixModeActivator({ fixName }: { fixName: string }) {
  const { setSelectedFixName, setIsSelectFixMode } = useSelectFixMode();
  useEffect(() => {
    setSelectedFixName(fixName);
    setIsSelectFixMode({ selectFixMode: true });
  }, [fixName, setSelectedFixName, setIsSelectFixMode]);
  return null;
}

const originalFetch = global.fetch;

beforeEach(() => {
  global.fetch = jest.fn();
});

afterEach(() => {
  global.fetch = originalFetch;
});

function renderWithProviders(callsign: string | null) {
  return render(
    <SelectedAircraftProvider>
      <SelectFixModeProvider>
        <StateSetter callsign={callsign} />
        <SelectFixMode />
      </SelectFixModeProvider>
    </SelectedAircraftProvider>
  );
}

describe("SelectFixMode", () => {
  it("shows No fixes selected by default", () => {
    renderWithProviders("JAL123");

    expect(screen.getByText(/No fixes selected/i)).toBeInTheDocument();
  });

  it("shows DIRECT TO FIX button when not in fix mode", () => {
    renderWithProviders("JAL123");

    expect(
      screen.getByRole("button", { name: /DIRECT TO FIX/i })
    ).toBeInTheDocument();
  });

  it("shows Select aircraft first when clicking without callsign", async () => {
    const user = userEvent.setup();
    renderWithProviders(null);

    await user.click(screen.getByRole("button", { name: /DIRECT TO FIX/i }));

    await waitFor(() => {
      expect(screen.getByText(/Select aircraft first/i)).toBeInTheDocument();
    });
  });

  it("enters fix mode and shows CONFIRM/CANCEL when DIRECT TO FIX clicked with callsign", async () => {
    const user = userEvent.setup();
    renderWithProviders("JAL123");

    await user.click(screen.getByRole("button", { name: /DIRECT TO FIX/i }));

    await waitFor(() => {
      expect(
        screen.getByRole("button", { name: /CONFIRM/i })
      ).toBeInTheDocument();
      expect(
        screen.getByRole("button", { name: /CANCEL/i })
      ).toBeInTheDocument();
    });
  });

  it("CANCEL exits fix mode and resets selected fix name", async () => {
    const user = userEvent.setup();
    renderWithProviders("JAL123");

    await user.click(screen.getByRole("button", { name: /DIRECT TO FIX/i }));
    await waitFor(() => {
      expect(
        screen.getByRole("button", { name: /CANCEL/i })
      ).toBeInTheDocument();
    });

    await user.click(screen.getByRole("button", { name: /CANCEL/i }));

    await waitFor(() => {
      expect(
        screen.getByRole("button", { name: /DIRECT TO FIX/i })
      ).toBeInTheDocument();
      expect(screen.getByText(/No fixes selected/i)).toBeInTheDocument();
    });
  });

  it("CONFIRM calls fetch with correct URL and body when fix is selected", async () => {
    const user = userEvent.setup();
    const mockFetch = jest.fn().mockResolvedValue({ ok: true });
    global.fetch = mockFetch;

    render(
      <SelectedAircraftProvider>
        <SelectFixModeProvider>
          <StateSetter callsign="JAL123" />
          <FixModeActivator fixName="ABENO" />
          <SelectFixMode />
        </SelectFixModeProvider>
      </SelectedAircraftProvider>
    );

    await waitFor(() => {
      expect(
        screen.getByRole("button", { name: /CONFIRM/i })
      ).toBeInTheDocument();
    });

    await user.click(screen.getByRole("button", { name: /CONFIRM/i }));

    await waitFor(() => {
      expect(mockFetch).toHaveBeenCalledWith(
        expect.stringContaining("/api/aircraft/JAL123/direct-to"),
        expect.objectContaining({
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ fixName: "ABENO", resumeFlightPlan: false }),
        })
      );
    });
  });
});
