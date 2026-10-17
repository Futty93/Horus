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

  it("keeps the saved cruise altitude until the field is left with a positive number", async () => {
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

    await user.clear(
      screen.getByRole("spinbutton", { name: "Cruise altitude (ft)" })
    );
    expect(onApplyRoute).not.toHaveBeenCalled();
    await user.tab();
    expect(onApplyRoute).not.toHaveBeenCalled();
    expect(
      screen.getByRole("spinbutton", { name: "Cruise altitude (ft)" })
    ).toHaveValue(31000);

    await user.clear(
      screen.getByRole("spinbutton", { name: "Cruise altitude (ft)" })
    );
    await user.type(
      screen.getByRole("spinbutton", { name: "Cruise altitude (ft)" }),
      "32000"
    );
    await user.tab();
    expect(onApplyRoute).toHaveBeenCalledWith(
      {
        waypoints: ["KOITO", "BOKJO"],
        cruiseAltitude: 32000,
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
