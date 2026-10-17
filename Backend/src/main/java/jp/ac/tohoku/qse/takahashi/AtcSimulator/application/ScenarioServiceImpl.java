package jp.ac.tohoku.qse.takahashi.AtcSimulator.application;

import java.util.Optional;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.exception.FixNotFoundException;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.aggregate.airspace.AirspaceManagement;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.Aircraft;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.AircraftBase;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.flightplan.HoldTurnDirection;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.flightplan.NavigationMode;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.Latitude;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.Longitude;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Callsign.Callsign;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.AircraftPosition;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.FixPosition;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.InstructedVector;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.shared.utility.GeodeticUtils;

public class ScenarioServiceImpl implements ScenarioService {

    static final double MAX_FINAL_APPROACH_JOIN_NM = 120.0;

    private final AirspaceManagement airspaceManagement;

    public ScenarioServiceImpl(AirspaceManagement airspaceManagement) {
        this.airspaceManagement = airspaceManagement;
    }

    @Override
    public void spawnAircraft(Aircraft aircraft) {
        airspaceManagement.addAircraft(aircraft);
    }

    @Override
    public void instructAircraft(Callsign callsign, InstructedVector instructedVector) {
        instructAircraft(callsign, instructedVector, null);
    }

    @Override
    public void instructAircraft(Callsign callsign, InstructedVector instructedVector,
                                   Integer indicatedAirspeedKnots) {
        Aircraft aircraft = airspaceManagement.findAircraftByCallsign(callsign);
        AircraftBase base = (AircraftBase) aircraft;
        base.setNavigationMode(NavigationMode.HEADING);
        base.setInstructedVector(instructedVector, indicatedAirspeedKnots);
    }

    @Override
    public void joinFinalApproach(Callsign callsign, double interceptLatitude, double interceptLongitude,
                                  String interceptLabel, double approachCourseHeading) {
        Aircraft aircraft = airspaceManagement.findAircraftByCallsign(callsign);
        AircraftBase base = (AircraftBase) aircraft;
        AircraftPosition interceptPosition = new AircraftPosition(
                new Latitude(interceptLatitude),
                new Longitude(interceptLongitude),
                base.getAircraftPosition().altitude);
        double distanceNm = GeodeticUtils.calculateHorizontalDistance(
                base.getAircraftPosition(), interceptPosition);
        if (distanceNm > MAX_FINAL_APPROACH_JOIN_NM) {
            throw new IllegalArgumentException(
                    "Aircraft is too far from final approach intercept ("
                            + String.format("%.1f", distanceNm) + " NM, max "
                            + MAX_FINAL_APPROACH_JOIN_NM + " NM)");
        }
        FixPosition intercept = new FixPosition(
                new Latitude(interceptLatitude),
                new Longitude(interceptLongitude));
        base.joinFinalApproachIntercept(intercept, interceptLabel, approachCourseHeading);
    }

    @Override
    public void goAround(Callsign callsign, double thresholdLatitude, double thresholdLongitude,
                         double runwayHeading, double climbAltitudeFeet) {
        Aircraft aircraft = airspaceManagement.findAircraftByCallsign(callsign);
        AircraftBase base = (AircraftBase) aircraft;
        AircraftPosition threshold = new AircraftPosition(
                new Latitude(thresholdLatitude),
                new Longitude(thresholdLongitude),
                base.getAircraftPosition().altitude);
        double distanceNm = GeodeticUtils.calculateHorizontalDistance(
                base.getAircraftPosition(), threshold);
        if (distanceNm > MAX_FINAL_APPROACH_JOIN_NM) {
            throw new IllegalArgumentException(
                    "Aircraft is too far from the runway for a go-around ("
                            + String.format("%.1f", distanceNm) + " NM, max "
                            + MAX_FINAL_APPROACH_JOIN_NM + " NM)");
        }
        base.beginGoAround(runwayHeading, climbAltitudeFeet);
    }

    @Override
    public void handOffToTower(Callsign callsign, double thresholdLatitude, double thresholdLongitude,
                               double runwayHeading) {
        Aircraft aircraft = airspaceManagement.findAircraftByCallsign(callsign);
        AircraftBase base = (AircraftBase) aircraft;
        base.beginTowerHandoff(thresholdLatitude, thresholdLongitude, runwayHeading);
    }

    @Override
    public void setAtcClearance(Callsign callsign, InstructedVector clearance) {
        Aircraft aircraft = airspaceManagement.findAircraftByCallsign(callsign);
        if (aircraft instanceof AircraftBase base) {
            base.setAtcClearance(clearance);
            return;
        }
        throw new IllegalStateException(
                "ATC clearance is only supported for AircraftBase: " + aircraft.getClass().getName());
    }

    @Override
    public void setSquawk(Callsign callsign, String squawk) {
        Aircraft aircraft = airspaceManagement.findAircraftByCallsign(callsign);
        if (aircraft instanceof AircraftBase base) {
            base.setSquawk(squawk);
            return;
        }
        throw new IllegalStateException(
                "Squawk assignment is only supported for AircraftBase: " + aircraft.getClass().getName());
    }

    @Override
    public void directFixAircraft(Callsign callsign, String fixName) {
        directToFix(callsign, fixName, false);
    }

    @Override
    public void directToFix(Callsign callsign, String fixName, boolean resumeFlightPlan) {
        Aircraft aircraft = airspaceManagement.findAircraftByCallsign(callsign);
        Optional<FixPosition> fixPosition = airspaceManagement.getFixPosition(fixName);
        if (fixPosition.isEmpty()) {
            throw new FixNotFoundException(fixName);
        }
        ((AircraftBase) aircraft).setDirectTo(fixPosition.get(), fixName, resumeFlightPlan);
    }

    @Override
    public void holdAtFix(Callsign callsign, String fixName, HoldTurnDirection turnDirection) {
        Aircraft aircraft = airspaceManagement.findAircraftByCallsign(callsign);
        Optional<FixPosition> fixPosition = airspaceManagement.getFixPosition(fixName);
        if (fixPosition.isEmpty()) {
            throw new FixNotFoundException(fixName);
        }
        ((AircraftBase) aircraft).setHoldAtFix(fixPosition.get(), fixName, turnDirection);
    }

    @Override
    public void resumeNavigation(Callsign callsign) {
        Aircraft aircraft = airspaceManagement.findAircraftByCallsign(callsign);
        ((AircraftBase) aircraft).setResumeNavigation();
    }
}
