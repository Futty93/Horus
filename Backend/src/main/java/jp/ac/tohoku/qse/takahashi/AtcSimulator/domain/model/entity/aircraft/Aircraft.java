package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Callsign.Callsign;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.AircraftPosition;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.AircraftVector;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.FixPosition;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.InstructedVector;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Type.AircraftType;

public interface Aircraft {
    // aircraftVector から 航空機の次の位置を計算する
    public void calculateNextAircraftPosition();

    // instructedVector から 航空機の次のaircraftVectorを計算する
    public void calculateNextAircraftVector();

    public double calculateTurnAngle(FixPosition fixPosition);

    public boolean isEqualCallsign(Callsign callsign);

    // ゲッターメソッド（コンフリクト検出機能で必要）
    public Callsign getCallsign();

    public AircraftPosition getAircraftPosition();

    public AircraftVector getAircraftVector();

    public InstructedVector getInstructedVector();

    public AircraftType getAircraftType();
}
