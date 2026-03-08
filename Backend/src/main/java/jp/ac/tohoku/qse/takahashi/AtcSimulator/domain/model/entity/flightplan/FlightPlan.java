package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.flightplan;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.Altitude;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.GroundSpeed;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Callsign.Callsign;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 航空機のフライトプラン。
 * 出発・到着空港、巡航高度・速度、ウェイポイント列を保持する。
 */
public final class FlightPlan {
    private final Callsign callsign;
    private final String departureAirport;
    private final String arrivalAirport;
    private final List<FlightPlanWaypoint> waypoints;
    private final Altitude cruiseAltitude;
    private final GroundSpeed cruiseSpeed;

    public FlightPlan(
            Callsign callsign,
            String departureAirport,
            String arrivalAirport,
            List<FlightPlanWaypoint> waypoints,
            Altitude cruiseAltitude,
            GroundSpeed cruiseSpeed) {
        this.callsign = Objects.requireNonNull(callsign, "callsign");
        this.departureAirport = Objects.requireNonNull(departureAirport, "departureAirport");
        this.arrivalAirport = Objects.requireNonNull(arrivalAirport, "arrivalAirport");
        this.waypoints = waypoints != null
                ? Collections.unmodifiableList(List.copyOf(waypoints))
                : Collections.emptyList();
        this.cruiseAltitude = Objects.requireNonNull(cruiseAltitude, "cruiseAltitude");
        this.cruiseSpeed = Objects.requireNonNull(cruiseSpeed, "cruiseSpeed");
    }

    public Callsign getCallsign() {
        return callsign;
    }

    public String getDepartureAirport() {
        return departureAirport;
    }

    public String getArrivalAirport() {
        return arrivalAirport;
    }

    public List<FlightPlanWaypoint> getWaypoints() {
        return waypoints;
    }

    public Altitude getCruiseAltitude() {
        return cruiseAltitude;
    }

    public GroundSpeed getCruiseSpeed() {
        return cruiseSpeed;
    }

    public Optional<FlightPlanWaypoint> getNextWaypoint(int currentIndex) {
        if (currentIndex < 0 || currentIndex >= waypoints.size()) {
            return Optional.empty();
        }
        return Optional.of(waypoints.get(currentIndex));
    }

    public Optional<FlightPlanWaypoint> getWaypointByName(String fixName) {
        return waypoints.stream()
                .filter(wp -> wp.getFixName().equals(fixName))
                .findFirst();
    }

    public int findWaypointIndex(String fixName) {
        for (int i = 0; i < waypoints.size(); i++) {
            if (waypoints.get(i).getFixName().equals(fixName)) {
                return i;
            }
        }
        return -1;
    }

    public boolean isLastWaypoint(int index) {
        return index >= 0 && index == waypoints.size() - 1;
    }

    public boolean hasWaypoints() {
        return !waypoints.isEmpty();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        FlightPlan that = (FlightPlan) obj;
        return Objects.equals(callsign, that.callsign)
                && Objects.equals(departureAirport, that.departureAirport)
                && Objects.equals(arrivalAirport, that.arrivalAirport)
                && Objects.equals(waypoints, that.waypoints)
                && Objects.equals(cruiseAltitude, that.cruiseAltitude)
                && Objects.equals(cruiseSpeed, that.cruiseSpeed);
    }

    @Override
    public int hashCode() {
        return Objects.hash(callsign, departureAirport, arrivalAirport, waypoints, cruiseAltitude, cruiseSpeed);
    }
}
