package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.service.scenario;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.dto.CreateAircraftDto;

public interface ScenarioService {
    void spawnAircraft(CreateAircraftDto createAircraftDto);
    void stepAircraft();
}
