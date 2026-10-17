import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { AircraftRouteEditor } from "./AircraftRouteEditor";
import type { ScenarioAircraft } from "@/types/scenario";

const aircraft: ScenarioAircraft = {
  flightPlan: {
    callsign: "JAL101",
    departureAirport: "RJTT",
    arrivalAirport: "RJAA",
    cruiseAltitude: 31000,
    cruiseSpeed: 450,
    route: [
      { fix: "KOITO", action: "CONTINUE" },
      { fix: "BOKJO", action: "CONTINUE" },
    ],
  },
  initialPosition: {
    latitude: 35.5,
    longitude: 139.7,
    altitude: 31000,
    heading: 90,
    groundSpeed: 450,
    verticalSpeed: 0,
  },
};

describe("AircraftRouteEditor", () => {
  it("reorders a fix without a separate apply step", async () => {
    const user = userEvent.setup();
    const onApplyRoute = jest.fn();
    render(
      <AircraftRouteEditor
        aircraft={aircraft}
        atsRoutes={{ atsLowerRoutes: [], rnavRoutes: [] }}
        fixSources={{ waypoints: [], radioNavAids: [] }}
        onApplyRoute={onApplyRoute}
      />
    );

    expect(
      screen.queryByRole("button", { name: /apply waypoint/i })
    ).not.toBeInTheDocument();
    await user.click(screen.getByRole("button", { name: "Move KOITO later" }));
    expect(onApplyRoute).toHaveBeenCalledWith(
      {
        waypoints: ["BOKJO", "KOITO"],
        cruiseAltitude: 31000,
        cruiseSpeed: 450,
      },
      undefined
    );
  });

  it("removes a fix immediately", async () => {
    const user = userEvent.setup();
    const onApplyRoute = jest.fn();
    render(
      <AircraftRouteEditor
        aircraft={aircraft}
        atsRoutes={{ atsLowerRoutes: [], rnavRoutes: [] }}
        fixSources={{ waypoints: [], radioNavAids: [] }}
        onApplyRoute={onApplyRoute}
      />
    );

    await user.click(screen.getByRole("button", { name: "Remove BOKJO" }));
    expect(onApplyRoute).toHaveBeenCalledWith(
      {
        waypoints: ["KOITO"],
        cruiseAltitude: 31000,
        cruiseSpeed: 450,
      },
      undefined
    );
  });
});
