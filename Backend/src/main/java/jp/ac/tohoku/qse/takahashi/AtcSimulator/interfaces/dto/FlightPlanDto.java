package jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.dto;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.FlightPlan.FlightPlan;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Callsign.Callsign;

public class FlightPlanDto {
    public String callsign;
    public String departureAirport;
    public String arrivalAirport;
    public String aircraftType;
    public FlightRouteDto flightRoute;

}
