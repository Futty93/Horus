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

    /**
     * Check if the aircraft with the given callsign exists in the repository
     * @param callsign the callsign of the aircraft
     * @return true if the aircraft exists, false otherwise
     */
    public boolean isAircraftExist(Callsign callsign){
        return aircrafts.stream()
                .anyMatch(aircraft -> aircraft.isEqualCallsign(callsign));
    }

    public void add(Aircraft aircraft) {
        aircrafts.add(aircraft);
    }

    public Aircraft findByCallsign(Callsign callsign) {
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

    public void NextStep() {
        for (Aircraft aircraft : aircrafts) {
            aircraft.calculateNextAircraftVector();
            aircraft.calculateNextAircraftPosition();
        }
    }
}
