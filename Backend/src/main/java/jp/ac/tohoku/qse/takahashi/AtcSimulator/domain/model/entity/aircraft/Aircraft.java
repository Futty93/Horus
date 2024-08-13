package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Callsign.Callsign;

public interface Aircraft {
    public void setSpeed(double speed);
    public void setHeading(double heading);
    public void setVerticalSpeed(double verticalSpeed);

    String getLocation();
    void NextStep();
    boolean IsEqualCallsign(Callsign callsign);
}