package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.entity.aircraft;

import jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.valueObject.Callsign.Callsign;

public interface Aircraft {
    public void setSpeed_kt(double speed_kt);
    public void setHeading(double heading);
    public void setVerticalSpeed_ft_sec(double verticalSpeed_ft_sec);

    String getLocation();
    void NextStep();
    boolean IsEqualCallsign(Callsign callsign);
}