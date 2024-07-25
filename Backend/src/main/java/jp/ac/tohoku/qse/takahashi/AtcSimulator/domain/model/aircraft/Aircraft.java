package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.aircraft;

public interface Aircraft {
    public void setSpeed(double speed);
    public void setAltitude(double altitude);
    public void setHeading(double heading);
    public void setLatitude(double latitude);
    public void setLongitude(double longitude);
    public void setVerticalSpeed(double verticalSpeed);
    public int getId();

    String getLocation();
}