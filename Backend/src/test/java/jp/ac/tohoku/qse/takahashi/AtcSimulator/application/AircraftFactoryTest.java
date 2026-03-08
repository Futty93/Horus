package jp.ac.tohoku.qse.takahashi.AtcSimulator.application;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.types.commercial.CommercialAircraft;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Callsign.Callsign;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.dto.CreateAircraftDto;

class AircraftFactoryTest {

    @Test
    @DisplayName("createCommercialAircraft converts DTO to CommercialAircraft")
    void createCommercialAircraft_convertsDtoCorrectly() {
        CreateAircraftDto dto = new CreateAircraftDto(
                "TEST123", 35.0, 139.0, 35000, 450, 0, 90,
                "B738", "HND", "RJTT", "NRT", "RJAA", "2024-12-13T14:30:00Z"
        );

        CommercialAircraft aircraft = AircraftFactory.createCommercialAircraft(dto);

        assertThat(aircraft).isNotNull();
        assertThat(aircraft.getCallsign()).isEqualTo(new Callsign("TEST123"));
        assertThat(aircraft.getAircraftPosition().latitude.toDouble()).isEqualTo(35.0);
        assertThat(aircraft.getAircraftPosition().longitude.toDouble()).isEqualTo(139.0);
        assertThat(aircraft.getAircraftPosition().altitude.toDouble()).isEqualTo(35000);
    }
}
