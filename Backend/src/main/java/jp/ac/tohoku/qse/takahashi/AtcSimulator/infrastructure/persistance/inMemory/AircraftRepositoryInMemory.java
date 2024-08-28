package jp.ac.tohoku.qse.takahashi.AtcSimulator.infrastructure.persistance.inMemory;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.Aircraft;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.AircraftRepository;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Callsign.Callsign;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Callsign.Company;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Callsign.FlightNumber;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class AircraftRepositoryInMemory implements AircraftRepository {
    private final List<Aircraft> aircrafts = new ArrayList<>();

    public AircraftRepositoryInMemory() {
//        aircrafts.add(new CommercialAircraft(1,0,0,0,0,0,0,"Company 1","Flight 1"));
//        aircrafts.add(new CommercialAircraft(2, 0, 0, 0, 0, 0, 0, "Company 2", "Flight 2"));
//        aircrafts.add(new CommercialAircraft(3, 0, 0, 0, 0, 0, 0, "Company 3", "Flight 3"));
    }

    public void add(Aircraft aircraft) {
        aircrafts.add(aircraft);
    }

    public Aircraft find(Callsign callsign) {
        return aircrafts.stream()
                .filter(aircraft -> aircraft.isEqualCallsign(callsign))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Aircraft not found"));
    }

    public List<Aircraft> findAll() {
        return aircrafts;
    }

    public void remove(Aircraft airplane) {
        aircrafts.remove(airplane);
    }
}
