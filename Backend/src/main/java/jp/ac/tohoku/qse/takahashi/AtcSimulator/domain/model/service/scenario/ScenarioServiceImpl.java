package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.service.scenario;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.aggregate.airspace.AirspaceManagement;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.Aircraft;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.AircraftRepository;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.CommercialAircraft;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Callsign.Callsign;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.AircraftPosition;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.AircraftVector;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Type.AircraftType;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces.dto.CreateAircraftDto;

import java.util.ArrayList;
import java.util.List;

public class ScenarioServiceImpl implements ScenarioService {

    private final AirspaceManagement airspaceManagement;
    private final AircraftRepository aircraftRepository;
    private String scenarioStatus;

    // コンストラクタで必要な依存関係を注入
    public ScenarioServiceImpl(AirspaceManagement airspaceManagement, AircraftRepository aircraftRepository) {
        this.airspaceManagement = airspaceManagement;
        this.aircraftRepository = aircraftRepository;
        this.scenarioStatus = "Not Initialized";
    }

    public void spawnAircraft(CreateAircraftDto aircraftDto) {
        CommercialAircraft aircraft = aircraftDto.createCommercialAircraft();
        airspaceManagement.addAircraft(aircraft);
        aircraftRepository.add(aircraft);
    }

    @Override
    public List<Aircraft> generateAircraftsForScenario() {
        // シナリオに基づいて航空機を生成する
        List<Aircraft> aircrafts = new ArrayList<>();

        // 例: 新しい航空機を生成してリストに追加
        Aircraft aircraft1 = new Aircraft("AAL123", 35.553333, 139.781113, 150, 35000, 250, 340, "B777", "HND", "RJTT", "LAX", "KLAX", "2024-05-21T12:00:00Z");
        aircraftRepository.save(aircraft1);
        aircrafts.add(aircraft1);

        // 他の航空機も生成可能
        // Aircraft aircraft2 = ...

        scenarioStatus = "Aircrafts Generated";
        System.out.println("Aircrafts generated for the scenario.");

        return aircrafts;
    }

    @Override
    public void updateScenarioAircraftPositions() {
        // シナリオに基づいて、すべての航空機の位置を更新する
        List<Aircraft> aircrafts = aircraftRepository.findAll();

        for (Aircraft aircraft : aircrafts) {
            // 各航空機の位置を更新 (シンプルな例として、座標を更新)
            aircraft.updatePositionBasedOnScenario();
            aircraftRepository.save(aircraft);
        }

        scenarioStatus = "Aircraft Positions Updated";
        System.out.println("Scenario aircraft positions updated.");
    }

    @Override
    public String getScenarioStatus() {
        // 現在のシナリオのステータスを返す
        return scenarioStatus;
    }

    @Override
    public void terminateScenario() {
        // シナリオ終了時の処理を行う
        scenarioStatus = "Terminated";
        System.out.println("Scenario terminated.");
    }
}