package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.service.scenario;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Callsign.Callsign;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.dto.ControlAircraftDto;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.dto.CreateAircraftDto;

public interface ScenarioService {
    public void spawnAircraft(CreateAircraftDto createAircraftDto);
    public void instructAircraft(Callsign callsign, ControlAircraftDto controlAircraftDto);
    public void directFixAircraft(Callsign callsign, String fixName);
}
