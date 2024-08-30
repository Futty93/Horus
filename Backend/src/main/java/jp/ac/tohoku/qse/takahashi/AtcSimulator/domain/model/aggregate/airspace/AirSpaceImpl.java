package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.aggregate.airspace;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.Aircraft;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AirSpaceImpl implements AirSpace {
    private final List<Aircraft> aircrafts = new ArrayList<Aircraft>();

    public void addAircraft(Aircraft aircraft) {
        aircrafts.add(aircraft);
    }

    public void removeAircraft(Aircraft aircraft) {
        aircrafts.remove(aircraft);
    }

    public void NextStep() {
        for (Aircraft aircraft : aircrafts) {
            aircraft.calculateNextAircraftPosition();
        }
    }
}
