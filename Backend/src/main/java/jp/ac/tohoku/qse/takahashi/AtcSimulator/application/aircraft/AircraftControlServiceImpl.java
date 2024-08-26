package jp.ac.tohoku.qse.takahashi.AtcSimulator.application.aircraft;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.application.AircraftControlService;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.Aircraft;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.aggregate.airspace.AirspaceManagement;
import org.springframework.stereotype.Service;

@Service
public class AircraftControlServiceImpl implements AircraftControlService {

    @Override
    public void changeAltitude(Aircraft aircraft, int newAltitude) {
        aircraft.setAltitude(newAltitude);
    }

    @Override
    public void changeHeading(Aircraft aircraft, int newHeading) {
        aircraft.setHeading(newHeading);
    }

    @Override
    public void changeSpeed(Aircraft aircraft, int newSpeed) {
        aircraft.setGSpeed(newSpeed);
    }

    @Override
    public void updatePosition(Aircraft aircraft) {
        // Implement logic to calculate new position based on heading, speed, and time.
    }
}