package jp.ac.tohoku.qse.takahashi.AtcSimulator.domain.model.aircraft;

public class CommercialAircraft implements Aircraft {
    private int id;
    private double speed;
    private double altitude;
    private double heading;
    private double latitude;
    private double longitude;
    private double verticalSpeed;
    private String companyName;
    private String flightNumber;

    public CommercialAircraft(int id, double speed, double altitude, double heading, double latitude, double longitude, double verticalSpeed, String companyName, String flightNumber) {
        this.id = id;

        this.speed = speed;
        this.altitude = altitude;
        this.heading = heading;
        this.latitude = latitude;
        this.longitude = longitude;
        this.verticalSpeed = verticalSpeed;
        this.companyName = companyName;
        this.flightNumber = flightNumber;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public void setHeading(double heading) {
        this.heading = heading;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setVerticalSpeed(double verticalSpeed) {
        this.verticalSpeed = verticalSpeed;
    }

    public int getId() {
        return this.id;
    }

    public String getLocation() {
        return "Flight: " + this.companyName + "-" + this.flightNumber + ", Latitude: " + this.latitude + ", Longitude: " + this.longitude;
    }
}
