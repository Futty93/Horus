package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.service.scenario;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.dto.CreateAircraftDto;

public interface ScenarioService {
    public void spawnAircraft(CreateAircraftDto createAircraftDto);

}
