package jp.ac.tohoku.qse.takahashi.AtcSimulator.infrastructure.persistance.inMemory;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.Aircraft;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.CommercialAircraft;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.AircraftAttributes.*;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Callsign.Callsign;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.AircraftPosition;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.AircraftVector;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Type.AircraftType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AircraftRepositoryInMemoryTest {
    AircraftRepositoryInMemory repository;

    @BeforeEach
    void setUp() {
        repository = new AircraftRepositoryInMemory();

        // Add test aircraft to repository
        Aircraft aircraft1 = createTestAircraft("JAL001", 35.0, 139.0, 35000, 90, 450, 0);
        Aircraft aircraft2 = createTestAircraft("ANA002", 36.0, 140.0, 36000, 180, 500, 0);
        Aircraft aircraft3 = createTestAircraft("UAL003", 37.0, 141.0, 37000, 270, 400, 0);

        repository.add(aircraft1);
        repository.add(aircraft2);
        repository.add(aircraft3);
    }

    @Test
    void shouldFindAllAircrafts() {
        List<Aircraft> aircrafts = repository.findAll();
        assertEquals(3, aircrafts.size(), "There should be 3 aircrafts");
    }

    private Aircraft createTestAircraft(String callsign, double lat, double lon, double alt,
                                      double heading, double groundSpeed, double verticalSpeed) {
        Callsign cs = new Callsign(callsign);
        AircraftType type = new AircraftType("B777");

        AircraftPosition position = new AircraftPosition(
            new Latitude(lat),
            new Longitude(lon),
            new Altitude(alt)
        );

        AircraftVector vector = new AircraftVector(
            new Heading(heading),
            new GroundSpeed(groundSpeed),
            new VerticalSpeed(verticalSpeed)
        );

        return new CommercialAircraft(cs, type, position, vector,
            "NRT", "RJAA", "KIX", "RJBB", "2024-01-01T12:00:00Z");
    }
}
