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
    }

    @Test
    void shouldFindAllAircrafts() {
        List<Aircraft> aircrafts = repository.findAll();
        assertEquals(3, aircrafts.size(), "There should be 3 aircrafts");
    }
}
