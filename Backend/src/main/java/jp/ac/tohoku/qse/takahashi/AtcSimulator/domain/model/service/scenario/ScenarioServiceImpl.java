package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.service.scenario;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.aggregate.airspace.AirspaceManagement;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.Aircraft;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.AircraftBase;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.AircraftRepository;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.types.commercial.CommercialAircraft;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.Heading;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Callsign.Callsign;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.AircraftPosition;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.AircraftVector;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.FixPosition;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.InstructedVector;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Type.AircraftType;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.dto.ControlAircraftDto;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.dto.CreateAircraftDto;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ScenarioServiceImpl implements ScenarioService {

    private final AirspaceManagement airspaceManagement;

    public ScenarioServiceImpl(AirspaceManagement airspaceManagement) {
        this.airspaceManagement = airspaceManagement;
    }

    public void spawnAircraft(CreateAircraftDto aircraftDto) {
        CommercialAircraft aircraft = aircraftDto.createCommercialAircraft();
        airspaceManagement.addAircraft(aircraft);
    }

    public void instructAircraft(Callsign callsign, ControlAircraftDto controlAircraftDto) {
        Aircraft instructedAircraft = airspaceManagement.findAircraftByCallsign(callsign);
        controlAircraftDto.setInstruction((AircraftBase) instructedAircraft);
    }

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
