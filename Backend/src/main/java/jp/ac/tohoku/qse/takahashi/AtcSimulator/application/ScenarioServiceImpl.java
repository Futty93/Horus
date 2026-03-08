package jp.ac.tohoku.qse.takahashi.AtcSimulator.application;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.aggregate.airspace.AirspaceManagement;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.Aircraft;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.AircraftBase;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.Heading;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Callsign.Callsign;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.FixPosition;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.InstructedVector;

import java.util.Optional;

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
        ((AircraftBase) aircraft).setInstructedVector(instructedVector);
    }

    @Override
    public void directFixAircraft(Callsign callsign, String fixName) {
        Aircraft directedAircraft = airspaceManagement.findAircraftByCallsign(callsign);
        Optional<FixPosition> fixPosition = airspaceManagement.getFixPosition(fixName);
        if (fixPosition.isEmpty()) {
            return;
        }
        double turnAngle = directedAircraft.calculateTurnAngle(fixPosition.get());
        InstructedVector instructedVector = new InstructedVector(new Heading(turnAngle), directedAircraft.getInstructedVector().instructedAltitude, directedAircraft.getInstructedVector().instructedGroundSpeed);
        ((AircraftBase) directedAircraft).setInstructedVector(instructedVector);
    }
}
