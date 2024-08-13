package jp.ac.tohoku.qse.takahashi.AtcSimulator.infrastructure.persistance.inMemory;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.Aircraft;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.CommercialAircraft;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AircraftRepositoryInMemoryTest {
    AircraftRepositoryInMemory repository;

    @BeforeEach
    void setUp() {
        repository = new AircraftRepositoryInMemory();
        repository.add(new CommercialAircraft( 0, 0, 0, 0, 0, 0, "Company 1", "Flight 1"));
        repository.add(new CommercialAircraft( 0, 0, 0, 0, 0, 0, "Company 2", "Flight 2"));
        repository.add(new CommercialAircraft( 0, 0, 0, 0, 0, 0, "Company 3", "Flight 3"));
    }

    @Test
    void shouldFindAllAircrafts() {
        List<Aircraft> aircrafts = repository.findAll();
        assertEquals(3, aircrafts.size(), "There should be 3 aircrafts");
    }
}
