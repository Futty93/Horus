package jp.ac.tohoku.qse.takahashi.AtcSimulator.application;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.fix.FixPositionRepository;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.flightplan.AltitudeConstraint;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.flightplan.FlightPlan;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.flightplan.FlightPlanWaypoint;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.flightplan.WaypointAction;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.Altitude;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.GroundSpeed;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.Latitude;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.Longitude;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Callsign.Callsign;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.FixPosition;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.dto.FlightPlanDto;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.dto.FlightPlanWaypointDto;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class FlightPlanFromDtoConverter {

    private final FixPositionRepository fixPositionRepository;

    public FlightPlanFromDtoConverter(FixPositionRepository fixPositionRepository) {
        this.fixPositionRepository = fixPositionRepository;
    }

    public FlightPlan toDomain(FlightPlanDto dto) {
        Callsign callsign = new Callsign(dto.callsign());
        String departure = dto.departureAirport() != null ? dto.departureAirport() : "";
        String arrival = dto.arrivalAirport() != null ? dto.arrivalAirport() : "";
        Altitude cruiseAltitude = new Altitude(dto.cruiseAltitude());
        GroundSpeed cruiseSpeed = new GroundSpeed(dto.cruiseSpeed());

        List<FlightPlanWaypoint> waypoints = new ArrayList<>();
        if (dto.route() != null) {
            for (FlightPlanWaypointDto wpDto : dto.route()) {
                waypoints.add(toWaypoint(wpDto));
            }
        }

        return new FlightPlan(callsign, departure, arrival, waypoints, cruiseAltitude, cruiseSpeed);
    }

    private FlightPlanWaypoint toWaypoint(FlightPlanWaypointDto dto) {
        FixPosition position = fixPositionRepository.findFixPositionByName(dto.fix())
                .orElseThrow(() -> new IllegalArgumentException("Fix not found: " + dto.fix()));

        Altitude targetAltitude = dto.altitude() != null ? new Altitude(dto.altitude()) : null;
        GroundSpeed targetSpeed = dto.speed() != null ? new GroundSpeed(dto.speed()) : null;
        AltitudeConstraint constraint = parseAltitudeConstraint(dto.constraint());
        WaypointAction action = parseWaypointAction(dto.action());

        return new FlightPlanWaypoint(dto.fix(), position, targetAltitude, targetSpeed, constraint, action);
    }

    private static AltitudeConstraint parseAltitudeConstraint(String s) {
        if (s == null || s.isBlank()) return AltitudeConstraint.NONE;
        try {
            return AltitudeConstraint.valueOf(s.toUpperCase().replace("-", "_"));
        } catch (IllegalArgumentException e) {
            return AltitudeConstraint.NONE;
        }
    }

    private static WaypointAction parseWaypointAction(String s) {
        if (s == null || s.isBlank()) return WaypointAction.CONTINUE;
        try {
            return WaypointAction.valueOf(s.toUpperCase());
        } catch (IllegalArgumentException e) {
            return WaypointAction.CONTINUE;
        }
    }
}
