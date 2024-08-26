package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.service.scenario;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft.Aircraft;

import java.util.List;

public interface ScenarioService {

    // シナリオの初期化
    void initializeScenario();

    // シナリオに基づいた航空機の生成
    List<Aircraft> generateAircraftsForScenario();

    // シナリオに基づいた航空機の動作を更新
    void updateScenarioAircraftPositions();

    // 現在のシナリオのステータスを取得
    String getScenarioStatus();

    // シナリオを終了する処理
    void terminateScenario();
}