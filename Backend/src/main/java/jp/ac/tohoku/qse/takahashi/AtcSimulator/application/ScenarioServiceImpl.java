package jp.ac.tohoku.qse.takahashi.AtcSimulator.application;

import java.util.Optional;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.exception.FixNotFoundException;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.aggregate.airspace.AirspaceManagement;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.Aircraft;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.AircraftBase;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.flightplan.NavigationMode;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Callsign.Callsign;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.FixPosition;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.InstructedVector;

public class ScenarioServiceImpl implements ScenarioService {

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
        Aircraft aircraft = airspaceManagement.findAircraftByCallsign(callsign);
        AircraftBase base = (AircraftBase) aircraft;
        base.setNavigationMode(NavigationMode.HEADING);
        base.setInstructedVector(instructedVector);
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
    public void resumeNavigation(Callsign callsign) {
        Aircraft aircraft = airspaceManagement.findAircraftByCallsign(callsign);
        ((AircraftBase) aircraft).setResumeNavigation();
    }
}
