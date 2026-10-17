import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { AddAircraftForm } from "./AddAircraftForm";
import type { AirportOption } from "./airportOption";

const airports: AirportOption[] = [
  {
    icaoCode: "RJAA",
    name: "Narita International Airport",
    iataCode: "NRT",
    latitude: 35.7,
    longitude: 140.3,
  },
  {
    icaoCode: "RJTT",
    name: "Tokyo International Airport",
    iataCode: "HND",
    latitude: 35.5,
    longitude: 139.7,
  },
];

describe("AddAircraftForm", () => {
  it("places a flight from a callsign and two airports", async () => {
    const user = userEvent.setup();
    const onSubmit = jest.fn();
    render(
      <AddAircraftForm
        existingCallsigns={[]}
        airports={airports}
        onSubmit={onSubmit}
      />
    );

    expect(screen.queryByPlaceholderText(/lat/i)).not.toBeInTheDocument();
    await user.type(screen.getByPlaceholderText("JAL101"), "jal101");

    const [origin, destination] = screen.getAllByRole("combobox");
    await user.click(origin);
    await user.type(origin, "nari{Enter}");
    await user.click(destination);
    await user.type(destination, "hnd{Enter}");
    await user.click(
      screen.getByRole("button", { name: "Place on the chart" })
    );

    expect(onSubmit).toHaveBeenCalledWith({
      callsign: "JAL101",
      departureAirport: "RJAA",
      arrivalAirport: "RJTT",
    });
  });
});
