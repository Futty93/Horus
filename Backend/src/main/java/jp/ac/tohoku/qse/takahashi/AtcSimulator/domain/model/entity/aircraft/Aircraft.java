package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Callsign.Callsign;
import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Position.FixPosition;

public interface Aircraft {
    // aircraftVector から 航空機の次の位置を計算する
    public void calculateNextAircraftPosition();

    // instructedVector から 航空機の次のaircraftVectorを計算する
    public void calculateNextAircraftVector();

    public double calculateTurnAngle(FixPosition fixPosition);

    public boolean isEqualCallsign(Callsign callsign);
}