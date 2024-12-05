package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.FlightPlan;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.airport.Airport;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Callsign.Callsign;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Type.AircraftType;

public class FlightPlan {
    public Callsign callsign;
    public Airport departureAirport = null;
    public Airport arrivalAirport = null;
    public AircraftType aircraftType;
    public FlightRoute flightRoute;

    public FlightPlan(String callsign, String departureAirport, String arrivalAirport, String aircraftType, FlightRoute flightRoute){
        this.callsign = new Callsign(callsign);
        this.aircraftType = new AircraftType(aircraftType);
        this.flightRoute = flightRoute;
    }
}
